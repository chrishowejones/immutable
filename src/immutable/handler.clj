(ns immutable.handler
  (:require [clojure.string :as s]
            [compojure
             [core :refer :all]
             [route :as route]]
            [hiccup
             [form :refer [form-to label submit-button text-field]]
             [page :refer [html5 include-css]]
             [element :refer [link-to]]]
            [ring.middleware
             [defaults :refer [site-defaults wrap-defaults]]
             [reload :refer [wrap-reload]]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.java.jdbc :as jdbc]
            [jdbc.pool.c3p0 :as pool]))

;; PERSISTENCE
(def db-spec {:classname   "org.h2.Driver"
              :subprotocol "h2:mem"
              :subname     "people;DB_CLOSE_DELAY=-1"
              :user        "sa"
              :password    ""})

(def pooled-ds-spec (pool/make-datasource-spec db-spec))

(defn create-person-table
  []
  (jdbc/db-do-commands pooled-ds-spec
                       (jdbc/create-table-ddl :PERSON
                                              [[:id "bigint auto_increment PRIMARY KEY"]
                                               [:first_name "varchar(256)"]
                                               [:last_name "varchar(256)"]
                                               [:dob "varchar(256)"]])))

(defn drop-tables
  []
  (jdbc/db-do-commands pooled-ds-spec
                       (jdbc/drop-table-ddl :PERSON)))

;; temporary store - remove and replace with h2 database
(defonce temp-store (atom {}))

(def get-id
  (let [id (atom 0)]
    (fn []
      (swap! id inc))))

(defn store-person [person]
  (let [id (get-id)]
    (swap! temp-store assoc id person)
    id))

(defn get-person
  [person-id]
  (@temp-store person-id))

(defn- search-people
  [search]
  (fn [people person]
    (let [search-pattern (re-pattern search)]
      (if (or (re-find search-pattern (:first-name person))
              (re-find search-pattern (:last-name person))
              (re-find search-pattern (:dob person)))
        (conj people person)
        people))))

(defn find-people
  [search]
  (reduce (search-people search) [] (vals @temp-store)))

;; RENDER
(defn page-layout
  ([page-body] (page-layout page-body ""))
  ([page-body status-message]
   (html5
    [:head
     (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")
     [:body
      [:div.container
       [:div.row.col-xs-12
        [:div.col-xs-4.text-href (link-to "/" "Home")]
        [:div.col-xs-4
         [:h1.text-info "Person Demo App"]]]
       [:div.row.col-xs-12 [:br]]
       page-body
       [:div.row.col-xs-12 status-message]]]])))

(defn person-table
  [{:keys [first-name last-name dob]}]
  [:div.table
   [:div.row.col-xs-12.col-xs-offset-2 [:h3.text-info "Person"]]
   [:div.row [:br]]
   [:div.row
    [:div.col-xs-3 [:label "First name:"]]
    [:div.col-xs-3 first-name]]
   [:div.row [:br]]
   [:div.row
    [:div.col-xs-3 [:label "Last name:"]]
    [:div.col-xs-3 last-name]]
   [:div.row [:br]]
   [:div.row
    [:div.col-xs-3 [:label "Date of Birth:"]]
    [:div.col-xs-3 dob]]])

(defn create-person-form
  []
  [:div.table
   (form-to [:post "createperson"]
            (anti-forgery-field)
    [:div.row.col-xs-12.col-xs-offset-2 [:h3.text-info "Person"]]
    [:div.row [:br]]
    [:div.row
     [:div.col-xs-3 (label "first-name" "First name:")]
     [:div (text-field {:placeholder "Enter first name"} "first-name")]]
    [:div.row [:br]]
    [:div.row
     [:div.col-xs-3 (label "last-name" "Last name:")]
     [:div (text-field {:placeholder "Enter last name"} "last-name")]]
    [:div.row [:br]]
    [:div.row
     [:div.col-xs-3 (label "dob" "Date of Birth:")]
     [:div (text-field {:placeholder "Enter Date of Birth"} "dob")]]
    [:div.row [:br]]
    [:div.row
     [:div.col-xs-4 (submit-button "Create Person")]])])

(defn find-people-form
  []
  [:div.table
   (form-to [:post "findpeople"]
            (anti-forgery-field)
    [:div.row.col-xs-12.col-xs-offset-4 [:h3.text-info "Search for people"]]
    [:div.row [:br]]
    [:div.row
     [:div.col-xs-3 (label "search" "Enter search:")]
     [:div (text-field {:placeholder "Enter search term"} "search")]]
    [:div.row [:br]]
    [:div.row
     [:div.col-xs-4 (submit-button "Search")]])])

(defn- key->label
  [key]
  (-> key str (s/replace ":" "") (s/replace #"-" " ") s/capitalize (str ":")))

(defn people-found-table
  [people]
  [:div.table
   [:div.row.col-xs-12.col-xs-offset-4 [:h3.text-info "Search Results"]]
   (if (empty? people)
     [:div.row.col-xs-12 "No people found."]
     (for [person people]
       [:div.row.col-xs-12
        (for [[k v] person]
          [:div
           [:div.col-xs-2  [:label (key->label k)]]
           [:div.col-xs-2 v]])]))])

(defn home-page
  []
  (page-layout
   [:div
    [:div.row.col-xs-12 (link-to "/createperson" "Create a person")]
    [:div.row.col-xs-12 (link-to "/findpeople" "Search for people")]]))

;; NAVIGATION
(defn show-person-page
  [person-id]
  (let [person (get-person person-id)
        status-msg (if person "" (format "No person with id: %s found" person-id))]
    (page-layout (person-table person) status-msg)))

(defn create-person-page
  ([] (create-person-page ""))
  ([status-message]
   (page-layout (create-person-form) status-message)))

(defn create-person [person]
  (-> (store-person person)
      (#(if % (format "Saved person with id: %s" %) ""))
      create-person-page))

(defn find-people-page
  []
  (page-layout (find-people-form)))

(defn people-found-page
  [search]
  (page-layout (people-found-table search)))

(defroutes app-routes
  (GET "/" [] (home-page))
  (GET "/createperson" [] (create-person-page))
  (POST "/createperson" {params :params} (create-person params))
  (GET "/person/:id" [id] (-> id read-string show-person-page))
  (GET "/findpeople" [] (find-people-page))
  (POST "/findpeople" [search] (people-found-page (find-people search)))
  (route/not-found "Not Found"))

(defn wrap-remove-anti-forgery-token
  [handler]
  (fn [req]
    (handler (update-in req [:params] dissoc :__anti-forgery-token))))

(def app
  (wrap-defaults (wrap-remove-anti-forgery-token app-routes) site-defaults))

(def reloadable-app
  (wrap-reload app))

(defn app-handle
  [reload?]
  (if reload? #'app #'reloadable-app))

(comment

  (jdbc/with-db-connection [db-conn ds-spec]
   (jdbc/insert! db-conn :person
                 {:first_name "Test2"
                  :last_name "Last"
                  :dob "2nd Feb"}))

  (jdbc/with-db-connection [db-conn ds-spec]
    (jdbc/query db-conn "SELECT * FROM person"))




  )
