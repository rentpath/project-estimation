(ns planning-poker.message-handler
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [planning-poker.notifier :as notifier]
            [planning-poker.table :as table]))

(defonce players (atom {}))

(defn user-id
  [ring-request]
  (-> ring-request
      :cookies
      (get "ring-session")
      :value))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn user-id})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def connected-uids connected-uids)) ; Watchable, read-only atom

(defn uids
  [connected-uids]
  (:any connected-uids))

(defmulti message-handler :id)

(defmethod message-handler :default
  [{:keys [event ?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:umatched-as-echoed-from-from-server event})))

(defmethod message-handler :chsk/ws-ping
  [event-message]
  :no-op)

(defmethod message-handler :table/player-joined
  [{:keys [?data ring-req]}]
  (table/add-player! players (user-id ring-req) ?data)
  (notifier/notify-players-updated @connected-uids @players chsk-send!))

(defmethod message-handler :table/player-estimated
  [{:keys [?data ring-req]}]
  (table/estimate! players (user-id ring-req) ?data)
  (notifier/notify-players-estimated @connected-uids @players chsk-send!))

(defmethod message-handler :table/new-round-requested
  [_]
  (table/reset-estimates! players)
  (notifier/notify-new-round-started @connected-uids @players chsk-send!))

(defn uids-to-remove
  [old-connected-uids current-connected-uids]
  (clojure.set/difference (uids old-connected-uids) (uids current-connected-uids)))

(sente/start-chsk-router! ch-chsk message-handler)
(add-watch connected-uids
           :remove-players
           (fn [_key _ref old-state new-state]
             (let [invalid-uids (uids-to-remove old-state new-state)]
               (when (seq invalid-uids)
                 (table/remove-players! players invalid-uids)
                 (notifier/notify-players-updated @connected-uids @players chsk-send!)))))

(comment
  (chsk-send! "a2-b2-c3-d4-e5" [:table/players-updated {"a1-b2-c3-d4-e5" "Michael"}])
  (chsk-send! :sente/all-users-without-uid [:table/player-estimated {:a :data}])
  )
