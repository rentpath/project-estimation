(ns planning-poker.client.card-state)

(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))

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
