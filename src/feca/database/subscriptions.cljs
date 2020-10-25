(ns feca.database.subscriptions
  (:require [re-frame.core :as re-frame]))

;;;
;;; Subscriptions
;;;


;;; Layer-2 :data subscription
(re-frame/reg-sub
  :data
  (fn
    [db [_ data-path]]
    (get-in db (vec (cons :data data-path)))))

(re-frame/reg-sub
  :related-data
  (fn
    [db [_ data-path]]
    (get-in db (vec (cons :data data-path)))))

(re-frame/reg-sub
  :sort-info
  (fn
    [db [_ data-path]]
    (get-in db (vec (cons :meta data-path)))))
