(ns starcity-db.schema.community-safety
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     community-safety
     (s/fields
      [account :ref
       "Account associated with this community safety information."]

      ;; TODO: add `:db.unique/ident`
      [report-url :string
       "API Location of the Community Safety info."]

      [wants-report? :boolean
       "Indicates whether or not this user wants a copy of their report."]))]))

(def ^{:added "1.0.x"} add-consent-given
  (s/generate-schema
   [(s/schema
     community-safety
     (s/fields
      [consent-given? :boolean
       "Has user given us consent to perform a background check?"]))]))

(defn norms [part]
  {:starcity/add-community-safety-schema
   {:txes [schema]}

   :schema/add-community-safety-consent-9-28-16
   {:txes [add-consent-given]}})
