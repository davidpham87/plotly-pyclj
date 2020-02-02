(ns plotly-pyclj.core
  (:require
   ["react-plotly.js" :default react-plotly]
   [reagent.core :as reagent]
   [plotly-pyclj.websockets :as ws]))

(def content (reagent/atom "<h1> Loading... </h1>"))

(defn pandoc-content [data]
  [:div {:dangerouslySetInnerHTML {:__html data}}])

(defn init! []
  (ws/make-websocket! (str "ws://localhost:3000") #(reset! content %)))

(defn app [] [:> react-plotly @content])

(defn mount-component []
  (reagent/render [app] (.getElementById js/document "app")))

(defn ^:dev/after-load main []
  (init!)
  (mount-component))
