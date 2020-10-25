;;;; Database
;;;;
;;;; This module contains the `:initialise-db` event.
;;;; This event loads some default data in the database used to bootstrap the app.

(ns feca.database.events
  (:require [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [re-frame.core :as re-frame]))
;;;
;;; Events
;;;

;;; The server url for the API service to use.
(defonce api-url "https://5b87a97d35589600143c1424.mockapi.io")

;;; Initialisation of the app-db with default data.
(re-frame/reg-event-db
  :initialise-db
  (fn
    [_ _]
    {:data {:advertisers {"0" {:id "0" :name "loading..."}}
            :statistics  {}}
     :meta {:advertisers {:order-by :name :direction :unsort}}}))

;;; Fetch the data from the API path and store it in [:data path] in our state DB.
(re-frame/reg-event-fx
  :fetch-data
  (fn
    [_ [_ id-field key path]]
    {:http-xhrio {:method          :get
                  :uri             (str api-url path)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:fetch-data-success id-field key]
                  :on-failure      [:fetch-data-failed key]}}))

;; Request handler for successful requests
(re-frame/reg-event-db
  :fetch-data-success
  (fn
    [db [_ id-field key data]]
    (-> db
        (assoc-in [:data key] (into {} (map (fn [row]
                                              [(id-field row) row])) data)))))
        ;(update-in [:data key] (dissoc "0")))))

;; Request handler for successful requests
(re-frame/reg-event-db
  :fetch-data-failed
  (fn
    [db [_ key _]]
    (assoc-in db [:data key "0"] {:id "0" :name (str "Could not load " (name key))})))

(re-frame/reg-event-db
  :meta-data
  (fn
    [db [_ path order-by direction]]
    (let [directions [:unsort :desc :asc]
          current-order-by (get-in db (vec (concat [:meta] (conj path :order-by))) nil)
          current-direction (if (= order-by current-order-by)
                              (get-in db (vec (concat [:meta] (conj path :direction))) :unsort)
                              :unsort)
          toggle (fn [key]
                   (if (= (.indexOf directions key) 2)
                     (first directions)
                     (nth directions (inc (.indexOf directions key)))))]
      (-> db
          (assoc-in (vec (concat [:meta] (conj path :order-by))) order-by)
          (assoc-in (vec (concat [:meta] (conj path :direction))) (if direction direction (toggle current-direction)))))))

(comment
  (re-frame/dispatch [:fetch-data :id :advertisers "/api/v1/advertisers"])
  (re-frame/dispatch [:fetch-data :advertiserId :statistics "/api/v1/advertiser-statistics"])
  (re-frame/dispatch [:meta-data [:advertisers] :name])
  (re-frame/dispatch [:meta-data [:advertisers] :name :unsort]))