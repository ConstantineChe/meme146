(ns meme146.view.templates.core
  (:require
   [hiccup.core :as hc]
   [hiccup.page :as hp]
   [hiccup.element :as el]
   [hiccup.def :refer :all]))

(defn include-bootstrap []
  (list (hp/include-js "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js")
        (hp/include-js "/bootstrap/js/bootstrap.js")
        (hp/include-css "/bootstrap/css/bootstrap.css")
        (hp/include-css "/bootstrap/css/bootstrap-glyphicons.css")))

(defelem navbar [menu]
  [:nav {:class "navbar navbar-inverse navbar-default"}
   [:div {:class "container"}
    [:div {:class "navbar-header"}
     [:button {:type "button"
               :class "navbar-toggle collapsed"
               :data-toggle "collapse"
               :data-target "#navbar"
               :aria-extended "false"
               :aria-controls "navbar"}
      [:span {:class "sr-only"} "Toggle navigation"]
      (for [_ menu] [:span {:class "icon-bar"}])
      ]
     (el/link-to {:class "navbar-brand"} "#" "Meme146")]
    [:div {:id "navbar" :class "collapse navbar-collapse"}
     (el/unordered-list {:class "nav navbar-nav"} menu)
     ]]])

(defelem header [menu]
  [:header {:class "header"} [:div {:class "container main-header"}
                              [:div {:class :row}
                               [:div {:class :col-sm-8}
                                [:h1 {:class "text-uppercase"} "memes"]]
                               [:div {:class "col-sm-2 pull-right"}
                                [:h3 "Lang"]]]]
   (navbar menu)])

(defhtml blank-page [title content]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (include-bootstrap)
   [:title title]
   (hp/include-css "/css/main.css")]
  [:body
   [:div {:class "main"}
    [:div {:class "container"} content]]]
  )

(defn base-template [title content]
  (blank-page title (list (header (map #(apply el/link-to %)
                                       [["/" "Home"]
                                        ["/dictionary" "Dictionary"]
                                        ["user" "Account"]
                                          ]))
                        [:div {:class "main-content"}
                         [:div {:class "container"} content]])))


(defn dictionary-view [dictionary]
  (base-template "Dictionary"
                 (list [:div {:class "container about"} [:h4 "This is a dictionary view page"]]
                  [:div {:class "container"} [:h4 "dictionary contents"]
                   [:table {:class "table table-hover"}
                    [:thead
                     [:tr
                      [:th "base"]
                      [:th "translation"]
                      [:th "tag"]]]
                    [:tbody
                     (for [row dictionary]
                       [:tr
                        [:td (:base row)]
                        [:td (:translation row)]
                        [:td (:tag row)]])]]]))
  )
