(ns planning-poker.game-table)

(defn remove-estimates
  [players table-id]
  (reduce-kv (fn [acc id data]
               (if (= table-id (:table-id data))
                 (assoc acc id (dissoc data :estimate))
                 (assoc acc id data)))
             {}
             players))

(defn remove-players!
  [all-players players-to-remove]
  (swap! all-players (fn [collection] (apply dissoc collection players-to-remove))))

(defn add-player!
  [players id data]
  (swap! players assoc id data))

(defn estimate!
  [players id estimate]
  (swap! players assoc-in [id :estimate] estimate))

(defn reset-estimates!
  [players table-id]
  (reset! players (remove-estimates @players table-id)))
