(ns plotly-pyclj.core
  (:require
   [plotly-pyclj.plot :as pp]
   [plotly-pyclj.server :as ps]))

(def start! ps/start!)
(def stop! ps/stop!)
(def plot pp/plot)

(def export pp/export)
(def ensure-kaleido! pp/ensure-kaleido!)

(defn -main [& args]
  (let [port (or (first args) 8987)]
    (println (str "Started a server on port: ") port)
    (start! {:options {:port port}})))

(comment
  (stop!)
  (start!)
  (plot {:data [{:x [0 2] :y [2 3]}
                {:x [0 2] :y [0 4]}]})

  (plot {:data [{:x [0 2] :y [0 4]}]})
  (plot {:data (repeatedly
                (inc (rand-int 5))
                (fn []
                  (let [n (inc (rand-int 10))]
                    {:x (vec (sort (repeatedly n rand)))
                     :y (vec (repeatedly n rand))})))})


  )
