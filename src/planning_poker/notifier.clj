(ns planning-poker.notifier)

(defn- notify
  [player-ids notify-client-fn]
  (fn [event data]
    (doseq [id player-ids]
      (notify-client-fn id [event data]))))

(defn notify-players-updated
  [{player-ids :any} players notify-client-fn]
  ((notify player-ids notify-client-fn) ::players-updated players))

(defn notify-players-estimated
  [{player-ids :any} players notify-client-fn]
  ((notify player-ids notify-client-fn) ::players-updated players))

(defn notify-new-round-started
  [{player-ids :any} players notify-client-fn]
  ((notify player-ids notify-client-fn) ::new-round-started players))
