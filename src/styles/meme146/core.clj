(ns meme146.core
  (:require [garden.def :refer [defstylesheet defstyles]
             [garden.units :refer [px]]]))

(defstyles main
  [:header
   {:position: "fixed"
    :left 0
    :right 0
    :padding-left 0
    :padding-right 0}]
  [:main-content
   {:padding-top (px 125)}])
