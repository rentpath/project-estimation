(ns planning-poker.client.form-parser)

(defn value
  "Retrieve the value of the event's target."
  [evt]
  (-> evt .-target .-value))
