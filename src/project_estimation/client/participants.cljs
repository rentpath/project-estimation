(ns project-estimation.client.participants
  (:require
   [project-estimation.client.participant :as participant]))

(defn- all-participants-estimated?
  [participants]
  (every? :estimate (vals participants)))

(defn- add-estimate-flag
  [participants]
  (if (all-participants-estimated? participants)
    (reduce-kv (fn [acc k v]
                 (assoc acc k (assoc v :show-estimate? true)))
               {}
               participants)
    participants))

(defn component
  [participants]
  (let [participants (add-estimate-flag participants)]
    [:div.participants
     [:h2 "Participants"]
     [:div.names
      [:ul
       (for [[participant-id participant] participants]
         ^{:key participant-id} [participant/component participant])]]]))

(comment
  (all-participants-estimated? {"abc-def" {:name "El Guapo"}})
  (all-participants-estimated? {"abc-def" {:name "El Guapo" :estimate 3}})
  )
