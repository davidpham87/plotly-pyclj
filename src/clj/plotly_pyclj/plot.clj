(ns plotly-pyclj.plot
  (:require
   [libpython-clj.python :refer (py.. py.) :as py]
   [libpython-clj.require :refer (require-python)]
   [jsonista.core :as j]
   [plotly-pyclj.routes :refer (notify-clients!)]))

(require-python '[plotly.express :as px])

(def df (py.. px/data iris))
(def fig (px/scatter df :x "petal_length" :y "petal_width" :color "species"))

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(defn fig-py->clj [fig]
  (j/read-value (py. fig to_json) object-mapper))

(defn fig-py->web [fig]
  (let [s (py. fig to_json)]
    (notify-clients! nil s)))

(defn fig->web [m]
  (let [s (j/write-value-as-string m)]
    (notify-clients! nil s)))

(comment
  (fig-py->web fig)
  (-> (fig-py->clj fig)
      #_(update :layout assoc :height 560 :width 960)
      fig->web)

  ;; benchmarking a bit the difference in performance. JSON seams the fastest
  ;; way for now on my AMD 3700X.
  (require '[criterium.core :as criterium])
  (criterium/quick-bench (py. fig to_dict))

  ;; Evaluation count : 96 in 6 samples of 16 calls.
  ;; Execution time mean : 1.162875 ms
  ;; Execution time std-deviation : 121.294843 µs
  ;; Execution time lower quantile : 1.088489 ms ( 2.5%)
  ;; Execution time upper quantile : 1.367614 ms (97.5%)
  ;; Overhead used : 7.932566 ns

  (criterium/quick-bench (py. fig to_json))

  ;; Evaluation count : 456 in 6 samples of 76 calls.
  ;; Execution time mean : 1.364200 ms
  ;; Execution time std-deviation : 46.968651 µs
  ;; Execution time lower quantile : 1.323572 ms ( 2.5%)
  ;; Execution time upper quantile : 1.430867 ms (97.5%)
  ;; Overhead used : 7.932566 ns

  (let [data (py. fig to_dict)]
    (criterium/quick-bench (py/->jvm data)))

  ;; Evaluation count : 12 in 6 samples of 2 calls.
  ;; Execution time mean : 9.018108 ms
  ;; Execution time std-deviation : 1.092000 ms
  ;; Execution time lower quantile : 7.462432 ms ( 2.5%)
  ;; Execution time upper quantile : 10.205197 ms (97.5%)
  ;; Overhead used : 7.932566 ns

  (let [data (py. fig to_json)]
    (criterium/quick-bench (j/read-value data object-mapper)))

  ;; Evaluation count : 3282 in 6 samples of 547 calls.
  ;; Execution time mean : 185.980892 µs
  ;; Execution time std-deviation : 4.024344 µs
  ;; Execution time lower quantile : 182.419192 µs ( 2.5%)
  ;; Execution time upper quantile : 192.199435 µs (97.5%)
  ;; Overhead used : 7.932566 ns


  )
