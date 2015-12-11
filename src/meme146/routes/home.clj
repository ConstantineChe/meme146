(ns meme146.routes.home
  (:require [meme146.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [ring.util.response :refer [redirect]]
            [clojure.java.io :as io]
            [meme146.db.core :as db]
            [clojure-csv.core :as csv]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [buddy.auth :refer [authenticated?]]))

(defn home-page []
  (layout/render "home.html"))

(defn upload-page []
  (layout/render "upload.html"))

(defn dictionary-page []
  (layout/render-dictionary (db/get-dictionary)))

(defn validate-dic-entry [params]
  (first
   (b/validate
    params
    :base v/required
    :translation v/required)))

(defn add-enrty [{:keys [params]}]
  (do
    (apply db/add-entry!
           (select-keys params
                        [:base :translation :tag]))
    (redirect "/dictionary")))

(defn edit-entry [{:keys [params]}]
  (do
    (db/update-entry!
     (:id params)
     (select-keys params [:base :translation :tag]))
    (redirect "/dictionary")))

(defn upload-csv [{:keys [params]}]
  (do
    (with-open [file (io/reader (:tempfile (:csv-upload params)))]
      (db/add-dictionary!  (map (fn [record]
                                    {:base (first record)
                                     :translation (second record)
                                     :tag (:tags params)})
                                  (csv/parse-csv (slurp file)))))
    (redirect "/dictionary")))

(defn process-csv [csv-seq]
  )

(defn remove-entry [{:keys [params]}]
  (do
    (db/remove-entry! (:id params))
    (redirect "/dictionary")))

(defn user-page [request]
  (if (authenticated? request) (layout/render-hiccup [:h1 "%username%"])
      (redirect "/login")))

(defn login-page [request]
  (layout/render-hiccup [:h1 "login"]))

(defroutes home-routes
  (GET "/boot/:msg" [msg] (layout/render-hiccup [:h1 msg]))
  (GET "/" [] (layout/render-hiccup [:h1 "Welcome %username%"]))
  (GET "/user" [] user-page)
  (GET "/login" [] login-page)
  (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp)))
  (GET "/upload" [] (upload-page))
  (POST "/upload" request (add-enrty request))
  (POST "/upload/batch" request (upload-csv request))
  (GET "/dictionary" [] (dictionary-page))
  (POST "/dictionary/remove" request (remove-entry request))
  (POST "/dictionary/edit" request (edit-entry request)))
