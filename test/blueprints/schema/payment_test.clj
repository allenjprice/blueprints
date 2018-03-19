(ns blueprints.schema.payment-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest payment-schema-conformed

  (test-attr a :payment/id
    (is (tdt/value-type a :uuid))
    (is (tdt/unique-identity a)))

  (test-attr a :payment/method
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (tdt/enum-present :payment.method/stripe-charge)
  (tdt/enum-present :payment.method/stripe-invoice)
  (tdt/enum-present :payment.method/check)

  (test-attr a :payment/status
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (tdt/enum-present :payment.status/due)
  (tdt/enum-present :payment.status/canceled)
  (tdt/enum-present :payment.status/paid)
  (tdt/enum-present :payment.status/pending)
  (tdt/enum-present :payment.status/failed)

  (test-attr a :payment/for
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (tdt/enum-present :payment.for/rent)
  (tdt/enum-present :payment.for/deposit)
  (tdt/enum-present :payment.for/order)

  (test-attr a :payment/due
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :payment/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :payment/check
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a))
    (is (tdt/component a)))

  (test-attr a :payment/property
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a))))
