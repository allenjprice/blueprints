(ns blueprints.seed.accounts
  (:require [blueprints.models.approval :as approval]
            [blueprints.models.check :as check]
            [blueprints.seed.utils :as utils :refer [tempid]]
            [clojure.string :as string]
            [clj-time.core :as t]))

;; =============================================================================
;; Helpers
;; =============================================================================


;; =============================================================================
;; Account
;; =============================================================================


(def ^:private password
  "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243")


(defn- rand-first-name []
  (-> "Noah Liam William Mason James Benjamin Jacob Michael Elija Ethan Emma Olivia Ava Sophia Isabella Mia Charlotte Abigail Emily Harper"
      (string/split #" ")
      (rand-nth)))


(defn- rand-last-name []
  (-> "Smith Jones Taylor Williams Brown Davies Evans Wilson Thomas Roberts Johnson Lewis Walker Robinson Wood Thompson White Watson Jackson Wright"
      (string/split #" ")
      (rand-nth)))


(defn- rand-phone []
  (let [start (rand-nth (range 2 10))]
    (apply str start (take 9 (repeatedly #(rand-int 10))))))


(defn- test-email [first-name last-name domain]
  (format "%s.%s@%s.com" (string/lower-case first-name) (string/lower-case last-name) domain))


(defn- role-email
  [first-name last-name role]
  (test-email first-name last-name (name role)))


(defn account
  [role & {:keys [email first-name last-name phone activated n]
           :or   {first-name (rand-first-name)
                  last-name  (rand-last-name)
                  phone      (rand-phone)
                  activated  true}}]
  {:db/id                     (tempid n)
   :person/first-name         first-name
   :person/last-name          last-name
   :person/phone-number       phone
   :account/email             (or email (test-email first-name last-name "test"))
   :account/activated         activated
   :account/role              role
   :account/password          password
   :account/emergency-contact {:person/first-name   (rand-first-name)
                               :person/last-name    last-name
                               :person/phone-number (rand-phone)}})


;; =============================================================================
;; Member
;; =============================================================================


(defn- mlicense [account license-id unit-id amount]
  {:db/id                       (tempid)
   :member-license/status       :member-license.status/active
   :member-license/commencement (utils/now)
   :member-license/ends         (utils/weeks-from-now 12)
   :member-license/license      license-id
   :member-license/unit         unit-id
   :member-license/rate         amount})


(defn member
  [unit-id license-id
   & {:keys [first-name last-name email n]
      :or   {first-name (rand-first-name)
             last-name  (rand-last-name)}}]
  (let [account  (account :account.role/member
                          :first-name first-name
                          :last-name last-name
                          :email (or email (role-email first-name last-name :account.role/member)))
        mlicense (mlicense account license-id unit-id 2000.0)]
    [(assoc account :account/licenses (:db/id mlicense))
     mlicense
     {:db/id            (tempid)
      :deposit/account  (:db/id account)
      :deposit/amount   2000.0
      :deposit/due      (utils/weeks-from-now 4)}]))


;; =============================================================================
;; Onboarding
;; =============================================================================



(defn approval [account approver-id unit-id license-id]
  {:db/id             (tempid)
   :approval/account  (:db/id account)
   :approval/approver approver-id
   :approval/unit     unit-id
   :approval/license  license-id
   :approval/move-in  (utils/weeks-from-now 1)
   :approval/status   :approval.status/approved})


(defn onboard
  [approver-id unit-id license-id
   & {:keys [first-name last-name email n]
      :or   {first-name (rand-first-name)
             last-name  (rand-last-name)}}]
  (let [account     (account :account.role/onboarding
                             :n n
                             :first-name first-name
                             :last-name last-name
                             :email (or email (role-email first-name last-name :account.role/onboarding)))
        application {:db/id               (tempid)
                     :application/status  :application.status/approved
                     :application/license license-id}]
    [(assoc account :account/application (:db/id application))
     {:db/id           (utils/tempid)
      :onboard/account (:db/id account)}
     application
     {:db/id           (tempid)
      :deposit/account (:db/id account)
      :deposit/amount  2000.0
      :deposit/due     (utils/weeks-from-now 4)}
     (approval account approver-id unit-id license-id)]))


;; =============================================================================
;; Admin
;; =============================================================================


(defn admin
  [& {:keys [first-name last-name email n]
      :or   {first-name (rand-first-name)
             last-name  (rand-last-name)}}]
  [(account :account.role/admin
            :n n
            :first-name first-name
            :last-name last-name
            :email (or email (role-email first-name last-name :account.role/admin)))])


;; =============================================================================
;; Applicant
;; =============================================================================


(defn applicant
  [& {:keys [first-name last-name email n]
      :or   {first-name (rand-first-name)
             last-name  (rand-last-name)}}]
  [(account :account.role/applicant
            :n n
            :first-name first-name
            :last-name last-name
            :email (or email (role-email first-name last-name :account.role/applicant)))])
