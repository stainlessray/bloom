

## 🧠 Interactive Bloom Filter Demo (v0.1.1)

This console-based demo allows you to explore **Bloom filter variants** in real time, observe their bit patterns, and persist or reload filter states between sessions.

### 🏫 Build
```bash
mvn clean package
````

### 🎮 Launch

```bash
mvn clean package
java -cp target/classes com.bloomfilter.demo.InteractiveBloomDemo
````

### Play

```
=======================================
 BLOOM FILTER INTERACTIVE DEMO 
=======================================

Switched to classic mode.
>
```

---

### 🧩 Commands Overview

| Command                                  | Description                                                                                   |               |                                       |
|------------------------------------------|-----------------------------------------------------------------------------------------------|---------------|---------------------------------------|
| `mode <classic partitioned counting>`    | Switches the active Bloom filter type                                                         |               |                                       |
| `add <word>`                             | Inserts an element                                                                            |               |                                       |
| `check <word>`                           | Tests membership of an element                                                                |               |                                       |
| `remove <word>`                          | Removes an element *(Counting only)*                                                          |               |                                       |
| `clear`                                  | Resets all bits/counters                                                                      |               |                                       |
| `info`                                   | Shows estimated count and false-positive rate                                                 |               |                                       |
| `ingestlist <input.txt> <outputFolder/>` | Loads a word list, creates and saves a standardized binary                                    |               |                                       |
| `save <file>`                            | Manually saves the active filter state                                                        |               |                                       |
| `load <file>`                            | Loads a legacy `.bin` file saved by `save`                                                    |               |                                       |
| `loadstd <file>`                         | Loads a standardized `.bin` file created by `ingestlist`                                      |               |                                       |
| `loadmeta <file>`                        | Displays metadata (algorithm, bit size, hashes, source, timestamp) without loading            |               |                                       |
| `crossload <file>`                       | Hot-swaps the current in-memory filter with a serialized binary (same configuration required) |               |                                       |
| `help`                                   | Displays available commands                                                                   |               |                                       |
| `exit`                                   | Quits the demo                                                                                |               |                                       |

---

### 🧰 Ingestion and Auto-Naming

The `ingestlist` command standardizes source text data into persistent `.bin` filters.

#### Example

```text
> mode classic
> ingestlist src/main/resources/data/fruit.txt filters/
```

Produces:

```
[Standardized binary created] filters/fruit_Classic_m64_k3_v20251028192638.bin
Algorithm=ClassicBloomFilter | Bits=64 | Hashes=3 | Source=src/main/resources/data/fruit.txt
```

#### Auto-Naming Convention

```
<listName>_<Algorithm>_m<bitArraySize>_k<hashCount>_v<timestamp>.bin
```

Examples:

```
fruit_Classic_m64_k3_v20251028192638.bin
fruit_Counting_m64_k3_v20251028192700.bin
fruit_Partitioned_p4x32_k3_v20251028192812.bin
```

* `m` = total bit array size
* `p#x#` = partitioned filter configuration (partitions × size)
* `k` = number of hash functions
* `v` = creation timestamp (local time, `yyyyMMddHHmmss`)

Each algorithm uses its own storage layout.
✅ **One text file may be ingested for multiple algorithms.**
🚫 **One binary file cannot be used across modes.**

---

### 🧬 Metadata and Standardization

Each standardized binary embeds descriptive metadata in the header:

```
Algorithm=ClassicBloomFilter | Bits=64 | Hashes=3 |
Source=src/main/resources/data/fruit.txt | Created=2025-10-28T19:26:38
```

This enables:

* Self-documenting filters
* Mode validation on load/crossload
* Clean long-term persistence for experimentation

---

### 🧩 Example Workflow

```text
> mode classic
> ingestlist src/main/resources/data/fruit.txt filters/
> loadstd filters/fruit_Classic_m64_k3_v20251028192638.bin
> check apple
Result: apple → possibly in set
> mode counting
> ingestlist src/main/resources/data/fruit.txt filters/
> remove apple
> check apple
Result: apple → definitely not
> save filters/fruit_counting_m64_k3_v20251028192700.bin
```

---

### 🧠 Notes

* Verbose mode is automatically enabled for interactive learning, showing each hash, index, and bit operation.
* All binary filters are portable within this framework version.
* Cross-mode loading gracefully errors if configuration mismatch occurs, preserving the console session.
* Planned enhancements for v0.2:

    * `lsfilters` command to list recent filters
    * optional JSON metadata export
    * Docker sandbox environment for browser-based testing

---

**Bloom Filter Family**
Educational implementation and visualization framework.
Not intended for production — designed for **learning, experimentation, and extension.**


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

bloom_project_extracted/
│
├── README.md
├── how_it_started.md
├── .gitignore
├── pickup-notes-102825.md
├── literal_example.md
├── LICENSE
├── about_bloom_filters.md
├── pom.xml
├── CHANGELOG.md
├── EDUCATIVE.md
├── QUICKSTART.md
│
├── .git/
│   ├── config
│   ├── HEAD
│   ├── index
│   ├── refs/
│   ├── objects/
│   ├── hooks/
│   ├── logs/
│   └── branches/
│
├── .idea/
│   ├── compiler.xml
│   ├── encodings.xml
│   ├── misc.xml
│   ├── modules.xml
│   ├── vcs.xml
│   ├── workspace.xml
│   └── dictionaries/project.xml
│
├── src/
│   ├── main/
│   │   ├── java/com/bloomfilter/
│   │   │   ├── AbstractBloomFilter.java
│   │   │   ├── ClassicBloomFilter.java
│   │   │   ├── CountingBloomFilter.java
│   │   │   ├── FilterIO.java
│   │   │   ├── FilterMetadata.java
│   │   │   ├── HashUtils.java
│   │   │   ├── MembershipFilter.java
│   │   │   ├── PartitionedBloomFilter.java
│   │   │   └── demo/
│   │   │       ├── InteractiveBloomDemo.java
│   │   │       └── MasterVisualDemo.java
│   │   │
│   │   └── resources/
│   │       ├── data/
│   │       │   ├── animals.txt
│   │       │   ├── cities.txt
│   │       │   ├── fruit.txt
│   │       │   ├── random.txt
│   │       │   ├── uuids.txt
│   │       │   └── doc/
│   │       │       ├── ABOUT_BLOOM_FILTERS.md
│   │       │       ├── CHANGELOG.md
│   │       │       └── PROJECT_ORIGIN.md
│   │
│   └── test/
│       └── java/com/bloomfilter/
│           ├── BloomFilterTest.java
│           ├── ClassicBloomFilterTest.java
│           ├── CountingBloomFilterTest.java
│           ├── HashUtilsTest.java
│           └── PartitionedBloomFilterTest.java
│
├── target/
│   ├── bloom-0.1-SNAPSHOT.jar
│   ├── classes/...
│   ├── test-classes/...
│   ├── surefire-reports/ (JUnit results)
│   ├── generated-sources/
│   ├── generated-test-sources/
│   └── maven-status/
│
└── filters/
    ├── animals_ClassicBloomFilter_m64_k3_v20251028210153.bin
    ├── animals_CountingBloomFilter_m64_k3_v20251028210218.bin
    ├── animals_PartitionedBloomFilter_p4x32_k3_v20251028210240.bin
    ├── cities_ClassicBloomFilter_m64_k3_v20251028210444.bin
    ├── cities_CountingBloomFilter_m64_k3_v20251028210459.bin
    ├── cities_PartitionedBloomFilter_p4x32_k3_v20251028210507.bin
    ├── fruit_ClassicBloomFilter_m64_k3_v20251028205146.bin
    ├── fruit_CountingBloomFilter_m64_k3_v20251028202122.bin
    └── fruit_PartitionedBloomFilter_p4x32_k3_v20251028205314.bin


````


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

*Built with persistence and curiosity. Probabilistic data structures in action.*

````

