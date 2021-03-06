; $ lein repl
; (-main "1" "1" "1")
; (.paramTable (.getLayer @rnet 0))
; (.fit @rnet @rds)
; (.paramTable (.getLayer @rnet 0))

(ns comp1-multi.core
  (:gen-class))

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
    ;[(DataSet. (Nd4j/create (float-array input)
    ;                        (int-array [(count ij) field-size]))
    ;           (Nd4j/create (float-array labels)
    ;                        (int-array [(count ij) field-size])))
    ; field-size field-size]))
    [(DataSet. (Nd4j/create (float-array [-1 1]) (int-array [2 1]))
               (Nd4j/create (float-array [ 0 1]) (int-array [2 1])))
     1 1]))

(defn make-builder [iter]
  (let [builder (NeuralNetConfiguration$Builder.)]
    (doto builder
      ;(.iterations iter)
      (.iterations 1)
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
      (.activation "sigmoid")
      ;(.activation "softmax")
      ;(.weightInit WeightInit/DISTRIBUTION)
      (.weightInit WeightInit/ZERO))
      ;(.dist (UniformDistribution. 0 1)))
    output-layer-builder))

(defn make-list-builder [ni no iter nlayer layersize]
  (let [list-builder (.list (make-builder iter))]
    ;(doseq [i (range 1 (dec nlayer))]
    ;  (.layer list-builder i
    ;   (.build (make-hidden-layer-builder layersize layersize))))
    (doto list-builder
      ;(.layer           0  (.build (make-hidden-layer-builder ni layersize)))
      ;(.layer (dec nlayer) (.build (make-output-layer-builder layersize no)))
      (.layer 0 (.build (make-output-layer-builder ni layersize)))
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

(defn dump-result [ds net no]
  (let [output (.output net (.getFeatureMatrix ds)) 
        eval (Evaluation. no)]
    (println output)
    (.eval eval (.getLabels ds) output)
    (println (.stats eval))
    (println (.score net))
    ))

(def rds  (ref nil))
(def rnet (ref nil))

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
    ;(time (.fit net ds))
    ;(dump-result ds net no)
    (dosync
      (ref-set rds ds)
      (ref-set rnet net))
    ))
