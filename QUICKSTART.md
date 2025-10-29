

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

```
=======================================
 BLOOM FILTER INTERACTIVE DEMO 
=======================================
```

```bash
> mode counting
> ingestlist src/main/resources/data/fruit.txt filters/
> check cherry
Result: cherry → possibly in set
> remove cherry
Result: cherry → definitely not
> save filters/fruit_Counting_m64_k3.bin
```

```bash
> mode partitioned
> ingestlist src/main/resources/data/cities.txt filters/
Partitions:
  P0: █········█
  P1: ·█····█··█
  P2: ···█·····█
  P3: ·····█···█

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


