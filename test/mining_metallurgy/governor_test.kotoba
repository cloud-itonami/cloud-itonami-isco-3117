(ns mining-metallurgy.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [mining-metallurgy.governor :as gov]
            [mining-metallurgy.store :as store]))

(deftest hard-invariants
  (testing "no-project: unregistered project returns hard violation"
    (let [s (store/mem-store)
          req {:project-id "unknown-proj"}
          prop {:op :draft-test-record :effect :propose}
          result (gov/check req nil prop s)]
      (is (:hard? result))
      (is (some #(= :no-project (:rule %)) (:violations result)))))

  (testing "no-actuation: non-:propose effect returns hard violation"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :draft-test-record :effect :commit}
          result (gov/check req nil prop s)]
      (is (:hard? result))
      (is (some #(= :no-actuation (:rule %)) (:violations result)))))

  (testing "operator-class-blocked: :extract is hard-blocked"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :extract :effect :propose}
          result (gov/check req nil prop s)]
      (is (:hard? result))
      (is (some #(= :operator-class-blocked (:rule %)) (:violations result)))))

  (testing "operator-class-blocked: :blast is hard-blocked"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :blast :effect :propose}
          result (gov/check req nil prop s)]
      (is (:hard? result))
      (is (some #(= :operator-class-blocked (:rule %)) (:violations result)))))

  (testing "operator-class-blocked: :ore-grade-assess is hard-blocked"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :ore-grade-assess :effect :propose}
          result (gov/check req nil prop s)]
      (is (:hard? result))
      (is (some #(= :operator-class-blocked (:rule %)) (:violations result))))))

(deftest escalation-invariants
  (testing "flag-safety-hazard always escalates"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :flag-safety-hazard :effect :propose :confidence 0.95}
          result (gov/check req nil prop s)]
      (is (:escalate? result))
      (is (not (:hard? result)))))

  (testing "low confidence escalates"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :draft-test-record :effect :propose :confidence 0.5}
          result (gov/check req nil prop s)]
      (is (:escalate? result))
      (is (not (:hard? result))))))

(deftest ok-case
  (testing "valid proposal with high confidence and allowed op succeeds"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          req {:project-id "proj1"}
          prop {:op :draft-test-record :effect :propose :confidence 0.8}
          result (gov/check req nil prop s)]
      (is (:ok? result))
      (is (not (:hard? result)))
      (is (not (:escalate? result))))))
