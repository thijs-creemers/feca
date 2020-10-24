;;;; Database
;;;;
;;;; This module contains the `:initialise-db` event.
;;;; This event loads some default data in the database used to bootstrap the app.

(ns feca.database.events
  (:require [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [re-frame.core :as re-frame]
            [day8.re-frame.tracing :as re-frame-tracing]))

;;;
;;; Events
;;;

(defonce api-url "https://5b87a97d35589600143c1424.mockapi.io")

;;; Initialisation of the app-db with default data.
(re-frame/reg-event-db
  :initialise-db
  (fn
    [_ _]
    {:data           {:advertisers []
                      :statistics  []}}))

(re-frame/reg-event-fx
  :fetch-data
  (re-frame-tracing/fn-traced
    [_ [_ key path]]
    (prn (str api-url path))
    {:http-xhrio {:method          :get
                  :uri             (str api-url path)
                  ;:headers         {:Authorization
                  ;                  (str "JWT " (:token @(re-frame/subscribe [::subs/jwt-token])))
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:fetch-data-success key]
                  :on-failure      [:request-failed key]}}))

(re-frame/reg-event-db
  :fetch-data-success
  (re-frame-tracing/fn-traced
    [db [_ key data]]
    "Failed request handler"

    (-> db
        (assoc-in [:data key] data))))

(re-frame/reg-event-db
  :request-failed
  (re-frame-tracing/fn-traced
    [db [_ key data]]
    "Failed request handler"
    (prn "FAILED" data)
    db))

(comment
  (re-frame/dispatch [::fetch-data :advertisers "/api/v1/advertisers"])
  (re-frame/dispatch [::fetch-data :statistics "/api/v1/advertiser-statistics"]))
