(ns planning-poker.notifier)

(defn- notify
  [players notify-client-fn event]
  (doseq [id (keys players)]
    (notify-client-fn id [event players])))

(defn notify-players-updated
  [players notify-client-fn]
  (notify players notify-client-fn ::players-updated))

(def notify-players-estimated notify-players-updated)

(defn notify-new-round-started
  [players notify-client-fn]
  (notify players notify-client-fn ::new-round-started))

(comment
  (chsk-send! "a2-b2-c3-d4-e5" [:table/players-updated {"a1-b2-c3-d4-e5" "Michael"}])
  )
