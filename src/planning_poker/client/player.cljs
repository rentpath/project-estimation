(ns planning-poker.client.player)

(defn component
  [player]
  [:li.player
   [:span.name (:name player)]
   [:span.estimate {:class (cond
                             (:show-estimate? player) :estimate-show
                             (:estimate player) :estimate-hide
                             :else :estimate-waiting)}
    (when (:show-estimate? player) (:estimate player))]])
