(ns planning-poker.notifier)

(defn- notify
  [player-ids notify-client-fn]
  (fn [event data]
    (doseq [id player-ids]
      (notify-client-fn id [event data]))))

(defn notify-players-updated
  [player-ids players notify-client-fn]
  ((notify player-ids notify-client-fn) ::players-updated players))

(defn notify-players-estimated
  [player-ids players notify-client-fn]
  ((notify player-ids notify-client-fn) ::players-updated players))

(defn notify-new-round-started
  [player-ids players notify-client-fn]
  ((notify player-ids notify-client-fn) ::new-round-started players))

(comment
  (chsk-send! "a2-b2-c3-d4-e5" [:table/players-updated {"a1-b2-c3-d4-e5" "Michael"}])
  )
