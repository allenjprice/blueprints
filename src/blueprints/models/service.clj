(ns blueprints.models.service
  (:refer-clojure :exclude [name])
  (:require [datomic.api :as d]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]))


;; =============================================================================
;; Selectors
;; =============================================================================


(def code
  "The code used to refer to this service."
  :service/code)

(s/fdef code
        :args (s/cat :service p/entity?)
        :ret string?)


(def name
  "The human-friendly name of this service."
  :service/name)

(s/fdef name
        :args (s/cat :service p/entity?)
        :ret string?)


(def desc
  "The human-friendly description of this service."
  :service/desc)

(s/fdef desc
        :args (s/cat :service p/entity?)
        :ret string?)


(def price
  "The price fo this service."
  :service/price)

(s/fdef price
        :args (s/cat :service p/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn rental
  "Is this service a rental?"
  [service]
  (get service :service/rental false))

(s/fdef rental
        :args (s/cat :service p/entity?)
        :ret boolean?)


(def billed
  "Billing method of this service."
  :service/billed)

(s/fdef billed
        :args (s/cat :service p/entity?)
        :ret #{:service.billed/once :service.billed/monthly})


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-code
  "Find the service identified by `code`, optionally restricted to `property`."
  ([db code]
   (->> (d/q '[:find ?e .
               :in $ ?c
               :where
               [?e :service/code ?c]]
             db code)
        (d/entity db)))
  ([db code property]
   (->> (d/q '[:find ?e .
               :in $ ?c ?p
               :where
               [?e :service/code ?c]
               [?e :service/properties ?p]]
             db code (:db/id property))
        (d/entity db))))

(s/fdef by-code
        :args (s/cat :db p/db?
                     :code string?
                     :property (s/? p/entity?))
        :ret (s/or :nothing nil? :service p/entity?))


(defn ordered-from-catalogue
  "Produce a list of service ids that have been ordered by `account` from
  `catalogue`."
  [db account catalogue]
  (d/q '[:find [?s ...]
         :in $ ?a ?ca
         :where
         [?o :order/account ?a]
         [?o :order/service ?s]
         [?ca :catalogue/items ?ci]
         [?ci :cat-item/service ?s]]
       db (:db/id account) (:db/id catalogue)))

(s/fdef ordered-from-catalogue
        :args (s/cat :db p/db?
                     :account p/entity?
                     :catalogue p/entity?)
        :ret (s/* integer?))


;; =============================================================================
;; Services
;; =============================================================================


(defn moving-assistance [db]
  (by-code db "moving,move-in"))


(defn small-bin [db property]
  (by-code db "storage,bin,small" property))


(defn large-bin [db property]
  (by-code db "storage,bin,large" property))


(defn misc-storage [db]
  (by-code db "storage,misc"))


(defn customize-furniture [db]
  (by-code db "customize,furniture,quote"))


(defn customize-room [db]
  (by-code db "customize,room,quote"))


(defn weekly-cleaning [db]
  (by-code db "cleaning,weekly"))
