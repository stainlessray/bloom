# Literal View

Say your list (the *real* set) is:

```text
{ apple, banana, cherry }
````

To prepare a Bloom filter for membership checking, you’d:

1. **Decide on structure parameters.**

   * Let’s say your bit array has **10 bits** (indexes 0–9).
   * You’ll use **3 hash functions** (`h1`, `h2`, `h3`).

2. **Start with all bits = 0.**

   ```
   0 0 0 0 0 0 0 0 0 0
   ```

3. **Insert each fruit:**

   * For `apple`, compute three hash values:
     `h1(apple)=2`, `h2(apple)=4`, `h3(apple)=8`
     → set bits 2, 4, 8.

     ```
     0 0 1 0 1 0 0 0 1 0
     ```

   * For `banana`:
     `h1(banana)=1`, `h2(banana)=4`, `h3(banana)=7`
     → set bits 1, 4, 7.

     ```
     0 1 1 0 1 0 0 1 1 0
     ```

   * For `cherry`:
     `h1(cherry)=3`, `h2(cherry)=6`, `h3(cherry)=9`
     → set bits 3, 6, 9.

     ```
     0 1 1 1 1 0 1 1 1 1
     ```

Now that’s your **Bloom filter** — a 10-bit array encoding your fruit “set.”
It doesn’t contain the words *apple*, *banana*, or *cherry*; it just knows that those words light up certain positions.

To **check** if “banana” might be in the set, you hash it again, check bits (1, 4, 7).
All are 1 → *probably yes*.
To check “grape”:
Say its hashes are (2, 5, 7). Bit 5 = 0 → *definitely not*.

Would you like me to extend this example to show where a false positive could appear?


