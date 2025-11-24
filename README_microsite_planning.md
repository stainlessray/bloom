Microsite configuration plan for Evergreen contract

Context (from README/QUICKSTART)
- CLI demo v0.1.1 with three Bloom filter variants (classic, counting, partitioned) and verbose visualization.
- `ingestlist` standardizes word lists to `.bin` with metadata + auto-naming; `loadstd`, `loadmeta`, `crossload` behaviors documented.
- Quickstart flow: `mvn clean package` then `java -cp target/classes com.bloomfilter.demo.InteractiveBloomDemo`; sample sessions for each mode; commands table.
- Roadmap calls out v0.2 (loadmeta/enhanced help) and v0.3 (Docker sandbox) as upcoming.

Decisions to confirm up front
- Microsite root: `public/site/`; host at stainlessray.com (owner stainlessray).
- Slug: `bloom`; Repo: `stainlessray/bloom`.
- Build an `index.html` using `ABOUT_BLOOM_FILTERS.md` + `EDUCATIVE.md` content with good formatting and syntax highlights.

Candidate initial cards (titles/levels to refine)
- v0.1.1 console demo availability and feature set (level `event`/`announcement`; purpose `announcement`).
- Standardized `.bin` ingestion + auto-naming + metadata header (level `process`; purpose `how-to`).
- Interactive commands & sample sessions (level `io`; purpose `how-to`).
- Roadmap highlights (v0.2 loadmeta/enhanced help, v0.3 Docker sandbox) (level `event` or `warn` for upcoming changes).
- Evergreen contract change card: capture the microsite creation (index.html/index.json/details) being implemented now.

Index.html content plan (using ABOUT_BLOOM_FILTERS + EDUCATIVE)
- Hero: title/subtitle for Bloom Filter Lab (educational + interactive demo).
- Section “What is a Bloom Filter?”: key bullets from ABOUT_BLOOM_FILTERS, include attribution, pros/cons, literal view snippet with code block.
- Section “Variants & Tradeoffs”: table/summary (classic, counting, partitioned, scalable, cuckoo) with short blurbs.
- Section “Learn-by-doing”: quickstart commands block (`mvn clean package`, `java -cp ...`), sample CLI interactions, highlight `ingestlist` auto-naming.
- Section “Educational Path”: checkpoints from EDUCATIVE (learning concepts table, what comes next bullets).
- Section “Roadmap”: v0.2/v0.3 bullets.
- Links: to repo, ABOUT_BLOOM_FILTERS.md, EDUCATIVE.md.
- Styling: simple CSS with syntax highlight styles for code blocks; responsive layout.

Evergreen card draft (for current microsite work)
- ID pattern: `bloom-event-2025-11-24-001` (date = today).
- Title: “Microsite & Evergreen Contract live”.
- Level: `event`; Purpose: `announcement`.
- Summary: Published microsite at stainlessray.com/public/site with evergreen index and narrative.
- Source: manual (or commit once available); repo `stainlessray/bloom`.
- Paths: `micro_site_home: "index.html"`, `details_md: "details/bloom-event-2025-11-24-001.md"`.

Execution plan
1) Create `public/site/` scaffold (root + `details/`).
2) Finalize IDs using `<slug>-<level>-<yyyy-mm-dd>-<seq>`; use slug `bloom`, repo `stainlessray/bloom`, base URL stainlessray.com.
3) Draft initial card set, including a card for this microsite launch; pick levels/purposes, dates, summaries, sources (commit/manual).
4) Author `index.html` leveraging ABOUT_BLOOM_FILTERS + EDUCATIVE (formatted sections, code blocks for commands/workflows).
5) Build `index.json` array with required fields and paths; add `paths.micro_site_home` when `index.html` exists; add resources if any.
6) Write narrative markdown for each card in `details/<id>.md` (markdown-only, outbound links ok).
7) Validate against evergreen checklist (JSON parses, files exist, paths match, modal uses markdown only); spot-check URLs under `${BASE_URL}`.
8) Document publish step if needed (how stainlessray.com will serve `public/site/`).
