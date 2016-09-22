(ns planning-poker.client.cards
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   planning-poker.client.extensions))

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

(defn notify-estimate
  [event channel]
  (let [estimate (-> event .-target .-textContent)]
    (go (>! channel [:table/player-estimated estimate]))))

(defn select-card
  [channel]
  (fn [event]
    (change-active-card event)
    (notify-estimate event channel)))

(defn component
  [channel]
  [:ol.cards
   (for [x ["?" 0 1 2 3 5 8 13 20]]
     ^{:key x} [:li
                [:button.card {:on-click (select-card channel)} x]])])
