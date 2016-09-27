(ns planning-poker.table-test
  (:require
   [planning-poker.table :refer :all]
   [clojure.test :refer :all]))

(deftest player-names-test
  (testing "returns a collection of player names"
    (let [all-players {"a1-b2" {:name "El Guapo" :estimate 3}
                       "r2-d2" {:name "R2-D2" :estimate 5}}
          names {"a1-b2" {:name "El Guapo"}
                 "r2-d2" {:name "R2-D2"}}]
      (is (= names
             (player-names all-players))))))

(deftest remove-players!-test
  (testing "removes a player from the collection of players"
    (let [players (atom {"a1-b2" {:name "El Guapo" :estimate 3}
                         "e5-f6" {:name "R2-D2" :estimate 5}})]
      (remove-players! players #{"e5-f6"})
      (is (= {"a1-b2" {:name "El Guapo" :estimate 3}}
             @players)))))

(deftest add-player!-test
  (testing "adds a player to the collection of players"
    (let [players (atom {"a1-b2" {:name "El Guapo"}})
          final-players {"a1-b2" {:name "El Guapo"}
                         "c3-d4" {:name "Luke Skywalker"}}]
      (add-player! players "c3-d4" "Luke Skywalker")
      (is (= final-players
             @players)))))

(deftest estimate!-test
  (testing "adds an estimate to a player"
    (let [players (atom {"a1-b2" {:name "El Guapo"}})]
      (estimate! players "a1-b2" 3)
      (is (= {"a1-b2" {:name "El Guapo" :estimate 3}}
             @players)))))

(deftest reset-estimates!-test
  (testing "removes all player estimates"
    (let [players (atom {"a1-b2" {:name "El Guapo" :estimate 3}
                         "e5-f6" {:name "R2-D2" :estimate 5}})]
      (reset-estimates! players)
      (is (= {"a1-b2" {:name "El Guapo"}, "e5-f6" {:name "R2-D2"}}
             @players)))))
