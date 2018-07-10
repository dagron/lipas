(ns lipas.backend.system
  (:require [environ.core :refer [env]]
            [clojure.pprint :refer [pprint]]
            [integrant.core :as ig]
            [lipas.backend.db.db :as db]
            [lipas.backend.handler :as handler]))

(def default-config
  {:db  {:dbtype   "postgresql"
         :dbname   (:db-name env)
         :host     (:db-host env)
         :user     (:db-user env)
         :port     (:db-port env)
         :password (:db-password env)}
   :app {:db (ig/ref :db)}})

(defmethod ig/init-key :db [_ db-spec]
  (db/->SqlDatabase db-spec))

(defmethod ig/init-key :app [_ config]
  (handler/create-app config))

(defn mask [s]
  "[secret]")

(defn start-system!
  ([]
   (start-system! default-config))
  ([config]
   (let [system (ig/init config)]
     (prn "System started with config:")
     (pprint (-> config
                 (update-in [:db :password] mask)))
     system)))

(defn stop-system! [system]
  (ig/halt! system))
