---

## 🧭 `README.md`

Save to:
`/projects/bloom/README.md`

```markdown
# 🌸 Bloom Filter Family — Educational & Visual Java Implementation

> **Version:** v0.1.0 (MVP)  
> **Purpose:** A modular, educational Bloom Filter library and interactive console tool designed to visualize, compare, and persist different Bloom Filter variants.

---

## 🧠 Overview

This project provides a **learning-focused**, **extensible** set of Bloom Filter implementations with full visualization and persistence support.  
It allows you to explore the probabilistic behavior of these filters through console interaction, wordlist ingestion, and standardized binary serialization.

---

## ⚙️ Features

| Capability | Description |
|-------------|-------------|
| 🧩 Modular Filters | `ClassicBloomFilter`, `CountingBloomFilter`, `PartitionedBloomFilter` all implement `MembershipFilter<T>`. |
| 🧮 Hashing | MurmurHash3 (x64/128) via `HashUtils` for stable, fast indexing. |
| 🧠 Visualization | Real-time console visualization of bit arrays and counters. |
| 💾 Persistence | Filters can be saved, loaded, or ingested from `.txt` wordlists as standardized `.bin` files. |
| 🧾 Metadata | Each `.bin` file includes `FilterMetadata` (algorithm, source, bit size, hash count, timestamp). |
| 🧑‍💻 Interactive Console | Learn Bloom filters by directly adding, checking, and removing elements. |

---

## 🧩 Project Structure

```

/src
├── main/java/com/bloomfilter/
│    ├── AbstractBloomFilter.java
│    ├── ClassicBloomFilter.java
│    ├── CountingBloomFilter.java
│    ├── PartitionedBloomFilter.java
│    ├── HashUtils.java
│    ├── MembershipFilter.java
│    ├── FilterIO.java
│    ├── FilterMetadata.java
│    └── demo/InteractiveBloomDemo.java
│
└── resources/data/
├── fruit.txt
└── (other wordlists)

````

---

## 🚀 Quick Start

### **Build & Run**
```bash
mvn clean package
java -cp target/classes com.bloomfilter.demo.InteractiveBloomDemo
````

### **Sample Session**

```
=======================================
 BLOOM FILTER INTERACTIVE DEMO 
=======================================

> mode classic
Switched to classic mode.

> ingestlist src/main/resources/data/fruit.txt filters/fruit.bin
Ingesting 15 words from src/main/resources/data/fruit.txt...
[Standardized binary created] /home/ray/projects/bloom/filters/fruit.bin
Algorithm=ClassicBloomFilter | Bits=64 | Hashes=3 | Source=src/main/resources/data/fruit.txt | Created=2025-10-28T17:30:28
Ingestion complete and saved to filters/fruit.bin

Bits: ··█·██████··██···█···█·········█···██···········█·████·
```

---

## 🧰 Commands

| Command                               | Description                                                      |               |                                |
|---------------------------------------|------------------------------------------------------------------|---------------|--------------------------------|
| `add <word>`                          | Inserts a new element.                                           |               |                                |
| `check <word>`                        | Tests for membership.                                            |               |                                |
| `remove <word>`                       | Removes an element (counting mode only).                         |               |                                |
| `clear`                               | Clears all bits/counters.                                        |               |                                |
| `mode <classic counting partitioned>` | Switches between filter types.                                   |               |                                |
| `info`                                | Displays stats and estimated FPR.                                |               |                                |
| `loadlist <file>`                     | Loads a word list (`.txt`) and adds to the active filter.        |               |                                |
| `ingestlist <input.txt> <output.bin>` | Converts a `.txt` list into a standardized `.bin` with metadata. |               |                                |
| `save <file>`                         | Serializes the current filter state to disk.                     |               |                                |
| `load <file>`                         | Loads a previously saved filter.                                 |               |                                |
| `crossload <file>`                    | Swaps the currently active filter’s data with state from disk.   |               |                                |
| `help`                                | Shows command help.                                              |               |                                |
| `exit`                                | Quits the demo.                                                  |               |                                |

---

## 💾 Standardized Binary Format

Each `.bin` file contains:

1. A serialized [`FilterMetadata`](src/main/java/com/bloomfilter/FilterMetadata.java) header:

   ```
   Algorithm=ClassicBloomFilter | Bits=64 | Hashes=3 | Source=data/fruit.txt | Created=2025-10-28T17:30:28
   ```
2. A binary-encoded representation of the filter’s internal state (`BitSet` or counter array).

This format ensures deterministic, comparable runs across filter variants and datasets.

---

## 🧪 Tests

Run JUnit tests:

```bash
mvn test
```

You’ll see console output illustrating the add/check/remove workflow for each filter type, verifying correctness and demonstrating expected FPR behavior.

---

## 🧱 Roadmap

| Milestone        | Description                                            | Status     |
| ---------------- | ------------------------------------------------------ | ---------- |
| **v0.1.0 (MVP)** | Ingestion, persistence, visualization                  | ✅ Complete |
| **v0.2.0**       | `loadmeta` (metadata inspection) and enhanced CLI help | ⏳ Planned  |
| **v0.3.0**       | Docker containerization for sandboxed demo             | ⏳ Planned  |
| **v1.0.0**       | Documentation, optimization, educational release       | ⏳ Future   |

---

## 🧑‍🎓 Educational Use

This project is not optimized for production performance or concurrency.
It is intentionally verbose, instrumented for learning, and safe for experimentation.

---

## 🧭 License

This repository is provided under the MIT License (add `LICENSE` file if not yet present).
You are free to modify and extend it for educational or research purposes.

---

*Built with persistence and curiosity — a demonstration of probabilistic data structures in action.*

````

