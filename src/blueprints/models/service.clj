(ns blueprints.models.service
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [toolbelt.datomic.schema :as tds]))

;; =============================================================================
;; Spec
;; =============================================================================


(s/def ::billed
  #{:service.billed/once :service.billed/monthly})


;; =============================================================================
;; Selectors
;; =============================================================================


(defn code
  "The code used to refer to this service."
  [service]
  (:service/code service))

(s/fdef code
        :args (s/cat :service td/entity?)
        :ret string?)


(defn fields
  [service]
  (:service/fields service))


(def service-name
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


(defn ^{:deprecated "2.3.0"} by-code
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


(defn ^{:deprecated "2.3.0"} ordered-from-catalogue
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


(defn ^{:deprecated "2.3.0"} list-all
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
  [db {:keys [q properties catalogs]}]
  (let [init '{:find  [[?s ...]]
               :in    [$]
               :args  []
               :where []}]
    (cond-> init
      true
      (update :args conj db)

      (not (string/blank? q))
      (-> (update :in conj '?q)
          (update :args (td/safe-wildcard q))
          (update :where conj
                  '(or [(fulltext $ :service/code ?q) [[?s]]]
                       [(fulltext $ :service/name ?q) [[?s]]]
                       [(fulltext $ :service/desc ?q) [[?s]]])))

      (not (empty? properties))
      (-> (update :in conj '[?p ...])
          (update :where conj '[?s :service/properties ?p])
          (update :args conj properties))

      (not (empty? catalogs))
      (-> (update :in conj '[?c ...])
          (update :where conj '[?s :service/catalogs ?c])
          (update :args conj catalogs))

      true
      (update :where #(if (empty? %) (conj % '[?s :service/code _]) %)))))


;; TODO: billed
(defn query
  "Query services using `params`."
  [db params]
  (->> (services-query db params)
       (td/remap-query)
       (d/query)
       (map (partial d/entity db))))


(comment

  (->> (query (d/db user/conn)
              {:q "room  "})
       (map #(select-keys % [:service/code
                             :service/desc])))
  )


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


(defn create-field
  "Create a new field."
  ([label type]
   (create-field label type {}))
  ([label type {:keys [index] :or {index 0}}]
   {:service-field/index index
    :service-field/type  (keyword "service-field.type" (name type))
    :service-field/label label}))


(defn create
  "Create a new service."
  ([code name desc]
   (create code name desc {}))
  ([code name desc {:keys [price rental billed fields catalogs
                           name-internal]
                    :or   {rental        false
                           name-internal name
                           billed        :service.billed/once}}]
   (tb/assoc-when
    {:db/id          (tds/tempid)
     :service/code   code
     :service/name   name
     :service/desc   desc
     :service/billed billed
     :service/rental rental}
    :service/price (when-some [p price] (float p))
    :service/catalogs (when-some [cs catalogs] cs)
    :service/fields (when-some [fs fields]
                      (map-indexed
                       #(assoc %2 :service-field/index %1)
                       fs)))))


;; TODO: Finish this
(defn edit
  [service {:keys [name desc]}]
  (tb/assoc-when
   {:db/id (td/id service)}
   :service/name name
   :service/desc desc))


;; TODO: Add field


(defn remove-field
  "Remove `field` from `service`, keeping index attrs in order."
  [service field]
  (let [fields (->> (fields service)
                    (sort-by :service-field/index)
                    (remove #(= (td/id %) (td/id field))))]
    (conj
     (map-indexed
      (fn [i f]
        [:db/add (td/id f) :service-field/index i])
      fields)
     [:db.fn/retractEntity (td/id field)])))
