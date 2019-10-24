(ns project-estimation.client.participant)

(defn component
  [participant]
  [:li.participant
   [:span.name (:name participant)]
   [:span.estimate {:class (cond
                             (:show-estimate? participant) :estimate-show
                             (:estimate participant) :estimate-hide
                             :else :estimate-waiting)}
    (when (:show-estimate? participant) (:estimate participant))]])
