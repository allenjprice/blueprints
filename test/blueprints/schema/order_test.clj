(ns blueprints.schema.order-test
  (:require [blueprints.test.datomic :as dbt :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest orders-conformed?
  (test-attr a :order/account
    (is (value-type a :ref))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/service
    (is (value-type a :ref))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/quantity
    (is (value-type a :float))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/price
    (is (value-type a :float))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/request
    (is (value-type a :string))
    (is (fulltext a))
    (is (cardinality a :one)))

  (test-attr a :order/status
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr _ :order.status/pending)
  (test-attr _ :order.status/placed)
  (test-attr _ :order.status/fulfilled)
  (test-attr _ :order.status/charged)
  (test-attr _ :order.status/canceled)
  (test-attr _ :order.status/failed)

  (test-attr a :order/billed-on
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :order/fulfilled-on
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :order/projected-fulfillment
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :order/cost
    (is (value-type a :float))
    (is (indexed a)))

  (test-attr a :order/summary
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :order/lines
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (component a))
    (is (indexed a)))

  (test-attr a :line-item/desc
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :line-item/price
    (is (value-type a :float))
    (is (indexed a)))

  (test-attr a :line-item/cost
    (is (value-type a :float))
    (is (indexed a))))
