(ns plotly-pyclj.core
  (:require
   ["react-plotly.js" :default react-plotly]
   [reagent.core :as reagent]
   [plotly-pyclj.websockets :as ws]))

(def content (reagent/atom nil))


(defn init! []
  (ws/make-websocket!
   "ws://localhost:8083/ws"
   (fn [msg]
     (.log js/console "Message:" (.parse js/JSON msg))
     (reset! content (.parse js/JSON msg)))))

(defn app []
  (fn []
    (let [data (or @content #js {:layout #js {:title "Loading Test"}})]
      (reagent/create-element react-plotly data))))

(defn mount-component []
  (reagent/render [app] (.getElementById js/document "app")))

(defn ^:dev/after-load main []
  (init!)
  (mount-component))

(defn ^:dev/before-load stop-ws []
  (ws/close-chan))
