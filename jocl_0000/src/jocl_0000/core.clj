(ns jocl-0000.core
  (:gen-class))

(require 'jocl-0000.cl)
(require 'clojure.pprint)

(alias 'cl 'jocl-0000.cl)

(import '(org.jocl CL Sizeof Pointer cl_device_id cl_event))

(defn handle-cl-error [err-code]
  (when (not= err-code CL/CL_SUCCESS)
    (throw (Exception. (CL/stringFor_errorCode err-code)))))

(defn lcg [seed]
  (let [val (mod (+ (* seed 1103515245) 12345)
                 (bit-shift-left 1 32))]
    (cons val (lazy-seq (lcg val)))
    ))

;(def simd true)
(def simd false)

;(def N 8)
;(def col-vec (float-array [1 2 3 4 5 6 7 8]))
;(def row-vec (float-array [2 3 4 5 6 7 8 9]))
(def N 8192)
(def col-vec (float-array (map #(- (mod % 19) 9)
                               (take N (lcg 1)))))
(def row-vec (float-array (map #(- (mod % 19) 9)
                               (take N (lcg 2)))))

(defn prepare-mem [context]
  (let [err (int-array 1)
        cv-mem (CL/clCreateBuffer context CL/CL_MEM_READ_WRITE
                (* N Sizeof/cl_float) nil err)
        _ (handle-cl-error (first err))
        rv-mem (CL/clCreateBuffer context CL/CL_MEM_READ_WRITE
                (* N Sizeof/cl_float) nil err)
        _ (handle-cl-error (first err))
        prod-mem (CL/clCreateBuffer context CL/CL_MEM_READ_WRITE
                  (* N N Sizeof/cl_float) nil err)
        _ (handle-cl-error (first err))]
    {:cv cv-mem :rv rv-mem :prod prod-mem}))

(defn init-mem [queue {cv :cv rv :rv}]
  (handle-cl-error
   (CL/clEnqueueWriteBuffer
    queue cv CL/CL_TRUE 0 (* N Sizeof/cl_float)
    (Pointer/to (float-array col-vec)) 0 nil nil))
  (handle-cl-error
   (CL/clEnqueueWriteBuffer
    queue rv CL/CL_TRUE 0 (* N Sizeof/cl_float)
    (Pointer/to (float-array row-vec)) 0 nil nil)))

(defn prepare-kernels [context devices]
  (let [src (slurp "cv_rv.cl")
        err (int-array 1)
        program (CL/clCreateProgramWithSource
                 context 1 (into-array String [src])
                 (long-array [(count src)]) err)
        _ (handle-cl-error (first err))
        er (CL/clBuildProgram
            program 1 (into-array cl_device_id devices)
            (if simd "-D SIMD=1" nil)
            nil nil)
        _ (doseq [d devices]
            (println (cl/parse-str-info
                      (cl/clGetProgramBuildInfo program d
                       'CL_PROGRAM_BUILD_LOG))))
        _ (handle-cl-error er)
        kernel (CL/clCreateKernel program "cv_rv" err) 
        ;kernel (CL/clCreateKernel program "cv_rv_loop" err) 
        _ (handle-cl-error (first err))]
    {:program program :kernel kernel}))

(defn engine [ctx queue kernel {cv :cv rv :rv prod :prod}]
  (let [err (int-array 1)
        event (CL/clCreateUserEvent ctx err)
        _ (handle-cl-error (nth err 0))
        events (into-array cl_event [event])]
    (handle-cl-error
     (CL/clSetKernelArg kernel 0 Sizeof/cl_mem (Pointer/to prod)))
    (handle-cl-error
     (CL/clSetKernelArg kernel 1 Sizeof/cl_mem (Pointer/to cv)))
    (handle-cl-error
     (CL/clSetKernelArg kernel 2 Sizeof/cl_mem (Pointer/to rv)))
    (handle-cl-error
     (CL/clSetKernelArg kernel 3 Sizeof/cl_int
      (Pointer/to (int-array [(if simd (/ N 4) N)]))))
    (handle-cl-error
     (CL/clEnqueueNDRangeKernel queue kernel 2
      nil (long-array [(if simd (/ N 4) N) N]) (long-array [128 1])
      0 nil event))
    ;(handle-cl-error (CL/clEnqueueTask queue kernel 0 nil event))
    (handle-cl-error (CL/clWaitForEvents 1 events))
    ))

(defn print-result [queue {cv :cv rv :rv prod :prod}]
  (let [cv-array (float-array N)
        rv-array (float-array N)
        prod-array (float-array (* N N))]
    (handle-cl-error
     (CL/clEnqueueReadBuffer queue cv CL/CL_TRUE
      0 (* N Sizeof/cl_float) (Pointer/to cv-array) 0 nil nil))
    (handle-cl-error
     (CL/clEnqueueReadBuffer queue rv CL/CL_TRUE
      0 (* N Sizeof/cl_float) (Pointer/to rv-array) 0 nil nil))
    (handle-cl-error
     (CL/clEnqueueReadBuffer queue prod CL/CL_TRUE
      0 (* N N Sizeof/cl_float) (Pointer/to prod-array) 0 nil nil))
    (println "col vector:") (clojure.pprint/pprint cv-array)
    (println "row vector:") (clojure.pprint/pprint rv-array)
    (println "product:")
    (clojure.pprint/pprint (partition N prod-array))))

(defn compare-result [queue {prod :prod}]
  (let [prod-array (float-array (* N N))
        prod-ref (mapcat (fn [ce] (map (fn [re] (* ce re))
                                       row-vec))
                         col-vec)
        calc-err (fn [val ref]
                   (if (< -0.005 ref 0.005)
                     val
                     (- (/ val ref) 1.0)))]
    (handle-cl-error
     (CL/clEnqueueReadBuffer queue prod CL/CL_TRUE
      0 (* N N Sizeof/cl_float) (Pointer/to prod-array) 0 nil nil))
    (print "compare ")
    (if (every? #(< -0.01 % 0.01) (map calc-err prod-array prod-ref))
      (println "[OK]")
      (println "[ER]"))))

(defn compare-result-loop [queue {prod :prod}]
  (let [prod-read (float-array (* N N))
        prod-array (make-array Float/TYPE N N)
        calc-err (fn [val ref]
                   (if (< -0.005 ref 0.005)
                     val
                     (- (/ val ref) 1.0)))]
    (time
      (dotimes [i N]
        (dotimes [j N]
          (aset-float prod-array i j
                      (* (aget col-vec i) (aget row-vec j))
                      ))))
    (handle-cl-error
     (CL/clEnqueueReadBuffer queue prod CL/CL_TRUE
      0 (* N N Sizeof/cl_float) (Pointer/to prod-read) 0 nil nil))
    (print "compare ")
    (if (every? #(< -0.01 % 0.01)
                (map calc-err (apply concat prod-array) prod-read))
      (println "[OK]")
      (println "[ER]"))))

(defn finalize [queue kernel program {cv :cv rv :rv prod :prod} context]
  (CL/clFlush queue)
  (CL/clFinish queue)
  (CL/clReleaseKernel kernel)
  (CL/clReleaseProgram program)
  (CL/clReleaseMemObject cv)
  (CL/clReleaseMemObject rv)
  (CL/clReleaseMemObject prod)
  (CL/clReleaseCommandQueue queue)
  (CL/clReleaseContext context))

(defn -main [& args]
  (let [{dev :device ctx :context q :queue} (cl/context 'CL_DEVICE_TYPE_CPU)
        {cv :cv rv :rv prod :prod :as mem} (prepare-mem ctx)
        {kernel :kernel program :program} (prepare-kernels ctx [(dev :id)])]
    (init-mem q mem)
    (time (engine ctx q kernel mem))
    ;(print-result q mem)
    (time (compare-result q mem))
    ;(compare-result-loop q mem)
    (finalize q kernel program mem ctx)))
