(ns planning-poker.client.game-table
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            [planning-poker.client.cards :as cards]
            [planning-poker.client.login :as login]
            [planning-poker.client.players :as table-players]
            [planning-poker.client.utils :refer [path]]))

(defn- start-new-round
  [channel]
  (fn [_event]
    (go (>! channel [:table/new-round-requested (path)]))))

(defn- reveal-cards
  [channel]
  (fn [_event]
    (go (>! channel [:table/reveal-cards (path)]))))

(defn active-players
  [players]
  (into {} (filter #(-> % val :observer not) players)))

(defn component
  [players channel]
  [:div
   [login/component channel]
   [:div.game-table
    [:h1.game-table-heading "Remote Planning Poker"]
    [cards/component channel]
    [table-players/component (active-players @players)]
    [:div
     [:button.reset {:on-click (start-new-round channel)} "Play a New Round"]
     [:button.reveal-cards {:on-click (reveal-cards channel)} "Reveal Cards"]]]])
