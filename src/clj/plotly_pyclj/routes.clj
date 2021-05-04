(ns plotly-pyclj.routes
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [clojure.pprint :refer (pprint)]
   [immutant.web.async :as async]
   [jsonista.core :as j]
   [muuntaja.core :as m]
   [muuntaja.middleware :refer (wrap-format wrap-params)]
   [plotly-pyclj.utils :refer (->transit)]
   [ring.middleware.anti-forgery :refer (wrap-anti-forgery)]
   [ring.util.http-response :as response]))

;; middleware
(def instance
  (m/create m/default-options))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    {:status 403
     :title "Invalid anti-forgery token"}}))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

;; routes

(defonce channels (atom #{}))

(defn notify-clients! [_ msg]
  (doseq [channel @channels]
    (async/send! channel msg)))

(defn connect! [channel]
  (log/info "channel open")
  (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
  (log/info "close code:" code "reason:" reason)
  (swap! channels #(remove #{channel} %)))

(def websocket-callbacks
  "WebSocket callback functions"
  {:on-open    connect!
   :on-close   disconnect!
   :on-message notify-clients!})

(defn ws-handler [request]
  (async/as-channel request websocket-callbacks))

(defn home [_]
  (-> (io/resource "public/index.html")
      slurp
      (response/ok)
      (response/content-type "text/html")))

(defn main-js [_]
  (-> (io/resource "public/js/main.js")
      slurp
      (response/ok)
      (response/content-type "application/javascript")))

(defn home-routes []
  [""
   {:middleware [#_wrap-csrf wrap-formats]}
   ["/" {:get home}]
   ["/plotly"
    {:post
     (fn [request]
       (notify-clients! nil (->transit (:body-params request)))
       (response/ok "Sent!"))}]
   ["/js/main.js" {:get main-js}]
   ["/ws" {:get ws-handler}]])

(comment

  (notify-clients!
   nil
   (j/write-value-as-string {:data [{:x [0 1 2] :y [3 5 10]}]
                             :layout {:title "WOOW Mimi this is speed"}}))
  (notify-clients! nil "Test 2")
  (home nil)

  (->transit
   (j/read-value
    (j/write-value-as-string {:data [{:x [0 1 2] :y [3 5 10]}]
                                           :layout {:title "WOOW Mimi this is speed"}})))
  )
