(ns blueprints.models.address)


;; =============================================================================
;; Selectors
;; =============================================================================


(def locality :address/locality)
(def region :address/region)
(def postal-code :address/postal-code)


;; =====================================
;; Convenience

(def city locality)
(def state region)
(def zip postal-code)
