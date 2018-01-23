(ns blueprints.schema.property-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]
            [clojure.string :as string]))


(use-fixtures :once dbt/conn-fixture)


(deftest property-schema-conformed

  (test-attr a :property/name
    (is (dbt/value-type a :string))
    (is (dbt/fulltext a)))

  (test-attr a :property/description
    (is (dbt/value-type a :string))
    (is (dbt/fulltext a)))

  (test-attr a :property/code
    (is (dbt/value-type a :string))
    (is (dbt/fulltext a))
    (is (dbt/unique-identity a)))

  (test-attr a :property/address
    (is (dbt/value-type a :ref))
    (is (dbt/component a)))

  (test-attr a :property/units
    (is (dbt/value-type a :ref))
    (is (dbt/cardinality a :many)))

  (test-attr a :property/available-on
    (is (dbt/value-type a :instant))
    (is (dbt/indexed a)))

  (test-attr a :property/ops-fee-rent
    (is (dbt/value-type a :float))
    (is (dbt/indexed a)))

  (test-attr a :property/ops-fee-orders
    (is (dbt/value-type a :float))
    (is (dbt/indexed a)))

  (test-attr a :property/tours
    (is (dbt/value-type a :boolean))
    (is (dbt/indexed a)))

  (test-attr a :property/rent-connect-id
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))

  (test-attr a :property/deposit-connect-id
    (is (dbt/value-type a :string))
    (is (dbt/indexed a)))

  (test-attr a :property/cover-image-url
    (is (dbt/value-type a :string))
    (is (string/starts-with? (:db/doc a) "DEPRECATED"))))
