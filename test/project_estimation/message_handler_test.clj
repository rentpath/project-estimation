(ns project-estimation.message-handler-test
  (:require [clojure.test :refer :all]
            [project-estimation.message-handler :refer :all]))

(deftest user-id-test
  (testing "returns the correct participant ID from a request"
    (is (= "abcd"
           (user-id {:cookies {"ring-session" {:value "abcd"}}})))))

(deftest participants-at-board-test
  (testing "returning all participants at a board"
    (let [participants {"a1-b2" {:name "El Guapo" :board-id "x1"}
                        "e5-f6" {:name "Beth" :board-id "x1"}
                        "g8-h9" {:name "Michael" :board-id "y2"}}]
      (is (= {"a1-b2" {:name "El Guapo" :board-id "x1"}
              "e5-f6" {:name "Beth" :board-id "x1"}}
             (participants-at-board participants "x1"))))))

(deftest board-for-participant-id-test
  (testing "the board ID of a participant"
    (let [participants {"a1-b2" {:name "El Guapo" :board-id "x1"}
                        "e5-f6" {:name "Beth" :board-id "y2"}}]
      (is (= "x1"
             (board-for-participant-id participants "a1-b2")))))
  (testing "a new participant"
    (is nil? (board-for-participant-id {} "a1-b2"))))

(deftest boards-for-participant-ids-test
  (let [participants {"a1-b2" {:name "El Guapo" :board-id "x1"}
                      "e5-f6" {:name "Beth" :board-id "y2"}
                      "g8-h9" {:name "Michael" :board-id "z3"}}]
    (is (= #{"x1" "y2"}
           (boards-for-participant-ids participants #{"a1-b2" "e5-f6"})))))
