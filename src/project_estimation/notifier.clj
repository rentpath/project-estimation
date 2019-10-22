(ns project-estimation.notifier)

(defn- notify
  [participants notify-client-fn event]
  (doseq [id (keys participants)]
    (notify-client-fn id [event participants])))

(defn notify-participants-updated
  [participants notify-client-fn]
  (notify participants notify-client-fn ::participants-updated))

(def notify-participants-estimated notify-participants-updated)

(defn notify-new-round-started
  [participants notify-client-fn]
  (notify participants notify-client-fn ::new-round-started))

(comment
  (chsk-send! "a2-b2-c3-d4-e5" [:board/participants-updated {"a1-b2-c3-d4-e5" "Michael"}])
  )
