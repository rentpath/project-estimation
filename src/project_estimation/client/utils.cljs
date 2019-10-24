(ns project-estimation.client.utils)

(defn path
  []
  (.. js/window -location -pathname))
