(ns linechart.core
  (:gen-class))

(import '(org.jfree.data.category DefaultCategoryDataset)
        '(org.jfree.chart ChartFactory ChartPanel)
        '(org.jfree.chart.plot PlotOrientation)
        '(org.jfree.ui ApplicationFrame RefineryUtilities))

(defn create-dataset []
  (let [dataset (DefaultCategoryDataset.)]
    (doto dataset
      (.addValue  15 "schools" "1970")
      (.addValue  30 "schools" "1980")
      (.addValue  60 "schools" "1990")
      (.addValue 120 "schools" "2000")
      (.addValue 240 "schools" "2010")
      (.addValue 300 "schools" "2014"))
    dataset))

(defn graph [application-title chart-title]
  (let [chart (ApplicationFrame. application-title)
        line-chart (ChartFactory/createLineChart
                    chart-title "Years" "Number of Schools"
                    (create-dataset) PlotOrientation/VERTICAL
                    true true false)
        chart-panel (ChartPanel. line-chart)]
    (.setPreferredSize chart-panel (java.awt.Dimension. 560 367))
    (.setContentPane chart chart-panel)
    (.pack chart)
    (RefineryUtilities/centerFrameOnScreen chart)
    (.setVisible chart true)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (graph "School Vs Years" "Number of Schools vs years"))
