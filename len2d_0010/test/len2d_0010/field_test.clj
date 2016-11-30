(ns len2d-0010.field-test
  (:require [clojure.test :refer :all]
            [len2d-0010.field :refer :all]))

(deftest a-test
  (is (= (start-stops 4 2)
         [[0 0] [0 1] [1 1] [1 2] [2 2] [2 3] [3 3]]))
  (is (= (field1 4 0 1 2 :v)
         [[0 0 1 0]
          [0 0 1 0]
          [0 0 0 0]
          [0 0 0 0]])))
