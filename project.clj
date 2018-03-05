(defproject starcity/blueprints "2.3.1-SNAPSHOT"
  :description "The Starcity database schema and migration API."
  :url "https://github.com/starcity-properties/blueprints"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.rkn/conformity "0.4.0"]
                 [starcity/toolbelt-core "0.3.0"]
                 [starcity/toolbelt-date "0.2.0"]
                 [starcity/toolbelt-datomic "0.2.0"]
                 [datomic-schema "1.3.0"]
                 [clj-time "0.14.2"]]

  :source-paths ["src"]

  :profiles {:provided {:dependencies [[com.datomic/datomic-free "0.9.5544"]]}
             :dev      {:source-paths ["src" "test"]}}

  :plugins [[s3-wagon-private "1.2.0"]]

  :repositories {"releases" {:url        "s3://starjars/releases"
                             :username   :env/aws_access_key
                             :passphrase :env/aws_secret_key}})
