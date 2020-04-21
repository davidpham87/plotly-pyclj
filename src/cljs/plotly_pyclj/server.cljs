(ns plotly-pyclj.server
  (:require
   ["react-plotly.js" :default react-plotly]
   [cljs-bean.core :refer (->clj ->js)]
   [cognitect.transit :as transit]
   [goog.object :as gobj]
   [plotly-pyclj.websockets :as ws]
   [reagent.core :as reagent]
   [reagent.dom :as dom]))

(defn deep-merge [a b]
  (if (map? a)
    (into a (for [[k v] b] [k (deep-merge (a k) v)]))
    b))

(defonce content (reagent/atom []))

(defn connect!
  ([] (connect! 8987))
  ([port] (connect! port :transit))
  ([port format]
   (when (ws/connected?)
     (ws/close-chan!)
     (reset! ws/ws-connected? nil))
   (ws/make-websocket!
    (str "ws://localhost:" port "/ws")
    (fn [msg]
      (let [format (or format :transit)
            parser (case format
                     :json #(.parse js/JSON %)
                     #(transit/read (transit/reader :json) %))]
        (.log js/console "Message:" (parser msg))
        (swap! content conj (parser msg)))))))

(defonce app-state (reagent/atom {:msg-format :transit}))
(defonce msg-format (reagent/cursor app-state [:msg-format]))

(defn port-label [connected?]
  [:label {:for "port" :style {:color (if connected? :green :red)}} "Backend port: "])

(defn port-input-comp []
  (let [sent? (reagent/atom false)
        initial-value (reagent/atom @(reagent/cursor app-state [:port]))
        msg-format (reagent/atom @msg-format)]
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
             (connect! value @msg-format)))
        :on-key-press (fn [e]
                        (let [value (-> (.. e -target -value))]
                          (swap! app-state assoc :port value)
                          (when (= (.-key e) "Enter")
                            (.preventDefault e)
                            (connect! value @msg-format)
                            (reset! sent? true))))}])))

(defn port-input []
  [:form {:style {:margin 10 :display :inline}}
   [port-label @ws/ws-connected?]
   [port-input-comp]])

(defn msg-format-view []
  [:div {:style {:display :inline}} @(reagent/cursor app-state [:msg-format])])

(def default-config
  {:toImageButtonOptions {:format "png" :height 560 :width 960 :scale 2}
   :displayModeBar "hover"
   :editable true
   :displaylogo false})

(defmulti set-default-args :format)
(defmethod set-default-args :default [m]
  (set-default-args (assoc m :format :transit)))
(defmethod set-default-args :transit [{:keys [data]}]
  (-> data
      (assoc-in [:style] {:width "100%" :height "100%"})
      (update-in [:config] #(deep-merge default-config %))
      (assoc-in [:layout :autosize] true)
      (assoc :useResizeHandler true)))
(defmethod set-default-args :json [{:keys [data]}]
  (gobj/set! data "style" #js {:width "100%" :height "100%"})
  (let [config (->> (.-config data) ->clj (deep-merge default-config) ->js)]
    (gobj/set data "config" config))
  (set! (.. data -layout -autosize) true)
  (gobj/set data "useResizeHandler" true)
  data)

(defn plot [data msg-format]
  (let [empty-data (cond-> {:layout {:title "Loading Test"}}
                     (= msg-format :json) ->js
                     :always or)
        data (set-default-args {:data (or data empty-data)
                                :msg-format msg-format})]
    [:<>
     [:div {:style {:height "90vh" :width "100%"}}
      (case msg-format
        :json (reagent/create-element react-plotly data)
        :transit [:> react-plotly data]
        [:div "Invalid message format"])]]))

(defn app []
  [plot (last @content) @msg-format])

(defn mount-component []
  (dom/render [:<> [:div [port-input] [msg-format-view]]
               [app]] (.getElementById js/document "app")))

(defn ^:dev/after-load main []
  (connect!)
  (mount-component))


(defn ^:dev/before-load stop-ws []
  (ws/close-chan!))
