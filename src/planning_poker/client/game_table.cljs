(ns planning-poker.client.game-table
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [planning-poker.client.cards :as cards]
   [planning-poker.client.login :as login]
   [planning-poker.client.players :as table-players]))

(defn- start-new-round
  [channel]
  (fn [event]
    (go (>! channel [:table/new-round-requested]))))

(defn component
  [players channel]
  [:div
   [login/component channel]
   [:div.game-table
    [:h1.game-table-heading "Remote Planning Poker"]
    [cards/component channel]
    [table-players/component players]
    [:button.reset {:on-click (start-new-round channel)} "Play a New Round"]]])
