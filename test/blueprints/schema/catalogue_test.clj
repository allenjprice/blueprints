(ns blueprints.schema.catalogue-test
  (:require [blueprints.test.datomic :as dbt :refer :all]
            [clojure.test :refer :all]))

(use-fixtures :once dbt/conn-fixture)

(deftest catalogue-conformed?
  (test-attr a :catalogue/name
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :catalogue/code
    (is (value-type a :keyword))
    (is (indexed a)))

  (test-attr a :catalogue/properties
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a)))

  (test-attr a :catalogue/items
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a))
    (is (component a)))

  (test-attr a :cat-item/index
    (is (value-type a :long))
    (is (indexed a)))

  (test-attr a :cat-item/service
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :cat-item/name
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :cat-item/desc
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :cat-item/fields
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (component a)))

  (test-attr a :cat-item/properties
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a)))

  (test-attr a :cat-field/label
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :cat-field/type
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :cat-field/key
    (is (value-type a :keyword))
    (is (indexed a)))

  (test-attr _ :cat-field.type/desc)
  (test-attr _ :cat-field.type/quantity)

  (test-attr a :cat-field/min
    (is (value-type a :long)))

  (test-attr a :cat-field/max
    (is (value-type a :long)))

  (test-attr a :cat-field/step
    (is (value-type a :float))))
