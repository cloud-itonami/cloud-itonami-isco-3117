(ns mining-metallurgy.governor
  "MiningMetallurgyGovernor — the independent safety/traceability layer for the
  ISCO-08 3117 mining and metallurgical technicians field test and inspection
  actor. Wired as its own `:govern` node in `mining-metallurgy.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of project provenance or
  extraction/blasting risk, so this MUST be a separate system able to reject a
  proposal (itonami actor pattern, per ADR-2607011000 / CLAUDE.md Actors section).

  CRITICAL DOMAIN NOTE: This actor supports a MINING/METALLURGICAL TECHNICIAN's
  field data collection, lab testing, and inspection workflow — ore/metal sample
  testing, assay data recording, inspection logging, site visit scheduling, safety
  concern flagging. It does NOT make extraction decisions, authorize blasting, make
  ore-grade assessments for targeting, or set production quotas. Those remain the
  MINING OPERATOR's/SUPERVISOR's exclusive authority. Scope boundaries:
    ✓ Ore/metal sample test and assay data recording
    ✓ Inspection data logging
    ✓ Site visit scheduling
    ✓ Safety concern flagging (always escalated)
    ✗ Extraction/sequencing decisions
    ✗ Blasting authorization
    ✗ Ore-grade assessment for targeting
    ✗ Production targeting/quotas
    ✗ Mine-safety authority determinations

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. project provenance      — the request's project must be registered.
    2. no-actuation            — proposal :effect must be :propose.
    3. no-extraction-authority — any proposal flagged :op with operator-class
                               ops (:extract/:blast/:ore-grade-assess/
                               :set-production-target/:mine-safety-auth/
                               :equipment-sequence) is an instant hard block
                               with no override path.

  ESCALATION invariants (:escalate? true, ALWAYS human sign-off):
    4. :op :flag-safety-hazard — ALL safety concerns escalate.
    5. low confidence          — < `confidence-floor`."
  (:require [mining-metallurgy.store :as store]))

(def confidence-floor 0.6)

;; Ops that are hard-blocked (not in technician's domain; operator/supervisor exclusive)
(def ^:private operator-class-ops
  #{:extract :blast :ore-grade-assess :set-production-target :mine-safety-auth
    :equipment-sequence :ventilation-auth})

;; Ops that always escalate
(def ^:private always-escalate-ops #{:flag-safety-hazard})

(defn- hard-violations [{:keys [proposal]} project-record]
  (cond-> []
    (nil? project-record)
    (conj {:rule :no-project :detail "unregistered project"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect must be :propose only (no direct write)"})

    (contains? operator-class-ops (:op proposal))
    (conj {:rule :operator-class-blocked
           :detail "extraction/blasting/ore-grading/targeting/mine-safety ops are OPERATOR/SUPERVISOR-exclusive; not in technician's scope"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `mining-metallurgy.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [project-record (store/project store (:project-id request))
        hard (hard-violations {:proposal proposal} project-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        safety-op? (contains? always-escalate-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not safety-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? safety-op?))}))
