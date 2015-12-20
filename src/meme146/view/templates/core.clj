(ns meme146.view.templates.core
  (:require
   [hiccup.core :as hc]
   [hiccup.page :as hp]
   [hiccup.element :as el]
   [hiccup.form :as form]
   [hiccup.def :refer :all]
   [meme146.db.core :as db]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn include-bootstrap []
  (list (hp/include-js "https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js")
        (hp/include-js "/bootstrap/js/bootstrap.js")
        (hp/include-css "/bootstrap/css/bootstrap.css")))

(defelem navbar [menu]
  [:nav.navbar.navbar-inverse.navbar-default
   [:div.container
    [:div.navbar-header
     [:button.navbar-toggle.collapsed {:type "button"
               :data-toggle "collapse"
               :data-target "#navbar"
               :aria-extended "false"
               :aria-controls "navbar"}
      [:span.sr-only "Toggle navigation"]
      (for [_ menu] [:span.icon-bar])
      ]
     (el/link-to {:class "navbar-brand"} "#" "Meme146")]
    [:div#navbar.collapse.navbar-collapse
     (el/unordered-list {:class "nav navbar-nav"} menu)
     ]]])

(defelem header [menu]
  [:header.header
   [:div.container.main-header
    [:div.row
     [:div.col-sm-8
      [:h1.text-uppercase "memes"]]
     [:div.col-sm-2.pull-right
      [:h4 "Lang"]]]]
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
   [:div.main
    [:div.container content]]])

(defn base-template [title content]
  (blank-page title (list (header (map #(apply el/link-to %)
                                       [["/" "Home"]
                                        ["/dictionary" "Dictionary"]
                                        ["/user" "Account"]
                                          ]))
                        [:div.main-content
                         [:div.container content]])))

(defn pager [current]
  (let [total (inc (/ (db/dictionary-count) 20))]
    [:ul.pagination
     [:li (el/link-to (str "/dictionary/page/" (dec current)) "«")]
     (for [page (range 1 total)]
       (if-not (= page current)
         [:li (el/link-to (str "/dictionary/page/" page)
                      (str page " "))]))
     [:li (el/link-to (str "/dictionary/page/" (inc current)) "»")]]))


(defn dictionary-view [dictionary page]
  (base-template "Dictionary"
                 (list [:div.container.about [:h4 "This is a dictionary view page"]]
                  [:div.container  [:h4 "dictionary contents"]
                   [:table.table.table-hover
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
                        [:td (:tag row)]])]]
                   (pager page)])))


(defelem input-text [label field comment password?]
  (list [:label.control-label {:for field} label]
   [:div.control-group
    [:div.controls [:p.help-block comment]
     (if-let [type (if password? form/password-field form/text-field)]
       (type {:class "input-xlarge required"}
                                 field))]]))

(defelem submit-button [text]
  [:div.control-group
     [:div.controls [:p] [:button.btn.btn-lg.btn-primary text]]])

(defn login [errors]
  (base-template "Login"
                 (form/form-to {:class "form=horisontal"} [:post "/login"]
                               [:fieldset [:div#legend [:legend "Login"]]
                                (when-not (empty? errors) [:div.error-msg errors])
                                (anti-forgery-field)
                                (input-text "Username" "username" "" false)
                                (input-text "Password" "password" "" true)
                                (submit-button "Login") [:span " or " (el/link-to "/sign-up" "sign-up")]])))

(defn sign-up [errors]
  (base-template "Sign-up"
                 (form/form-to {:class "form-horizontal"} [:post "/sign-up"]
                               [:fieldset [:div#legend [:legend "Sign-up"]]
                                (when-not (empty? errors) [:div.error-msg errors])
                                (anti-forgery-field)
                                (input-text "Username" "username"
                                            (str "Username can contain letters"
                                                 " and numbers without spaces") false)
                                (input-text "Email" "email" "" false)
                                (input-text "Password" "password"
                                            (str "Password contain at least 7 caracters. "
                                                 " One digit, one uppercase and one lowercase.")
                                            true)
                                (input-text "Password (Confirm)" "password_confirm"
                                            "Please confirm your password" true)
                                (submit-button "Sign-up")])))
