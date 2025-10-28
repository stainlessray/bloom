# Bloom Filter 
### Intent
At a high level:
A <em>Bloom filter</em> is a <em>probabilistic data structure</em> used to test whether an element <em>is</em> in a set — with <em>no false negatives</em> (if it says “no,” the item’s definitely not there) but <em>possible false positives</em> (it might say “yes” when the item isn’t actually there).

#### Important concept: 
The "Set" here is not the elements themselves, but a probabilistic representation of one.

#### Simplified Intent:
It's a filter with zero chance of false negative which is used on numbers or strings (highly generalized) with low time complexity O(n).

### Popular use cases for bloom filters
Optimizing database lookups, preventing users from seeing duplicate ads or content recommendations, filtering malicious URLs, and quickly checking for username or password availability. They are especially useful in systems with large datasets where a probabilistic "may be in the set" check can avoid expensive, slow lookups of the actual data. 
