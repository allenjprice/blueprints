(ns blueprints.schema.sync-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest sync-schema-conformed

  (test-attr a :sync/ref
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :sync/ext-id
    (is (dbt/value-type a :string))
    (is (dbt/indexed a))
    (is (dbt/unique-identity a)))

  (test-attr a :sync/service
    (is (dbt/value-type a :keyword))
    (is (dbt/indexed a)))

  (test-attr a :sync/last-synced
    (is (dbt/value-type a :instant))
    (is (dbt/indexed a))))
