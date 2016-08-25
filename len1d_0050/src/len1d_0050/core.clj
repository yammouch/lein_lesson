; lein 20000 2 6

(ns len1d-0050.core
  (:gen-class))

(import '(org.deeplearning4j.eval Evaluation)
        '(org.deeplearning4j.nn.api Layer OptimizationAlgorithm)
        '(org.deeplearning4j.nn.conf NeuralNetConfiguration$Builder)
        '(org.deeplearning4j.nn.conf.distribution UniformDistribution)
        '(org.deeplearning4j.nn.conf.layers ConvolutionLayer$Builder
                                            DenseLayer$Builder
                                            OutputLayer$Builder)
        '(org.deeplearning4j.nn.conf.layers.setup ConvolutionLayerSetup)
        ;'(org.deeplearning4j.nn.conf.preprocessor ReshapePreProcessor)
        '(org.deeplearning4j.nn.conf.preprocessor
          FeedForwardToCnnPreProcessor CnnToFeedForwardPreProcessor)
        '(org.deeplearning4j.nn.graph ComputationGraph)
        '(org.deeplearning4j.nn.weights WeightInit)
        '(org.deeplearning4j.optimize.listeners ScoreIterationListener)
        '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.dataset DataSet)
        '(org.nd4j.linalg.factory Nd4j)
        '(org.nd4j.linalg.lossfunctions LossFunctions$LossFunction))

(defn one-hot [field-size i]
  (assoc (vec (repeat field-size 0)) (dec i) 1))

(defn a-field [field-size i j]
  (loop [k i acc (vec (repeat field-size 0))]
    (if (<= j k)
      acc
      (recur (inc k) (assoc acc k 1))
      )))

(defn make-ds []
  (let [field-size 10
        ij (for [i (range      field-size )
                 j (range (inc field-size)) :when (< i j)]
             [i j])
        input  (mapcat (partial apply a-field field-size)        ij)
        labels (mapcat (fn [[i j]] (one-hot field-size (- j i))) ij)]
    [(DataSet. (Nd4j/create (float-array input)
                            (int-array [(count ij) field-size]))
               (Nd4j/create (float-array labels)
                            (int-array [(count ij) field-size])))
     field-size field-size]))

(defn make-conv-layer [ni no]
  (.. (ConvolutionLayer$Builder. (int-array [1 10]))
      (nIn ni)
      (nOut no)
      (stride (int-array [1 1]))
      (activation "sigmoid")
      (build)))

(defn make-hidden-layer [ni no]
  (.. (DenseLayer$Builder.)
      (nIn ni)
      (nOut no)
      (activation "sigmoid")
      (weightInit WeightInit/DISTRIBUTION)
      (dist (UniformDistribution. 0 1))
      (build)))

(defn make-output-layer [ni no]
  (.. (OutputLayer$Builder. LossFunctions$LossFunction/NEGATIVELOGLIKELIHOOD)
      (nIn ni)
      (nOut no)
      (activation "softmax")
      (weightInit WeightInit/DISTRIBUTION)
      (dist (UniformDistribution. 0 1))
      (build)))

(defn make-net-conf [ni no nlayer layersize]
  (.. (NeuralNetConfiguration$Builder.)
      (learningRate 0.1)
      (seed 123)
      (iterations 1) ; default 5
      (miniBatch false)
      (graphBuilder)
      (addInputs (into-array String ["input"]))
      (addLayer "L0" (make-conv-layer 1 layersize)
                (into-array String ["input"]))
      (inputPreProcessor "L0"
       (FeedForwardToCnnPreProcessor. 1 10 1))
      (addLayer "L1" (make-output-layer layersize no)
                (into-array String ["L0"]))
      (inputPreProcessor "L1"
       (CnnToFeedForwardPreProcessor. 1 1 layersize))
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
  (let [output (.output net (into-array INDArray [(.getFeatureMatrix ds)]))
        eval (Evaluation. no)]
    (println (get output 0))
    (.eval eval (.getLabels ds) (get output 0))
    (println (.stats eval))
    (println (.score net))
    (println (.paramTable net))
    ))

(defn -main
  [iter nlayer layersize]
  (let [[ds ni no] (make-ds)
        conf (apply make-net-conf ni no
              (map read-string [nlayer layersize]))
        net (ComputationGraph. conf)
        _ (doto net
            (.init)
            (.setListeners [(ScoreIterationListener. 100)]))
        layers (.getLayers net)]
    (dump-layers-params layers)
    (time (doseq [_ (range (read-string iter))] (.fit net ds)))
    (dump-result ds net no)
    ))
