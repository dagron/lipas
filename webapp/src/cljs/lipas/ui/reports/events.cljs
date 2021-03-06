(ns lipas.ui.reports.events
  (:require
   [ajax.core :as ajax]
   [ajax.protocols :as ajaxp]
   [clojure.set :as cset]
   [lipas.utils :as cutils]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::toggle-dialog
 (fn [db _]
   (update-in db [:reports :dialog-open?] not)))

(re-frame/reg-event-db
 ::set-selected-fields
 (fn [db [_ v append?]]
   (if append?
     (update-in db [:reports :selected-fields] (comp set into) v)
     (assoc-in db [:reports :selected-fields] v))))

(def basic-fields
  ["lipas-id"
   "name"
   "marketing-name"
   "type.type-code"
   "type.type-name"
   "location.city.city-code"
   "location.city.city-name"
   "construction-year"
   "admin"
   "owner"
   "renovation-years"
   "www"
   "phone-number"
   "email"
   "location.address"
   "location.city.neighborhood"
   "location.postal-code"
   "location.postal-office"
   "comment"])

(defn sort-headers
  "Returns vector of headers where basic-fields are in predefined order
  and rest in natural order."
  [fields]
  (let [basic (filterv #(some #{%} fields) basic-fields)
        others (cset/difference (set fields) (set basic))]
    (into [] cat
          [basic
           (sort others)])))

(re-frame/reg-event-fx
 ::create-report
 (fn [{:keys [db]} [_ query fields]]
   (let [fields (sort-headers fields)]
     {:http-xhrio
      {:method          :post
       :uri             (str (:backend-url db) "/actions/create-sports-sites-report")
       :params          {:search-query query
                         :fields       fields}
       :format          (ajax/json-request-format)
       :response-format {:type         :blob
                         :content-type (-> cutils/content-type :xlsx)
                         :description  (-> cutils/content-type :xlsx)
                         :read         ajaxp/-body}
       :on-success      [::report-success]
       :on-failure      [::report-failure]}
      :db (assoc-in db [:reports :downloading?] true)})))

(re-frame/reg-event-fx
 ::report-success
 (fn [{:keys [db ]} [_ blob]]
   {:lipas.ui.effects/save-as! {:blob         blob
                                :filename     "lipas.xlsx"
                                :content-type (-> cutils/content-type :xlsx)}
    :db (assoc-in db [:reports :downloading?] false)}))

(re-frame/reg-event-fx
 ::report-failure
 (fn [{:keys [db]} [_ error]]
   ;; TODO display error msg
   (let [fatal? false]
     {:db           (assoc-in db [:reports :downloading?] false)
      :ga/exception [(:message error) fatal?]})))
