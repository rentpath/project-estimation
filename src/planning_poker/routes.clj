(ns planning-poker.routes
  (:use [org.httpkit.server :only [run-server]])
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [planning-poker.views :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [planning-poker.message-handler :refer [ring-ajax-get-or-ws-handshake
                                                    ring-ajax-post]]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (GET "/:game-number" [] (voting-page))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defonce server (atom nil))

(defn -main [& args]
  (reset! server (run-server #'app {:port 8080})))

(defn reset
  []
  (when @server
    (@server)
    (-main)))
