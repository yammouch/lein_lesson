(defproject len2d_0010 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.deeplearning4j/deeplearning4j-core "0.7.0"]
                 [org.nd4j/nd4j-native-platform "0.7.0"]
                 [org.jfree/jfreechart "1.0.19"]]
  :main ^:skip-aot len2d-0010.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
