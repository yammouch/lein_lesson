(ns xychart02.core
  (:gen-class))

(import '(java.awt Dimension Color BasicStroke)
        '(org.jfree.data.xy XYSeries XYSeriesCollection)
        '(org.jfree.chart.renderer.xy XYLineAndShapeRenderer)
        '(org.jfree.chart ChartFactory ChartPanel)
        '(org.jfree.chart.plot PlotOrientation)
        '(org.jfree.ui ApplicationFrame RefineryUtilities))

(defn create-dataset [pre-ds]
  (let [d1 (XYSeries. "data1")
        dataset   (XYSeriesCollection.)]
    (doseq [[x y] (map vector (range) pre-ds)] (.add d1 x y))
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

(defn graph [application-title]
  (let [frame (ApplicationFrame. application-title)
        panel (ChartPanel. (create-chart [1.0 2.0 1.0]))]
    (.setPreferredSize panel (Dimension. 560 367))
    (.setContentPane frame panel)
    (.pack frame)
    (RefineryUtilities/centerFrameOnScreen frame)
    (.setVisible frame true)
    frame))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [frame (graph "Browser Usage Statistics")]
    (println "Press Enter")
    (read-line)
    (.. frame getContentPane
        (setChart (create-chart [1.0 2.0 3.0])))
    :done))
