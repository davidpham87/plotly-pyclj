(ns plotly-pyclj.plot
  (:require
   [clojure.core.async :as a]
   [babashka.process :as bp]
   [clojure.java.io :as io]
   [clojure.java.shell :as sh]
   [clojure.pprint :refer (pprint)]
   [cognitect.transit :as transit]
   [jsonista.core :as j]
   [plotly-pyclj.routes :refer (notify-clients!)])
  (:import [java.io ByteArrayOutputStream]
           java.util.Base64))

(def ^:dynamic *kaleido* "kaleido")

(defn set-kaleido-command!
  "Set the default kaleido command"
  [s]
  (alter-var-root #'*kaleido* (constantly s)))

(def ^:dynamic *kaleido-default-args*
  "Default kaleido command args as map. All keys will be included."
  {:disable-gpu  nil
   :allow-file-access-from-files nil
   :disable-breakpad nil
   :disable-dev-shm-usage nil})

(defn update-kaleido-args! [f]
  (alter-var-root #'*kaleido-default-args* f))

(defn ->shell-args [m]
  (reduce (fn [acc [k v]]
            (conj acc (str "--" (name k) (when v (str "=" v))))) [] m))

(def ^:dynamic *kaleido-home* "")

(def kaleido-processes (atom {:default nil}))

(def object-mapper (j/object-mapper {:decode-key-fn true}))

(defn decode [to-decode]
  (.decode (Base64/getDecoder) to-decode))

(defn fig->json [m] (j/write-value-as-string m))

(defn ->file [filename {:keys [result format] :as kaleido-output}]
  (let [s (String. (.getBytes result) "UTF-8")]
    (-> (if (#{"svg" "json" "eps"} format) s (decode s))
        (io/copy (io/file filename)))))

(defn kaleido-alive?
  ([] (kaleido-alive? :default))
  ([k] (when-let [p (get-in @kaleido-processes [k :proc])] (.isAlive p))))

(defn ensure-kaleido!
  ([] (ensure-kaleido! {}))
  ([{:keys [exec-path exec-args chan id]
     :or {id :default
          exec-path *kaleido*
          exec-args (->shell-args *kaleido-default-args*)}}]
   (if (kaleido-alive? id)
     chan
     (let [chan (or chan (a/chan (a/sliding-buffer 1000)))]
       (a/thread
         (let [p (bp/process (into [exec-path "plotly"] exec-args))]
           (swap! kaleido-processes assoc id (assoc p :chan chan))
           (with-open [r (io/reader (:out p))]
             (println (.readLine ^java.io.BufferedReader r))
             (loop []
               (a/>!! chan (.readLine ^java.io.BufferedReader r))
               (recur)))))
       chan))))

(comment
  (ensure-kaleido!)
  (kaleido-alive?))

(defn ->kaleido
  "Send a map of data to kaleido."
  [kaleido-process m]
  (let [w (io/writer (:in kaleido-process))]
    (.write w (j/write-value-as-string m))
    (.write w (System/lineSeparator))
    (.flush w)))

(defn export
  "Export a plotly spec with the export specification.

  - `plotly-spec` is a standard plotly graph definition.

  - `export-spec` contains on top of the kaleido with the
  typical arguments `(:format, :width, :height, :scale)`, a
  `:filename` (without format) key to define the path of the output
  file.

  - kaleido-process is the result of a babashka.process/process call
  to kaleido.
  "
  ([plotly-spec] (export plotly-spec {}))
  ([plotly-spec export-spec]
   (export plotly-spec export-spec (:default @kaleido-processes)))
  ([plotly-spec export-spec kaleido-process]
   (if-not (and kaleido-process (.isAlive (:proc kaleido-process)))
     (do
       (ensure-kaleido! {:exec-path *kaleido*
                         :exec-args *kaleido-default-args*})
       (loop []
         (when-not (:default @kaleido-processes)
           (a/<!! (a/timeout 100))
           (recur)))
       (export plotly-spec export-spec (:default @kaleido-processes)))
     (do
       (->kaleido kaleido-process (assoc export-spec :data plotly-spec))
       (let [kaleido-output (j/read-value (a/<!! (:chan kaleido-process)) object-mapper)
             filename (str (:filename export-spec "plotly_export")
                           "."
                           (:format kaleido-output))]
         (if (zero? (:code kaleido-output))
           (->file filename kaleido-output)
           (throw (ex-info "Non Zero Kaleido Code" kaleido-output))))))))

(defn ->transit [x]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer x)
    (.toString out)))

(defn fig->web [m]
  (->> (j/write-value-as-string m)
       (notify-clients! nil)))

(defn fig->web-transit [m] (notify-clients! nil (->transit m)))

(defn plot [m] (fig->web-transit m))

(comment
  (ensure-kaleido!)

  (export {:data [{:x [0 1] :y [2 3]}
                  {:x [0 1] :y [0 4]}
                  {:x [0 1] :y [(rand-int 10) (rand-int 10)]}]
           :layout {:title "Hello"}}
          {:filename "output/test2"
           :format "svg"})

  (.destroy (:proc (:default @kaleido-processes)))

  (fig->web-transit {:data [{:x [0 1] :y [2 3]}
                            {:x [0 1] :y [0 4]}]
                     :layout {:title "Hello"}})

  (def p-atom (atom nil))
  (def p-results (atom nil))
  (def p (bp/process (into ["kaleido" "plotly"] ["--disable-gpu"
                                                 "--allow-file-access-from-files"
                                                 "--disable-breakpad"
                                                 "--disable-dev-shm-usage"])))
  (.destroy (:proc p))

  (a/thread
    (let [plotly-spec {:data [{:x [0 1] :y [2 3]}
                              {:x [0 1] :y [0 4]}]
                       :layout {:title "Hello"}}
          p (bp/process
             (into ["kaleido" "plotly"] ["--disable-gpu"
                                         "--allow-file-access-from-files"
                                         "--disable-breakpad"
                                         "--disable-dev-shm-usage"]))
          r (io/reader (:out p))
          c (a/chan 100)]
      (reset! p-atom p)

      (a/go-loop []
        (if-let [value (.readLine r)]
          (do (a/>! c (.readLine r))
              (recur))
          (a/close! c)))

      (a/go-loop []
        (when-let [x (a/<! c)]
          (let [results (j/read-value x object-mapper)]
            (reset! p-results results)
            (pprint results))
          (recur)))))



  (let [w (io/writer (:in @p-atom))]
    (.write w (fig->json {:data {:data [{:x [0 1] :y [2 3]}
                                        {:x [0 1] :y [0 4]}]
                                 :layout {:title "Hello"}}
                          :format "pdf"}))
    (.write w (System/lineSeparator))
    (.flush w))




  (spit "test.pdf" (decode (String. (.getBytes (:result @p-results)) "UTF-8")))

  (->file "test2.pdf" @p-results)

  #_(-> (fig-py->clj fig)
      #_(update :layout assoc :height 560 :width 960)
      fig->web)

  #_(-> (fig-py->clj fig)
      #_(update :layout assoc :height 560 :width 960)
      fig->web-transit)

  ;; benchmarking a bit the difference in performance. JSON seams the fastest
  ;; way for now on my AMD 3700X.
  #_(require '[criterium.core :as criterium])
  #_(criterium/quick-bench (py. fig to_dict))

  ;; Evaluation count : 96 in 6 samples of 16 calls.
  ;; Execution time mean : 1.162875 ms
  ;; Execution time std-deviation : 121.294843 µs
  ;; Execution time lower quantile : 1.088489 ms ( 2.5%)
  ;; Execution time upper quantile : 1.367614 ms (97.5%)
  ;; Overhead used : 7.932566 ns

  #_(criterium/quick-bench (py. fig to_json))

  ;; Evaluation count : 456 in 6 samples of 76 calls.
  ;; Execution time mean : 1.364200 ms
  ;; Execution time std-deviation : 46.968651 µs
  ;; Execution time lower quantile : 1.323572 ms ( 2.5%)
  ;; Execution time upper quantile : 1.430867 ms (97.5%)
  ;; Overhead used : 7.932566 ns

  #_(let [data (py. fig to_dict)]
    (criterium/quick-bench (py/->jvm data)))

  ;; Evaluation count : 12 in 6 samples of 2 calls.
  ;; Execution time mean : 9.018108 ms
  ;; Execution time std-deviation : 1.092000 ms
  ;; Execution time lower quantile : 7.462432 ms ( 2.5%)
  ;; Execution time upper quantile : 10.205197 ms (97.5%)
  ;; Overhead used : 7.932566 ns

  #_(let [data (j/read-value (py. fig to_json) object-mapper)]
    (criterium/quick-bench (j/write-value-as-string data object-mapper)))

  ;; Evaluation count : 5124 in 6 samples of 854 calls.
  ;; Execution time mean : 118.406625 µs
  ;; Execution time std-deviation : 3.472181 µs
  ;; Execution time lower quantile : 115.207694 µs ( 2.5%)
  ;; Execution time upper quantile : 123.997555 µs (97.5%)
  ;; Overhead used : 7.861177 ns

  #_(let [x (atom nil)
        data (j/read-value (py. fig to_json) object-mapper)]
    (criterium/quick-bench (->transit data)))

  ;; Evaluation count : 1578 in 6 samples of 263 calls.
  ;; Execution time mean : 379.894141 µs
  ;; Execution time std-deviation : 3.661274 µs
  ;; Execution time lower quantile : 374.122840 µs ( 2.5%)
  ;; Execution time upper quantile : 383.639254 µs (97.5%)
  ;; Overhead used : 7.861177 ns

  )
