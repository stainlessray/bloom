# bloom
### Fun repo for exploring the bloom filter algorithm.

This is a sef directed practical code project focussed on exploring the Bloom filter algorithm and the many claims around their usage from statistical theory to under-the-microscope practice. It was born from some normal online chatter, a recent joke or something which captured my attention. After dropping some references into NotebookLM I was off to the races. The races took me through the evolution of the algorithm and some reading of the math behind it which lead me to think the claims should be tested. This produced the effect of my knowing that I did not yet grasp what I was doing.

#### Here's why: 
I do not have a statistical background, but have some hard won skills with data and statistics. This algorithm makes sense and is elegant as a concept with regard to solutions for high volume comparison operations driven by statistics in constant time O(n). So I was overconfident that I could break it the way described, despite needing an LLM to produce the code for me.

#### What happened:
With the function discussed with said LLM implemented and running it appeared to work well within the expected variance for the hashing function chosen. Multiple LLM's confirmed that it was functioning as expected after some tuning and tweaking we could see good indication it was just working up to n = 1_000_000. The optimal k was 7, and it seemed to distribute the encoded tallies very well when playing with the knobs.

#### And then:
I decided to write some tests in Junit not for the purpose of testing the methods (already did that), but to display the limits of the algorithm. I wanted to devise a clever test which would walk you through the success and then saturation which leads to intra-element hash collisions; a known failure mode of the process as defined. The test could then instruct one to try a partitioned implementation or possibly just proceed into that.

#### BUT:
Bad news, I could not break it. This little ole algo was showing resilience which was statistically impossible given the simple double hashing used in service of expressing the first-tier solution to the collision problem inter-element wise. Where hash collissions occur between two unequal elements. This comes before the saturation problem of exploding false-positives. It just wouldn't break.

#### Alas:
This is obviously going to be way more informative as I flesh out my simplistic understanding. Hence the repo.
