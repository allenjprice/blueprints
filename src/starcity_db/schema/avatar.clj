(ns starcity-db.schema.avatar
  (:require [datomic-schema.schema :as s]))

;; NOTE: Could go a lot further with this. The *best* approach would be likely
;; to have:
;; `avatar` -> `image` -> `dimensions`
;; Then we have a flexible `image` entity, that has `dimensions` that can be
;; queried. Don't think we need that yet though.

(def ^{:added "1.2.0"} schema
  (s/generate-schema
   [(s/schema
     avatar
     (s/fields
      [name :keyword :unique-identity
       "The unique identity of this avatar. Internal convenience."]

      [url :string :index
       "The url of the image resource."]

      [account :ref :index
       "The account that this avatar belongs to."]))]))

(def norms
  {:schema.avatar/add-avatar-schema
   {:txes [schema]}})
