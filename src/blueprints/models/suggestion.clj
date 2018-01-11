(ns blueprints.models.suggestion
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Selectors
;; =============================================================================


(defn city
  "The city that was suggested for a new Starcity location."
  [suggestion]
  (:suggestion/city suggestion))

(s/fdef city
        :args (s/cat :suggestion td/entity?)
        :ret (s/or :nothing nil?
                   :string string?))


(defn account
  "The account that provided the suggestion."
  [suggestion]
  (:suggestion/account suggestion))

(s/fdef account
        :args (s/cat :suggestion td/entity?)
        :ret (s/or :nothing nil?
                   :entity td/entityd?))


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a new suggestion."
  [city & [account]]
  (tb/assoc-when
   {:db/id           (d/tempid :db.part/starcity)
    :suggestion/city (-> city string/trim string/lower-case)}
   :suggestion/account (when-let [a account] (td/id a))))

(s/fdef create
        :args (s/cat :city string?
                     :account (s/? td/entity?))
        :ret map?)


(defn create-many
  "Create many suggestions."
  [cities & [account]]
  (mapv #(if (some? account)
           (create % account)
           (create %))
        cities))

(s/fdef create-many
        :args (s/cat :cities (s/spec (s/+ string?))
                     :account (s/? td/entity?))
        :ret vector?)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-city
  [db city]
  (->> (d/q '[:find [?e ...]
              :in $ ?c
              :where
              [?e :suggestion/city ?c]]
            db city)
       (map (partial d/entity db))))

(s/fdef by-city
        :args (s/cat :db td/db? :city string?)
        :ret (s/* td/entityd?))


;; (defn by-account
;;   [db account])

;; (s/fdef by-account
;;         :args (s/cat :db td/db? :account td/entity?)
;;         :ret (s/* td/entityd?))


(defn counts
  "Produce the number of times each city has been suggested."
  [db]
  (->> (d/q '[:find ?e ?c
              :where
              [?e :suggestion/city ?c]]
            db)
       (group-by second)
       (reduce
        (fn [acc [k v]]
          (assoc acc k (count v)))
        {})))

(s/fdef counts
        :args (s/cat :db td/db?)
        :ret (s/map-of string? pos-int?))
