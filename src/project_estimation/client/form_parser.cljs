(ns project-estimation.client.form-parser)

(defn value
  "Retrieve the value of the event's target."
  [evt]
  (-> evt .-target .-value))
