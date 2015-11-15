(ns meme146.routes.home
  (:require [meme146.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [meme146.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn home-page []
  (layout/render "home.html"))

(defn upload-page []
  (layout/render "upload.html"))

(defn dictionary-page []
  (layout/render
   "dictionary.html"
   {:dictionary (db/get-dictionary)}))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/docs" [] (ok (-> "docs/docs.md" io/resource slurp)))
  (GET "/upload" [] (upload-page))
  (POST "/upload" request (upload! request))
  (GET "/dictionary" [] (dictionary-page)))
