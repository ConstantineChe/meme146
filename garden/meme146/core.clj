(ns meme146.core
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]))

(def styles
  [[:header {
             :position "fixed"
             :padding-left 0
             :padding-right 0
             :left 0
             :right 0}]
   [:.main-content {:padding-top (px 125)}]]
  )

(defstyles main-styles
  styles)
