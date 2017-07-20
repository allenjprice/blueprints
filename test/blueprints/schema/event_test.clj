(ns blueprints.schema.event-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr with-conn]]
            [clojure.test :refer :all]
            [datomic.api :as d]))


(use-fixtures :once dbt/conn-fixture)


(deftest schema-conformed?
  (test-attr a :event/uuid
    (is (dbt/value-type a :uuid))
    (is (dbt/unique-identity a)))

  (test-attr a :event/id
    (is (dbt/value-type a :string))
    (is (dbt/unique-identity a)))

  (test-attr a :event/key
    (is (dbt/value-type a :keyword))
    (is (dbt/indexed a)))

  (test-attr a :event/topic
    (is (dbt/value-type a :keyword))
    (is (dbt/indexed a)))

  (test-attr a :event/params
    (is (dbt/value-type a :string)))

  (test-attr a :event/meta
    (is (dbt/value-type a :string)))

  (test-attr a :event/status
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :event/triggered-by
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr _ :event.status/pending)
  (test-attr _ :event.status/successful)
  (test-attr _ :event.status/failed))
