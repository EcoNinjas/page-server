(ns econinja.vars
  (:require [environ.core :refer [env]]))

(defonce defaults
  {:page-root "../page"
   :update-script "../update-page.sh"
   :database-url "postgresql://postgres@localhost:5432/oeko"})

(defn get-var [varname]
  (or (env varname) (defaults varname) nil))
