(ns blueprints.schema.account-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]))

(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest account-schema-conformed

  (test-attr a :account/first-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/first-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))


  (test-attr a :account/middle-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/middle-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :account/last-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/last-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :account/phone-number
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/phone-number
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :account/email
    (is (tdt/unique-identity a))
    (is (tdt/value-type a :string)))

  (test-attr a :account/emergency-contact
    (is (tdt/value-type a :ref))
    (is (tdt/component a))))
