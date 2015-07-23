(ns planning-poker.message-handler
  (:require [taoensso.sente :as sente]
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

(defn notify-all
  [{uids :any}]
  (doseq [uid uids]
    (chsk-send! uid [::user-joined-session {:names uids}])))

(defmulti message-handler :id)

(defn message-handler*
  [{:as event-message :keys [id ?data event]}]
  (message-handler event-message))

(defmethod message-handler :default
  [{:as event-message :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-as-echoed-from-from-server event}))))

(defmethod message-handler :chsk/ws-ping
  [evt]
  :no-op)

(defmethod message-handler :planning-poker.core/user-joined-session
  [evt]
  (println "User joined: " evt)
  (println (str @connected-uids))
  (notify-all @connected-uids))

;; Add your (defmethod message-handler <id> [event-message] <body>)s here...

(sente/start-chsk-router! ch-chsk message-handler*)

(defonce players (atom {}))

; (notify-all @connected-uids)
;; run-server returns a function that stops the server
; (let [server (run-server app options)]
;   (server))

; (add-watch connected-uids :update-players (fn [key ref old new] (notify-all new)))

(comment
  (chsk-send! "ebf214e7-d41a-4990-acad-c60675d30a30" [::user-joined-session {:name "Michael"}])
  (chsk-send! :sente/all-users-without-uid [::user-estimated {:a :data}])
  )
