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

      [key :keyword :index
       "App-defined keyword to dispatch upon."]

      [params :bytes
       "Serialized parameters for the cmd handler."]

      [meta :bytes
       "Serialized metadata about the cmd."]

      [status :ref :index
       "pending, successful, failed"]))]))

(defn ^{:added "1.3.0"} cmd-statuses [part]
  [{:db/id (d/tempid part)
    :db/ident :cmd.status/pending}
   {:db/id (d/tempid part)
    :db/ident :cmd.status/successful}
   {:db/id (d/tempid part)
    :db/ident :cmd.status/failed}])

(defn norms [part]
  {:schema.cmd/add-cmd-schema
   {:txes [schema (cmd-statuses part)]}})
