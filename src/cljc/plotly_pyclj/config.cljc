(ns plotly-pyclj.config
  (:require
   #?(:cljs [plotly-pyclj.utils :refer-macros [reg-paths]]
      :clj  [plotly-pyclj.utils :refer (reg-path reg-paths)])
   [plotly-pyclj.schema :as schema]))

(def subtree (schema/api-subtree (:config schema/paths)))
(def symbol+paths
  (->> (:config schema/paths)
       schema/api-paths
       (schema/paths->symbol+paths [:config])))
(def help-path (schema/api-help subtree))
(defn help [f & ks] (apply help-path (drop 1 (f)) ks))

#_(reg-path "config" [:config] "Configurations" [])

;; automatically generated
;; (doseq [x (reg-paths (into (sorted-map) symbol+paths))]
;;   (println x))
