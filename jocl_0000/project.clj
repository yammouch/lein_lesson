(defproject jocl_0000 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jocl/jocl "0.2.0-RC00"]]
  :main ^:skip-aot jocl-0000.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
