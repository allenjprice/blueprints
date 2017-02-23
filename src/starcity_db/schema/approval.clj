(ns starcity-db.schema.approval
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "< 1.1.3"} schema
  (s/generate-schema
   [(s/schema
     approval
     (s/fields
      [account :ref
       "The account that is being approved."]
      [approved-by :ref
       "Administrator that approved this account."]
      [approved-on :instant
       "Instant at which this approval was made."]
      [property :ref
       "The property that this account is being approved for."]))]))

(def ^{:added "1.3.0"} add-approval-attrs
  (s/generate-schema
   [(s/schema
     approval
     (s/fields
      [unit :ref :index
       "The unit that this account has been approved for."]
      [move-in :instant :index
       "The move-in date that this account has been approved for."]
      [license :ref :index
       "The license that this account has been approved for."]
      [status :ref :index
       "The status of the approval."]))]))

(defn- ^{:added "1.3.0"} add-approval-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :approval.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :approval.status/canceled}
   {:db/id    (d/tempid part)
    :db/ident :approval.status/approved}])

(def ^{:added "1.3.0"} improve-approval-attrs
  [{:db/id               :approval/approved-on
    :db/doc "DEPRECATED 02/13/17 in favor of innate Datomic superpowers."
    :db.alter/_attribute :db.part/db}
   {:db/id               :approval/approved-by
    :db/ident            :approval/approver
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :approval/account
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id  :approval/property
    :db/doc "DEPRECATED 02/13/17 in favor of `:approval/unit`."}])

(defn norms [part]
  {:schema/add-approval-schema-9-8-16
   {:txes [schema]}

   :schema.approval/improvements-02-17-16
   {:txes [add-approval-attrs
           (add-approval-statuses part)
           improve-approval-attrs]
    :requires [:schema/add-approval-schema-9-8-16]}})
