(ns plotly-pyclj.plot
  (:require
   [libpython-clj.python :refer (py.. py.) :as py]
   [libpython-clj.require :refer (require-python)]
   [jsonista.core :as j]
   [plotly-pyclj.routes :refer (notify-clients!)]))

(require-python '[plotly.express :as px])

(def df (py.. px/data iris))
(def fig (px/scatter df :x "sepal_width" :y "sepal_length" #_#_:color "species"))

(defn fig->clj [fig]
  (j/read-value (py. fig to_json)))

(defn fig->web [fig]
  (let [s (py. fig to_json)]
    (notify-clients! nil s)))

(defn clj->web [m]
  (let [s (j/write-value-as-string m)]
    (notify-clients! nil s)))
