(ns project-estimation.client.estimation-board-test
  (:require
   [cljs.test :refer-macros [deftest is testing]]
   [project-estimation.client.estimation-board :as t]))

(deftest active-participants-test
  (testing "when all participants are active"
    (let [participants {"a1" {:name "Michael"}}]
      (is (= participants
             (t/active-participants participants)))))
  (testing "when there is an observer"
    (let [participants {"a1" {:name "Michael"}
                        "b2" {:name "Joy"
                              :observer true}}]
      (is (= {"a1" {:name "Michael"}}
             (t/active-participants participants))))))
