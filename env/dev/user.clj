(ns user
  (:require [mount.core :as mount :refer [defstate]]
            [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
            [datomic.api :as d]
            [blueprints.core :as blueprints]
            [clojure.spec.test.alpha :as stest]))



(def start mount/start)


(def stop mount/stop)


(defn go []
  (start)
  (stest/instrument)
  :ready)


(defn reset []
  (stop)
  (refresh :after 'user/go))


(defn- new-connection [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (blueprints/conform-db conn :db.part/starcity)
    conn))


(defn- disconnect [conn]
  (d/release conn))


(defstate conn
  :start (new-connection "datomic:mem://localhost:4334/starcity")
  :stop (disconnect conn))
