(ns planning-poker.client.game-table-test
  (:require
   [cljs.test :refer-macros [deftest is testing]]
   [planning-poker.client.game-table :as t]))

(deftest active-players-test
  (testing "when all players are active"
    (let [players {"a1" {:name "Michael"}}]
      (is (= players
             (t/active-players players)))))
  (testing "when there is an observer"
    (let [players {"a1" {:name "Michael"}
                   "b2" {:name "Joy"
                         :observer true}}]
      (is (= {"a1" {:name "Michael"}}
             (t/active-players players))))))
