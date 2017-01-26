(ns planning-poker.message-handler-test
  (:require [clojure.test :refer :all]
            [planning-poker.message-handler :refer :all]))

(deftest user-id-test
  (testing "returns the correct player ID from a request"
    (is (= "abcd"
           (user-id {:cookies {"ring-session" {:value "abcd"}}})))))

(deftest player-ids-at-table-test
  (testing "returning all player ids at a table"
    (let [players {"a1-b2" {:name "El Guapo" :table-id "x1"}
                   "e5-f6" {:name "R2-D2" :table-id "x1"}
                   "g8-h9" {:name "Michael" :table-id "y2"}}]
      (is (= ["a1-b2" "e5-f6"]
             (player-ids-at-table players "x1"))))))

(deftest players-at-table-test
  (testing "returning all players at a table"
    (let [players {"a1-b2" {:name "El Guapo" :table-id "x1"}
                   "e5-f6" {:name "R2-D2" :table-id "x1"}
                   "g8-h9" {:name "Michael" :table-id "y2"}}]
      (is (= {"a1-b2" {:name "El Guapo" :table-id "x1"}
              "e5-f6" {:name "R2-D2" :table-id "x1"}}
             (players-at-table players "x1"))))))

(deftest table-for-player-id-test
  (testing "the table ID of a player"
    (let [players {"a1-b2" {:name "El Guapo" :table-id "x1"}
                   "e5-f6" {:name "R2-D2" :table-id "y2"}}]
      (is (= "x1"
             (table-for-player-id players "a1-b2")))))
  (testing "a new player"
    (is nil? (table-for-player-id {} "a1-b2"))))

(deftest tables-for-player-ids-test
  (let [players {"a1-b2" {:name "El Guapo" :table-id "x1"}
                 "e5-f6" {:name "R2-D2" :table-id "y2"}
                 "g8-h9" {:name "Michael" :table-id "z3"}}]
    (is (= #{"x1" "y2"}
           (tables-for-player-ids players #{"a1-b2" "e5-f6"})))))
