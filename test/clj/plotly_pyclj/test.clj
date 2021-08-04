(ns plotly-pyclj.test
  (:require [plotly-pyclj.core :as pc]))

(pc/start!)

(pc/plot {:data [{:x ["2021-02-14"
                      "2021-03-14"
                      "2021-04-14"
                      "2021-05-01"]
                  :y [3.5 3.9 4.2 5.5]}]})

(pc/plot {:data [{:values [1 1 2]
                  :type :pie}]})

(pc/export
 {:data [{:values [1 1 2]
          :type :pie}]}
 {:filename "test"
  :format "pdf"}
 )
