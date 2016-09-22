; lein run  10 10  5  2 20000 # converges
; lein run  20 10  5 10 50000 # converges
; lein run 100 10  5 10 50000 # converges
; lein run 100 20  5 10 50000 # not converges
; lein run 100 20 10 10 50000 # not converges
; lein run 100 20 10 20 50000 # not converges
; lein run 100 20 10 40 50000 # not converges
; lein run 100 20 20 40 50000 # converges

(ns len1d-0080.core
  (:gen-class))

(import '(org.deeplearning4j.eval Evaluation)
        '(org.deeplearning4j.nn.api Layer OptimizationAlgorithm)
        '(org.deeplearning4j.nn.conf NeuralNetConfiguration$Builder)
        '(org.deeplearning4j.nn.conf.distribution UniformDistribution)
        '(org.deeplearning4j.nn.conf.layers ConvolutionLayer$Builder
                                            DenseLayer$Builder
                                            OutputLayer$Builder)
        '(org.deeplearning4j.nn.conf.layers.setup ConvolutionLayerSetup)
        '(org.deeplearning4j.nn.conf.preprocessor
          FeedForwardToCnnPreProcessor CnnToFeedForwardPreProcessor)
        '(org.deeplearning4j.nn.graph ComputationGraph)
        '(org.deeplearning4j.nn.weights WeightInit)
        '(org.deeplearning4j.optimize.listeners ScoreIterationListener)
        '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.dataset MultiDataSet)
        '(org.nd4j.linalg.factory Nd4j)
        '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.lossfunctions LossFunctions$LossFunction))

(defn one-hot [field-size i]
  (assoc (vec (repeat field-size 0)) (dec i) 1))

(defn a-field [field-size i j]
  (loop [k i acc (vec (repeat field-size 0))]
    (if (<= j k)
      acc
      (recur (inc k) (assoc acc k 1))
      )))

(defn xorshift [x y z w]
  (let [t  (bit-xor x (bit-shift-left x 11))
        wn (bit-and 0xFFFFFFFF
                    (bit-xor w (bit-shift-right w 19)
                             t (bit-shift-right t  8)))]
    (cons w (lazy-seq (xorshift y z w wn)))))

(defn make-input-labels [field-size max-len]
  (let [ij (for [i (range      field-size )
                 j (range (inc field-size)) :when (<= 1 (- j i) max-len)]
             [i j])
        input  (mapcat (partial apply a-field field-size)        ij)
        labels (mapcat (fn [[i j]] (one-hot max-len (- j i))) ij)]
    [(Nd4j/create (float-array input)
                  (int-array [(count ij) field-size]))
     (Nd4j/create (float-array labels)
                  (int-array [(count ij) max-len])
                  )]))

(defn make-minibatches [sb-size in-nd lbl-nd]
  (map (fn [indices]
         (MultiDataSet.
          (into-array INDArray [(.getRows in-nd  (int-array indices))])
          (into-array INDArray [(.getRows lbl-nd (int-array indices))])))
       (partition sb-size (map #(mod % (.size in-nd 0))
                               (xorshift 2 4 6 8)
                               ))))

(defn make-conv-layer [conv-size conv-depth]
  (.. (ConvolutionLayer$Builder. (int-array [1 conv-size]))
      (nIn 1)
      (nOut conv-depth)
      (stride (int-array [1 1]))
      (padding (int-array [0 (quot conv-size 2)]))
      (activation "sigmoid")
      (weightInit WeightInit/DISTRIBUTION)
      (dist (UniformDistribution. 0 1))
      (build)))

(defn make-output-layer [field-size conv-size conv-depth max-len]
  (.. (OutputLayer$Builder. LossFunctions$LossFunction/NEGATIVELOGLIKELIHOOD)
      (nIn (* (+ field-size (if (even? conv-size) 1 0))
              conv-depth))
      (nOut max-len)
      (activation "softmax")
      (weightInit WeightInit/DISTRIBUTION)
      (dist (UniformDistribution. 0 1))
      (build)))

(defn make-net-conf [field-size conv-size conv-depth max-len]
  (.. (NeuralNetConfiguration$Builder.)
      (learningRate 0.1)
      (seed 123)
      (iterations 1) ; default 5
      (miniBatch false)
      (graphBuilder)
      (addInputs (into-array String ["input"]))
      (addLayer "L0" (make-conv-layer conv-size conv-depth)
                (into-array String ["input"]))
      (inputPreProcessor "L0"
       (FeedForwardToCnnPreProcessor. 1 field-size 1))
      (addLayer "L1" (make-output-layer field-size conv-size conv-depth max-len)
                (into-array String ["L0"]))
      (inputPreProcessor "L1"
       (CnnToFeedForwardPreProcessor.
        1
        (+ field-size (if (even? conv-size) 1 0))
        conv-depth))
      (setOutputs (into-array String ["L1"]))
      (build)))

(defn dump-layers-params [layers]
  (loop [i 0, totalNumParams 0]
    (if (<= (count layers) i)
      (println "Total number of network parameters: " totalNumParams)
      (let [nParams (.numParams (nth layers i))]
        (println "Number of parameters in layer " i ": " nParams)
        (recur (inc i) (+ totalNumParams nParams))
        ))))

(defn dump-result [ds net no]
  (let [output (.output net (into-array INDArray [(.getFeatures ds 0)]))
        eval (Evaluation. no)]
    (.eval eval (.getLabels ds 0) (get output 0))
    (println (.stats eval))
    (println (.score net))
    ;(println (.paramTable net))
    ))

(defn -main
  [field-size max-len conv-size conv-depth iter]
  (let [[field-size max-len conv-size conv-depth iter]
        (map read-string [field-size max-len conv-size conv-depth iter])
        [in-nd lbl-nd] (make-input-labels field-size max-len)
        conf ( make-net-conf field-size conv-size conv-depth max-len)
        net (ComputationGraph. conf)
        _ (doto net
            (.init)
            ;(.setListeners [(ScoreIterationListener. 100)]))
            )
        layers (.getLayers net)]
    (dump-layers-params layers)
    (loop [i 0, minibatches (make-minibatches 16 in-nd lbl-nd)]
      (if (< iter i)
        :done
        (do (.fit net (first minibatches))
            (when (= (mod i 1000) 0)
              (println "iter: " i)
              (dump-result (MultiDataSet. (into-array INDArray [in-nd])
                                          (into-array INDArray [lbl-nd]))
                           net max-len))
            (recur (inc i) (next minibatches))
            )))))
