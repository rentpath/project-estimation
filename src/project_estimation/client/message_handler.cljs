(ns project-estimation.client.message-handler
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require [project-estimation.client.payload-handler :as payload-handler]
            [cljs.core.async :refer (<!)]))

(defmulti process!
  (fn [message participants channel-data] (first message)))

(defmethod process! :default
  [message _ _]
  :no-op)

(defmethod process! :chsk/handshake
  [_ _ {:keys [events-to-send send-fn]}]
  (go
    (loop []
      (let [event (<! events-to-send)]
        (send-fn event))
      (recur))))

(defmethod process! :chsk/state
  [_ _ _]
  :no-op)

(defmethod process! :chsk/recv
  [message participants _]
  (payload-handler/process! message participants))
