(defproject len1d_0050 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
  ; refer to
  ; https://github.com/deeplearning4j/deeplearning4j/issues/1819 
                 [org.nd4j/nd4j-native "0.4.0" :classifier "windows-x86_64-openblas"]
                 [org.nd4j/nd4j-native "0.4.0" :classifier "windows-x86_64" :scope "provided"]
                 [org.nd4j/nd4j-native-platform "0.4.0"]
                 ;[org.nd4j/nd4j-cuda-7.5-platform "0.4.0"]
                 [org.deeplearning4j/deeplearning4j-nlp  "0.4.0"]
                 [org.deeplearning4j/deeplearning4j-core "0.4.0"]
                 [org.deeplearning4j/deeplearning4j-ui   "0.4.0"]
                 [com.google.guava/guava "19.0"]
                 ;[org.ng4j/nd4j-native-platform "0.4.0"]
                 [org.nd4j/canova-nd4j-image "0.0.0.17"]
                 [org.nd4j/canova-nd4j-codec "0.0.0.17"]
                 [jfree/jfreechart "1.0.13"]
                 [org.deeplearning4j/arbiter-deeplearning4j "0.0.0.8"]
                 ]
  :main ^:skip-aot len1d-0050.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
