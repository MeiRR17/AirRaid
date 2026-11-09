# Repository code-license decision — pending owner approval

No approved open-source software license has been selected for AirRaid by the repository owner in this development cycle. A repository being publicly readable **does not** itself grant a source-code distribution or derivative-work license.

A file named `LICENSE` has **not** been fabricated or populated with an unapproved copyright holder, year or license. The required owner choice (including permissions from other contributors, if any) is an explicit **release gate**; `tools/release_gate.py --mode release` fails when there is no owner-approved source-code license.

The original 23 graphics, fonts, GIF and related resources have **separate** unresolved redistribution permissions. See `docs/ASSET_LICENSE_AUDIT.md`, `THIRD_PARTY_NOTICES.md` and Issue #9. A future software code license does not automatically cover those media files.

To unblock release: verify code contributors and rights, have the owner choose a license, add an accurate `LICENSE` and provenance/attribution records, and record explicit human release approval. Until then, do not publicly assert production distribution readiness.
