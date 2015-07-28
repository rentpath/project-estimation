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
  ((notify uids) ::players-updated (player-names @players)))

(defn notify-players-estimated
  [{uids :any}]
  ((notify uids) ::players-updated @players))

(defn notify-new-round-started
  [{uids :any}]
  ((notify uids) ::players-updated @players))

(defn player-names
  [players]
  (reduce (fn [accumulator [player-id player-data]]
            (assoc accumulator player-id {:name (:name player-data)}))
            {}
            players))
; (player-names {"a1-b2" {:name "El Guapo" :estimate 3}})

(defn player-estimates
  [players]
  (reduce (fn [accumulator [player-id player-data]]
            (assoc accumulator player-id {:estimate (:estimate player-data)}))
            {}
            players))
; (player-estimates {"a1-b2" {:name "El Guapo" :estimate 3}})

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
  [{:keys [?data ring-req]}]
  (swap! players assoc (player-id ring-req) {:name ?data})
  (notify-players-updated @connected-uids))

(defmethod message-handler :planning-poker.core/player-estimated
  [{:keys [?data ring-req]}]
  (swap! players assoc-in [(player-id ring-req) :estimate] ?data)
  (when (all-players-estimated? @players)
    (notify-players-estimated @connected-uids)))

(defmethod message-handler :planning-poker.core/new-round-requested
  [data]
  (reset! players (player-names @players))
  (notify-new-round-started @connected-uids))

(sente/start-chsk-router! ch-chsk message-handler*)

(defn player-estimated?
  [player]
  (boolean (:estimate (first (vals player)))))
; (player-estimated? {"a1-b2" {:name "El Guapo" :estimate 3}})
; (player-estimated? {"a1-b2" {:name "El Guapo"}})

; We expect parameter to be in this format: {"a1-b2" {:name "El Guapo" :estimate 3}}
(defn all-players-estimated?
  [players]
  (every? true? (map (fn [[id player-data]] (player-estimated? {id player-data})) players)))
; (all-players-estimated? {"a1-b2" {:name "El Guapo" :estimate 3}})
; (all-players-estimated? {"a1-b2" {:name "El Guapo"}})

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
