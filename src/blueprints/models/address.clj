(ns blueprints.models.address
  (:require [clojure.spec.alpha :as s]
            [toolbelt.datomic :as td]
            [datomic.api :as d]))


;; =============================================================================
;; Selectors
;; =============================================================================


(defn country
  [address]
  (:address/country address))

(s/fdef country
        :args (s/cat :address td/entity?)
        :ret (s/nilable string?))


(defn locality
  "Locality, i.e. municipality/city."
  [address]
  (:address/locality address))

(s/fdef locality
        :args (s/cat :address td/entity?)
        :ret (s/nilable string?))


(defn region
  "Region, i.e. state/province."
  [address]
  (:address/region address))

(s/fdef region
        :args (s/cat :address td/entity?)
        :ret (s/nilable string?))


(defn postal-code
  "Postal code, i.e. zip code."
  [address]
  (:address/postal-code address))

(s/fdef postal-code
        :args (s/cat :address td/entity?)
        :ret (s/nilable string?))


;; convenience ==========================

(def city locality)
(def state region)
(def zip postal-code)


;; ==============================================================================
;; transactions =================================================================
;; ==============================================================================


(defn create [lines locality region country postal-code]
  {:db/id               (d/tempid :db.part/starcity)
   :address/lines       lines
   :address/locality    locality
   :address/region      region
   :address/country     country
   :address/postal-code postal-code})


(comment

  (def address-tx [(create "414 Bryant St" "San Francisco" "CA" "USA" "94107")])

  )
