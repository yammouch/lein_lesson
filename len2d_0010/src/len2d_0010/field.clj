(ns len2d-0010.field
  (:gen-class))

(defn start-stops [fs ml] ; fs: field-size, ml: max-len
  (mapcat (fn [start]
            (map (fn [stop] [start stop])
                 (range start (min fs (+ start ml)))))
          (range fs)))
