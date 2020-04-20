(ns plotly-pyclj.websockets
  (:require [reagent.core :as reagent]))

(defonce ws-chan (atom nil))
(defonce ws-connected? (reagent/atom nil))

(defn connected? []
  (if @ws-chan
    (= (.-readyState @ws-chan) (.-OPEN js/WebSocket))
    false))

(defn receive-msg! [update-fn]
  (fn [msg]
    (update-fn (->> msg .-data))))

(defn send-msg!
  [msg]
  (if @ws-chan
    (.send @ws-chan msg)
    (throw (js/Error. "Websocket is not available!"))))

(defn close-chan! []
  (println "Stopping chan")
  (when @ws-chan
    (.close @ws-chan 1000 "Disconnected")))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect websocket")
  (let [on-error (fn [] (reset! ws-connected? false) (reset! ws-chan nil))
        chan (js/WebSocket. url)]
    (set! (.-onerror chan)
          (fn [event]
            (.error js/console event)
            (when (= (.-readyState chan) (.-CLOSED js/WebSocket))
              (on-error))))
    (if chan
      (do
        (set! (.-onmessage chan) (receive-msg! receive-handler))
        (reset! ws-chan chan)
        (reset! ws-connected? true)
        (println "Websocket connection established with: " url))
      (throw
       (js/Error. "Websocket connection failed!")))))

(comment
  (.send @ws-chan "Hello"))
