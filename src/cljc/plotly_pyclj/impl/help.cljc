(ns plotly-pyclj.impl.help
  (:require
   [plotly-pyclj.config :as config]
   [plotly-pyclj.layout :as layout]
   [plotly-pyclj.traces :as traces]))

(defn- ->fn [v-f] (if (fn? v-f) v-f (constantly v-f)))

(defmulti help (fn [v-f & _] (first (cond (fn? v-f) (v-f) :else v-f))))

(defmethod help :default [_ & _] (println "Not yet implemented"))
(defmethod help :config [v-f & ks] (apply config/help (->fn v-f) ks))
(defmethod help :traces [v-f & ks] (apply traces/help (->fn v-f) ks))
(defmethod help :layout [v-f & ks] (apply layout/help (->fn v-f) ks))
