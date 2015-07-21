(ns planning-poker.routes
  (:use planning-poker.views
        [hiccup.middleware :only (wrap-base-url)]
        [org.httpkit.server :only [run-server]])

  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/:game-number" [] (voting-page))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

;; TODO read a config variable from command line, env, or file?
(defn in-dev? [args] true)

;; Entry point. `lein run` will pick up and start from here
(defn -main [& args]
  (let [handler (if (in-dev? args)
                  (reload/wrap-reload (site #'app))
                  (site app))]
    (run-server handler {:port 8080})))

;; run-server returns a function that stops the server
; (let [server (run-server app options)]
;   (server))
