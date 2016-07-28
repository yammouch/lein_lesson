(ns dl4j-03.core)

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

(require 'dl4j-03.schemanip)
(alias 'smp 'dl4j-03.schemanip)

(defn make-ds []
  (let [training-data (->> {:field (read-string (slurp "td000.txt"))
                            :cmd   {:cmd :move-y :org [9 6] :dst 9}}
                           smp/expand
                           (map smp/mlp-input)
                           vec)
        n  (count training-data)
        ni (count ((first training-data) :niv))
        no (count ((first training-data) :eov))
        input  (make-array Float/TYPE n ni)
        labels (make-array Float/TYPE n no)]
    (doseq [i (range n)]
      (aset input  i (float-array ((nth training-data i) :niv)))
      (aset labels i (float-array ((nth training-data i) :eov))))
    [(DataSet. (Nd4j/create input)
               (Nd4j/create labels))
     ni no]))

(defn make-builder [iter]
  (let [builder (NeuralNetConfiguration$Builder.)]
    (doto builder
      (.iterations iter)
      (.learningRate 0.001)
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
      (.activation "sigmoid")
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
    ;(println output)
    (.eval eval (.getLabels ds) output)
    (println (.stats eval))
    (println (.score net))
    ))

(defn -main
  "I don't do a whole lot."
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
