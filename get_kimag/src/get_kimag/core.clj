(ns get-kimag.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [text (slurp "image-10908125849-11260684150.html")]
    (println (re-seq #"ayaka-web[^\"]+jpg" text))
    ))
