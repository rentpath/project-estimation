(ns project-estimation.estimation-board-test
  (:require
   [project-estimation.estimation-board :refer :all]
   [clojure.test :refer :all]))

(deftest remove-participants!-test
  (testing "removes a participant from the collection of participants"
    (let [participants (atom {"a1-b2" {:name "El Guapo" :estimate 3}
                              "e5-f6" {:name "Beth" :estimate 5}})]
      (remove-participants! participants #{"e5-f6"})
      (is (= {"a1-b2" {:name "El Guapo" :estimate 3}}
             @participants)))))

(deftest add-participant!-test
  (testing "adds a participant to the collection of participants"
    (let [participants (atom {"a1-b2" {:name "El Guapo"
                                       :board-id "z1"}})
          final-participants {"a1-b2" {:name "El Guapo"
                                       :board-id "z1"}
                              "c3-d4" {:name "Luke"
                                       :board-id "z1"}}]
      (add-participant! participants "c3-d4" {:name "Luke"
                                              :board-id "z1"})
      (is (= final-participants
             @participants)))))

(deftest estimate!-test
  (testing "adds an estimate to a participant"
    (let [participants (atom {"a1-b2" {:name "El Guapo"
                                       :board-id "z1"}})]
      (estimate! participants "a1-b2" 3)
      (is (= {"a1-b2" {:name "El Guapo"
                       :board-id "z1"
                       :estimate 3}}
             @participants)))))

(deftest reset-estimates!-test
  (testing "removes all participant estimates at a board"
    (let [participants (atom {"a1-b2" {:name "El Guapo"
                                       :board-id "x1"
                                       :estimate 3}
                              "e5-f6" {:name "Beth"
                                       :board-id "x1"
                                       :estimate 5}})]
      (reset-estimates! participants "x1")
      (is (= {"a1-b2" {:name "El Guapo"
                       :board-id "x1"}
              "e5-f6" {:name "Beth"
                       :board-id "x1"}}
             @participants)))))

(deftest remove-estimates-test
  (testing "all participants at sanme board"
    (let [participants {"a1-b2" {:name "El Guapo"
                                 :board-id "x1"
                                 :estimate 3}
                        "e5-f6" {:name "Beth"
                                 :board-id "x1"
                                 :estimate 5}}
          participants-without-estimates {"a1-b2" {:name "El Guapo"
                                                   :board-id "x1"}
                                          "e5-f6" {:name "Beth"
                                                   :board-id "x1"}}]
      (is (= participants-without-estimates
             (remove-estimates participants "x1")))))

  (testing "participants at different boards"
    (let [participants {"a1-b2" {:name "El Guapo"
                                 :board-id "x1"
                                 :estimate 3}
                        "e5-f6" {:name "Beth"
                                 :board-id "y2"
                                 :estimate 5}}
          participants-after {"a1-b2" {:name "El Guapo"
                                       :board-id "x1"}
                              "e5-f6" {:name "Beth"
                                       :board-id "y2"
                                       :estimate 5}}]
      (is (= participants-after
             (remove-estimates participants "x1"))))))

(deftest force-estimates!-test
  (testing "setting any unestimated participants to an estimate of '?'"
    (testing "all participants at same board"
      (let [participants (atom {"a1-b2" {:name "El Guapo"
                                         :board-id "x1"
                                         :estimate 3}
                                "e5-f6" {:name "Beth"
                                         :board-id "x1"}})]
        (force-estimates! participants "x1")
        (is (= "?"
               (get-in @participants ["e5-f6" :estimate])))))))
