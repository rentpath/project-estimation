(ns planning-poker.routes
  (:use planning-poker.views
        [hiccup.middleware :only (wrap-base-url)]
        [org.httpkit.server :only [run-server]])

  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn (fn [ring-req]
                                                                          (-> ring-req
                                                                              :cookies
                                                                              (get "ring-session")
                                                                              :value))})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def connected-uids connected-uids)) ; Watchable, read-only atom

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (GET "/:game-number" [] (voting-page))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

;; TODO read a config variable from command line, env, or file?
(defn in-dev? [args] true)

(defonce server (atom nil))

(defn -main [& args]
  (reset! server (run-server #'app {:port 8080})))

(defn reset
  []
  (when @server
    (@server)
    (-main)))

;; run-server returns a function that stops the server
; (let [server (run-server app options)]
;   (server))

(defmulti msg-handler :id) ; Dispatch on id

;; Wrap for logging, catching, etc.:
(defn msg-handler* [{:as ev-msg :keys [id ?data event]}]
  ; (println "Event: %s" event)
  (msg-handler ev-msg))

; Server-side methods
(defmethod msg-handler :default ; Fallback
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-as-echoed-from-from-server event}))))

(defmethod msg-handler :chsk/ws-ping
  [evt]
  :no-op)

(defmethod msg-handler :planning-poker.core/user-joined-session
  [evt]
  (println "User joined: " evt)
  (println (str @connected-uids))
  (notify-all @connected-uids))
;;(defmethod msg-handler :chsk/)
;; Add your (defmethod msg-handler <id> [ev-msg] <body>)s here...

(defn notify-all
  [{uids :any}]
  (doseq [uid uids]
    (chsk-send! uid [::user-joined-session {:names uids}])))
; (notify-all @connected-uids)
(sente/start-chsk-router! ch-chsk msg-handler*)

(comment
  (chsk-send! "35b37a13-4318-4434-a762-2f79b37ef5df" [::user-joined-session {:name "Michael"}])
  (chsk-send! :sente/all-users-without-uid [::user-joined-session {:name "Michael"}])
  (chsk-send! :sente/all-users-without-uid [::user-estimated {:a :data}])

  )
