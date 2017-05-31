(ns blueprints.schema.cmd
  "Cmds are used to express state changes to the system that... TODO"
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))

(def ^{:added "1.3.0"} schema
  (s/generate-schema
   [(s/schema
     cmd
     (s/fields
      [uuid :uuid :unique-identity
       "SQUUID to identify this cmd."]

      [id :string :unique-identity
       "An external (to the system) identifier for this cmd."]

      [key :keyword
       "App-defined keyword to dispatch upon."]

      [params :bytes
       "Serialized parameters for the cmd handler."]

      [meta :bytes
       "Serialized metadata about the cmd."]

      [status :ref
       "pending, successful, failed"]))]))

(defn ^{:added "1.3.0"} cmd-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :cmd.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :cmd.status/successful}
   {:db/id    (d/tempid part)
    :db/ident :cmd.status/failed}])

(def ^{:added "1.6.0"} add-data-and-ctx-attrs
  "The choice to use `:bytes` as the `:db.valueType` for `:cmd/params` and
  `:cmd/meta` has proven to be a poor one. Reasons:

  1. Requires an external library to use (nippy).
  2. Params aren't human-readable without library.
  3. (1) and (2) together make the use of database functions difficult.

  This schema change adds two more attributes that use strings as the
  serialization format, thus solving above issues."
  (concat
   (s/generate-schema
    [(s/schema
      cmd
      (s/fields
       [data :string
        "The serialized data for the command handler."]
       [ctx :string
        "Serialized contextual data (metadata) for the command handler."]))])
   [{:db/id  :cmd/params
     :db/doc "DEPRECATED 1.6.0: Prefer `:cmd/data`."}
    {:db/id  :cmd/meta
     :db/doc "DEPRECATED 1.6.0: Prefer `:cmd/ctx`."}]))

(def ^{:added "1.6.0"} index-schema-attrs
  [{:db/id               :cmd/key
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :cmd/status
    :db/index            true
    :db.alter/_attribute :db.part/db}])

(defn- ^{:added "1.6.0"} add-create-cmd [part]
  [{:db/id    (d/tempid part)
    :db/ident :db.cmd/create
    :db/doc   "Create a new cmd."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db key opts]
      :requires '[[datomic.api :as d]]
      :code     '(let [{:keys [id data ctx]} opts]
                   [(merge
                     {:db/id      (d/tempid :db.part/starcity)
                      :cmd/uuid   (d/squuid)
                      :cmd/key    key
                      :cmd/status :cmd.status/pending}
                     (when (some? id) {:cmd/id id})
                     (when (some? data) {:cmd/data (pr-str data)})
                     (when (some? ctx) {:cmd/ctx (pr-str ctx)}))])})}])

(defn norms [part]
  {:schema.cmd/add-cmd-schema
   {:txes [schema (cmd-statuses part)]}
   :schema.cmd/improvements-05252017
   {:txes     [index-schema-attrs
               add-data-and-ctx-attrs]
    :requires [:schema.cmd/add-cmd-schema]}
   :schema.cmd/add-create-cmd-05252017
   {:txes [(add-create-cmd part)]}})
