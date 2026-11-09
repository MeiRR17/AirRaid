# AirRaid beta evidence — no human sessions recorded

This folder is deliberately **empty of completed playtest records**. A real session must be carried out before adding `sessions.json` or `triage.json`; never create invented records to satisfy an automated gate.

When actual human testing has been performed, create `sessions.json` with `{"schema_version":1,"sessions":[...]}` containing records that match [BETA_PLAYTEST_PLAN.md](../BETA_PLAYTEST_PLAN.md). Include neutral session IDs, OS type, number of human participants, completed rounds, scenario booleans, defect issue references, and a non-sensitive evidence reference. Do not store names, personal emails, physical locations, screen recordings, passwords or internal device IDs.

When a human maintainer has reviewed all open issues, create `triage.json` containing `schema_version:1`, `open_p0_p1:0`, `signed_off:true` and a factual review evidence reference. **No human sign-off has occurred in this implementation**.

The gate additionally requires release-rights/approval evidence from the independent source/asset readiness report. Run `python3 tools/beta_gate.py --mode development` to inspect blockers. Do not conflate this with human approval.
