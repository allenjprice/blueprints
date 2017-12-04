(ns blueprints.schema.stripe-customer-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.string :as string]
            [clojure.test :refer :all]))

(use-fixtures :once dbt/conn-fixture)


(deftest stripe-customer-schema-conformed

  (test-attr a :customer/platform-id
    (is (dbt/value-type a :string))
    (is (dbt/unique-identity a)))

  (test-attr a :customer/account
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :customer/connected
    (is (dbt/value-type a :ref))
    (is (dbt/cardinality a :many))
    (is (dbt/component a))
    (is (dbt/indexed a)))

  (test-attr a :connected-customer/customer-id
    (is (dbt/value-type a :string))
    (is (dbt/unique-identity a)))

  (test-attr a :connected-customer/property
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :stripe-customer/bank-account-token
    (is (string/starts-with? (:db/doc a) "DEPRECATED")))

  (test-attr a :stripe-customer/managed
    (is (string/starts-with? (:db/doc a) "DEPRECATED"))))
