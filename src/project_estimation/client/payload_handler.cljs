(ns project-estimation.client.payload-handler
  (:require [project-estimation.client.points :refer [deactivate-all-points]]))

(defmulti process!
  (fn [message participants] (-> message second first)))

;; Initial parameter is in this format:
;; [:chsk/recv [:project-estimation.routes/participants-updated {"a13-18-434-a62-2f5df" {:name "Michael"}}]]
(defmethod process! :project-estimation.notifier/participants-updated
  [[_ [_ data]] participants]
  (reset! participants data))

(defmethod process! :project-estimation.notifier/new-round-started
  [[_ [_ data]] participants]
  (reset! participants data)
  (deactivate-all-points))
