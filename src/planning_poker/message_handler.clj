(ns planning-poker.message-handler
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [planning-poker.notifier :as notifier]
            [planning-poker.game-table :as table]))

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

(defn table-for-player-id
  [players player-id]
  (get-in players [player-id :table-id]))

(defn tables-for-player-ids
  [players player-ids]
  (->> player-ids
       (map (partial table-for-player-id players))
       set))

(defn players-at-table
  [players table-id]
  (reduce-kv (fn [acc id data]
               (if (= table-id (:table-id data))
                 (assoc acc id data)
                 acc))
          {}
          players))

(defn player-ids-at-table
  [players table-id]
  (keys (players-at-table players table-id)))

(defmulti message-handler :id)

(defmethod message-handler :default
  [{:keys [event ?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:umatched-as-echoed-from-from-server event})))

(defmethod message-handler :chsk/ws-ping
  [event-message]
  :no-op)

(defmethod message-handler :table/player-joined
  [{player :?data req :ring-req}]
  (let [old-table-id (table-for-player-id @players (user-id req))]
    (table/add-player! players
                       (user-id req)
                       player)
    (notifier/notify-players-updated (player-ids-at-table @players (:table-id player))
                                     (players-at-table @players (:table-id player))
                                     chsk-send!)
    (when old-table-id
      (notifier/notify-players-updated (player-ids-at-table @players old-table-id)
                                       (players-at-table @players old-table-id)
                                       chsk-send!))))

(defmethod message-handler :table/player-estimated
  [{estimate :?data req :ring-req}]
  (let [id (user-id req)
        table-id (table-for-player-id @players id)]
    (table/estimate! players id estimate)
    (notifier/notify-players-estimated (player-ids-at-table @players table-id)
                                       (players-at-table @players table-id)
                                       chsk-send!)))

(defmethod message-handler :table/new-round-requested
  [{table-id :?data}]
  (table/reset-estimates! players table-id)
  (notifier/notify-new-round-started (player-ids-at-table @players table-id)
                                     (players-at-table @players table-id)
                                     chsk-send!))

(defn uids-to-remove
  [old-connected-uids current-connected-uids]
  (clojure.set/difference (uids old-connected-uids) (uids current-connected-uids)))

(sente/start-chsk-router! ch-chsk message-handler)
(add-watch connected-uids
           :remove-players
           (fn [_key _ref old-state new-state]
             (let [invalid-uids (uids-to-remove old-state new-state)]
               (when (seq invalid-uids)
                 (let [table-ids (tables-for-player-ids @players invalid-uids)]
                   (table/remove-players! players invalid-uids)
                   (doseq [table-id table-ids]
                     (notifier/notify-players-updated
                      (player-ids-at-table @players table-id)
                      (players-at-table @players table-id)
                      chsk-send!)))))))
