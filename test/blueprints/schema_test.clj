(ns blueprints.schema-test
  (:require [blueprints.schema :as schema]
            [blueprints.test.datomic :as db :refer :all]
            [clojure.test :refer :all]
            [datomic.api :as d]))

(use-fixtures :once db/conn-fixture)

;; =============================================================================
;; account
;; =============================================================================

(deftest accounts-conformed?
  (test-attr a :account/email
    (is (created a))
    (is (unique-identity a))))

;; =============================================================================
;; catalogue
;; =============================================================================

(deftest catalogue-conformed?
  (test-attr a :catalogue/name
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :catalogue/code
    (is (value-type a :keyword))
    (is (indexed a)))

  (test-attr a :catalogue/properties
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a)))

  (test-attr a :catalogue/items
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a))
    (is (component a)))

  (test-attr a :cat-item/index
    (is (value-type a :long))
    (is (indexed a)))

  (test-attr a :cat-item/service
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :cat-item/name
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :cat-item/desc
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :cat-item/fields
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (component a)))

  (test-attr a :cat-field/label
    (is (value-type a :string))
    (is (indexed a)))

  (test-attr a :cat-field/type
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :cat-field/key
    (is (value-type a :keyword))
    (is (indexed a)))

  (test-attr _ :cat-field.type/desc)
  (test-attr _ :cat-field.type/quantity)

  (test-attr a :cat-field/min
    (is (value-type a :long)))

  (test-attr a :cat-field/max
    (is (value-type a :long)))

  (test-attr a :cat-field/step
    (is (value-type a :float))))


;; =============================================================================
;; onboard
;; =============================================================================

(deftest onboard-conformed?
  (test-attr a :onboard/account
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :onboard/move-in
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :onboard/seen
    (is (value-type a :keyword))
    (is (cardinality a :many))
    (is (indexed a))))

;; =============================================================================
;; service & order
;; =============================================================================

(deftest services-conformed?
  (test-attr a :service/code
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/name
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/desc
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/desc-internal
    (is (value-type a :string))
    (is (fulltext a)))

  (test-attr a :service/price
    (is (value-type a :float))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :service/rental
    (is (value-type a :boolean))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :service/properties
    (is (value-type a :ref))
    (is (cardinality a :many))
    (is (indexed a)))

  (test-attr a :service/billed
    (is (value-type a :ref))
    (is (cardinality a :one))
    (is (indexed a)))

  (test-attr _ :service.billed/once)
  (test-attr _ :service.billed/monthly))

(deftest orders-conformed?
  (test-attr a :order/account
    (is (value-type a :ref))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/service
    (is (value-type a :ref))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/quantity
    (is (value-type a :float))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/price
    (is (value-type a :float))
    (is (indexed a))
    (is (cardinality a :one)))

  (test-attr a :order/desc
    (is (value-type a :string))
    (is (fulltext a))
    (is (cardinality a :one)))

  (test-attr a :order/ordered
    (is (value-type a :instant))
    (is (indexed a))))

;; =============================================================================
;; stripe
;; =============================================================================

(deftest stripe-conformed?
  (test-attr a :stripe/plan-id
    (is (value-type a :string))
    (is (unique-identity a)))

  (test-attr a :stripe/subs-id
    (is (value-type a :string))
    (is (unique-identity a)))

  (test-attr a :stripe/charge
    (is (value-type a :ref))
    (is (indexed a))))
