(ns blueprints.models.license
  (:require [datomic.api :as d]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================


(defn term
  "The term of this license."
  [license]
  (:license/term license))

(s/fdef term
        :args (s/cat :license p/entity?)
        :ret integer?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn available
  "All available licenses."
  [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :license/term _]
              (or [?e :license/available true]
                  [(missing? $ ?e :license/available)])]
            db)
       (map (partial d/entity db))))

(s/fdef available
        :args (s/cat :db p/db?)
        :ret (s/* p/entity?))


(defn by-term
  "Find a license by `term`."
  [db term]
  (->> (d/q '[:find ?e .
              :in $ ?t
              :where
              [?e :license/term ?t]]
            db term)
       (d/entity db)))

(s/fdef by-term
        :args (s/cat :db p/db? :term integer?)
        :ret p/entity?)
