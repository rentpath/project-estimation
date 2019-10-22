(ns project-estimation.message-handler
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [project-estimation.notifier :as notifier]
            [project-estimation.estimation-board :as board]))

(defonce participants (atom {}))

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

(defn board-for-participant-id
  [participants participant-id]
  (get-in participants [participant-id :board-id]))

(defn boards-for-participant-ids
  [participants participant-ids]
  (->> participant-ids
       (map (partial board-for-participant-id participants))
       set))

(defn participants-at-board
  [participants board-id]
  (reduce-kv (fn [acc id data]
               (if (= board-id (:board-id data))
                 (assoc acc id data)
                 acc))
             {}
             participants))

(defmulti message-handler :id)

(defmethod message-handler :default
  [{:keys [event ?reply-fn]}]
  (when ?reply-fn
    (?reply-fn {:umatched-as-echoed-from-from-server event})))

(defmethod message-handler :chsk/ws-ping
  [event-message]
  :no-op)

(defmethod message-handler :board/participant-joined
  [{participant :?data req :ring-req}]
  (let [old-board-id (board-for-participant-id @participants (user-id req))]
    (board/add-participant! participants
                            (user-id req)
                            participant)
    (notifier/notify-participants-updated (participants-at-board @participants (:board-id participant))
                                          chsk-send!)
    (when old-board-id (notifier/notify-participants-updated (participants-at-board @participants old-board-id)
                                                             chsk-send!))))

(defmethod message-handler :board/participant-estimated
  [{estimate :?data req :ring-req}]
  (let [id (user-id req)
        board-id (board-for-participant-id @participants id)]
    (board/estimate! participants id estimate)
    (notifier/notify-participants-estimated (participants-at-board @participants board-id)
                                            chsk-send!)))

(defmethod message-handler :board/new-round-requested
  [{board-id :?data}]
  (board/reset-estimates! participants board-id)
  (notifier/notify-new-round-started (participants-at-board @participants board-id)
                                     chsk-send!))

(defmethod message-handler :board/reveal-estimates
  [{board-id :?data}]
  (board/force-estimates! participants board-id)
  (notifier/notify-participants-estimated (participants-at-board @participants board-id)
                                          chsk-send!))

(defn uids-to-remove
  [old-connected-uids current-connected-uids]
  (clojure.set/difference (uids old-connected-uids) (uids current-connected-uids)))

(sente/start-chsk-router! ch-chsk message-handler)
(add-watch connected-uids
           :remove-participants
           (fn [_key _ref old-state new-state]
             (let [invalid-uids (uids-to-remove old-state new-state)]
               (when (seq invalid-uids)
                 (let [board-ids (boards-for-participant-ids @participants invalid-uids)]
                   (board/remove-participants! participants invalid-uids)
                   (doseq [board-id board-ids]
                     (notifier/notify-participants-updated (participants-at-board @participants board-id)
                                                           chsk-send!)))))))
