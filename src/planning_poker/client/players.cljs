(ns planning-poker.client.players
  (:require
   [planning-poker.client.player :as player]))

(defn- estimated-players
  [players]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (assoc v :show-estimate? true)))
    {}
    players))

(defn- all-players-estimated?
  [players]
  (every? :estimate (vals players)))

(defn- active-players
  [players]
  (into {} (filter #(-> % val :observer not) players)))

(defn component
  [players]
  (when (all-players-estimated? (active-players @players))
    (swap! players estimated-players))
  [:div.players
   [:h2 "Players"]
   [:div.names
    [:ul
     (for [[player-id player] (active-players @players)]
       ^{:key player-id} [player/component player])]]])

(comment
  (all-players-estimated? {"abc-def" {:name "El Guapo"}})
  (all-players-estimated? {"abc-def" {:name "El Guapo" :estimate 3}})
  )
