(ns blueprints.models.license
  (:require [datomic.api :as d]))

;; =============================================================================
;; Selectors
;; =============================================================================

(def term :license/term)

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

(defn by-term
  "Find a license by `term`."
  [db term]
  (->> (d/q '[:find ?e .
              :in $ ?t
              :where
              [?e :license/term ?t]]
            db term)
       (d/entity db)))
