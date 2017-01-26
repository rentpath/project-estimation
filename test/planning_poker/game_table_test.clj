(ns planning-poker.game-table-test
  (:require
   [planning-poker.game-table :refer :all]
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
    (let [players (atom {"a1-b2" {:name "El Guapo"
                                  :table-id "z1"}})
          final-players {"a1-b2" {:name "El Guapo"
                                  :table-id "z1"}
                         "c3-d4" {:name "Luke Skywalker"
                                  :table-id "z1"}}]
      (add-player! players "c3-d4" {:name "Luke Skywalker"
                                    :table-id "z1"})
      (is (= final-players
             @players)))))

(deftest estimate!-test
  (testing "adds an estimate to a player"
    (let [players (atom {"a1-b2" {:name "El Guapo"
                                  :table-id "z1"}})]
      (estimate! players "a1-b2" 3)
      (is (= {"a1-b2" {:name "El Guapo"
                       :table-id "z1"
                       :estimate 3}}
             @players)))))

(deftest reset-estimates!-test
  (testing "removes all player estimates at a table"
    (let [players (atom {"a1-b2" {:name "El Guapo"
                                  :table-id "x1"
                                  :estimate 3}
                         "e5-f6" {:name "R2-D2"
                                  :table-id "x1"
                                  :estimate 5}})]
      (reset-estimates! players "x1")
      (is (= {"a1-b2" {:name "El Guapo"
                       :table-id "x1"}
              "e5-f6" {:name "R2-D2"
                       :table-id "x1"}}
             @players)))))

(deftest remove-estimates-test
  (testing "all players at same table"
    (let [players {"a1-b2" {:name "El Guapo"
                            :table-id "x1"
                            :estimate 3}
                   "e5-f6" {:name "R2-D2"
                            :table-id "x1"
                            :estimate 5}}
          players-without-estimates {"a1-b2" {:name "El Guapo"
                                              :table-id "x1"}
                                     "e5-f6" {:name "R2-D2"
                                              :table-id "x1"}}]
      (is (= players-without-estimates
             (remove-estimates players "x1")))))

  (testing "players at different tables"
    (let [players {"a1-b2" {:name "El Guapo"
                            :table-id "x1"
                            :estimate 3}
                   "e5-f6" {:name "R2-D2"
                            :table-id "y2"
                            :estimate 5}}
          players-after {"a1-b2" {:name "El Guapo"
                                  :table-id "x1"}
                         "e5-f6" {:name "R2-D2"
                                  :table-id "y2"
                                  :estimate 5}}]
      (is (= players-after
             (remove-estimates players "x1"))))))
