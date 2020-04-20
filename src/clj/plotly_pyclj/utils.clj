(ns plotly-pyclj.utils
  "Macro for creating functions")

(defmacro reg-path
  "Register a function targeting a path in the the plotly configuration.
  Given a fn-name and path, it creates a function with 4 different arities.
  - The null arities returns the path.
  - The one arities accepts a map and returns the value on the path.
  - The two arities additionally accepts a value or a function and will set
  path to either the value or update the value with the recieved function.
  - The last arity is a variadic and updates (as in update-in) the the value on the path."
  [fname p]
  `(defn ~(symbol fname)
     ([] ~p)
     ([m#] (get-in m# ~p))
     ([m# v-or-fn#] (assoc-in m# ~p v-or-fn#))
     ([m# f# & args#] (apply update-in m# ~p f# args#))))

(defmacro reg-paths [paths]
  `(for [[f# p#] ~paths]
     `(reg-path ~f# ~p#)))

(defmacro pullall [ns]
  `(do ~@(for [i (map first (ns-publics ns))]
           `(def ~i ~(symbol (str ns "/" i))))))

(comment


    (def paths
      {:layout [:layout]
       :margin [:layout :margin]
       :title [:layout :title]
       :legend [:layout :legend]
       :xaxis [:layout :xaxis]
       :x-axis [:layout :xaxis]
       :yaxis [:layout :yaxis]
       :y-axis [:layout :yaxis]})

    (macroexpand-1 (reg-path :layout [:layout]))
    (reg-path layout [:layout])
    (reg-paths (take 2 (reduce-kv #(assoc %1 (symbol %2) %3) {} paths)))

    (map (fn [[k v]] (println k v) `(reg-path ~k ~v)) (seq paths))

    (defn show-fns []
      (->> (ns-publics 'plotly-pyclj.utils)
           seq
           (filterv (comp fn? deref second))
           (mapv first)))
    (show-fns)

    (reg-paths paths)
    (show-fns)


    (defmacro reg-fn+path
      [fname p]
      `(defn ~(symbol fname)
         ([] ~p)
         ([m#] (get-in m# ~p))
         ([m# v-or-fn#] (assoc-in m# ~p v-or-fn#))
         ([m# f# & args#] (apply update-in m# ~p f# args#))))

    (defmacro reg-fns+paths [fns+paths]
      `(for [[f# p#] ~paths]
         (eval `(reg-fn+path ~f# ~p#))))


    )
