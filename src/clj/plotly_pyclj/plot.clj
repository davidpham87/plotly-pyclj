(ns plotly-pyclj.plot
  (:require
   [cognitect.transit :as transit]
   [jsonista.core :as j]
   [libpython-clj.python :refer (py.. py.) :as py]
   [libpython-clj.require :refer (require-python)]
   [plotly-pyclj.data.iris :refer (iris)]
   [plotly-pyclj.routes :refer (notify-clients!)])
  (:import [java.io ByteArrayOutputStream]))

(require-python '[plotly.express :as px])

#_(def df (py.. px/data iris))
(def fig (px/scatter (py/->py-dict iris) :x "petal_length" :y "petal_width" :color "species"))

(defn ->transit [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer x)
    (.toString out)))

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(defn fig-py->clj [fig]
  (j/read-value (py. fig to_json) object-mapper))

(defn fig-py->web [fig]
  (->> (py. fig to_json) (notify-clients! nil)))

(defn fig->web [m]
  (->> (j/write-value-as-string m)
       (notify-clients! nil)))

(defn fig->web-transit [m] (notify-clients! nil (->transit m)))

(comment
  (fig-py->web fig)
  (-> (fig-py->clj fig)
      #_(update :layout assoc :height 560 :width 960)
      fig->web)

  (-> (fig-py->clj fig)
      #_(update :layout assoc :height 560 :width 960)
      fig->web-transit)

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

  (let [data (j/read-value (py. fig to_json) object-mapper)]
    (criterium/quick-bench (j/write-value-as-string data object-mapper)))

  ;; Evaluation count : 5124 in 6 samples of 854 calls.
  ;; Execution time mean : 118.406625 µs
  ;; Execution time std-deviation : 3.472181 µs
  ;; Execution time lower quantile : 115.207694 µs ( 2.5%)
  ;; Execution time upper quantile : 123.997555 µs (97.5%)
  ;; Overhead used : 7.861177 ns

  (let [x (atom nil)
        data (j/read-value (py. fig to_json) object-mapper)]
    (criterium/quick-bench (->transit data)))

  ;; Evaluation count : 1578 in 6 samples of 263 calls.
  ;; Execution time mean : 379.894141 µs
  ;; Execution time std-deviation : 3.661274 µs
  ;; Execution time lower quantile : 374.122840 µs ( 2.5%)
  ;; Execution time upper quantile : 383.639254 µs (97.5%)
  ;; Overhead used : 7.861177 ns

  )
