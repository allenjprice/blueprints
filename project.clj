(defproject starcity-db "0.1.0"
  :description "The Starcity database schema and migration API."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.datomic/datomic-pro "0.9.5372"]
                 [io.rkn/conformity "0.4.0"]
                 [org.postgresql/postgresql "9.4.1211"]
                 [datomic-schema "1.3.0"]]

  :repositories {"my.datomic.com" {:url   "https://my.datomic.com/repo"
                                   :creds :gpg}

                 "releases" {:url      "s3://starjars/releases"
                             :username :env/aws_access_key
                             :password :env/aws_secret_key}})
