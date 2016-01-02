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
            [buddy.auth :refer [authenticated?]]
            [buddy.hashers :refer [encrypt check]]))

(defn home-page []
  (layout/render "home.html"))

(defn upload-page []
  (layout/render "upload.html"))

(defn dictionary-page
  ([page]
   (if-let [dictionary (seq (db/get-dictionary
                          (- (* 20 page) 20)
                          (* 20 page)))]
     (layout/render-dictionary dictionary page)
     (redirect "/not-found")))
  ([]
   (redirect "/dictionary/page/1")))

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

(defn validate-registration [params]
  (first (b/validate
          params
          :username v/required
          :email v/required
          :password v/required
          :password_confirm [[(partial = (:password params))
                              :message "passwords confirmation doesen't match."]] )))

(defn user-page [request]
  (println (:session request))
  (if (authenticated? request)
    (layout/render-hiccup [:h1 "%username%"])
    (redirect "/login")))

(defn login-page [request]
  (layout/render-hiccup [:div.container
                         [:h1 "login"]
                         [:p "or " [:a {:href "/sign-up"} "sign-up"]]]))

(defn authenticate [request]
  (let [user (db/get-user (:username (:params request)))
        password (:password (:params request))
        session (:session request)]
    (println user)
    (if (check password (:password user))
      (let [updated-session (assoc session :identity (keyword (:email user)))]
        (-> (redirect "/user")
            (assoc :session updated-session))))))

(defn sign-up-page [request]
  (layout/sign-up (:errors request)))


(defn sign-up [request]
  (let [params (:params request)]
    (if-let [errors (validate-registration params)]
            (-> (redirect "/sign-up")
                (assoc :flash (assoc params :errors errors)))
            (do (db/create-user! (merge
                                  {:password (encrypt (:password params))}
                                  (select-keys params [:username :email])))
                (-> (redirect "/user")
                    (assoc-in [:session :identity] (:email params)))))))

(defroutes home-routes
  (GET "/boot/:msg" [msg] (layout/render-hiccup [:h1 msg]))
  (GET "/" [] (layout/render-hiccup [:div.container
                                     [:h1 "Welcome %username%"]]))
  (GET "/user" [] user-page)
  (GET "/login" [] login-page)
  (POST "/login" [] authenticate)
  (GET "/sign-up" [] sign-up-page)
  (POST "/sign-up" [params] sign-up)
  (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp)))
  (GET "/upload" [] (upload-page))
  (POST "/upload" request (add-enrty request))
  (POST "/upload/batch" request (upload-csv request))
  (GET "/dictionary" [] (dictionary-page))
  (GET "/dictionary/page/:page" [page] (dictionary-page (Integer. page)))
  (POST "/dictionary/remove" request (remove-entry request))
  (POST "/dictionary/edit" request (edit-entry request)))
