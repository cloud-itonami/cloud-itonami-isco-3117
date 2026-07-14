(ns mining-metallurgy.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [mining-metallurgy.actor :as actor]
            [mining-metallurgy.store :as store]
            [mining-metallurgy.advisor :as advisor]))

(deftest graph-build
  (testing "graph builds successfully with defaults"
    (let [s (store/mem-store)
          g (actor/build-graph {:store s})]
      (is (not (nil? g))))))

(deftest run-request-ok
  (testing "run-request! completes successfully for allowed proposal"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          g (actor/build-graph {:store s})
          req {:project-id "proj1" :op :draft-test-record :stake :high}
          result (actor/run-request! g req nil "thread1")]
      (is (= :done (:status result)))
      (let [final-state (:state result)]
        (is (not (nil? (:record final-state)))))))

  (testing "record is committed to store"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          g (actor/build-graph {:store s})
          req {:project-id "proj1" :op :draft-test-record :stake :high}
          _ (actor/run-request! g req nil "thread1")
          records (store/records-of s "proj1")]
      (is (= 1 (count records)))
      (is (= :draft-test-record (:op (first records)))))))

(deftest run-request-hard-hold
  (testing "hard violation holds without commit"
    (let [s (store/mem-store)
          g (actor/build-graph {:store s})
          req {:project-id "unknown" :op :draft-test-record}
          result (actor/run-request! g req nil "thread2")]
      (is (= :done (:status result)))
      (let [records (store/records-of s "unknown")]
        (is (= 0 (count records)))))))

(deftest run-request-escalation
  (testing "escalation interrupts before commit"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          g (actor/build-graph {:store s})
          req {:project-id "proj1" :op :flag-safety-hazard}
          result (actor/run-request! g req nil "thread3")]
      (is (= :interrupted (:status result)))))

  (testing "approve! resumes and commits"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          g (actor/build-graph {:store s})
          req {:project-id "proj1" :op :flag-safety-hazard}
          _ (actor/run-request! g req nil "thread4")
          result (actor/approve! g "thread4")]
      (is (= :done (:status result)))
      (let [records (store/records-of s "proj1")]
        (is (= 1 (count records)))))))

(deftest audit-ledger
  (testing "all operations are logged to ledger"
    (let [s (store/mem-store)
          _ (store/register-project! s {:project-id "proj1" :name "Test Site"})
          g (actor/build-graph {:store s})
          req {:project-id "proj1" :op :draft-test-record :stake :high}
          _ (actor/run-request! g req nil "thread5")
          ledger (store/ledger s)]
      (is (> (count ledger) 0))
      (is (some #(= :commit (:disposition %)) ledger)))))
