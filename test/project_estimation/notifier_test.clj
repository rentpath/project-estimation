(ns project-estimation.notifier-test
  (:require [project-estimation.notifier :refer :all]
            [clojure.test :refer :all]))

(def participant-ids ["a1" "b2"])
(def participants {"a1" {:name "A"}, "b2" {:name "B"}})

(deftest notifiy-participants-updated-test
  (testing "notifying each participant"
    (let [counter (atom 0)
          notify-client-fn (fn [_ _] (swap! counter inc))]
      (notify-participants-updated participants notify-client-fn)
      (is (= 2 @counter)
          "The counter got called once for each of our two participant IDs"))))

(deftest notifiy-participants-estimated-test
  (testing "notifying each participant"
    (let [counter (atom 0)
          notify-client-fn (fn [_ _] (swap! counter inc))]
      (notify-participants-estimated participants notify-client-fn)
      (is (= 2 @counter)
          "The counter got called once for each of our two participant IDs"))))

(deftest notifiy-new-round-started-test
  (testing "notifying each participant"
    (let [counter (atom 0)
          notify-client-fn (fn [_ _] (swap! counter inc))]
      (notify-new-round-started participants notify-client-fn)
      (is (= 2 @counter)
          "The counter got called once for each of our two participant IDs"))))
