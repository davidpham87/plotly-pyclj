(ns plotly-pyclj.server
  (:require
   [clojure.tools.logging :as log]
   [immutant.web.middleware :refer (wrap-session)]
   [luminus.http-server :as http]
   [plotly-pyclj.routes :refer (home-routes)]
   [reitit.ring :as ring]
   [ring.middleware.content-type :refer (wrap-content-type)]
   [ring.middleware.defaults :refer (site-defaults wrap-defaults)]
   [ring.middleware.flash :refer (wrap-flash)]
   [ring.middleware.webjars :refer (wrap-webjars)]))

(def app-routes
  (ring/ring-handler
   (ring/router
    [(home-routes)])
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (wrap-content-type
     (wrap-webjars (constantly nil)))
    (ring/create-default-handler
     {:not-found
      (constantly {:status 404, :title "404 - Page not found"})
      :method-not-allowed
      (constantly {:status 405, :title "405 - Not allowed"})
      :not-acceptable
      (constantly {:status 406, :title "406 - Not acceptable"})}))))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        {:status 500
         :title "Something very bad has happened!"
         :message "We've dispatched a team of highly trained gnomes to take care of the problem."}))))

(defn wrap-base [handler]
  (-> (identity  handler)
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))
      wrap-internal-error))

(defn app [] (wrap-base #'app-routes))

(defonce server (atom nil))

(defn start!
  ([] (start! {}))
  ([{:keys [options] :as env}]
   (let [http-server
         (http/start
          (-> env
              (assoc :handler (app))
              (update :port #(or (-> options :port) % 8987))))]
     (reset! server http-server)
     server)))

(defn stop! [] (http/stop @server))

(comment
  (start! {:options {:port 8987}})
  (stop!)
  )
