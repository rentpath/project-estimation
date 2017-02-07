(ns planning-poker.client.players
  (:require
   [planning-poker.client.player :as player]))

(defn- all-players-estimated?
  [players]
  (every? :estimate (vals players)))

(defn- add-estimate-flag
  [players]
  (if (all-players-estimated? players)
    (reduce-kv (fn [acc k v]
                 (assoc acc k (assoc v :show-estimate? true)))
               {}
               players)
    players))

(defn component
  [players]
  (let [players (add-estimate-flag players)]
    [:div.players
     [:h2 "Players"]
     [:div.names
      [:ul
       (for [[player-id player] players]
         ^{:key player-id} [player/component player])]]]))

(comment
  (all-players-estimated? {"abc-def" {:name "El Guapo"}})
  (all-players-estimated? {"abc-def" {:name "El Guapo" :estimate 3}})
  )
