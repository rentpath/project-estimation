(ns project-estimation.estimation-board)

(defn remove-estimates
  [participants board-id]
  (reduce-kv (fn [acc id data]
               (if (= board-id (:board-id data))
                 (assoc acc id (dissoc data :estimate))
                 (assoc acc id data)))
             {}
             participants))

(defn fill-estimates
  [participants board-id]
  (reduce-kv (fn [acc id data]
               (if (= board-id (:board-id data))
                 (assoc acc id (merge {:estimate "?"} data))
                 (assoc acc id data)))
             {}
             participants))

(defn remove-participants!
  [all-participants participants-to-remove]
  (swap! all-participants (fn [collection] (apply dissoc collection participants-to-remove))))

(defn add-participant!
  [participants id data]
  (swap! participants assoc id data))

(defn estimate!
  [participants id estimate]
  (swap! participants assoc-in [id :estimate] estimate))

(defn reset-estimates!
  [participants board-id]
  (reset! participants (remove-estimates @participants board-id)))

(defn force-estimates!
  [participants board-id]
  (reset! participants (fill-estimates @participants board-id)))
