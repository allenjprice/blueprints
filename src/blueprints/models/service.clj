(ns blueprints.models.service
  (:refer-clojure :exclude [name])
  (:require[clojure.spec.alpha :as s]
           [datomic.api :as d]
           [toolbelt.core :as tb]
           [toolbelt.datomic :as td]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def ::billed
  #{:service.billed/once :service.billed/monthly})


;; =============================================================================
;; Selectors
;; =============================================================================


(def code
  "The code used to refer to this service."
  :service/code)

(s/fdef code
        :args (s/cat :service td/entity?)
        :ret string?)


(def name
  "The human-friendly name of this service."
  :service/name)

(s/fdef name
        :args (s/cat :service td/entity?)
        :ret string?)


(def desc
  "The human-friendly description of this service."
  :service/desc)

(s/fdef desc
        :args (s/cat :service td/entity?)
        :ret string?)


(def price
  "The price of this service."
  :service/price)

(s/fdef price
        :args (s/cat :service td/entity?)
        :ret (s/or :nothing nil? :price float?))


(def cost
  "The cost of this service."
  :service/cost)

(s/fdef cost
        :args (s/cat :service td/entity?)
        :ret (s/or :nothing nil? :cost float?))


(defn rental
  "Is this service a rental?"
  [service]
  (get service :service/rental false))

(s/fdef rental
        :args (s/cat :service td/entity?)
        :ret boolean?)


(def billed
  "Billing method of this service."
  :service/billed)

(s/fdef billed
        :args (s/cat :service td/entity?)
        :ret ::billed)


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
        :args (s/cat :db td/db?
                     :code string?
                     :property (s/? td/entity?))
        :ret (s/or :nothing nil? :service td/entity?))


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
        :args (s/cat :db td/db?
                     :account td/entity?
                     :catalogue td/entity?)
        :ret (s/* integer?))


(defn list-all
  "List all services in a human-readable way."
  [db]
  (let [svcs (->> (d/q '[:find [?e ...]
                         :where
                         [?e :service/code _]]
                       db)
                  (map (partial d/entity db)))]
    (println "CODE :: NAME :: DESCRIPTION")
    (doseq [svc (sort-by :service/code svcs)]
      (println (format "%s: '%s' (%s)" (code svc) (name svc) (desc svc))))))


(defn- services-query
  [db {:keys [q]}]
  (let [init '{:find  [[?s ...]]
               :in    [$]
               :args  []
               :where []}]
    (cond-> init
      true
      (update :args conj db)

      (some? q)
      (-> (update :in conj '?q)
          (update :args conj (str q "*"))
          (update :where conj
                  '(or [(fulltext $ :service/code ?q) [[?s]]]
                       [(fulltext $ :service/name ?q) [[?s]]]
                       [(fulltext $ :service/desc ?q) [[?s]]])))

      true
      (update :where #(if (empty? %) (conj % '[?s :service/code _]) %)))))


(defn query
  "Query services using `params`."
  [db & {:as params}]
  (->> (services-query db params)
       (td/remap-query)
       (d/query)
       (map (partial d/entity db))))


;; =============================================================================
;; Lookups


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


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Create a new service."
  [code name desc billed & {:keys [price rental]
                            :or   {rental false}}]
  (tb/assoc-when
   {:db/id          (d/tempid :db.part/starcity)
    :service/code   code
    :service/name   name
    :service/desc   desc
    :service/billed billed
    :service/rental rental}
   :service/price (float price)))

(s/def ::price (s/and number? pos?))
(s/def ::rental boolean?)
(s/fdef create
        :args (s/cat :code string?
                     :name string?
                     :desc string?
                     :billed ::billed
                     :opts (s/keys* :opt-un [::price ::rental]))
        :ret map?)
