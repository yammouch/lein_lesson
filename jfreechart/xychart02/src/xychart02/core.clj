(ns xychart02.core
  (:gen-class))

(import '(java.awt Dimension Color BasicStroke)
        '(org.jfree.data.xy XYSeries XYSeriesCollection)
        '(org.jfree.chart.renderer.xy XYLineAndShapeRenderer)
        '(org.jfree.chart ChartFactory ChartPanel)
        '(org.jfree.chart.plot PlotOrientation)
        '(org.jfree.ui ApplicationFrame RefineryUtilities))

(defn create-dataset []
  (let [firefox   (XYSeries. "Firefox")
        chrome    (XYSeries. "Chrome")
        iexplorer (XYSeries. "InternetExplorer")
        dataset   (XYSeriesCollection.)]
    (doseq [[x y] [[1.0 1.0] [2.0 4.0] [3.0 3.0]]] (.add firefox   x y))
    (doseq [[x y] [[1.0 4.0] [2.0 5.0] [3.0 6.0]]] (.add chrome    x y))
    (doseq [[x y] [[3.0 4.0] [4.0 5.0] [5.0 4.0]]] (.add iexplorer x y))
    (doseq [d [firefox chrome iexplorer]] (.addSeries dataset d))
    dataset))

(defn create-chart [chart-title]
  (let [xyline-chart (ChartFactory/createXYLineChart
                      chart-title "Category" "Score"
                      (create-dataset) PlotOrientation/VERTICAL
                      true true false)
        renderer (XYLineAndShapeRenderer.)]
    (doseq [[i c] [[0 Color/RED] [1 Color/GREEN] [2 Color/YELLOW]]]
      (.setSeriesPaint renderer i c))
    (doseq [[i s] [[0 4.0] [1 3.0] [2 2.0]]]
      (.setSeriesStroke renderer i (BasicStroke. (float s))))
    (.. xyline-chart getXYPlot (setRenderer renderer))
    xyline-chart))

(defn graph [application-title]
  (let [frame (ApplicationFrame. application-title)
        panel (ChartPanel. (create-chart "Which browser are you using?"))]
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
    :done))
    ;(.. frame getContentPane
    ;    (setChart (create-chart "Which Browser are you using?")))
    ;    )))
    ;))
