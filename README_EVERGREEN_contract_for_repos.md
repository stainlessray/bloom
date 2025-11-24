# Evergreen Microsite Contract for Tracked Repos
# V1

Copy this file to the root of any repo that should surface updates in the raycool.dev card stream (or downstream viewers using the same contract). It defines the filesystem layout, JSON contract, and migration steps so a human or AI agent can build or upgrade the microsite in one pass.

---

## TL;DR (New Microsite)
1. Create a static folder served as the microsite root (e.g., `/public/site/` or your Pages/GitHub Pages root).
2. Add `index.json` (array) following the card contract below.
3. Add one markdown file per card at `details/<id>.md` (narrative only).
4. If you have HTML views, keep `index.html` as the microsite home and place any other `.html` in the same directory; they will auto-surface as resources.
5. Deploy so that `${BASE_URL}/index.json` and `${BASE_URL}/details/<id>.md` are publicly reachable.

---

## Card Contract (recap)
Each entry in `index.json` is a card object:

```jsonc
{
  "id": "img2bio-alert-2025-11-23-001",   // unique per card (slug-level-date-seq)
  "slug": "img2bio",                      // channel/repo identity
  "title": "New Late-Fusion Model Deployed",
  "level": "alert",                       // alert | io | warn | event | process | all
  "purpose": "announcement",              // semantic intent
  "summary": "Late-fusion RidgeCV model is now live with improved weighted R².",
  "date": "2025-11-23",
  "repo": "littlelectron/img2bio",
  "source": {
    "type": "commit",
    "ref": "a1b2c3d",
    "label": "feat: add late fusion ridge model",
    "url": "https://github.com/littlelectron/img2bio/commit/a1b2c3d"
  },
  "paths": {
    "micro_site_home": "index.html",
    "resources": ["view.html", "eda.html"],
    "details_md": "details/img2bio-alert-2025-11-23-001.md"
  },
  "meta": {
    "tags": ["ml", "kaggle", "fusion"],
    "priority": 10,
    "version": "1.0"
  }
}
```

**Required fields:** `id`, `slug`, `title`, `level`, `purpose`, `summary`, `date`, `repo`, `paths.details_md`.  
**Optional but recommended:** `source`, `paths.micro_site_home`, `paths.resources[]`, `meta`.

### ID Construction
`<slug>-<level>-<yyyy-mm-dd>-<seq>` (zero-padded seq, e.g., `001`). IDs are the primary key and the filename for details markdown.

---

## Paths Contract
- `paths.details_md` (required) → markdown narrative for the modal body; located at `details/<id>.md`.
- `paths.micro_site_home` (optional) → `index.html` when present in the microsite root.
- `paths.resources` (optional) → array of other `.html` files in the microsite root. Labels are inferred from filenames:
  - strip extension
  - replace `-`/`_` with spaces
  - capitalize first letter
- Never point modal content at HTML; modal always renders markdown, then shows links to microsite pages.

---

## Directory Layout (example)
```
<microsite-root>/
├─ index.json
├─ index.html              # optional microsite home
├─ view.html               # optional resource (auto-labeled "View")
├─ eda.html                # optional resource (auto-labeled "Eda")
└─ details/
   └─ <id>.md              # one per card
```

---

## Content Rules
- `details/<id>.md` contains only narrative content (no JSON metadata duplication). Markdown links should be standard `[label](https://url)`; they will render with `target="_blank"` and `rel="noopener noreferrer"`.
- Keep `summary` short; keep the full story in markdown.
- `source` is for traceability (commit/PR/manual/etc.).
- Use lowercase, hyphenated `slug`; keep it stable across cards.

### Optional host-side defaults
- Aggregators (e.g., raycool.dev) may inject project-level defaults for stable fields like `slug`, `repo`, `paths.micro_site_home`, `paths.resources`, or `source.url`.  
- To stay portable across hosts, still include these fields in your cards. Defaults only fill missing values—they never override what you specify.

---

## Migration (Legacy → Evergreen)
If you have an older contract (single object, slug-based IDs, HTML overrides):
1. **Enumerate cards** and assign evergreen IDs.
2. **Restructure `index.json`** to an array of card objects with the fields above.
3. **Rename detail files** to `details/<id>.md`; remove duplicated metadata inside markdown.
4. **Update paths**:
   - Set `micro_site_home: "index.html"` if present.
   - Collect other `.html` files in the microsite root into `paths.resources[]`.
   - Ensure `paths.details_md` matches the renamed markdown file.
5. **Remove HTML overrides** from the contract; modal content should come from markdown only.
6. **Validate** with the checklist below.

---

## Validation Checklist
- `index.json` parses as a JSON array; every card has required fields and `paths.details_md`.
- Every `paths.details_md` file exists at `details/<id>.md`.
- If `index.html` exists, `paths.micro_site_home` is set; other `.html` files appear in `paths.resources`.
- Links in markdown render correctly; modal shows markdown first, then Microsite Home / Related Resources links.
- No iframe or HTML overrides in modal content.
- Base URL serves `index.json` and all detail markdown files (suitable for Cloudflare Pages/GitHub Pages/static hosting).

---

## Ready-to-Copy Summary
- Produce `index.json` (array of cards) at the microsite root.
- Place per-card markdown at `details/<id>.md`.
- Keep `index.html` as the microsite home if present; other `.html` files are resources.
- Modal rendering is markdown-first; HTML pages are linked, not embedded.
