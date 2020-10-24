(ns feca.core
  (:require [reagent.dom :as reagent]
            [re-frame.core :as re-frame]
            [feca.database.events]
            [feca.database.subscriptions]))

(defn advertiser-head []
  [:thead
   [:tr
    [:th "Name"]
    [:th "Creation date"]
    [:th {:style {:text-align "center"}} "# of campaigns"]
    [:th {:style {:text-align "center"}} "Impressions"]
    [:th {:style {:text-align "center"}} "Clicks"]]])

(defn advertiser-row [row]
  [:tr {:key (str "advertiser" (:id row))}
   [:td (:name row)]
   [:td (:createdAt row)]
   [:td {:style {:text-align "center"}} (count (:campaignIds row))]
   [:td {:style {:text-align "center"}} "n/a"]
   [:td {:style {:text-align "center"}} "n/a"]])

(defn advertiser-table
  []
  (let [advertisers @(re-frame/subscribe [:data [:advertisers]])]
    [:div
     [:h1.ui.header "Overview of advertisers"]
     [:table.ui.celled.striped.table
      [advertiser-head]
      (for [advertiser advertisers]
        (advertiser-row advertiser))]]))

(defn app
  []
  (re-frame/dispatch [:fetch-data :advertisers "/api/v1/advertisers"])
  (re-frame/dispatch [:fetch-data :statistics "/api/v1/advertiser-statistics"])
  [:div.ui.container
   (let [advertisers @(re-frame/subscribe [:data [:advertisers]])]
     [advertiser-table advertisers])])

;;; Bootstrap the CLJS frontend by rendering the app.
;;;
;;; Calls the reagent/render function to render the `app` function on the html element with id `app`.
(defn ^:dev/after-load start
  []
  (reagent/render [app]
                  (.getElementById js/document "app")))



;;; Initialisation of the app.
;;;
;;; This method is called first, initialises the router and db. After router and db are initialised the rendering is
;;; started.
(defn ^:export init ; ^:export ensures the function name is not truncated by the Closure compiler.
  []
  (re-frame/dispatch-sync [:initialise-db]) ; `dispatch-sync` to ensure db is initialised before the app is started.
  ;(router/start!) ; Enable pushy.
  (start))
