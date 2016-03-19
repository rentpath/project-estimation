(ns planning-poker.client.card-state
  (:require planning-poker.client.extensions))

(defn deactivate-all-cards
  []
  (let [cards (.getElementsByClassName js/document "card")]
    (doseq [card cards]
      (-> card .-classList (.remove "active")))))

(defn change-active-card
  [event]
  (deactivate-all-cards)
  (let [card (.-currentTarget event)]
    (-> card .-classList (.add "active"))))
