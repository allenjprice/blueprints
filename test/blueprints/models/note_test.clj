(ns blueprints.models.note-test
  (:require [blueprints.models.note :as sut]
            [blueprints.schema :refer [conform]]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [with-conn]]
            [datomic.api :as d]))

(use-fixtures :each (tdt/conn-fixture conform))


(defn- seed-account [conn]
  (:db-after @(d/transact conn [{:db/id         (d/tempid :db.part/user)
                                 :account/email "member@test.com"}])))


(defn- seed-note [conn]
  (let [tx-data [(sut/create "subject" "content" [[:account/email "member@test.com"]])]]
    (:db-after @(d/transact conn tx-data))))


(deftest notes-query
  (with-conn conn
    (let [db (seed-account conn)]
      (testing "there are no notes yet"
        (is (zero? (count (sut/query db {})))
            "No notes exist")
        (is (zero? (count (sut/query db {:refs [[:account/email "member@test.com"]]})))
            "No notes exist for this account")))

    (let [db (seed-note conn)]
      (testing "there's one note"
        (is (= 1 (count (sut/query db {}))))
        (is (= 1 (count (sut/query db {:refs [[:account/email "member@test.com"]]})))
            "There's one note that references this account")
        (is (= 1 (count (sut/query db {:q "cont"})))
            "There is one note that has the string `cont`"))
      )))
