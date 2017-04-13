(ns blueprints.core
  (:require [io.rkn.conformity :as c]
            [blueprints.schema :as schema]
            [blueprints.migrations :as migrations]
            [datomic.api :as d]))

(def default-partition :db.part/user)

(defn conform-schema [conn & [part]]
  (schema/conform conn (or part default-partition)))

(defn conform-migrations [conn & [part]]
  (let [part (or part default-partition)]
    (c/ensure-conforms conn (migrations/norms conn part))))
