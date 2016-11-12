(ns xychart.core
  (:gen-class))

(import '(org.jfree.data.category DefaultCategoryDataset)
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

(defn graph [application-title chart-title]
  (let [chart (ApplicationFrame. application-title)
        xyline-chart (ChartFactory/createXYLineChart
                    chart-title "Category" "Score"
                    (create-dataset) PlotOrientation/VERTICAL
                    true true false)
        chart-panel (ChartPanel. xyline-chart)
        renderer (XYLineAndShapeRenderer.)]
    (.setPreferredSize chart-panel (java.awt.Dimension. 560 367))
    (doseq [[i c] [[0 Color/RED] [1 Color/GREEN] [2 Color/YELLOW]]]
      (.setSeriesPaint renderer i c))
    (doseq [[i s] [[0 4.0f] [1 3.0f] [2 2.0f]]]
      (.setSeriesStroke renderer i s))
    (.. xyline-chart getXYPlot (setRenderer renderer))
    (.setContentPane chart chart-panel)))
    (.pack chart)
    (RefineryUtilities/centerFrameOnScreen chart)
    (.setVisible chart true)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (graph "Browser Usage Statistics" "Which Browser are you using?"))
