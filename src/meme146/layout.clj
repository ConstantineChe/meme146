(ns meme146.layout
  (:require [selmer.parser :as parser]
            [selmer.filters :as filters]
            [markdown.core :refer [md-to-html-string]]
            [ring.util.http-response :refer [content-type ok]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [environ.core :refer [env]]
            [hiccup.core :as hc]
            [hiccup.page :as hp]
            [hiccup.bootstrap.page :as hbp]
            [hiccup.def :refer :all
             ]))

(declare ^:dynamic *identity*)
(declare ^:dynamic *app-context*)
(parser/set-resource-path!  (clojure.java.io/resource "templates"))
(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))
(filters/add-filter! :markdown (fn [content] [:safe (md-to-html-string content)]))

(defelem navbar [menu]
  [:nav {:class "navbar"}
   [:div {:class "container"}
    [:div {:class "navbar-header"}
     [:button {:type "button"
               :class "navbar-toggle collapsed"
               :data-toggle "collapse"
               :data-target "#navbar"
               :aria-extended "false"
               :aria-controls "navbar"}
      (for [_ (range 3)] [:span {:class :icon-bar}])
      ]
     [:a {:class "navbar-brand" :href "#"} "Meme146"]]
    [:div {:id "navbar" :class "collapse navbar-collapse"}
     [:ul {:class "nav navbar-nav"}
      (for [item menu] [:li item])]]]])

(def base-tpl (fn [title content]
                (hp/html5
                 [:head
                  [:title title]
                  (hp/include-js "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js")
                  (hbp/include-bootstrap)]
                 [:body (navbar [
                                 [:a {:href "#"} "Home"]
                                 [:a {:href "#about"} "About"]
                                 [:a {:href "#contact"} "Contact"]
                                 ]) content])))



(defn render
  "renders the HTML template located relative to resources/templates"
  [template & [params]]
  (content-type
    (ok
      (parser/render-file
        template
        (assoc params
          :page template
          :dev (env :dev)
          :csrf-token *anti-forgery-token*
          :servlet-context *app-context*)))
    "text/html; charset=utf-8"))

(defn render-home [welcome-msg]
  (base-tpl "home bootstrap" welcome-msg))



(defn error-page
  "error-details should be a map containing the following keys:
   :status - error status
   :title - error title (optional)
   :message - detailed error message (optional)

   returns a response map with the error page as the body
   and the status specified by the status key"
  [error-details]
  {:status  (:status error-details)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body    (parser/render-file "error.html" error-details)})
