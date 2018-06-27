(ns blueprints.models.region
  (:require [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.datomic :as td]))



;; ==============================================================================
;; Selectors ====================================================================
;; ==============================================================================


(defn name
  "The name of the region."
  [region]
  (:region/name region))

(s/fdef name
        :args (s/cat :region td/entity?)
        :ret string?)


(defn tipe-document-id
  "The document-id for the Tipe document associated with this region."
  [region]
  (:tipe/document-id region))

(s/fdef tipe-document-id
        :args (s/cat :region td/entity?)
        :ret (s/nilable string?))


(defn communities
  "A list of communities in this region."
  [region]
  (:property/_region region))

(s/fdef communities
        :args  (s/cat :region td/entity?)
        :ret (s/* td/entityd?))
