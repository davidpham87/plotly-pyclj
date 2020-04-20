(ns plotly-pyclj.utils)

(defn show-fns [ns-name]
  (->> (ns-publics ns-name)
       seq
       (filterv (comp fn? deref second))
       (mapv first)
       sort))
