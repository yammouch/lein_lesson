(loop [s (sorted-map :c 0 :b 1 :a 2)]
  (if (empty? s)
    :done
    (do (print (first s))
        (println s)
        (recur (dissoc s (ffirst s))))))
