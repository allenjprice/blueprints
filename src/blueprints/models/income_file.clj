(ns blueprints.models.income-file
  (:require [datomic.api :as d]))

;; =============================================================================
;; Transactions
;; =============================================================================

(defn create
  "Produce transaction data to create a new income file."
  [account content-type path size]
  {:income-file/account      (:db/id account)
   :income-file/content-type content-type
   :income-file/path         path
   :income-file/size         (long size)})

;; =============================================================================
;; Queries
;; =============================================================================

(defn by-account
  "Fetch the income files for this account."
  [db account]
  (->> (d/q '[:find [?e ...]
              :in $ ?a
              :where
              [?e :income-file/account ?a]]
            db (:db/id account))
       (map (partial d/entity db))))
