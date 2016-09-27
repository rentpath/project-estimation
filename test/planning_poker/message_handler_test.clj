(ns planning-poker.message-handler-test
  (:require [clojure.test :refer :all]
            [planning-poker.message-handler :refer :all]))

(deftest user-id-test
  (testing "returns the correct player ID from a request"
    (is (= "abcd"
           (user-id {:cookies {"ring-session" {:value "abcd"}}})))))
