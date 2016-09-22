(ns planning-poker.message-handler
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [planning-poker.notifier :as notifier]))

(defonce players (atom {}))

(defn player-id
  [ring-request]
  (-> ring-request
      :cookies
      (get "ring-session")
      :value))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn player-id})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def connected-uids connected-uids)) ; Watchable, read-only atom

(defn player-names
  [players]
  (reduce (fn [accumulator [player-id player-data]]
            (assoc accumulator player-id {:name (:name player-data)}))
            {}
            players))
; (player-names {"a1-b2" {:name "El Guapo" :estimate 3}})

(defn uids
  [connected-uids]
  (:any connected-uids))

(defmulti message-handler :id)

(defn message-handler*
  [{:as event-message :keys [id ?data event]}]
  (message-handler event-message))

(defmethod message-handler :default
  [{:as event-message :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid (:uid session)]
    (when ?reply-fn
      (?reply-fn {:umatched-as-echoed-from-from-server event}))))

(defmethod message-handler :chsk/ws-ping
  [event]
  :no-op)

(defmethod message-handler :table/player-joined
  [{:keys [?data ring-req]}]
  (swap! players assoc (player-id ring-req) {:name ?data})
  (notifier/notify-players-updated @connected-uids @players chsk-send!))

(defmethod message-handler :table/player-estimated
  [{:keys [?data ring-req]}]
  (swap! players assoc-in [(player-id ring-req) :estimate] ?data)
  (notifier/notify-players-estimated @connected-uids @players chsk-send!))

(defmethod message-handler :planning-poker.client.core/new-round-requested
  [data]
  (reset! players (player-names @players))
  (notifier/notify-new-round-started @connected-uids @players chsk-send!))

(sente/start-chsk-router! ch-chsk message-handler*)

(defn uids-to-remove
  [old-connected-uids current-connected-uids]
  (clojure.set/difference (uids old-connected-uids) (uids current-connected-uids)))

(defn remove-players
  [players-to-remove]
  (swap! players (fn [collection] (apply dissoc collection players-to-remove))))

(add-watch connected-uids
           :remove-players
           (fn [key ref old-state new-state]
             (let [invalid-uids (uids-to-remove old-state new-state)]
               (when (seq invalid-uids)
                 (remove-players invalid-uids)
                 (notifier/notify-players-updated @connected-uids @players chsk-send!)))))

(comment
  (chsk-send! "a2-b2-c3-d4-e5" [::players-updated {"a1-b2-c3-d4-e5" "Michael"}])
  (chsk-send! :sente/all-users-without-uid [::player-estimated {:a :data}])
  (player-id {:cookies {"ring-session" {:value "abcd"}}})
  (remove-players #{"7aa0b59a-7407-4426-8deb-60501425a1cc"})
  )
