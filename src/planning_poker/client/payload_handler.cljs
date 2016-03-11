(ns planning-poker.client.payload-handler)

(defmulti process!
  (fn [message players callback] (-> message second first)))

;; Initial parameter is in this format:
;; [:chsk/recv [:planning-poker.routes/players-updated {"a13-18-434-a62-2f5df" {:name "Michael"}}]]
(defmethod process! :planning-poker.message-handler/players-updated
  [[_ [_ data]] players _]
  (reset! players data))

(defmethod process! :planning-poker.message-handler/new-round-started
  [[_ [_ data]] players callback]
  (reset! players data)
  (callback))
