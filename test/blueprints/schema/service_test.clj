(ns blueprints.schema.service-test
  (:require [blueprints.test.datomic :as dbt :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest services-conformed?
  (test-attr a :service/code
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/name
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/desc
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/desc-internal
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/price
    (is (value-type a :float))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :service/rental
    (is (value-type a :boolean))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :service/properties
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a)))

  (test-attr a :service/billed
    (is (value-type a :ref))
    (is (cardinality a :one))
    (is (indexed a)))

  (test-attr a :service/fields
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (component a))
    (is (indexed a)))

  (test-attr _ :service.billed/once)
  (test-attr _ :service.billed/monthly))
