(ns blueprints.seed.norms.licenses
  (:require [datomic.api :as d]))

(defn add-initial-licenses [part]
  [{:db/id (d/tempid part) :license/term 1 :license/available false}
   {:db/id (d/tempid part) :license/term 3}
   {:db/id (d/tempid part) :license/term 6 :license/available true}
   {:db/id (d/tempid part) :license/term 12}])

(defn by-term [db term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       db term))

(defn licenses-present?
  "Are the licenses to be seeded already present in the database? This is needed
  because the production licenses arrived in the db prior to use of
  conformity."
  [conn]
  (let [db (d/db conn)]
    (and (by-term db 1) (by-term db 3) (by-term db 6) (by-term db 12))))

(defn norms
  "The norms to conform licenses."
  [conn part]
  (when-not (licenses-present? conn)
    {:blueprints.seed/add-initial-licenses
    {:txes [(add-initial-licenses part)]}}))
