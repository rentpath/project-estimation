(ns planning-poker.routes
  (:use planning-poker.views
        [hiccup.middleware :only (wrap-base-url)]
        [org.httpkit.server :only [run-server]])

  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def connected-uids connected-uids)) ; Watchable, read-only atom

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/:game-number" [] (voting-page))
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params))

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
