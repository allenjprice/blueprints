(ns blueprints.schema.note-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest note-schema-conformed

  (test-attr a :note/uuid
    (is (dbt/value-type a :uuid))
    (is (dbt/unique-identity a)))

  (test-attr a :note/author
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :note/subject
    (is (dbt/value-type a :string))
    (is (dbt/fulltext a)))

  (test-attr a :note/content
    (is (dbt/value-type a :string))
    (is (dbt/fulltext a)))

  (test-attr a :note/children
    (is (dbt/value-type a :ref))
    (is (dbt/cardinality a :many))
    (is (dbt/indexed a))
    (is (dbt/component a)))

  (test-attr a :note/tags
    (is (dbt/value-type a :ref))
    (is (dbt/cardinality a :many))
    (is (dbt/indexed a)))

  (test-attr a :note/ref
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :ticket/status
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :ticket/assigned-to
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a))))
