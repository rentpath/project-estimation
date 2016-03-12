(ns planning-poker.client.payload-handler
  (:require [planning-poker.client.card-state :refer [deactivate-all-cards]]))

(defmulti process!
  (fn [message players] (-> message second first)))

;; Initial parameter is in this format:
;; [:chsk/recv [:planning-poker.routes/players-updated {"a13-18-434-a62-2f5df" {:name "Michael"}}]]
(defmethod process! :planning-poker.message-handler/players-updated
  [[_ [_ data]] players]
  (reset! players data))

(defmethod process! :planning-poker.message-handler/new-round-started
  [[_ [_ data]] players]
  (reset! players data)
  (deactivate-all-cards))
