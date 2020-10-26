(ns feca.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [feca.database.events]
            [feca.database.subscriptions]
            [cljs-time.coerce :as ctime]
            [cljs-time.format :as ftime]
            [secretary.core :as secretary :refer-macros [defroute]]
            [pushy.core :as pushy]))

(secretary/set-config! :prefix "#")
(declare advertiser-view)

(defn format-date [s]
  "Simple date formating function to display a human readable date."
  (ftime/unparse (ftime/formatter "dd-MM-y") (ctime/from-string s)))

(defn sort-column [key]
  "Sort the table by the column mentioned in `key"
  (re-frame/dispatch [:meta-data [:advertisers] key])
  [advertiser-view key])

(defn column-title
  "set the column title."
  [key title]
  (let [{:keys [order-by direction]} @(re-frame/subscribe [:sort-info [:advertisers]])]
    (if (= key order-by)
      (cond
        (= direction :asc) (str title " ^")
        (= direction :desc) (str title " v")
        (= direction :unsort) title)
      title)))

(defn advertiser-head
  "Render the advertisers table hader."
  []
  [:thead
   [:tr
    [:th {:on-click #(sort-column :name)
          :style {:cursor "pointer"}} (column-title :name "Name")]
    [:th {:on-click #(sort-column :createdAt)
          :style {:cursor "pointer"}} (column-title :createdAt "Creation date")]
    [:th {:on-click #(sort-column :campaign-count)
          :style {:text-align "center" :cursor "pointer"}}
     (column-title :campaign-count "# of campaigns")]
    [:th {:on-click #(sort-column :impressions)
          :style {:text-align "right" :cursor "pointer"}}
     (column-title :impressions "Impressions")]
    [:th {:on-click #(sort-column :clicks)
          :style {:text-align "right"
                  :cursor "pointer"}} (column-title :clicks "Clicks")]]])

(defn advertiser-row
  "Render a table row for advertisers."
  [{:keys [id name createdAt campaign-count impressions clicks]}]
  [:tr {:key (str "advertiser" id)}
   [:td name]
   [:td (if-not (= id "0") (format-date createdAt) "")]
   [:td {:style {:text-align "center"}} campaign-count]
   [:td {:style {:text-align "right"}} (if-not (= 0 impressions) impressions "n/a")]
   [:td {:style {:text-align "right"}} (if-not (= 0 clicks) clicks "n/a")]])

(defn advertiser-table
  "Render the advertisers table."
  [advertisers]
  [:div
   [:h1.ui.header "Overview of advertisers"]
   [:table.ui.celled.striped.table
    [advertiser-head]
    [:tbody
     (doall (for [advertiser advertisers]
              (advertiser-row advertiser)))]]])

(def history (pushy/pushy secretary/dispatch!
                          (fn [x] (when secretary/locate-route x) x)))

(defn advertiser-view
  "Display a table with advertisers"
  []
  (re-frame/dispatch [:fetch-data :id :advertisers "/api/v1/advertisers"])
  (re-frame/dispatch [:fetch-data :advertiserId :statistics "/api/v1/advertiser-statistics"])
  (let [{:keys [order-by direction]} @(re-frame/subscribe [:sort-info [:advertisers]])
        advertisers (map
                      (fn [[id row]]
                        (let [stats @(re-frame/subscribe [:data [:statistics id]])]
                          (-> row
                              (assoc :campaign-count (count (:campaignIds row))
                                     :impressions (get stats :impressions 0)
                                     :clicks (get stats :clicks 0)))))
                      @(re-frame/subscribe [:data [:advertisers]]))]
    (pushy/replace-token! history (str "/?order-by=" order-by "&direction="direction))
    [:div.ui.container
     [advertiser-table (cond
                         (= (keyword direction) :asc) (sort-by (keyword order-by) advertisers)
                         (= (keyword direction) :desc) (reverse (sort-by (keyword order-by) advertisers))
                         (= (keyword direction) :unsort) advertisers)]]))

(defn routes
  "The routes in this app."
  []
  (defroute "/" [query-params]
            #_(if query-params
                (let [order-by (get query-params :order-by :name)
                      direction (get query-params :direction :unsort)]
                  (re-frame/dispatch [:meta-data [:advertisers] order-by direction])
                  [advertiser-view]))
            [advertiser-view]))

(defn app
  "Dispatch the page withe the entered URL"
  []
  (secretary/dispatch! (str (-> js/window .-location .-pathname) (-> js/window .-location .-search))))


;;; Bootstrap the CLJS frontend by rendering the app.
;;;
;;; Calls the rdom/render function to render the `app` function on the html element with id `app`.
(defn ^:dev/after-load start
  []
  (rdom/render [app] (.getElementById js/document "app")))

;;; Initialisation of the app.
;;;
;;; This method is called first, initialises the router and db. After router and db are initialised the rendering is
;;; started.
(defn ^:export init                                         ; ^:export ensures the function name is not truncated by the Closure compiler.
  []
  (re-frame/dispatch-sync [:initialise-db])                 ; `dispatch-sync` to ensure db is initialised before the app is started.
  (routes)
  (pushy/start! history)
  (start))