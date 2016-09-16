(ns planning-poker.notifier-test
  (:require [planning-poker.notifier :refer :all]
            [clojure.test :refer :all]))

(def player-ids ["a1" "b2"])
(def player-data {"a1" {:name "A"}, "b2" {:name "B"}})

(deftest notifiy-players-updated-test
  (testing "notifying each player"
    (let [counter (atom 0)
          notify-client-fn (fn [_ _] (swap! counter inc))]
      (notify-players-updated {:any player-ids}
                              player-data
                              notify-client-fn)
      (is (= 2 @counter)
          "The counter got called once for each of our two player IDs"))))

(deftest notifiy-players-estimated-test
  (testing "notifying each player"
    (let [counter (atom 0)
          notify-client-fn (fn [_ _] (swap! counter inc))]
      (notify-players-estimated {:any player-ids}
                                player-data
                                notify-client-fn)
      (is (= 2 @counter)
          "The counter got called once for each of our two player IDs"))))

(deftest notifiy-new-round-started-test
  (testing "notifying each player"
    (let [counter (atom 0)
          notify-client-fn (fn [_ _] (swap! counter inc))]
      (notify-new-round-started {:any player-ids}
                                player-data
                                notify-client-fn)
      (is (= 2 @counter)
          "The counter got called once for each of our two player IDs"))))
