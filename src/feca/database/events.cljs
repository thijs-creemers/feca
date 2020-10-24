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
    {:authentication {:uid "fake"}

     :navigation     {:active-page    :advertisers
                      :sidebar-active true}

     :users          {"fake" {:profile {:first-name "Fake"
                                        :last-name  "User"
                                        :email      "fake.user@odc.io"
                                        :password   "password"}}}
     :data           {:advertisers []
                      :statistics  []}}))

;;; Update data for a single key.
;;;
;;; This event stores an update to a single key of data in the :meta space.
;;;
;;; |Parameter |Description |Example |
;;; |----------|------------|--------|
;;; |path |The path where the original object is stored |:devices :device|
;;; |uuid |The UUID which identifies the record which is updated| |
;;; |key |The key which identifies the field being updated |:name |
;;; |value |The new value for the field | NMDCL-NLOUM1-012 |
;;;
;;; Creates a new entry in the :meta space with the updated value.

;(re-frame/reg-event-db
;  :update-data
;  (re-frame-tracing/fn-traced
;    [db [_ path uuid key value]]
;    (let [updated-key-path (vec (cons :meta (conj path uuid :changed key)))]
;      (assoc-in db updated-key-path value)}))


;;; Update data for a complete item.
;;;
;;; This event stores an update to an item of data in the :meta space.
;;;
;;; |Parameter |Description |Example |
;;; |----------|------------|--------|
;;; |path |The path where the original object is stored |:devices :device|
;;; |uuid |The UUID which identifies the record which is updated| |
;;; |values |The new values map for the item| |
;;;
;;; Merges an entry in the :meta space with the updated values.

;(re-frame/reg-event-db
;  :update-item
;  (re-frame-tracing/fn-traced
;    [db [_ path values]]
;    (let [updated-item-path (vec (cons :meta (conj path (:uuid values) :changed)))]
;      (update-in db updated-item-path merge values)}))


;;; Toggle an expanded item.
;;;
;;; |Parameter |Description |Example |
;;; |----------|------------|--------|
;;; |path |The path where the original object is stored within the :data space. |:devices :device|
;;; |key |The key which which identifies the field being expanded or collapsed |:name |
;;;
;;; Negates the expand status of an item. This is stored in the `:meta` space.

;(re-frame/reg-event-db
;  :toggle-expanded-item
;  (re-frame-tracing/fn-traced
;    [db [_ path item-key]]
;    (let [item-selected-path (vec (cons :meta (conj path item-key :expanded)))]
;      (assoc-in db item-selected-path (not (get-in db item-selected-path false)))}))


;;; Toggle a selected item.
;;;
;;; |Parameter |Description |Example |
;;; |----------|------------|--------|
;;; |path |The path where the original object is stored within the :data space. |:devices :device|
;;; |key |The key which which identifies the field being selected or deselected |:name |
;;;
;;; Negates the selection of an item. This is stored in the `:meta` space.

;(re-frame/reg-event-db
;  :toggle-select-item
;  (re-frame-tracing/fn-traced
;    [db [_ path item-key]]
;    (let [item-selected-path (vec (cons :meta (conj path item-key :selected)))]
;      (assoc-in db item-selected-path (not (get-in db item-selected-path false)))}))


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
