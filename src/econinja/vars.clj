(ns econinja.vars
  (:require [environ.core :refer [env]]))

(defonce defaults
  {:page-root "../page"
   :update-script "../update-page.sh"
   :database-url {:dbtype "postgresql"
                  :dbname "oeko"
                  :host   "localhost"
                  :user   "postgres"}})

(defn get-var [varname]
  (or (env varname) (defaults varname) nil))
