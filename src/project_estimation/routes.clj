(ns project-estimation.routes
  (:gen-class)
  (:use [org.httpkit.server :only [run-server]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [project-estimation.views :refer :all]
            [project-estimation.message-handler :refer [ring-ajax-get-or-ws-handshake
                                                        ring-ajax-post]]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (GET "/:estimation-board-id" [] (voting-page))
  (route/not-found "Not Found"))

(def app
  (wrap-gzip (wrap-defaults app-routes site-defaults)))

(defonce server (atom nil))

(defn port
  []
  (Integer. (or (env :port)
                8080)))

(defn -main [& args]
  (reset! server (run-server #'app {:port (port)})))

(defn reset
  []
  (when @server
    (@server)
    (-main)))
