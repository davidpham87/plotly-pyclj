(ns plotly-pyclj.utils
  "Macro for creating functions"
  (:require
   [clojure.pprint :refer (pprint)]
   [cognitect.transit :as transit])
  (:import [java.io ByteArrayOutputStream]))

(defn ->transit [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer x)
    (.toString out)))


(defmacro reg-path
  "Register a function targeting a path in the the plotly configuration.
  Given a fn-name and path, it creates a function with 4 different arities.
  - The null arities returns the path.
  - The one arities accepts a map and returns the value on the path.
  - The two arities additionally accepts a value will set path the value.
  - The last arity is a variadic and updates (as in update-in) the the value on the path."
  [fname p docstring defaults]
  `(defn ~(symbol fname)
     ~(str docstring "\n\nDefault: \n" (with-out-str (pprint defaults)))
     ([] ~p)
     ([~'m] (get-in ~'m ~p))
     (~(vector 'm {:keys (mapv symbol (keys defaults)) :as 'v}) (assoc-in ~'m ~p ~'v))
     (~'[m f & args]  (apply update-in ~'m ~p ~'f ~'args))))

(defmacro reg-paths [paths]
  (cons 'do
        (map (fn [[f# p# d# m#]]
               `(reg-path ~f# ~p# ~d# ~m#)) `~paths)))

(defmacro pullall [ns]
  `(do ~@(for [i (map first (ns-publics ns))]
           `(def ~i ~(symbol (str ns "/" i))))))

(defn show-fns [ns]
  (->> (ns-publics ns)
       seq
       (filterv (comp fn? deref second))
       (mapv first)))

(comment

  (macroexpand (reg-path :layout [:layout] "Layout" {:xaxis [0 2] :yaxis [0 2]}))

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

  (defn show-fns [ns]
    (->> (ns-publics ns)
         seq
         (filterv (comp fn? deref second))
         (mapv first)))
  (show-fns *ns*)

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
