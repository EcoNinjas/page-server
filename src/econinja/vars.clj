(ns econinja.vars
  (:require [environ.core :refer [env]]))

(defonce defaults
  {:port 8080
   :page-root "../page"
   :update-script "../update-page"})

(defn get-var [varname]
  (or (env varname) (defaults varname) nil))
