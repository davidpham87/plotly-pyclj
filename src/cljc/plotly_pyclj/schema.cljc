(ns plotly-pyclj.schema
  (:require
   #?(:clj [jsonista.core :as j])
   #?(:clj [clojure.java.io :as io])
   [clojure.string :as str]))

#_(set! *print-length* 1000)

;; write documentations about api-help, the two most useful keys are :dlft and :values

(defn keys-in
  "Returns a sequence of all key paths in a given map using DFS walk."
  [m]
  (letfn [(children [node]
            (let [v (get-in m node)]
              (if (map? v)
                (map (fn [x] (conj node x)) (keys v))
                [])))
          (branch? [node] (-> (children node) seq boolean))]
    (->> (keys m)
         (map vector)
         (mapcat #(tree-seq branch? children %)))))


#?(:clj (def object-mapper (j/object-mapper {:decode-key-fn true})))

(defn parse-json [s]
  #?(:cljs (.parse js/JSON s)
     :clj (j/read-value s object-mapper)))

(def plot-schema (atom {}))
(def plot-schema-url "https://api.plot.ly/v2/plot-schema?format=json&sha1=%27%27&_ga=2.88438941.745717888.1587289572-1181236392.1585152478")

#?(:clj (reset! plot-schema
                (-> (io/resource "public/data/plot-schema.json")
                    slurp
                    parse-json))
   :cljs (js/fetch plot-schema-url  #(reset! plot-schema %)))

(def api-excluded-keys #{:_arrayAttrRegexps
                         :_compareAsJSON
                         :_deprecated
                         :_isSubplotObj
                         :_noTemplating
                         :description
                         :dflt
                         :datarevision
                         :editrevision
                         :editType
                         :meta
                         :items
                         :role
                         :uirevision
                         :valType
                         :impliedEdits
                         :values})

(defn api-paths
  ([path] (api-paths path 2 []))
  ([path depth root]
   (let [m (get-in @plot-schema path)
         result (when (map? m)
                  (->> (keys m) (remove api-excluded-keys) (map #(conj root %)) concat))]
     (if (or (zero? depth) (nil? result))
       result
       (into result
             (mapcat #(api-paths (conj path (last %)) (dec depth) (conj root (last %))) result))))))

(defn leaf? [node]
  (if (map? node) (contains? node :valType) true))

#_(defmethod description :traces [{:keys [path]}])

(defn api-subtree [prefix-path] #(get-in @plot-schema (into prefix-path %)))
(defn api-help [subtree-fn]
  (fn api-help-fn
    ([path] (api-help-fn path [:dflt :values]))
    ([path k-or-ks]
     (let [rf #(if (map? %)
                 (if (keyword? k-or-ks) (get % k-or-ks)
                     (select-keys % k-or-ks)) %)
           tree (subtree-fn path)]
       (when (map? tree)
         (->> tree (mapv #(vector (first %) (rf (second %)))) (into (sorted-map))))))))

(def paths {:traces [:schema :traces]
            :layout [:schema :layout :layoutAttributes]
            :config [:schema :config]
            :transforms [:schema :transforms]
            :frames [:schema :frames]})

(def plotly-args-paths
  "Paths inside the plotly config"
  {:traces [:data] :layout [:layout] :config [:config]})

(defn paths->symbol+paths [root paths]
  (let [xf (comp (map #(mapv name %))
                 (map #(str/join "-" %)))]
    (zipmap (into [] xf paths)
            (mapv #(into root %) paths))))

(defn paths->fn-args [root paths path->node]
  (let [xf (comp (map #(mapv name %))
                 (map #(str/join "-" %)))
        paths (sort-by first (remove #(-> % path->node leaf?) paths))]
    (map vector
         (into [] xf paths)
         (mapv #(into root %) paths)
         (mapv #(-> % path->node :description) paths)
         (mapv #(->> (path->node % :dflt)
                     (remove (fn [[k _]] (api-excluded-keys k)))
                     (into (sorted-map))) paths))))


(comment
  ;; take default dflt for each
  (layout-help [:xaxis :tickmode])
  (layout-help [:xaxis] :description))
