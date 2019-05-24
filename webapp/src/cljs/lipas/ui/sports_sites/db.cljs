(ns lipas.ui.sports-sites.db
  (:require
   [lipas.data.admins :as admins]
   [lipas.data.materials :as materials]
   [lipas.data.owners :as owners]
   [lipas.data.prop-types :as prop-types]
   [lipas.data.sports-sites :as sports-sites]
   [lipas.data.types :as types]
   [lipas.ui.utils :as utils]))

(def default-db
  {:statuses          sports-sites/statuses
   :document-statuses sports-sites/document-statuses ;; unused?

   :admins admins/all
   :owners owners/all
   :types  types/all

   :prop-types prop-types/used

   :materials             materials/all
   :building-materials    materials/building-materials
   :supporting-structures materials/supporting-structures
   :ceiling-structures    materials/ceiling-structures
   :base-floor-structures materials/base-floor-structures
   :surface-materials     materials/surface-materials

   :delete-dialog
   {:open?           false
    :selected-status nil
    :selected-year   utils/this-year}})