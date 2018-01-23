(ns blueprints.schema.transaction-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest transaction-schema-conformed

  (test-attr a :transaction/id
             (is (dbt/value-type a :string))
             (is (dbt/unique-identity a)))

  (test-attr a :transaction/payment
             (is (dbt/value-type a :ref))
             (is (dbt/indexed a)))

  (test-attr a :transaction/payout-id
             (is (dbt/value-type a :string))
             (is (dbt/unique-identity a)))

  (test-attr a :transaction/source-id
             (is (dbt/value-type a :string))
             (is (dbt/unique-identity a))))
