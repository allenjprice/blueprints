(ns starcity-db.core
  (:require [io.rkn.conformity :as c]
            [starcity-db.schema :as schema]
            [starcity-db.migrations :as migrations]
            [datomic.api :as d]))

(def part
  "The name of our partition."
  :db.part/starcity)

(defn conform-schema [conn]
  (c/ensure-conforms conn (schema/partition-norms part))
  (c/ensure-conforms conn (schema/norms part)))

(defn conform-migrations [conn]
  (c/ensure-conforms conn (migrations/norms conn part)))

(defn conform-db [conn]
  (conform-schema conn)
  (conform-migrations conn))
