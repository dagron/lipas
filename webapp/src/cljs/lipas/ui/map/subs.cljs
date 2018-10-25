(ns lipas.ui.map.subs
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :as ratom]))

(defn ->feature [{:keys [lipas-id name] :as site}]
  (-> site
      :location
      :geometries
      (update-in [:features]
                 #(map-indexed (fn [idx f]
                         (-> f
                             (assoc-in [:id] (str lipas-id "-" idx))
                             (assoc-in [:properties :name] name)
                             (assoc-in [:properties :lipas-id] lipas-id)))
                       %))))

(re-frame/reg-sub
 ::filters
 (fn [db _]
   (-> db :map :filters)))

(re-frame/reg-sub
 ::basemap
 (fn [db _]
   (-> db :map :basemap)))

(re-frame/reg-sub
 ::center
 (fn [db _]
   (-> db :map :center)))

(re-frame/reg-sub
 ::zoom
 (fn [db _]
   (-> db :map :zoom)))

(re-frame/reg-sub
 ::popup
 (fn [db _]
   (-> db :map :popup)))

(re-frame/reg-sub-raw
 ::selected-sports-site
 (fn [app-db event]
   (ratom/reaction
    (let [lipas-id (-> @app-db :map :sports-site)]
      (when lipas-id
        {:display-data @(re-frame/subscribe
                         [:lipas.ui.sports-sites.subs/display-site lipas-id])
         :edit-data    @(re-frame/subscribe
                         [:lipas.ui.sports-sites.subs/editing-rev lipas-id])})))))

(re-frame/reg-sub
 ::geometries
 :<- [:lipas.ui.sports-sites.subs/latest-sports-site-revs]
 :<- [::filters]
 (fn [[sites filters] _]
   (let [type-codes (cond-> #{}
                      (:ice-stadium filters) (into #{2510 2520})
                      (:swimming-pool filters) (into #{3110 3130}))]
     (->> sites
          vals
          (filter (comp type-codes :type-code :type))
          (map ->feature)
          not-empty))))

(re-frame/reg-sub
 ::editing?
 (fn [db _]
   (-> db :map :editing :lipas-id some?)))

(re-frame/reg-sub
 ::selected-types
 (fn [db _]
   (-> db :map :filters :types set)))

(defn ->type-list-entry [locale selected-types m]
  (let [type-code (-> m :type-code)]
    {:type-code type-code
     :name      (-> m :name locale)
     :selected? (contains? selected-types type-code)}))

(re-frame/reg-sub
 ::types-list
 :<- [:lipas.ui.sports-sites.subs/types-list]
 :<- [::selected-types]
 (fn [[types selected-types] [_ locale]]
   (map (partial ->type-list-entry locale selected-types) types)))
