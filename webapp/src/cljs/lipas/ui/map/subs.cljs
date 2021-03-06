(ns lipas.ui.map.subs
  (:require
   [goog.array :as garray]
   [goog.object :as gobj]
   [re-frame.core :as re-frame]
   [reagent.ratom :as ratom]))

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
    (let [lipas-id (-> @app-db :map :mode :lipas-id)]
      (when lipas-id
        {:display-data @(re-frame/subscribe
                         [:lipas.ui.sports-sites.subs/display-site lipas-id])
         :edit-data    @(re-frame/subscribe
                         [:lipas.ui.sports-sites.subs/editing-rev lipas-id])})))))

(re-frame/reg-sub
 ::geometries-fast
 :<- [:lipas.ui.search.subs/search-results-fast]
 :<- [::editing-lipas-id]
 (fn [[results lipas-id'] _]
   (when results
     (let [data (or (gobj/getValueByKeys results "hits" "hits") #js[])]
       (->> data
            (keep
             (fn [obj]
               (let [obj       (gobj/get obj "_source")
                     geoms     (or
                                ;; Full geoms
                                (gobj/getValueByKeys obj
                                                     "location"
                                                     "geometries"
                                                     "features")
                                ;; Simplified geoms
                                (gobj/getValueByKeys obj
                                                     "search-meta"
                                                     "location"
                                                     "simple-geoms"
                                                     "features"))
                     type-code (gobj/getValueByKeys obj "type" "type-code")
                     lipas-id  (gobj/get obj "lipas-id")
                     name      (gobj/get obj "name")]

                 ;; To avoid displaying duplicates when editing
                 (when-not (= lipas-id' lipas-id)
                   #js{:type     "FeatureCollection"
                       :features (garray/map
                                  geoms
                                  (fn [geom idx]
                                    (gobj/set geom "id" (str lipas-id "-" idx))
                                    (gobj/set geom
                                              "properties"
                                              #js{:lipas-id  lipas-id
                                                  :name      name
                                                  :type-code type-code})
                                    geom))}))))
            not-empty)))))

(re-frame/reg-sub
 ::content-padding
 :<- [:lipas.ui.subs/screen-size]
 :<- [::drawer-open?]
 (fn [[screen-size drawer-open?] _]
   (let [margin 20]
     (if (and (#{"xs sm"} screen-size) (not drawer-open?))
       [margin margin margin margin]
       [margin margin margin (+ margin 430)])))) ;; drawer width is 430px

(re-frame/reg-sub
 ::mode*
 (fn [db _]
   (-> db :map :mode)))

(re-frame/reg-sub
 ::mode
 :<- [::content-padding]
 :<- [::mode*]
 (fn [[content-padding mode] _]
   (assoc mode :content-padding content-padding)))

(re-frame/reg-sub
 ::editing-lipas-id
 (fn [db _]
   (when (#{:editing :drawing} (-> db :map :mode :name))
     (-> db :map :mode :lipas-id))))

(re-frame/reg-sub
 ::zoomed-for-drawing?
 :<- [::zoom]
 (fn [zoom _]
   (> zoom 12)))

(re-frame/reg-sub
 ::drawing-geom-type
 (fn [db _]
   (-> db :map :drawing :geom-type)))

(re-frame/reg-sub
 ::new-geom
 (fn [db _]
   (when (= :adding (-> db :map :mode :name))
     (-> db :map :mode :geom))))

(re-frame/reg-sub
 ::drawer-open?
 (fn [db _]
   (-> db :map :drawer-open?)))


;; Import geoms ;;

(re-frame/reg-sub
 ::import-dialog-open?
 (fn [db _]
   (-> db :map :import :dialog-open?)))

(re-frame/reg-sub
 ::selected-import-file-encoding
 (fn [db _]
   (-> db :map :import :selected-encoding)))

(re-frame/reg-sub
 ::import-data
 (fn [db _]
   (-> db :map :import :data)))

(re-frame/reg-sub
 ::import-candidates
 :<- [::import-data]
 (fn [data _]
   data))

(def ignored-headers #{"id" "coordTimes"})

(re-frame/reg-sub
 ::import-candidates-headers
 :<- [::import-data]
 (fn [data _]
   (let [->entry (fn [s]
                   (assoc {} :label s :hidden? (contains? ignored-headers s)))]
     (when (not-empty data)
       (-> data keys first data :properties
           (->> (mapv (juxt first (comp ->entry name first)))))))))

(re-frame/reg-sub
 ::import-batch-id
 (fn [db _]
   (-> db :map :import :batch-id)))

(re-frame/reg-sub
 ::selected-import-items
 (fn [db _]
   (-> db :map :import :selected-items)))

(re-frame/reg-sub
 ::replace-existing-geoms?
 (fn [db _]
   (-> db :map :import :replace-existing?)))

(re-frame/reg-sub
 ::address-search-dialog-open?
 (fn [db _]
   (-> db :map :address-search :dialog-open?)))

(re-frame/reg-sub
 ::address-search-keyword
 (fn [db _]
   (-> db :map :address-search :keyword)))

(defn ->result [f]
  {:geometry (-> f :geometry)
   :label    (-> f :properties :label)})

(re-frame/reg-sub
 ::address-search-results
 (fn [db _]
   (-> db :map :address-search :results :features
       (->> (map ->result)))))

(re-frame/reg-sub
 ::sports-site-view
 (fn [[_ lipas-id type-code] _]
   [(re-frame/subscribe [:lipas.ui.user.subs/permission-to-cities])
    (re-frame/subscribe [:lipas.ui.user.subs/permission-to-types])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/geom-type lipas-id])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/admins])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/owners])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/editing? lipas-id])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/edits-valid? lipas-id])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/editing-allowed? lipas-id])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/delete-dialog-open? lipas-id])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/type-by-type-code type-code])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/types-props type-code])
    (re-frame/subscribe [:lipas.ui.user.subs/permission-to-publish? lipas-id])
    (re-frame/subscribe [:lipas.ui.user.subs/logged-in?])
    (re-frame/subscribe [:lipas.ui.ice-stadiums.subs/size-categories])
    (re-frame/subscribe [::mode])])
 (fn [[cities types geom-type admins owners editing? edits-valid?
       editing-allowed?  delete-dialog-open? type types-props
       can-publish? logged-in?  size-categories mode] _]
   {:types               (filter (comp #{geom-type} :geometry-type second) types)
    :cities              cities
    :admins              admins
    :owners              owners
    :editing?            editing?
    :edits-valid?        edits-valid?
    :editing-allowed?    editing-allowed?
    :delete-dialog-open? delete-dialog-open?
    :can-publish?        can-publish?
    :logged-in?          logged-in?
    :size-categories     size-categories
    :mode                mode
    :sub-mode            (:sub-mode mode)
    :type                type
    :types-props         types-props
    :geom-type           (:geometry-type type)
    :problems?           (-> mode :problems :data :features seq)
    :portal              (case (:type-code type)
                           (3110 3130) "uimahalliportaali"
                           (2510 2520) "jaahalliportaali"
                           nil)}))

(re-frame/reg-sub
 ::add-sports-site-view
 (fn [_]
   [(re-frame/subscribe [:lipas.ui.sports-sites.subs/new-site-type])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/new-site-data])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/new-site-valid?])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/admins])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/owners])
    (re-frame/subscribe [:lipas.ui.user.subs/permission-to-cities])
    (re-frame/subscribe [:lipas.ui.user.subs/permission-to-types])
    (re-frame/subscribe [:lipas.ui.sports-sites.subs/prop-types])
    (re-frame/subscribe [:lipas.ui.ice-stadiums.subs/size-categories])
    (re-frame/subscribe [::zoomed-for-drawing?])
    (re-frame/subscribe [::new-geom])
    (re-frame/subscribe [::mode])])
 (fn [[type data valid? admins owners cities types prop-types
       size-categories zoomed? geom mode] _]
   (let [geom-type (-> geom :features first :geometry :type)
         sub-mode  (mode :sub-mode)]
     {:type            type
      :type-code       (:type-code type)
      :geom-type       (:geometry-type type)
      :data            data
      :save-enabled?   (and valid? (= :finished sub-mode))
      :admins          admins
      :owners          owners
      :cities          cities
      :types           (if geom-type
                         (filter (comp #{geom-type} :geometry-type second) types)
                         types)
      :types-props     (reduce (fn [res [k v]]
                                 (let [prop-type (prop-types k)]
                                   (assoc res k (merge prop-type v))))
                               {}
                               (:props type))
      :size-categories size-categories
      :zoomed?         zoomed?
      :geom            geom
      :problems?       (-> mode :problems :data :features seq)
      :sub-mode        sub-mode
      :active-step     (cond
                         (= :finished sub-mode) 2
                         (some? type)           1
                         :else                  0)})))
