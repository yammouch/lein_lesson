{:val  '{a 0, b 0, c 0}
 :cell '{i0 [and c a b]}
 :dest '{a i0, b i0}
 :q    (apply sorted-map
        (mapcat (fn [[t q]] [t (into clojure.lang.PersistentQueue/EMPTY q)])
                [[2 ['a 1]]
                 [4 ['b 1]]]))}
