(ns dl4j-01.core)

(import '(org.deeplearning4j.eval Evaluation)
        '(org.deeplearning4j.nn.api Layer OptimizationAlgorithm)
        '(org.deeplearning4j.nn.conf MultiLayerConfiguration
                                     NeuralNetConfiguration
                                     NeuralNetConfiguration$ListBuilder)
        '(org.deeplearning4j.nn.conf.distribution UniformDistribution)
        '(org.deeplearning4j.nn.conf.layers DenseLayer OutputLayer
                                            OutputLayer$Builder)
        '(org.deeplearning4j.nn.multilayer MultiLayerNetwork)
        '(org.deeplearning4j.nn.weights WeightInit)
        '(org.deeplearning4j.optimize.listeners ScoreIterationListener)
        '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.dataset DataSet)
        '(org.nd4j.linalg.factory Nd4j)
        '(org.nd4j.linalg.lossfunctions LossFunctions))

(def input  (Nd4j/create (float-array [0 0, 1 0, 0 1, 1 1]) (int-array [4 2])))
(def labels (Nd4j/create (float-array [1 0, 0 1, 0 1, 1 0]) (int-array [4 2])))
(def ds (DataSet. input labels))

(def builder (NeuralNetConfiguration$Builder.))
(doto builder
  (.iterations 10000)
  (.learningRate 0.1)
  (.seed 123)
  (.useDropConnect false)
  (.optimizationAlgo OptimizationAlgorith/STOCHASTIC_GRADIENT_DESCENT)
  (.biasInit 0)
  (.miniBatch false))

(def listBuilder (.list builder))
(def hiddenLayerBuilder (DenseLayer$Builder.))
(doto hiddenLayerBuilder
  (.nIn 2)
  (.nOut 4)
  (.activation "sigmoid")
  (.weightInit WeightInit/DISTRIBUTIOIN)
  (.dist (UniformDistribution. 0 1)))

(.layer listBuilder 0 (.build hiddenLayerBuilder))

(def outputLayerBuilder
     (OutputLayer$Builder LossFunction$LossFunction/NEGATIVELOGLIKELIHOOD))
(doto outputLayerBuilder
  (.nIn 4)
  (.nOut 2)
  (.activation "sigmoid")
  (.weightInit WeightInit/DISTRIBUTION)
  (.dist (UniformDistribution. 0 1)))

(doto listBuilder
  (.layer 1 (.build outputLayerBuilder))
  (.pretrain false)
  (.backprop true))

(def conf (.build listBuilder))
(def net (MultiLayerNetwork. conf))
(.init net)
(.setListeners net (ScoreIterationListener. 100))
(def layers (.getLayers net))

(loop [i 0, totalNumParams 0]
  (if (<= (.length layers) i)
    (println "Total number of network parameters: " totalNumParams)
    (let [nParams (.numParams (nth layers i))]
      (println "Number of parameters in layer " i ": " nParams)
      (recur (inc i) (+ totalNumParams nParams))))

(.fit net ds)

(def output (.output net (.getFeatureMatrix ds)))
(println output)

(def eval (Evaluation. 2))
(.eval eval (.getLabels ds) output)
(println (.stats eval))

(defn -main
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
