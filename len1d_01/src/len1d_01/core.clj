; lein run 10000 2 400
; lein run 10000 3 40
; lein run 10000 4 15
; lein run 10000 5 10

(ns len1d-01.core)

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
        '(org.deeplearning4j.nn.multilayer MultiLayerNetwork)
        '(org.deeplearning4j.nn.weights WeightInit)
        '(org.deeplearning4j.optimize.listeners ScoreIterationListener)
        '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.dataset DataSet)
        '(org.nd4j.linalg.factory Nd4j)
        '(org.nd4j.linalg.lossfunctions LossFunctions
                                        LossFunctions$LossFunction))

(def training-data
  [0 0 0 0 1  0 0 0 0 1
   0 0 0 1 0  0 0 0 0 1
   0 0 1 0 0  0 0 0 0 1
   0 1 0 0 0  0 0 0 0 1
   1 0 0 0 0  0 0 0 0 1
   0 0 0 1 1  0 0 0 1 0
   0 0 1 1 0  0 0 0 1 0
   0 1 1 0 0  0 0 0 1 0
   1 1 0 0 0  0 0 0 1 0
   0 0 1 1 1  0 0 1 0 0
   0 1 1 1 0  0 0 1 0 0
   1 1 1 0 0  0 0 1 0 0
   0 1 1 1 1  0 1 0 0 0
   1 1 1 1 0  0 1 0 0 0
   1 1 1 1 1  1 0 0 0 0])

(defn make-ds []
  (let [intm (reduce #(partition %2 %1) training-data [5 2])
        input  (mapcat first intm)
        labels (mapcat fnext intm)]
    [(DataSet. (Nd4j/create (float-array input)  (int-array [(count intm) 5]))
               (Nd4j/create (float-array labels) (int-array [(count intm) 5])))
     5 5]))

(defn make-builder [iter]
  (let [builder (NeuralNetConfiguration$Builder.)]
    (doto builder
      (.iterations iter)
      ;(.learningRate 0.001)
      (.learningRate 0.1)
      (.seed 123)
      (.useDropConnect false)
      (.optimizationAlgo OptimizationAlgorithm/STOCHASTIC_GRADIENT_DESCENT)
      (.biasInit 0)
      (.miniBatch false))
    builder))

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
  (let [list-builder (.list (make-builder iter))]
    (doseq [i (range 1 (dec nlayer))]
      (.layer list-builder i
       (.build (make-hidden-layer-builder layersize layersize))))
    (doto list-builder
      (.layer           0  (.build (make-hidden-layer-builder ni layersize)))
      (.layer (dec nlayer) (.build (make-output-layer-builder layersize no)))
      (.pretrain false)
      (.backprop true))
    list-builder))

(defn dump-layers-params [layers]
  (loop [i 0, totalNumParams 0]
    (if (<= (count layers) i)
      (println "Total number of network parameters: " totalNumParams)
      (let [nParams (.numParams (nth layers i))]
        (println "Number of parameters in layer " i ": " nParams)
        (recur (inc i) (+ totalNumParams nParams))
        ))))

(defn dump-result [ds net]
  (let [output (.output net (.getFeatureMatrix ds)) 
        eval (Evaluation. 2)]
    (println output)
    (.eval eval (.getLabels ds) output)
    (println (.stats eval))
    (println (.score net))
    ))

(defn -main
  [iter nlayer layersize]
  (let [[ds ni no] (make-ds)
        list-builder (apply make-list-builder ni no
                      (map read-string [iter nlayer layersize]))
        conf (.build list-builder)
        net (MultiLayerNetwork. conf)
        _ (doto net
            (.init)
            (.setListeners [(ScoreIterationListener. 100)]))
        layers (.getLayers net)]
    (dump-layers-params layers)
    (time (.fit net ds))
    (dump-result ds net)
    ))
