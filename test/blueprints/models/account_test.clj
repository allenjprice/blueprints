(ns blueprints.models.account-test
  (:require [blueprints.models.account :as account]
            [toolbelt.datomic.test :as tdt :refer [with-conn]]
            [clojure.test :refer :all]
            [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.core :as tb]))


(defn create-account [email & {:keys [license-id phone-number]}]
  (tb/assoc-when
   {:account/email email}
   :account/licenses license-id
   :account/phone-number phone-number))


(defn member-license [status unit-id]
  {:db/id                 (d/tempid :db.part/user)
   :member-license/unit   unit-id
   :member-license/status status})


(defn account-with-license [email]
  (let [unit-id (d/tempid :db.part/user)
        license (member-license :member-license.status/active unit-id)]
    [{:db/id     unit-id
      :unit/name "52gilbert-1"}
     {:property/name  "52gilbert"
      :property/units unit-id}
     (create-account email :license-id (:db/id license))
     license]))


(defn account-with-inactive-license [email]
  (let [unit-id (d/tempid :db.part/user)
        license (member-license :member-license.status/inactive unit-id)]
    [{:db/id     unit-id
      :unit/name "52gilbert-1"}
     {:property/name  "52gilbert"
      :property/units unit-id}
     (create-account email :license-id (:db/id license))
     license]))


(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(defn speculate [db & tx-data]
  (:db-after (d/with db (apply concat (map #(if (map? %) [%] %) tx-data)))))


(deftest selectors

  (with-conn conn

    (let [db      (speculate (d/db conn) (create-account "test@test.com"
                                                         :phone-number "5308946131"))
          account (d/entity db [:account/email "test@test.com"])]

      (is (= "test@test.com" (account/email account)) "accounts have emails")
      (is (= (:account/email account) (account/email account)) "email selector produces email attribute")
      (is (nil? (account/email (account/by-email db "test2@test.com")))
          "produces nil for nil input")

      (is (= "5308946131" (account/phone-number account)) "accounts have phone numbers"))


    (testing "can get current property for a member"
      (let [db (speculate (d/db conn)
                          (account-with-license "active@test.com")
                          (account-with-inactive-license "inactive@test.com"))]

        ;; TODO:
        ;; (is (td/entityd? (account/current-property db (account/by-email db "active@test.com")))
        ;;     "produces an entity")
        (is (nil? (account/current-property db (account/by-email db "inactive@test.com"))))))


    ))
