(ns lipas-ui.register.views
  (:require [lipas-ui.i18n :as i18n]
            [lipas-ui.mui :as mui]
            [lipas-ui.register.events :as events]
            [lipas-ui.register.subs :as subs]
            [lipas-ui.subs :as global-subs]
            [lipas-ui.routes :refer [navigate!]]
            [re-frame.core :as re-frame]))

(defn set-form-field [path event]
  (let [path (into [] (flatten [path]))
        value (-> event
                  .-target
                  .-value)]
    (re-frame/dispatch [::events/set-registration-form-field path value])))

(comment (validate-email "kissakoira.fi"))
(defn validate-email
  [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (and (string? email) (re-matches pattern email))))

(defn invalid? [result]
  (not (some? result)))

(defn create-registration-form [tr form-data]
  [mui/form-group
   [mui/text-field {:label (tr :user/email)
                    :type "email"
                    :error (invalid? (validate-email (:email form-data)))
                    :value (:email form-data)
                    :on-change #(set-form-field :email %)
                    :required true
                    :placeholder (tr :user/email-example)}]
   [mui/text-field {:label (tr :user/username)
                    :type "text"
                    :value (:username form-data)
                    :on-change #(set-form-field :username %)
                    :required true
                    :placeholder (tr :user/username-example)}]
   [mui/text-field {:label (tr :user/password)
                    :type "password"
                    :value (:password form-data)
                    :on-change #(set-form-field :password %)
                    :required true}]
   [mui/text-field {:label (tr :user/firstname)
                    :required true
                    :value (-> form-data :user-data :firstname)
                    :on-change #(set-form-field [:user-data :firstname] %)}]
   [mui/text-field {:label (tr :user/lastname)
                    :required true
                    :value (-> form-data :user-data :lastname)
                    :on-change #(set-form-field [:user-data :lastname] %)}]
   [mui/text-field {:label (tr :user/permissions)
                    :multiline true
                    :value (-> form-data :user-data :permissions)
                    :on-change #(set-form-field [:user-data :permissions] %)
                    :rows 3
                    :placeholder (tr :user/permissions-example)
                    :helper-text (tr :user/permissions-help)}]
   [mui/button {:color "primary"
                :size "large"
                :on-click #(re-frame/dispatch
                            [::events/submit-registration-form form-data])}
    (tr :actions/save)]])

(defn create-registration-panel [tr form-data]
  (let [card-props {:square true
                    :style {:height "100%"}}]
    [mui/grid {:container true
               :justify "center"
               :style {:padding "1em"}}
     [mui/grid {:item true :xs 12 :md 8 :lg 6}
      [mui/card card-props
       [mui/card-header {:title (tr :user/registrate)}]
       [mui/card-content
        (create-registration-form tr form-data)]]]]))

(defn main []
  (let [tr (i18n/->tr-fn @(re-frame/subscribe [::global-subs/locale]))
        logged-in? (re-frame/subscribe [::subs/logged-in?])
        form-data (re-frame/subscribe [::subs/registration-form])]
    (if @logged-in?
      (navigate! "/#/profiili")
      (create-registration-panel tr @form-data))))
