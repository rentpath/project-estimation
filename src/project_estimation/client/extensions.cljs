(ns project-estimation.client.extensions)

(extend-type js/HTMLCollection
  ISeqable
  (-seq [array] (array-seq array 0)))

(extend-type js/NodeList
  ISeqable
  (-seq [array] (array-seq array 0)))
