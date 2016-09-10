(ns rand-0010.core
  (:gen-class))

(import '(org.nd4j.linalg.factory Nd4j)
        '(org.nd4j.linalg.ops.transforms Transforms)
        '(org.nd4j.linalg.indexing NDArrayIndex))

(defn print-rand []
  (let [r1 (Nd4j/rand (int-array [4]) 1)
        r2 (.mul r1 10)
        r3 (Transforms/floor r2)]
    (prn r1)
    (prn r2)
    (prn r3)))

(defn print-cols1 []
  (let [r1 (Nd4j/create (float-array [2 4 6 8]) (int-array [1 4]))
        r2 (.getColumns r1 (int-array [1 1 0]))]
    (prn r1)
    (prn r2)))

(defn print-cols2 []
  (let [r1 (Nd4j/create (float-array (range 16)) (int-array [4 4]))
        r2 (.getColumns r1 (int-array [1 1 0]))]
    (prn r1)
    (prn r2)))

(defn print-rows1 []
  (let [r1 (Nd4j/create (float-array (range 16)) (int-array [8 2]))
        r2 (.getRows r1 (int-array [1 1 0]))]
    (prn r1)
    (prn r2)))

(defn print-rows2 []
  (let [r1 (Nd4j/create (float-array (range 16)) (int-array [4 4]))
        s1 (Nd4j/create (float-array [0 0 0 1]) (int-array [2 2]))
        i1 (NDArrayIndex/create s1)
        ia (NDArrayIndex/all)
        ;r2 (.get r1 (aget i1 0))]
        r2 (.get r1 i1)]
    (println r1)
    (println s1)
    (println i1)
    (doseq [i i1] (println i))
    (println ia)
    ;(doseq [i ia] (println i))
    (println r2)))

(defn -main [& args]
  (print-rand)
  (print-cols1)
  (print-cols2)
  (print-rows1)
  (print-rows2))
