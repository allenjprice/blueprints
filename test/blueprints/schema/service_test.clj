(ns blueprints.schema.service-test
  (:require [toolbelt.datomic.test :as tdt :refer :all]
            [clojure.test :refer :all]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


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


(deftest service-fields-conformed?
  (test-attr a :service-field/type
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :service-field/label
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :service-field.time/range-start
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :service-field.time/range-end
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :service-field.time/interval
    (is (value-type a :long))
    (is (indexed a)))

  (test-attr _ :service-field.type/time)
  (test-attr _ :service-field.type/date)
  (test-attr _ :service-field.type/text)
  (test-attr _ :service-field.type/number))
