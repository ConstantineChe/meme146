(ns meme146.db.core
    (:require [monger.core :as mg]
              [monger.collection :as mc]
              [monger.operators :refer :all]
              [environ.core :refer [env]])
    (:import org.bson.types.ObjectId))


(defonce db (atom nil))

(defn oid [] (ObjectId.))

(def dictionary "dictionary")

(defn connect! []
  ;; Tries to get the Mongo URI from the environment variable
  (reset! db (-> (:database-url env) mg/connect-via-uri :db)))

(defn disconnect! []
  (when-let [conn @db]
    (mg/disconnect conn)
    (reset! db nil)))

(defn create-user! [user]
  (mc/insert @db "users" (merge user {:_id (oid)})))

(defn update-user! [id first-name last-name email]
  (mc/update @db "users" {:_id id}
             {$set {:first_name first-name
                    :last_name last-name
                    :email email}}))

(defn get-user [id]
  (mc/find-one-as-map @db "users" {:_id id}))


(defn add-entry! [base translation tag]
  (mc/insert @db dictionary
             {:_id (oid)
              :base base
              :translation translation
              :tag tag}))

(defn add-dictionary! [collection]
  (mc/insert-batch @db dictionary collection))

(defn remove-entry! [id]
  (mc/remove-by-id @db dictionary (ObjectId. id)))

(defn get-dictionary []
  (mc/find-maps @db dictionary))

(defn update-entry! [id data]
  (mc/update-by-id @db dictionary (ObjectId. id) data))
