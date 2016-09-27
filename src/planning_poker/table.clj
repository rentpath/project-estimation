(ns planning-poker.table)

(defn player-names
  [players]
  (reduce (fn [accumulator [player-id player-data]]
            (assoc accumulator player-id {:name (:name player-data)}))
            {}
            players))

(defn remove-players!
  [all-players players-to-remove]
  (swap! all-players (fn [collection] (apply dissoc collection players-to-remove))))

(defn add-player!
  [players id player-name]
  (swap! players assoc id {:name player-name}))

(defn estimate!
  [players id estimate]
  (swap! players assoc-in [id :estimate] estimate))

(defn reset-estimates!
  [players]
  (reset! players (player-names @players)))
