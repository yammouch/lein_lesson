(ns xychart02.core
  (:gen-class))

(import '(java.awt Dimension Color BasicStroke)
        '(org.jfree.data.xy XYSeries XYSeriesCollection)
        '(org.jfree.chart.renderer.xy XYLineAndShapeRenderer)
        '(org.jfree.chart ChartFactory ChartPanel)
        '(org.jfree.chart.plot PlotOrientation)
        '(org.jfree.ui ApplicationFrame RefineryUtilities))

(def ds (ref []))
(def fr (ref nil))

(defn create-dataset [pre-ds]
  (let [d1 (XYSeries. "data1")
        dataset   (XYSeriesCollection.)]
    (doseq [[x y] pre-ds] (.add d1 x y))
    (.addSeries dataset d1)
    dataset))

(defn create-chart [pre-ds]
  (let [xyline-chart (ChartFactory/createXYLineChart
                      "Which browser are you using?" "Category" "Score"
                      (create-dataset pre-ds) PlotOrientation/VERTICAL
                      true true false)
        renderer (XYLineAndShapeRenderer.)]
    (.setSeriesPaint renderer 0 Color/RED)
    (.setSeriesStroke renderer 0 (BasicStroke. (float 4.0)))
    (.. xyline-chart getXYPlot (setRenderer renderer))
    xyline-chart))

(defn graph [pre-ds]
  (let [frame (ApplicationFrame. "Schematic Disentangler")
        panel (ChartPanel. (create-chart pre-ds))]
    (.setPreferredSize panel (Dimension. 560 367))
    (.setContentPane frame panel)
    (.pack frame)
    (RefineryUtilities/centerFrameOnScreen frame)
    (.setVisible frame true)
    frame))

(defn add-data [x y]
  (dosync
    (alter ds conj [x y])
    (if @fr
      (.. @fr getContentPane
          (setChart (create-chart @ds)))
      (ref-set fr (graph @ds)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Press Enter") (read-line)
  (add-data 0 0)
  (println "Press Enter") (read-line)
  (add-data 1 1)
  (println "Press Enter") (read-line)
  (add-data 2 4)
  (println "Press Enter") (read-line)
  (add-data 3 9)
  (println "Press Enter") (read-line)
  (add-data 4 16)
  :done)
