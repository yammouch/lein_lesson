(ns cljtest.core-test
  (:require [clojure.test :refer :all]
            [cljtest.core :refer :all]))

(deftest ^:long-test long-test1
  (println "long test1"))

(deftest ^:long-test long-test2
  (println "long test2"))

(deftest a-short-test
  (println "short test"))
