(ns lipas-ui.login.events
  (:require [ajax.core :as ajax]
            [goog.crypt.base64 :as b64]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::set-login-form-field
 (fn [db [_ path value]]
   (let [path (into [:user :login-form] path)]
     (assoc-in db path value))))

(re-frame/reg-event-db
 ::login-success
 (fn [db [_ result]]
   (-> db
       (assoc-in [:logged-in?] true)
       (assoc-in [:user :login] result))))

(re-frame/reg-event-db
 ::login-failure
 (fn [db [_ result]]
   (assoc-in db [:user :login-error] result)))

(comment (->basic-auth {:username "kissa" :password "koira"}))
(defn ->basic-auth
  "Creates base64 encoded Authorization header value"
  [{:keys [username password]}]
  (str "Basic " (b64/encodeString (str username ":" password))))

(re-frame/reg-event-fx
 ::submit-login-form
 (fn [{:keys [db]} [_ form-data]]
   {:http-xhrio {:method          :get
                 :uri             "http://localhost:8090/api/v1/auth"
                 :headers         {:Authorization (->basic-auth form-data)}
                 ;;:format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::login-success]
                 :on-failure      [::login-failure]}

    :db         (assoc db :spinner true)}))