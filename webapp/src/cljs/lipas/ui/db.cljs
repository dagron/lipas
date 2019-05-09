(ns lipas.ui.db
  (:require
   [clojure.spec.alpha :as s]
   [lipas.data.admins :as admins]
   [lipas.data.cities :as cities]
   [lipas.data.ice-stadiums :as ice-stadiums]
   [lipas.data.materials :as materials]
   [lipas.data.owners :as owners]
   [lipas.data.prop-types :as prop-types]
   [lipas.data.sports-sites :as sports-sites]
   [lipas.data.styles :as styles]
   [lipas.data.swimming-pools :as swimming-pools]
   [lipas.data.types :as types]
   [lipas.i18n.core :as i18n]
   [lipas.reports :as reports]
   [lipas.schema.core :as schema]
   [lipas.ui.utils :as utils]))

(def default-db
  {:active-panel :front-page-panel
   :backend-url  "/api"
   :logged-in?   false
   :drawer-open? false
   :translator   (i18n/->tr-fn :fi)

   ;; Admin
   :admin
   {:selected-tab                0
    :magic-link-dialog-open?     false
    :magic-link-variants         [{:value "lipas" :label "Lipas"}
                                  {:value "portal" :label "Portaali"}]
    :selected-magic-link-variant "lipas"
    :color-picker                styles/temp-symbols}

   ;; Sports sites
   :sports-sites {}

   :delete-dialog
   {:open?           false
    :selected-status nil
    :selected-year   utils/this-year}

   :statuses              sports-sites/statuses
   :document-statuses     sports-sites/document-statuses
   :admins                admins/all
   :owners                owners/all
   :cities                cities/by-city-code
   :cities-by-avi-id      cities/by-avi-id
   :cities-by-province-id cities/by-province-id
   :provinces             cities/provinces
   :avi-areas             cities/avi-areas
   :types                 types/all
   :materials             materials/all
   :building-materials    materials/building-materials
   :supporting-structures materials/supporting-structures
   :ceiling-structures    materials/ceiling-structures
   :base-floor-structures materials/base-floor-structures
   :surface-materials     materials/surface-materials
   :prop-types            prop-types/used

   ;; Ice stadiums
   :ice-stadiums
   {:active-tab                0
    :editing                   nil
    :editing?                  false
    :dialogs
    {:rink {:open? false}}
    :size-categories           ice-stadiums/size-categories
    :condensate-energy-targets ice-stadiums/condensate-energy-targets
    :refrigerants              ice-stadiums/refrigerants
    :refrigerant-solutions     ice-stadiums/refrigerant-solutions
    :heat-recovery-types       ice-stadiums/heat-recovery-types
    :dryer-types               ice-stadiums/dryer-types
    :dryer-duty-types          ice-stadiums/dryer-duty-types
    :heat-pump-types           ice-stadiums/heat-pump-types
    :ice-resurfacer-fuels      ice-stadiums/ice-resurfacer-fuels}

   ;; Swimming pools
   :swimming-pools
   {:active-tab        0
    :pool-types        swimming-pools/pool-types
    :sauna-types       swimming-pools/sauna-types
    :filtering-methods swimming-pools/filtering-methods
    :heat-sources      swimming-pools/heat-sources
    :accessibility     swimming-pools/accessibility
    :pool-structures   materials/pool-structures
    :editing           nil
    :editing?          false
    :dialogs
    {:pool   {:open? false}
     :slide  {:open? false}
     :energy {:open? false}
     :sauna  {:open? false}}}

   ;; Energy stats
   :energy-stats
   {:chart-energy-type :energy-mwh}

   ;; User
   :user
   {:login-mode        :password
    :login-form        {}
    :magic-link-form   {}
    :registration-form {}}

   ;; Search
   :search
   {:string       nil
    :filters
    {:statuses   #{"active" "out-of-service-temporarily"}
     :type-codes #{}
     :city-codes #{}}
    :sort
    {:asc?    true
     :sort-fn :score}
    :results-view :list
    :selected-results-table-columns
    [:name :event-date :admin.name :owner.name :type.name :location.city.name]
    :pagination
    {:page       0
     :page-size  250
     :page-sizes [25 50 100 250 500]}}

   ;; Reminders
   :reminders
   {:dialog-open? false
    :form
    {:time "06:00:00.000Z"}}

   ;; Reports
   :reports
   {:dialog-open?    false
    :fields          reports/fields
    :selected-fields (keys reports/default-fields)}

   ;; Stats
   :stats
   {:selected-tab "sport"
    :city
    {:selected-cities #{179}
     :finance
     {:metrics               reports/stats-metrics
      :selected-metrics      ["net-costs" "investments"]
      :city-services         reports/city-services
      :selected-city-service "sports-services"
      :units                 reports/stats-units
      :selected-unit         "1000-euros"
      :selected-years        (range 2000 (dec utils/this-year))
      :selected-view         "chart"}}
    :age-structure
    {:groupings         reports/age-structure-groupings
     :selected-grouping "owner"
     :selected-interval 10
     :selected-view     "chart"}
    :sport
    {:groupings         reports/sports-stats-groupings
     :selected-grouping "location.city.city-code"
     :metrics           reports/sports-stats-metrics
     :selected-metric   "sites-count"
     :selected-view     "chart"}
    :finance
    {:selected-view           "chart"
     :selected-cities         [] ; whole country
     :selected-year           2017
     :units                   reports/stats-units
     :selected-unit           "euros-per-capita"
     :city-services           reports/city-services
     :selected-city-service   "sports-services"
     :groupings               reports/finance-stats-groupings
     :selected-grouping       "avi"
     :metrics                 reports/stats-metrics
     :selected-metrics        ["net-costs" "investments"]
     :selected-ranking-metric "net-costs"
     :chart-type              "comparison"}}

   ;; Map
   :map
   {:drawer-open? true
    :center       {:lon 435047 :lat 7201408}
    :zoom         2
    :mode         {:name :default}
    :basemap      :taustakartta
    :import
    {:dialog-open?      false
     :selected-encoding "ISO-8859-1"
     :selected-items    #{}
     :replace-existing? true}
    :address-search
    {:base-url     "https://api.digitransit.fi/geocoding/v1"
     :dialog-open? false}}})
