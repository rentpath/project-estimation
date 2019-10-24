(ns project-estimation.client.estimation-board
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [>!]]
            [project-estimation.client.points :as points]
            [project-estimation.client.login :as login]
            [project-estimation.client.participants :as participants]
            [project-estimation.client.utils :refer [path]]))

(defn- start-new-round
  [channel]
  (fn [_event]
    (go (>! channel [:board/new-round-requested (path)]))))

(defn- reveal-estimates
  [channel]
  (fn [_event]
    (go (>! channel [:board/reveal-estimates (path)]))))

(defn active-participants
  [participants]
  (into {} (filter #(-> % val :observer not) participants)))

(defn component
  [participants channel]
  [:div
   [login/component channel]
   [:div.estimation-board
    [:h1.estimation-board-heading "Remote Project Estimation"]
    [points/component channel]
    [participants/component (active-participants @participants)]
    [:div
     [:button.reset {:on-click (start-new-round channel)} "Start a New Round"]
     [:button.reveal-estimates {:on-click (reveal-estimates channel)} "Reveal Estimates"]]]])
