(ns blueprints.schema.security-deposit-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]
            [clojure.string :as string]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest security-deposit-schema-conformed

  (test-attr a :deposit/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :deposit/method
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (tdt/enum-present :deposit.method/ach)
  (tdt/enum-present :deposit.method/check)

  (test-attr a :deposit/type
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (tdt/enum-present :deposit.type/full)
  (tdt/enum-present :deposit.type/partial)

  (test-attr a :deposit/due
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :deposit/payments
    (is (tdt/value-type a :ref))
    (is (tdt/component a))
    (is (tdt/cardinality a :many)))

  (test-attr a :deposit/amount
    (is (tdt/value-type a :float))
    (is (tdt/indexed a)))

  (test-attr a :deposit/refund-status
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (tdt/enum-present :deposit.refund-status/initiated)
  (tdt/enum-present :deposit.refund-status/successful)
  (tdt/enum-present :deposit.refund-status/failed)

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
