(ns lipas.ui.ice-stadiums.views
  (:require [lipas.ui.components :as lui]
            [lipas.ui.energy :as energy]
            [lipas.ui.ice-stadiums.events :as events]
            [lipas.ui.ice-stadiums.rinks :as rinks]
            [lipas.ui.ice-stadiums.subs :as subs]
            [lipas.ui.mui :as mui]
            [lipas.ui.sports-sites.events :as site-events]
            [lipas.ui.sports-sites.subs :as site-subs]
            [lipas.ui.user.subs :as user-subs]
            [lipas.ui.utils :refer [<== ==>]]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(defn toggle-dialog
  ([dialog]
   (toggle-dialog dialog {}))
  ([dialog data]
   (==> [::events/toggle-dialog dialog data])))

(defn set-field
  [lipas-id & args]
  (==> [::site-events/edit-field lipas-id (butlast args) (last args)]))

(defn site-view [{:keys [tr logged-in?]}]
  (let [locale       (tr)
        display-data (<== [::subs/display-site locale])

        lipas-id (:lipas-id display-data)

        edit-data    (<== [::site-subs/editing-rev lipas-id])
        editing?     (<== [::site-subs/editing? lipas-id])
        edits-valid? (<== [::site-subs/edits-valid? lipas-id])

        dialogs               (<== [::subs/dialogs])
        types                 (<== [::subs/types-list])
        size-categories       (<== [::subs/size-categories])
        cities                (<== [::site-subs/cities-list])
        owners                (<== [::site-subs/owners])
        admins                (<== [::site-subs/admins])
        base-floor-structures (<== [::subs/base-floor-structures])
        cets                  (<== [::subs/condensate-energy-targets])
        refrigerants          (<== [::subs/refrigerants])
        refrigerant-solutions (<== [::subs/refrigerant-solutions])
        heat-recovery-types   (<== [::subs/heat-recovery-types])
        dryer-types           (<== [::subs/dryer-types])
        dryer-duty-types      (<== [::subs/dryer-duty-types])
        heat-pump-types       (<== [::subs/heat-pump-types])

        user-can-publish?  (<== [::user-subs/permission-to-publish? lipas-id])
        uncommitted-edits? (<== [::site-subs/uncommitted-edits? lipas-id])

        set-field (partial set-field lipas-id)]

    [lui/full-screen-dialog
     {:open? ((complement empty?) display-data)

      :title (if uncommitted-edits?
               (tr :statuses/edited (-> display-data :name))
               (-> display-data :name))

      :close-label (tr :actions/close)
      :on-close    #(==> [::events/display-site nil])

      :actions (lui/edit-actions-list
                {:uncommitted-edits? uncommitted-edits?
                 :editing?           editing?
                 :valid?             edits-valid?
                 :logged-in?         logged-in?
                 :user-can-publish?  user-can-publish?
                 :on-discard         #(==> [::site-events/discard-edits lipas-id])
                 :discard-tooltip    (tr :actions/discard)
                 :on-edit-start      #(==> [::site-events/edit-site lipas-id])
                 :on-edit-end        #(==> [::site-events/save-edits lipas-id])
                 :edit-tooltip       (tr :actions/edit)
                 :on-save-draft      #(==> [::site-events/commit-draft lipas-id])
                 :save-draft-tooltip (tr :actions/save-draft)
                 :on-publish         #(==> [::site-events/commit-edits lipas-id])
                 :publish-tooltip    (tr :actions/publish)})}

     [mui/grid {:container true}

        ;;; General info
      [lui/form-card {:title (tr :general/general-info)}
       [lui/sports-site-form {:tr              tr
                              :display-data    display-data
                              :edit-data       edit-data
                              :read-only?      (not editing?)
                              :types           types
                              :size-categories size-categories
                              :admins          admins
                              :owners          owners
                              :on-change       set-field}]]

        ;;; Location
      [lui/form-card {:title (tr :lipas.location/headline)}
       [lui/location-form {:tr           tr
                           :read-only?   (not editing?)
                           :cities       cities
                           :edit-data    (:location edit-data)
                           :display-data (:location display-data)
                           :on-change    (partial set-field :location)}]]

        ;;; Building
      (let [on-change    (partial set-field :building)
            edit-data    (:building edit-data)
            display-data (:building display-data)]
        [lui/form-card {:title (tr :lipas.building/headline)}
         [lui/form {:read-only? (not editing?)}

          ;; Main designers
          {:label (tr :lipas.building/main-designers)
           :value (-> display-data :main-designers)
           :form-field
           [lui/text-field
            {:value     (-> edit-data :main-designers)
             :spec      :lipas.building/main-designers
             :on-change #(on-change :main-designers %)}]}

          ;; Total surface area m2
          {:label (tr :lipas.building/total-surface-area-m2)
           :value (-> display-data :total-surface-area-m2)
           :form-field
           [lui/text-field
            {:type      "number"
             :value     (-> edit-data :total-surface-area-m2)
             :spec      :lipas.building/total-surface-area-m2
             :adornment (tr :physical-units/m2)
             :on-change #(on-change :total-surface-area-m2 %)}]}

          ;; Total volume m3
          {:label (tr :lipas.building/total-volume-m3)
           :value (-> display-data :total-volume-m3)
           :form-field
           [lui/text-field
            {:type      "number"
             :value     (-> edit-data :total-volume-m3)
             :spec      :lipas.building/total-volume-m3
             :adornment (tr :physical-units/m3)
             :on-change #(on-change :total-volume-m3 %)}]}

          ;; Total ice area m2
          {:label (tr :lipas.building/total-ice-area-m2)
           :value (-> display-data :total-ice-area-m2)
           :form-field
           [lui/text-field
            {:type      "number"
             :value     (-> edit-data :total-ice-area-m2)
             :spec      :lipas.building/total-ice-area-m2
             :adornment (tr :physical-units/m3)
             :on-change #(on-change :total-ice-area-m2 %)}]}


          {:label (tr :lipas.building/seating-capacity)
           :value (-> display-data :seating-capacity)
           :form-field
           [lui/text-field
            {:type      "number"
             :value     (-> edit-data :seating-capacity)
             :spec      :lipas.building/seating-capacity
             :adornment (tr :units/person)
             :on-change #(on-change :seating-capacity %)}]}]])

        ;;; Envelope structure
      (let [on-change    (partial set-field :envelope)
            display-data (:envelope display-data)
            edit-data    (:envelope edit-data)]
        [lui/form-card {:title (tr :lipas.ice-stadium.envelope/headline)}
         [lui/form {:read-only? (not editing?)}

          ;; Base floor structure
          {:label (tr :lipas.ice-stadium.envelope/base-floor-structure)
           :value (-> display-data :base-floor-structure)
           :form-field
           [lui/select
            {:value     (-> edit-data :base-floor-structure)
             :on-change #(on-change :base-floor-structure %)
             :items     base-floor-structures
             :value-fn  first
             :label-fn  (comp locale second)}]}

          ;; Insulated exterior?
          {:label (tr :lipas.ice-stadium.envelope/insulated-exterior?)
           :value (-> display-data :insulated-exterior?)
           :form-field
           [lui/checkbox
            {:value     (-> edit-data :insulated-exterior?)
             :on-change #(on-change :insulated-exterior? %)}]}

          ;; Insulated ceiling?
          {:label (tr :lipas.ice-stadium.envelope/insulated-ceiling?)
           :value (-> display-data :insulated-ceiling?)
           :form-field
           [lui/checkbox
            {:value     (-> edit-data :insulated-ceiling?)
             :on-change #(on-change :insulated-ceiling? %)}]}

          ;; Low emissivity coating?
          {:label (tr :lipas.ice-stadium.envelope/low-emissivity-coating?)
           :value (-> display-data :low-emissivity-coating?)
           :form-field
           [lui/checkbox
            {:value     (-> edit-data :low-emissivity-coating?)
             :on-change #(on-change :low-emissivity-coating? %)}]}]])

        ;;; Rinks
      [lui/form-card {:title (tr :lipas.ice-stadium.rinks/headline)}

       (when (-> dialogs :rink :open?)
         [rinks/dialog {:tr tr}])

       (if editing?
         [rinks/table {:tr tr :items (-> edit-data :rinks vals)}]
         [rinks/read-only-table {:tr tr :items (-> display-data :rinks)}])]

        ;;; Refrigeration
      (let [on-change    (partial set-field :refrigeration)
            display-data (:refrigeration display-data)
            edit-data    (:refrigeration edit-data)]
        [lui/form-card {:title (tr :lipas.ice-stadium.refrigeration/headline)}
         [lui/form {:read-only? (not editing?)}

          ;; Original?
          {:label (tr :lipas.ice-stadium.refrigeration/original?)
           :value (-> display-data :original?)
           :form-field
           [lui/checkbox
            {:value     (-> edit-data :original?)
             :on-change #(on-change :original? %)}]}

          ;; Individual metering?
          {:label (tr :lipas.ice-stadium.refrigeration/individual-metering?)
           :value (-> display-data :individual-metering?)
           :form-field
           [lui/checkbox
            {:value     (-> edit-data :individual-metering?)
             :on-change #(on-change :individual-metering? %)}]}

          ;; Condensate energy recycling?
          {:label (tr :lipas.ice-stadium.refrigeration/condensate-energy-recycling?)
           :value (-> display-data :condensate-energy-recycling?)
           :form-field
           [lui/checkbox
            {:value     (-> edit-data :condensate-energy-recycling?)
             :on-change #(on-change :condensate-energy-recycling? %)}]}

          ;; Condensate energy main target
          {:label (tr :lipas.ice-stadium.refrigeration/condensate-energy-main-targets)
           :value (-> display-data :condensate-energy-main-targets)
           :form-field
           [lui/multi-select
            {:value     (-> edit-data :condensate-energy-main-targets)
             :items     cets
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :condensate-energy-main-targets %)}]}

          ;; Power kW
          {:label (tr :lipas.ice-stadium.refrigeration/power-kw)
           :value (-> display-data :power-kw)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.refrigeration/power-kw
             :value     (-> edit-data :power-kw)
             :on-change #(on-change :power-kw %)}]}

          ;; Refrigerant
          {:label (tr :lipas.ice-stadium.refrigeration/refrigerant)
           :value (-> display-data :refrigerant)
           :form-field
           [lui/select
            {:value     (-> edit-data :refrigerant)
             :items     refrigerants
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :refrigerant %)}]}

          ;; Refrigerant amount kg
          {:label (tr :lipas.ice-stadium.refrigeration/refrigerant-amount-kg)
           :value (-> display-data :refrigerant-amount-kg)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.refrigeration/refrigerant-amount-kg
             :value     (-> edit-data :refrigerant-amount-kg)
             :on-change #(on-change :refrigerant-amount-kg %)}]}

          ;; Refrigerant solution
          {:label (tr :lipas.ice-stadium.refrigeration/refrigerant-solution)
           :value (-> display-data :refrigerant-solution)
           :form-field
           [lui/select
            {:value     (-> edit-data :refrigerant-solution)
             :items     refrigerant-solutions
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :refrigerant-solution %)}]}

          ;; Refrigerant solution amount l
          {:label (tr :lipas.ice-stadium.refrigeration/refrigerant-solution-amount-l)
           :value (-> display-data :refrigerant-solution-amount-l)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.refrigeration/refrigerant-solution-amount-l
             :value     (-> edit-data :refrigerant-solution-amount-l)
             :on-change #(on-change :refrigerant-solution-amount-l %)}]}]])

        ;;; Ventilation
      (let [on-change    (partial set-field :ventilation)
            edit-data    (:ventilation edit-data)
            display-data (:ventilation display-data)]
        [lui/form-card {:title (tr :lipas.ice-stadium.ventilation/headline)}
         [lui/form {:read-only? (not editing?)}

          ;; Heat recovery type
          {:label (tr :lipas.ice-stadium.ventilation/heat-recovery-type)
           :value (-> display-data :heat-recovery-type)
           :form-field
           [lui/select
            {:value     (-> edit-data :heat-recovery-type)
             :items     heat-recovery-types
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :heat-recovery-type %)}]}

          ;; Heat recovery thermal efficiency percent
          {:label (tr :lipas.ice-stadium.ventilation/heat-recovery-efficiency)
           :value (-> display-data :heat-recovery-efficiency)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.ventilation/heat-recovery-efficiency
             :adornment (tr :units/percent)
             :value     (-> edit-data :heat-recovery-efficiency)
             :on-change #(on-change :heat-recovery-efficiency %)}]}

          ;; Dryer type
          {:label (tr :lipas.ice-stadium.ventilation/dryer-type)
           :value (-> display-data :dryer-type)
           :form-field
           [lui/select
            {:value     (-> edit-data :dryer-type)
             :items     dryer-types
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :dryer-type %)}]}

          ;; Dryer duty type
          {:label (tr :lipas.ice-stadium.ventilation/dryer-duty-type)
           :value (-> display-data :dryer-duty-type)
           :form-field
           [lui/select
            {:value     (-> edit-data :dryer-duty-type)
             :items     dryer-duty-types
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :dryer-duty-type %)}]}

          ;; Heat pump type
          {:label (tr :lipas.ice-stadium.ventilation/heat-pump-type)
           :value (-> display-data :heat-pump-type)
           :form-field
           [lui/select
            {:value     (-> edit-data :heat-pump-type)
             :items     heat-pump-types
             :label-fn  (comp locale second)
             :value-fn  first
             :on-change #(on-change :heat-pump-type %)}]}]])

        ;;; Conditions
      (let [on-change    (partial set-field :conditions)
            display-data (-> display-data :conditions)
            edit-data    (-> edit-data :conditions)]
        [lui/form-card {:title (tr :lipas.ice-stadium.conditions/headline)}
         [lui/form {:read-only? (not editing?)}

          ;; Open months
          {:label (tr :lipas.ice-stadium.conditions/open-months)
           :value (-> display-data :open-months)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/open-months
             :adornment (tr :duration/month)
             :value     (-> edit-data :open-months)
             :on-change #(on-change :open-months %)}]}

          ;; Daily open hours
          {:label (tr :lipas.ice-stadium.conditions/daily-open-hours)
           :value (-> display-data :daily-open-hours)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/daily-open-hours
             :adornment (tr :units/hours-per-day)
             :value     (-> edit-data :daily-open-hours)
             :on-change #(on-change :daily-open-hours %)}]}

          ;; Air humidity min %
          {:label (tr :lipas.ice-stadium.conditions/air-humidity-min)
           :value (-> display-data :air-humidity-min)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/air-humidity-min
             :adornment (tr :units/percent)
             :value     (-> edit-data :air-humidity-min)
             :on-change #(on-change :air-humidity-min %)}]}

          ;; Air humidity max %
          {:label (tr :lipas.ice-stadium.conditions/air-humidity-max)
           :value (-> display-data :air-humidity-max)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/air-humidity-max
             :adornment (tr :units/percent)
             :value     (-> edit-data :air-humidity-max)
             :on-change #(on-change :air-humidity-max %)}]}

          ;; Ice surface temperature c
          {:label (tr :lipas.ice-stadium.conditions/ice-surface-temperature-c)
           :value (-> display-data :ice-surface-temperature-c)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/ice-surface-temperature-c
             :adornment (tr :physical-units/celsius)
             :value     (-> edit-data :ice-surface-temperature-c)
             :on-change #(on-change :ice-surface-temperature-c %)}]}

          ;; Skating area temperature c
          {:label (tr :lipas.ice-stadium.conditions/skating-area-temperature-c)
           :value (-> display-data :skating-area-temperature-c)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/skating-area-temperature-c
             :adornment (tr :physical-units/celsius)
             :value     (-> edit-data :skating-area-temperature-c)
             :on-change #(on-change :skating-area-temperature-c %)}]}

          ;; Stand temperature c
          {:label (tr :lipas.ice-stadium.conditions/stand-temperature-c)
           :value (-> display-data :stand-temperature-c)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/stand-temperature-c
             :adornment (tr :physical-units/celsius)
             :value     (-> edit-data :stand-temperature-c)
             :on-change #(on-change :stand-temperature-c %)}]}

          ;; ;; Daily maintenance count week days
          ;; {:label (tr :lipas.ice-stadium.conditions/daily-maintenances-week-days)
          ;;  :value (-> display-data :daily-maintenances-week-days)
          ;;  :form-field
          ;;  [lui/text-field
          ;;   {:type      "number"
          ;;    :spec      :lipas.ice-stadium.conditions/daily-maintenances-week-days
          ;;    :adornment (tr :units/times-per-day)
          ;;    :value     (-> edit-data :daily-maintenances-week-days)
          ;;    :on-change #(on-change :daily-maintenances-week-days %)}]}

          ;; ;; Daily maintenance count weekends
          ;; {:label (tr :lipas.ice-stadium.conditions/daily-maintenances-weekends)
          ;;  :value (-> display-data :daily-maintenances-weekends)
          ;;  :form-field
          ;;  [lui/text-field
          ;;   {:type      "number"
          ;;    :spec      :lipas.ice-stadium.conditions/daily-maintenances-weekends
          ;;    :adornment (tr :units/times-per-day)
          ;;    :value     (-> edit-data :daily-maintenances-weekends)
          ;;    :on-change #(on-change :daily-maintenances-weekends %)}]}

          ;; Weekly maintenance count
          {:label (tr :lipas.ice-stadium.conditions/weekly-maintenances)
           :value (-> display-data :weekly-maintenances)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/weekly-maintenances
             :adornment (tr :units/times-per-week)
             :value     (-> edit-data :weekly-maintenances)
             :on-change #(on-change :weekly-maintenances %)}]}

          ;; Average water consumption l
          {:label (tr :lipas.ice-stadium.conditions/average-water-consumption-l)
           :value (-> display-data :average-water-consumption-l)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/average-water-consumption-l
             :adornment (tr :physical-units/l)
             :value     (-> edit-data :average-water-consumption-l)
             :on-change #(on-change :average-water-consumption-l %)}]}

          ;; Ice average thickness mm
          {:label (tr :lipas.ice-stadium.conditions/ice-average-thickness-mm)
           :value (-> display-data :ice-average-thickness-mm)
           :form-field
           [lui/text-field
            {:type      "number"
             :spec      :lipas.ice-stadium.conditions/ice-average-thickness-mm
             :adornment (tr :physical-units/mm)
             :value     (-> edit-data :ice-average-thickness-mm)
             :on-change #(on-change :ice-average-thickness-mm %)}]}]])

        ;;; Energy consumption
      [lui/form-card {:title (tr :lipas.energy-consumption/headline)}
       [energy/table {:read-only? true
                      :cold?      true
                      :tr         tr
                      :items      (:energy-consumption display-data)}]]]]))

(defn ice-stadiums-tab [tr logged-in?]
  (let [locale (tr)
        sites  (<== [::subs/sites-list locale])]

    [mui/grid {:container true}

     [site-view {:tr tr :logged-in? logged-in?}]

     [mui/grid {:item true :xs 12}
      [mui/paper
       [lui/table
        {:headers   [[:name (tr :lipas.sports-site/name)]
                     [:city (tr :lipas.location/city)]
                     [:address (tr :lipas.location/address)]
                     [:postal-code (tr :lipas.location/postal-code)]
                     [:admin (tr :lipas.sports-site/admin)]
                     [:owner (tr :lipas.sports-site/owner)]]
         :items     sites
         :on-select #(==> [::events/display-site %])}]]]]))

(defn compare-tab []
  [mui/grid {:container true}
   [mui/grid {:item true :xs 12}
    [:iframe {:src "https://liikuntaportaalit.sportvenue.net/Jaahalli"
              :style {:min-height "800px" :width "100%"}}]]])

(defn energy-info-tab [tr]
  [mui/grid {:container true}
   [mui/grid {:item true :xs 12}
    [mui/typography (tr :ice-energy/description)]]])

(defn energy-form [{:keys [tr year]}]
  (let [data           (<== [::subs/editing-rev])
        energy-history (<== [::subs/energy-consumption-history])
        edits-valid?   (<== [::subs/edits-valid?])
        lipas-id       (:lipas-id data)
        set-field      (partial set-field lipas-id)]

    (r/with-let [monthly-energy? (r/atom false)]

      [mui/grid {:container true}

       ;; Energy consumption
       [lui/form-card {:title (tr :lipas.energy-consumption/headline-year year)}

        [mui/typography {:variant "subheading"
                         :style   {:margin-bottom "1em"}}
         (tr :lipas.energy-consumption/yearly)]
        [energy/form
         {:tr        tr
          :disabled? @monthly-energy?
          :cold?     true
          :data      (:energy-consumption data)
          :on-change (partial set-field :energy-consumption)}]

        [lui/checkbox
         {:label     (tr :lipas.energy-consumption/monthly?)
          :checked   @monthly-energy?
          :on-change #(swap! monthly-energy? not)}]

        (when @monthly-energy?
          [energy/form-monthly
           {:tr        tr
            :cold?     true
            :data      (:energy-consumption-monthly data)
            :on-change #(==> [::events/set-monthly-energy-consumption
                             lipas-id %1 %2 %3])}])

        [lui/expansion-panel {:label (tr :actions/show-all-years)}
         [energy/table {:tr         tr
                        :cold?      true
                        :read-only? true
                        :items      energy-history}]]]

       ;; Actions
       [lui/form-card {}
        [mui/button {:full-width true
                     :disabled   (not edits-valid?)
                     :color      "secondary"
                     :variant    "raised"
                     :on-click   #(==> [::events/commit-energy-consumption data])}
         (tr :actions/save)]]])))

(defn energy-form-tab [tr]
  (let [data   (<== [::subs/sites-to-edit-list])
        site   (<== [::subs/editing-site])
        years  (<== [::subs/energy-consumption-years-list])
        year   (<== [::subs/editing-year])]

    [mui/grid {:container true}

     (when-not data
       [mui/typography "Sinulla ei ole oikeuksia yhteenkään jäähalliin. :/"])

     (when data
       [lui/form-card {:title (tr :actions/select-hall)}
        [mui/form-group
         [lui/select
          {:label     (tr :actions/select-hall)
           :value     (get-in site [:history (:latest site) :lipas-id])
           :items     data
           :label-fn  :name
           :value-fn  :lipas-id
           :on-change #(==> [::events/select-energy-consumption-site
                             {:lipas-id %}])}]]])

     (when site
       [lui/form-card {:title (tr :actions/select-year)}
        [mui/form-group
         [lui/select
          {:label     (tr :actions/select-year)
           :value     year
           :items     years
           :on-change #(==> [::events/select-energy-consumption-year %])}]]])

     (when (and site year)
       [energy-form
        {:tr   tr
         :year year}])]))

(defn create-panel [tr logged-in?]
  (let [active-tab (re-frame/subscribe [::subs/active-tab])
        card-props {:square true}]
    [mui/grid {:container true}

     [mui/grid {:item true :xs 12}
      [mui/card card-props
       [mui/card-content
        [mui/tabs {:scrollable true
                   :full-width true
                   :text-color "secondary"
                   :on-change  #(==> [::events/set-active-tab %2])
                   :value      @active-tab}

         ;; 0 Ice stadiums tab
         [mui/tab {:label (tr :ice-rinks/headline)
                   :icon  (r/as-element [mui/icon "info"])}]

         ;; 1 Energy form tab
         [mui/tab {:label    (tr :ice-basic-data/headline)
                   :icon     (r/as-element [mui/icon "edit"])
                   :disabled (not logged-in?)}]

         ;; 2 Compare tab
         [mui/tab {:label (tr :swim/visualizations)
                   :icon  (r/as-element [mui/icon "compare"])}]

         ;; 3 Energy info tab
         [mui/tab {:label (tr :ice-energy/headline)
                   :icon  (r/as-element [mui/icon "flash_on"])}]]]]]

     [mui/grid {:item true :xs 12}
      (case @active-tab
        0 (ice-stadiums-tab tr logged-in?)
        1 (energy-form-tab tr)
        2 (compare-tab)
        3 (energy-info-tab tr))]]))

(defn main []
  (let [tr         (<== [:lipas.ui.subs/translator])
        logged-in? (<== [:lipas.ui.subs/logged-in?])]
    (re-frame/dispatch [:lipas.ui.sports-sites.events/get-by-type-code 2510])
    (re-frame/dispatch [:lipas.ui.sports-sites.events/get-by-type-code 2520])
    [create-panel tr logged-in?]))
