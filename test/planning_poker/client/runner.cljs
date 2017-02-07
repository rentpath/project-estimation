(ns planning-poker.client.runner
  (:require [cljs.test :as test]
            [doo.runner :refer-macros [doo-all-tests doo-tests]]
            [planning-poker.client.game-table-test]))

(doo-tests 'planning-poker.client.game-table-test)
