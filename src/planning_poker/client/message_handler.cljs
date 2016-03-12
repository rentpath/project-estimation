(ns planning-poker.client.message-handler
  (:require-macros [cljs.core.async.macros :refer (go)])
  (:require [planning-poker.client.payload-handler :as payload-handler]
            [cljs.core.async :refer (<!)]))

(defmulti process!
  (fn [message players channel-data] (first message)))

(defmethod process! :default
  [message _ _]
  (println "Unhandled event:" (first message)))

(defmethod process! :chsk/handshake
  [_ _ {:keys [events-to-send send-fn]}]
  (go
    (loop []
      (let [event (<! events-to-send)]
        (send-fn event))
      (recur))))

(defmethod process! :chsk/state
  [message _ _]
  (println "State" message))

(defmethod process! :chsk/recv
  [message players _]
  (payload-handler/process! message players))
