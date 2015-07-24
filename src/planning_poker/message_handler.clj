(ns planning-poker.message-handler
  (:require [taoensso.sente :as sente]
            [clojure.pprint]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]))

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

(defn uids
  [connected-uids]
  (:any connected-uids))

(defn notify
  [player-ids]
  (fn [event data]
    (doseq [id player-ids]
      (chsk-send! id [event data]))))

(defn notify-players-updated
  [{uids :any}]
  ((notify uids) ::players-updated @players))

(defn notify-player-estimated
  [{uids :any} estimate]
  ((notify uids) ::player-estimated estimate))

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
  [event]
  :no-op)

(defmethod message-handler :planning-poker.core/player-joined
  [{:as event-message :keys [?data]}]
  (let [ring-request (:ring-req event-message)]
    (swap! players assoc (player-id ring-request) ?data)
    (notify-players-updated @connected-uids)))

(defmethod message-handler :planning-poker.core/player-estimated
  [{:as event-message :keys [?data]}]
  (let [ring-request (:ring-req event-message)]
    (notify-player-estimated @connected-uids {(player-id ring-request) ?data})
    (clojure.pprint/pprint {(player-id ring-request) ?data})))

;; Add your (defmethod message-handler <id> [event-message] <body>)s here...

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
                 (notify-players-updated @connected-uids)))))

(comment
  (chsk-send! "a2-b2-c3-d4-e5" [::players-updated {"a1-b2-c3-d4-e5" "Michael"}])
  (chsk-send! :sente/all-users-without-uid [::player-estimated {:a :data}])
  (player-id {:cookies {"ring-session" {:value "abcd"}}})
  (clojure.pprint/pprint @players)
  (notify-players-updated @connected-uids)
  (remove-players #{"7aa0b59a-7407-4426-8deb-60501425a1cc"})
  )
