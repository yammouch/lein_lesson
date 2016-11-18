(defproject xychart02 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jfree/jfreechart "1.0.19"]]
  :main ^:skip-aot xychart02.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
