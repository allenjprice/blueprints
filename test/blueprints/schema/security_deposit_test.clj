(ns blueprints.schema.security-deposit-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]
            [clojure.string :as string]))


(use-fixtures :once dbt/conn-fixture)


(deftest security-deposit-schema-conformed

  (test-attr a :deposit/account
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :deposit/method
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (dbt/enum-present :deposit.method/ach)
  (dbt/enum-present :deposit.method/check)

  (test-attr a :deposit/type
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (dbt/enum-present :deposit.type/full)
  (dbt/enum-present :deposit.type/partial)

  (test-attr a :deposit/due
    (is (dbt/value-type a :instant))
    (is (dbt/indexed a)))

  (test-attr a :deposit/payments
    (is (dbt/value-type a :ref))
    (is (dbt/component a))
    (is (dbt/cardinality a :many)))

  (test-attr a :deposit/amount
    (is (dbt/value-type a :float))
    (is (dbt/indexed a)))

  (test-attr a :deposit/refund-status
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (dbt/enum-present :deposit.refund-status/initiated)
  (dbt/enum-present :deposit.refund-status/successful)
  (dbt/enum-present :deposit.refund-status/failed)

  ;; deprecations

  (test-attr a :security-deposit/amount-received
    (is (string/starts-with? (:db/doc a) "DEPRECATED")))

  (test-attr a :security-deposit/amount-required
    (is (string/starts-with? (:db/doc a) "DEPRECATED")))

  (test-attr a :security-deposit/check-cleared?
    (is (string/starts-with? (:db/doc a) "DEPRECATED")))

  (test-attr a :security-deposit/charges
    (is (string/starts-with? (:db/doc a) "DEPRECATED")))

  (test-attr a :security-deposit/checks
    (is (string/starts-with? (:db/doc a) "DEPRECATED"))))
