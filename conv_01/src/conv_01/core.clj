(ns conv-01.core
  (:gen-class))

(import
 '(org.deeplearning4j.datasets.iterator.impl MnistDataSetIterator)
 '(org.deeplearning4j.eval Evaluation)
 '(org.deeplearning4j.nn.api OptimizationAlgorithm)
 '(org.deeplearning4j.nn.conf NeuralNetConfiguration$Builder Updater)
 '(org.deeplearning4j.nn.conf.layers
   ConvolutionLayer$Builder DenseLayer$Builder OutputLayer$Builder
   SubsamplingLayer$Builder SubsamplingLayer$PoolingType)
 '(org.deeplearning4j.nn.conf.layers.setup ConvolutionLayerSetup)
 '(org.deeplearning4j.nn.multilayer MultiLayerNetwork)
 '(org.deeplearning4j.nn.weights WeightInit)
 '(org.nd4j.linalg.lossfunctions LossFunctions$LossFunction)
 '(org.slf4j LoggerFactory))

(defn make-layer-0 [n-channels]
  (.. (ConvolutionLayer$Builder. (int-array [5 5]))
      (nIn n-channels)
      (stride (int-array [1 1]))
      (nOut 20)
      (activation "identity")
      (build)))

(defn make-layer-1 []
  (.. (SubsamplingLayer$Builder. SubsamplingLayer$PoolingType/MAX)
      (kernelSize (int-array [2 2]))
      (stride (int-array [1 1]))
      (build)))

(defn make-layer-2 []
  (.. (ConvolutionLayer$Builder. (int-array [5 5]))
      (stride (int-array [1 1]))
      (nOut 50)
      (activation "identity")
      (build)))

(defn make-layer-3 []
  (.. (SubsamplingLayer$Builder. SubsamplingLayer$PoolingType/MAX)
      (kernelSize (int-array [2 2]))
      (stride (int-array [2 2]))
      (build)))

(defn make-layer-4 []
  (.. (DenseLayer$Builder.)
      (activation "relu")
      (nOut 500)
      (build)))

(defn make-layer-5 [output-num]
  (.. (OutputLayer$Builder. LossFunctions$LossFunction/NEGATIVELOGLIKELIHOOD)
      (nOut output-num)
      (activation "softmax")
      (build)))

(defn make-builder [seed iterations n-channels output-num]
  (.. (NeuralNetConfiguration$Builder.)
      (seed seed)
      (iterations iterations)
      (regularization true)
      (l2 0.0005)
      (learningRate 0.01)
      ;(biasLearningRate 0.02)
      ;(learningRateDecayPolicy LearningRatePolicy$Inverse)
      ;(lrPolicyDecayRate 0.001)
      ;(lrPolicyPower 0.75)
      (weightInit WeightInit/XAVIER)
      (optimizationAlgo OptimizationAlgorithm/STOCHASTIC_GRADIENT_DESCENT)
      (updater Updater/NESTEROVS)
      (momentum 0.9)
      (list)
      (layer 0 (make-layer-0 n-channels))
      (layer 1 (make-layer-1))
      (layer 2 (make-layer-2))
      (layer 3 (make-layer-3))
      (layer 4 (make-layer-4))
      (layer 5 (make-layer-5 output-num))
      (backprop true)
      (pretrain false)))

(defn train-loop [n-epochs model mnist-train log output-num mnist-test]
  (.init model)
  (.info log "Train model....")
  (doseq [i (range n-epochs)]
    (.fit model mnist-train)
    (.info log "*** Completed epoch {} ***" i)
    (.info log "Evaluate model....")
    (let [eval (Evaluation. output-num)]
      (while (.hasNext mnist-test)
        (let [ds (.next mnist-test)
              output (.output model (.getFeatureMatrix ds false))]
          (.eval eval (.getLabels ds) output)))
      (.info log (.stats eval)))
    (.reset mnist-test))
  (.info log "****************Example finished****************"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [;log (LoggerFactory/getLogger (.class LenetMnistExample))
        log (LoggerFactory/getLogger "foo")
        n-channels 1
        output-num 10
        batch-size 64
        n-epochs 10
        iterations 1
        seed 123
        _ (.info log "Load data....")
        mnist-train (MnistDataSetIterator. batch-size true  12345)
        mnist-test  (MnistDataSetIterator. batch-size false 12345)
        _ (.info log "Build model....")
        builder (make-builder seed iterations n-channels output-num)
        _ (ConvolutionLayerSetup. builder 28 28 1)
        conf (.build builder)
        model (MultiLayerNetwork. conf)]
    (train-loop n-epochs model mnist-train log output-num mnist-test)))
