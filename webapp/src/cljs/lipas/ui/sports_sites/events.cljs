(ns lipas.ui.sports-sites.events
  (:require
   [ajax.core :as ajax]
   [lipas.ui.interceptors :as interceptors]
   [lipas.ui.utils :as utils]
   [lipas.utils :as cutils]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::edit-site
 (fn [db [_ lipas-id]]
   (let [site (get-in db [:sports-sites lipas-id])
         rev  (-> (utils/make-revision site (utils/timestamp))
                  (utils/make-editable))]
     (-> db
         (assoc-in [:sports-sites lipas-id :editing] rev)))))

(re-frame/reg-event-db
 ::edit-field
 (fn [db [_ lipas-id path value]]
   (utils/set-field db (into [:sports-sites lipas-id :editing] path) value)))

(re-frame/reg-event-db
 ::discard-edits
 (fn [db [_ lipas-id]]
   (assoc-in db [:sports-sites lipas-id :editing] nil)))

(defn- commit-ajax [db data draft? on-success]
  (let [token  (-> db :user :login :token)
        params (when draft? "?draft=true")]
    {:http-xhrio
     {:method          :post
      :headers         {:Authorization (str "Token " token)}
      :uri             (str (:backend-url db) (str "/sports-sites" params))
      :params          data
      :format          (ajax/json-request-format)
      :response-format (ajax/json-response-format {:keywords? true})
      :on-success      [::save-success on-success]
      :on-failure      [::save-failure]}}))

(defn- dirty? [db rev]
  (let [lipas-id  (:lipas-id rev)
        site      (get-in db [:sports-sites lipas-id])
        year      (utils/resolve-year (:event-date rev))
        timestamp (if (utils/this-year? year)
                    (:latest site)
                    (-> (utils/latest-by-year (:history site))
                        (get year)))
        latest    (get-in site [:history timestamp])]
    (utils/different? rev latest)))

(defn- new-site? [rev]
  (nil? (:lipas-id rev)))

(defn- on-success-default []
  [[:lipas.ui.search.events/submit-search]])

(defn- on-success-new [{:keys [lipas-id]}]
  [[::discard-new-site]
   [:lipas.ui.map.events/stop-editing]
   [:lipas.ui.map.events/show-sports-site lipas-id]
   [:lipas.ui.search.events/submit-search]
   [:lipas.ui.login.events/refresh-login]])

(re-frame/reg-event-fx
 ::commit-rev
 [interceptors/check-token]
 (fn [{:keys [db]} [_ rev draft? on-success]]
   (let [new?       (new-site? rev)
         on-success (cond
                      on-success on-success
                      new?       on-success-new
                      :else      on-success-default)
         on-success (or on-success (when new? on-success-new))]
     (if (or new? (dirty? db rev))
       (commit-ajax db rev draft? on-success)
       {:dispatch [::save-success on-success rev]}))))

(re-frame/reg-event-fx
 ::save-edits
 [interceptors/check-token]
 (fn [{:keys [db]} [_ lipas-id]]
   (let [rev        (-> (get-in db [:sports-sites lipas-id :editing])
                        utils/make-saveable)
         draft?     false
         new?       (new-site? rev)
         on-success (cond
                      new?  #(on-success-new {:lipas-id lipas-id})
                      :else on-success-default)]
     (if (or new? (dirty? db rev))
       (commit-ajax db rev draft? on-success)
       {:dispatch [::save-success on-success rev]}))))

(re-frame/reg-event-fx
 ::save-success
 (fn [{:keys [db]} [_ on-success result]]
   ;; `on-success` is a function that returns vector of events to be
   ;; dispatched.
   (let [tr              (-> db :translator)
         status          (-> result :status)
         type            (-> result :type :type-code)
         lipas-id        (-> result :lipas-id)
         year            (dec utils/this-year)
         dispatch-extras (when on-success (on-success result))]
     {:db             (-> db
                          (utils/add-to-db result)
                          (assoc-in [:sports-sites lipas-id :editing] nil))
      :dispatch-n     (into
                       [[:lipas.ui.events/set-active-notification
                         {:message  (tr :notifications/save-success)
                          :success? true}]
                        (when (#{2510 2520 3110 3130} type)
                          [:lipas.ui.energy.events/fetch-energy-report year type])]
                       dispatch-extras)
      :ga/event       ["save-sports-site" status type]})))

(re-frame/reg-event-fx
 ::save-failure
 (fn [{:keys [db]} [_ error]]
   (let [tr     (:translator db)
         fatal? false]
     {:db           (assoc-in db [:sports-sites :errors (utils/timestamp)] error)
      :dispatch     [:lipas.ui.events/set-active-notification
                     {:message  (tr :notifications/save-failed)
                      :success? false}]
      :ga/exception [(:message error) fatal?]})))

(re-frame/reg-event-fx
 ::get-success
 (fn [{:keys [db]} [_ sites]]
   {:db (reduce utils/add-to-db db sites)}))

(re-frame/reg-event-fx
 ::get-failure
 (fn [{:keys [db]} [_ error]]
   (let [tr (:translator db)]
     {:db       (assoc-in db [:errors :sports-sites (utils/timestamp)] error)
      :dispatch [:lipas.ui.events/set-active-notification
                 {:message  (tr :notifications/get-failed)
                  :success? false}]})))

(re-frame/reg-event-fx
 ::get-by-type-code
 (fn [{:keys [db]} [_ type-code]]
   {:http-xhrio
    {:method          :get
     :uri             (str (:backend-url db) "/sports-sites/type/" type-code)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success      [::get-success]
     :on-failure      [::get-failure]}}))

(re-frame/reg-event-fx
 ::get-success-single
 (fn [{:keys [db]} [_ on-success site]]
   {:db         (utils/add-to-db db site)
    :dispatch-n (or on-success [])}))

(re-frame/reg-event-fx
 ::get
 (fn [{:keys [db]} [_ lipas-id on-success]]
   (let [latest (get-in db [:sports-sites lipas-id :latest])]
     (if latest
       {:dispatch-n (or on-success [])} ;; No need for get if we already have the data
       {:http-xhrio
        {:method          :get
         :uri             (str (:backend-url db) "/sports-sites/" lipas-id)
         :response-format (ajax/json-response-format {:keywords? true})
         :on-success      [::get-success-single on-success]
         :on-failure      [::get-failure]}}))))

(re-frame/reg-event-fx
 ::get-history
 (fn [{:keys [db]} [_ lipas-id]]
   {:http-xhrio
    {:method          :get
     :uri             (str (:backend-url db) "/sports-sites/history/" lipas-id)
     :response-format (ajax/json-response-format {:keywords? true})
     :on-success      [::get-success]
     :on-failure      [::get-failure]}}))

(re-frame/reg-event-fx
 ::download-contacts-report
 (fn [{:keys [db]} [_ data headers]]
   (let [tr     (:translator db)
         config {:filename (tr :reports/contacts)
                 :sheet
                 {:data (utils/->excel-data headers data)}}]
     {:lipas.ui.effects/download-excel! config})))

(re-frame/reg-event-fx
 ::start-adding-new-site
 (fn [{:keys [db]} [_]]
   {:db       (assoc-in db [:new-sports-site :adding?] true)
    :dispatch-n [[:lipas.ui.search.events/clear-filters]
                 [:lipas.ui.search.events/set-results-view :list]]}))

(re-frame/reg-event-db
 ::discard-new-site
 (fn [db [_]]
   (-> db
       (assoc-in [:new-sports-site :adding?] false)
       (assoc-in [:new-sports-site :type] nil)
       (assoc-in [:new-sports-site :data] nil))))

(re-frame/reg-event-db
 ::select-new-site-type
 (fn [db [_ type-code]]
   (assoc-in db [:new-sports-site :type] type-code)))

(re-frame/reg-event-fx
 ::init-new-site
 (fn [{:keys [db]} [_ type-code geoms]]
   ;; We guess city-code based on permissions.
   ;; TODO maybe use geolocation for better guesses
   (let [city-code (-> db :user :login :permissions :cities first)
         data      {:status     "active"
                    :event-date (utils/timestamp)
                    :type       {:type-code type-code}
                    :location   {:geometries geoms
                                 :city       {:city-code city-code}}}]
     {:db (-> db (update-in [:new-sports-site :data] cutils/deep-merge data))})))

(re-frame/reg-event-db
 ::edit-new-site-field
 (fn [db [_ path value]]
   (utils/set-field db (into [:new-sports-site :data] path) value)))

(re-frame/reg-event-db
 ::toggle-delete-dialog
 (fn [db _]
   (update-in db [:sports-sites :delete-dialog :open?] not)))

(re-frame/reg-event-db
 ::select-delete-status
 (fn [db [_ status]]
   (assoc-in db [:sports-sites :delete-dialog :selected-status] status)))

(re-frame/reg-event-db
 ::select-delete-year
 (fn [db [_ year]]
   (assoc-in db [:sports-sites :delete-dialog :selected-year] year)))

(re-frame/reg-event-fx
 ::delete
 (fn [db [_ data status year draft?]]
   (let [event-date (if (utils/this-year? year)
                      (utils/timestamp)
                      (utils/->end-of-year year))
         data       (assoc data :event-date event-date :status status)
         on-success (fn [] [[:lipas.ui.map.events/show-sports-site nil]
                            [:lipas.ui.search.events/submit-search]
                            [::select-delete-status nil]
                            [::select-delete-year utils/this-year]])]
     {:dispatch [::commit-rev data draft? on-success]})))

(re-frame/reg-event-fx
 ::duplicate
 (fn [{:keys [db]} [_ rev]]
   (let [data (merge
               (dissoc rev :lipas-id)
               {:event-date (utils/timestamp)
                :status     "active"})]
     {:db (-> db
              (assoc-in [:new-sports-site :data] data))})))
