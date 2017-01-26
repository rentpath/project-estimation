(ns planning-poker.client.cards
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [planning-poker.client.utils :refer [path]]
   planning-poker.client.extensions))

(defn notify-card-selected
  [card channel]
  (go (>! channel [:table/player-estimated card])))

(def cards ["?" 0 1 2 3 5 8 13 20])
(defonce active-card (r/atom nil))

(defn activate-card
  [card]
  (reset! active-card card))

(defn deactivate-all-cards
  []
  (reset! active-card nil))

(defn select-card
  [card channel]
  (fn [_event]
    (activate-card card)
    (notify-card-selected card channel)))

(defn active?
  [card]
  (= card @active-card))

(defn component
  [channel]
  [:ol.cards
   ;; doall allows component to re-render when active-card gets updated
   (doall (for [card cards]
            ^{:key card} [:li
                          [:button.card
                           {:on-click (select-card card channel)
                            :class (if (active? card) "active" "")}
                           card]]))])
