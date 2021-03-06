(ns blueprints.test.datomic
  (:require [datomic.api :as d]
            [blueprints.schema :as schema]
            [clojure.test :refer :all]))

;; =============================================================================
;; Connection Fixture
;; =============================================================================


(def ^:dynamic *conn* nil)


(defn acquire-conn []
  (let [db-name (gensym)
        db-uri  (str "datomic:mem://" db-name)]
    (d/create-database db-uri)
    (let [conn (d/connect db-uri)]
      (schema/conform conn :db.part/starcity)
      conn)))


(defn release-conn [conn]
  (d/release conn))


(defmacro with-conn
  "Acquires a datomic connection and binds it locally to symbol while executing
  body. Ensures resource is released after body completes. If called in a
  dynamic context in which *resource* is already bound, reuses the existing
  resource and does not release it."
  [symbol & body]
  `(let [~symbol (or *conn* (acquire-conn))]
     (try ~@body
          (finally
            (when-not *conn*
              (release-conn ~symbol))))))


(defn conn-fixture
  "Fixture function to acquire a Datomic connection for all tests in a
  namespace."
  [test-fn]
  (with-conn r
    (binding [*conn* r]
      (test-fn))))


;; =============================================================================
;; Helpers
;; =============================================================================

(defn speculate [db tx-data]
  (:db-after (d/with db tx-data)))


(defn attr
  "Retrieve attr from db."
  [conn attr]
  (d/entity (d/db conn) attr))


(defmacro test-attr
  [symbol attr & exprs]
  (let [conn (gensym)]
    `(testing (str "attribute " ~attr)
       (with-conn ~conn
         (let [~symbol (attr ~conn ~attr)]
           (is (created ~symbol))
           ~@exprs)))))


(defmacro enum-present [attr]
  (let [conn (gensym)]
    `(testing (str "attribute " ~attr)
       (with-conn ~conn
         (let [attr# (attr ~conn ~attr)]
           (is (created attr#)))))))


;; =============================================================================
;; Schema Validators
;; =============================================================================


(def created (comp not nil?))


(defn unique-identity [attr]
  (= (:db/unique attr) :db.unique/identity))


(defn value-type [attr type]
  (= (:db/valueType attr) (keyword "db.type" (name type))))


(defn cardinality [attr card]
  (= (:db/cardinality attr) (keyword "db.cardinality" (name card))))


(def fulltext
  "Is `attr` fulltext indexed?"
  :db/fulltext)


(def indexed
  "Is `attr` indexed?"
  :db/index)


(def component
  "Is `attr` a component attribute?"
  :db/isComponent)
