(ns project-estimation.client.runner
  (:require [cljs.test :as test]
            [doo.runner :refer-macros [doo-all-tests doo-tests]]
            [project-estimation.client.estimation-board-test]))

(doo-tests 'project-estimation.client.estimation-board-test)
