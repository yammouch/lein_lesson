; lein 10000 2 10

(ns len1d-0030.core)

(import '(org.deeplearning4j.eval Evaluation)
        '(org.deeplearning4j.nn.api Layer OptimizationAlgorithm)
        '(org.deeplearning4j.nn.conf MultiLayerConfiguration
                                     NeuralNetConfiguration
                                     NeuralNetConfiguration$Builder
                                     NeuralNetConfiguration$ListBuilder)
        '(org.deeplearning4j.nn.conf.distribution UniformDistribution)
        '(org.deeplearning4j.nn.conf.layers DenseLayer
                                            DenseLayer$Builder
                                            OutputLayer
                                            OutputLayer$Builder)
        ;'(org.deeplearning4j.nn.multilayer MultiLayerNetwork)
        '(org.deeplearning4j.nn.graph ComputationGraph)
        '(org.deeplearning4j.nn.weights WeightInit)
        '(org.deeplearning4j.optimize.listeners ScoreIterationListener)
        '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.dataset DataSet)
        '(org.nd4j.linalg.factory Nd4j)
        '(org.nd4j.linalg.lossfunctions LossFunctions
                                        LossFunctions$LossFunction))

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

(defn make-hidden-layer-builder [ni no]
  (let [hidden-layer-builder (DenseLayer$Builder.)]
    (doto hidden-layer-builder
      (.nIn ni)
      (.nOut no)
      (.activation "sigmoid")
      (.weightInit WeightInit/DISTRIBUTION)
      (.dist (UniformDistribution. 0 1)))
    hidden-layer-builder))

(defn make-output-layer-builder [ni no]
  (let [output-layer-builder
        (OutputLayer$Builder.
         LossFunctions$LossFunction/NEGATIVELOGLIKELIHOOD)]
    (doto output-layer-builder
      (.nIn ni)
      (.nOut no)
      ;(.activation "sigmoid")
      (.activation "softmax")
      (.weightInit WeightInit/DISTRIBUTION)
      (.dist (UniformDistribution. 0 1)))
    output-layer-builder))

(defn make-list-builder [ni no iter nlayer layersize]
  (.. (NeuralNetConfiguration$Builder.)
    (learningRate 0.1)
    (seed 123)
    (graphBuilder)
    (addInputs (into-array String ["input"]))
    (addLayer "L1" (.build (make-hidden-layer-builder ni layersize))
              (into-array String ["input"]))
    (addLayer "L2" (.build (make-output-layer-builder layersize no))
              (into-array String ["L1"]))
    (setOutputs (into-array String ["L2"]))
    ))

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
    ))

(defn -main
  [iter nlayer layersize]
  (let [[ds ni no] (make-ds)
        list-builder (apply make-list-builder ni no
                      (map read-string [iter nlayer layersize]))
        conf (.build list-builder)
        ;net (MultiLayerNetwork. conf)
        net (ComputationGraph. conf)
        _ (doto net
            (.init)
            (.setListeners [(ScoreIterationListener. 100)]))
        layers (.getLayers net)]
    (dump-layers-params layers)
    (time (doseq [_ (range (read-string iter))] (.fit net ds)))
    (dump-result ds net no)
    ))
