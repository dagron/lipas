(ns lipas-ui.user.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::logged-in?
 (fn [db _]
   (-> db :user :logged-in?)))