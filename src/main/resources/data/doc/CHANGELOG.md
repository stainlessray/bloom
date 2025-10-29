
# Changelog

All notable changes to this project will be documented in this file.

## [v0.1.0] — 2025-10-28
### Added
- Implemented **Bloom Filter Family** with modular, extensible design.
- Introduced three core filters:
  - `ClassicBloomFilter`
  - `CountingBloomFilter`
  - `PartitionedBloomFilter`
- Added interactive console tool `InteractiveBloomDemo`.
- Integrated verbose colorized console output for learning visualization.
- Introduced `FilterMetadata` class for standardized binary persistence.
- Added ingestion pipeline (`ingestlist`) for text → binary filter standardization.
- Enabled resource-based wordlist loading (`src/main/resources/data`).
- Provided base JUnit5 test suite verifying correctness and expected failure modes.
- Established persistent file format (`.bin`) for reproducible experiments.

### Fixed
- Aligned field naming: unified `numHashFunctions` → `hashCount`.
- Corrected partitioned filter delegation logic and error masking in tests.

### Notes
- This version is the **Minimum Viable Product (MVP)** milestone.
- Educational build — not optimized for production.
- Next focus: metadata inspection, Docker packaging, and full README setup.

