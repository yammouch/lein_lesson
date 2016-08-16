(ns jocl-0000.core-test
  (:require [clojure.test :refer :all]
            [jocl-0000.core :refer :all]))

(require 'jocl-0000.cl)
(alias 'cl 'jocl-0000.cl)

(deftest a-test
  (let [platforms (map cl/get-platform (cl/clGetPlatformIDs))]
    (clojure.pprint/pprint platforms)
    (testing "FIXME, I fail."
      (is true))))
