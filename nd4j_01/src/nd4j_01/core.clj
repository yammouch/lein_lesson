(ns nd4j-01.core
  (:gen-class))

(import '(org.nd4j.linalg.api.ndarray INDArray)
        '(org.nd4j.linalg.factory     Nd4j))

(defn body []
  (let [nd  (Nd4j/create (float-array [1 2]) (int-array [1 2])) ; vector as row
        nd2 (Nd4j/create (float-array [3 4]) (int-array [2 1])) ; vector as col
        nd3 (Nd4j/create (float-array [1 3 2 4]) (int-array [2 2]))
        nd4 (Nd4j/create (float-array [3 4 5 6]) (int-array [2 2]))
        _ (doseq [x [nd nd2 nd3]] (println x))
        _ (println "Createing nd array with data type " (Nd4j/dataType))
        ndv (.mmul nd nd2)
        _ (println ndv)
        ndv (.mmul nd nd4)
        _ (println ndv)
        ndv (.mmul nd3 nd4)
        _ (println ndv)
        ndv (.mmul nd4 nd3)
        _ (println ndv)
        ndv (.mmul nd2 nd)
        _ (println ndv)
        nd5 (Nd4j/create (float-array [1 1 2 2]) (int-array [2 2]))
        ndv (.mmul nd2 nd5)
        _ (println ndv)]
    :done))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (body))

