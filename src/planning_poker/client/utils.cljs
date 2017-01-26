(ns planning-poker.client.utils)

(defn path
  []
  (.. js/window -location -pathname))
