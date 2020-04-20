(ns plotly-pyclj.server
  (:require
   ["react-plotly.js" :default react-plotly]
   [cljs-bean.core :refer (->clj ->js)]
   [goog.object :as gobj]
   [reagent.core :as reagent]
   [reagent.dom :as dom]
   [plotly-pyclj.websockets :as ws]))

(defn deep-merge [a b]
  (if (map? a)
    (into a (for [[k v] b] [k (deep-merge (a k) v)]))
    b))

(defonce content (reagent/atom []))

(defn connect!
  ([] (connect! 8987))
  ([port]
   (when (ws/connected?)
     (ws/close-chan!)
     (reset! ws/ws-connected? nil))
   (ws/make-websocket!
    (str "ws://localhost:" port "/ws")
    (fn [msg]
      (.log js/console "Message:" (.parse js/JSON msg))
      (swap! content conj (.parse js/JSON msg))))))

(defonce app-state (reagent/atom {}))

(defn port-label [connected?]
  [:label {:for "port" :style {:color (if connected? :green :red)}} "Backend port: "])

(defn port-input-comp []
  (let [sent? (reagent/atom false)
        initial-value (reagent/atom @(reagent/cursor app-state [:port]))]
    (fn []
      [:input
       {:id :port
        :value @initial-value
        :name :port
        :type :numeric
        :on-change
        (fn [e] (reset! initial-value (.. e -target -value)) (reset! sent? false))
        :on-blur
        #(let [value (-> (.. % -target -value))]
           (when (and (seq value) (not @sent?))
             (reset! sent? true)
             (connect! value)))
        :on-key-press (fn [e]
                        (let [value (-> (.. e -target -value))]
                          (swap! app-state assoc :port value)
                          (when (= (.-key e) "Enter")
                            (.preventDefault e)
                            (connect! value)
                            (reset! sent? true))))}])))

(defn port-input []
  [:form {:style {:margin 10}}
   [port-label @ws/ws-connected?]
   [port-input-comp]])

(def default-config
  {:toImageButtonOptions {:format "png" :height 560 :width 960 :scale 2}
   :displayModeBar "hover"
   :editable true
   :displaylogo false})

(defn set-default-args! [data]
  (set! (.-style data) #js {:width "100%" :height "100%"})
  (let [config (->> (.-config data) ->clj (deep-merge default-config) ->js)]
    (set! (.. data -config) config))
  (set! (.. data -layout -autosize) true)
  (set! (.-useResizeHandler data) true)
  data)

(defn app []
  (fn []
    (let [data (or (last @content) #js {:layout #js {:title "Loading Test"}})]
      (set-default-args! data)
      [:<>
       [:div {:style {:height "90vh" :width "100%"}}
        (reagent/create-element react-plotly data)]])))

(defn mount-component []
  (dom/render [:<> [port-input] [app]] (.getElementById js/document "app")))

(defn ^:dev/after-load main []
  (connect!)
  (mount-component))


(defn ^:dev/before-load stop-ws []
  (ws/close-chan!))
