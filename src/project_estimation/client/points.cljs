(ns project-estimation.client.points
  (:require-macros
   [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [>!]]
   [reagent.core :as r]
   [project-estimation.client.utils :refer [path]]
   project-estimation.client.extensions))

(def points ["?" 0 1 2 3 5 8 13 20])

(defonce active-point (r/atom nil))

(defn notify-point-selected
  [point channel]
  (go (>! channel [:board/participant-estimated point])))

(defn activate-point
  [point]
  (reset! active-point point))

(defn deactivate-all-points
  []
  (reset! active-point nil))

(defn select-point
  [point channel]
  (fn [_event]
    (activate-point point)
    (notify-point-selected point channel)))

(defn active?
  [point]
  (= point @active-point))

(defn component
  [channel]
  [:ol.points
   ;; doall allows component to re-render when active-point gets updated
   (doall (for [point points]
            ^{:key point} [:li
                          [:button.point
                           {:on-click (select-point point channel)
                            :class (if (active? point) "active" "")}
                           point]]))])
