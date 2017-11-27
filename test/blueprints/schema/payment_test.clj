(ns blueprints.schema.payment-test
  (:require [blueprints.test.datomic :as dbt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once dbt/conn-fixture)


(deftest payment-schema-conformed

  (test-attr a :payment/id
    (is (dbt/value-type a :uuid))
    (is (dbt/unique-identity a)))

  (test-attr a :payment/method
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (dbt/enum-present :payment.method/stripe-charge)
  (dbt/enum-present :payment.method/stripe-invoice)
  (dbt/enum-present :payment.method/check)

  (test-attr a :payment/status
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (dbt/enum-present :payment.status/due)
  (dbt/enum-present :payment.status/canceled)
  (dbt/enum-present :payment.status/paid)
  (dbt/enum-present :payment.status/pending)
  (dbt/enum-present :payment.status/failed)

  (test-attr a :payment/for
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (dbt/enum-present :payment.for/rent)
  (dbt/enum-present :payment.for/deposit)
  (dbt/enum-present :payment.for/order)

  (test-attr a :payment/due
    (is (dbt/value-type a :instant))
    (is (dbt/indexed a)))

  (test-attr a :payment/account
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a)))

  (test-attr a :payment/check
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a))
    (is (dbt/component a)))

  (test-attr a :payment/property
    (is (dbt/value-type a :ref))
    (is (dbt/indexed a))))
