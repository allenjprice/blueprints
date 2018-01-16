(ns blueprints.models.transaction-test
  (:require [blueprints.models.transaction :as transaction]
            [blueprints.test.datomic :as dbt :refer [with-conn]]
            [clojure.test :refer :all]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]))


(defn create-transaction [id source-id]
  {:transaction/id id
   :transaction/source-id source-id})


(use-fixtures :once dbt/conn-fixture)


(defn speculate [db & tx-data]
  (:db-after (d/with db (apply concat (map #(if (map? %) [%] %) tx-data)))))


(deftest selectors

  (with-conn conn

    (let [db (speculate (d/db conn)
                        (transaction/create "txn_19XJJ02eZvKYlo2ClwuJ1rbA" "ch_19XJJ02eZvKYlo2CHfSUsSpl"))
          transaction (transaction/by-id db "txn_19XJJ02eZvKYlo2ClwuJ1rbA")]
      (is (= "txn_19XJJ02eZvKYlo2ClwuJ1rbA" (transaction/id transaction)))
      (is (= "ch_19XJJ02eZvKYlo2CHfSUsSpl" (transaction/source-id transaction)))
      (is (= nil (transaction/payment transaction)))
      (is (= nil (transaction/payout-id transaction))))

    ))
