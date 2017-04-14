## Exercise in Neural Networks and Reinforcement Learning for the Platformer Game
### Pt1. Neural Network
#### Introduction
In this repository, we see two different configurations of neural networks, differing in inputs and hidden layers, were run with varying population sizes, mutation rates, sigma, and crossover rates to examine the impact of these variables on the speed at which a neural network improves, and the ultimate skill ceiling reached by this neural network.

#### Selection, Breeding, and Mutation
In each generation of size *c*, the *p* parents of highest average fitness across all seeds are selected for reproduction. Two of these parents are selected at random for the production of each of the *c* children in the next generation, with a crossover rate of 30%. At each reproduction, there is a 10% chance that the parent will, instead of a randomly selected member of the group *p*, be instead the fittest parent to date.

In earlier versions of this program, strict elitism led to long periods of stagnation, whilst forgetting all ancestors at each generation could lead to significant valleys in overall performance. Preserving the fittest ancestor mitigates the depth of these valleys without sacrificing overall genetic variety.

Each time a child is produced, each weight is modified by a random number within the normal distribution N(0, σ). If the average of the current generation surpasses that of the previous, σ is decreased, whilst σ is increased if the current generation is equal to or worse than the previous generation. To prevent the mutation from becoming too extreme, σ is reset to its initial value if it exceeds two times the initial value, and is blocked from going below a set lower bound.

#### First Network Configuration

The inputs inputs of the first neural network are exclusively the types of surrounding blocks, rather than being informed by a variety of functions as seen in the second neural network. Here, the genome of the agent is divided into two sections, one if Mario is in the air, and the other if he is standing on solid ground. In this network, the block types are broken into six possible types

* Immutable Obstacle (Pipe, Border, Ground, etc.)
* Potentially Mutable Obstacle (Brick)
* Enemy Vulnerable to Jump Attacks (Goomba, shell, etc.)
* Enemy Immune to Jump Attacks (Spiky)
* Positive Object (Mushroom, Coin, Flower)
* Empty Space

Each of these types would then have a variable weight assigned to it, depending on its position, which would be passed to the a node corresponding which tallies all values pertaining to a particular key. For example, in a successful network an enemy directly in front of Mario will be weighted far more heavily than a coin to his upper left would be, and as such it has a far greater impact on the action he chooses to take. Additionally, while the enemy in front would have a strong weight to the jump key, it would have a minimal weight to the right directional input. If the values of these nodes exceed the requirement for this button to be pushed, then that button is set to activated in the returned set.

As there is a different kind of block for each 5 by 5 position for each of six types across five buttons, there are effectively 1500 neurons. High as this may seem, performance was surprisingly only slightly inferior to performance in the second neural net, which had only fifty nodes.

As testing continued, the input grid was expanded from 3 by 3 to 5 by 5, with Mario at the center, as it led to a significant increase in performance on the higher difficulties at a relatively minimal performance cost.

This neural network was run for 500 generations on ten randomly generated seeds at difficulty levels 0, 5, and 25, with *p* value of 20 and *c* value of 100, as well as *c* value of 15 and *p* value of 5

#### First Network Results
###### Generation Size 100, 15 Progenitors per Generation

Test 1: The results of this section generally conformed to expectations for the first 500 generations. The algorithm was most successful on level 0, where on average half the levels (those without tall walls at the end) were completed. Surprisingly, this agent did not kill as many of the enemies as it could have, though this may be in part due to the strong incentives given towards movement in the earlier generations.
![NN1FirstGraph](https://i.imgur.com/jSnzb5s.jpg)

###### Generation Size 15, 5 Progenitors per Generation

Test 2: A larger generation size and relatively large parental size (as seen in test 1) is helpful in that it allows for a slower, steadier, but more consistent increase in fitness, but at the cost that the fittest of your species are always have their traits brought down by the weakest. Those rare, exceptional mutations seldom find the ground in which to take flower, and a series of poor generations can practically reset a once decent population.

These qualities are readily apparent in this second test, in which *p* = 5 and *c* = 15. Due to the high mutability without a large population to balance out traits, the agent operating on level 25 never cracked a fitness score of 800, and level 0 had only a feeble 1800 as its maximum. (As a reminder, agents which did not most receive 0 points, hence the lack of a guaranteed 1000 minimum). By contrast, the agent operating on difficulty 5 had an enormously favorable mutation early on, and dominated this grouping with a peak score of 2600. Unfortunately, the small population made these moments of high performance very unstable, and this agent spend most its generations oscillating between 2500 and 1500.
![NN1GraphSmallGen](https://i.imgur.com/xrZE4jv.png)



###### Sigma Evolution

As mentioned earlier, sigma was set to increase upon failure, and decrease on success,  so as to mutate its way out of dead ends whilst not too dramatically modify its positive genes when it was succeeding. With the *p* of 20 and *c* of 100, σ was generally at it's lowest possible value as the performance generally veered positive. In the occasional valleys, σ would temporarily increase to roughly five times this minimal value, before returning to its set minimum.

In test 2, the much more variable performance led to σ following a congrous pattern of peaks and valleys. When I've the time to re-do these experiments, I would significantly lower the rate at which σ increases, as I now think that the high values it climbed to only magnified the depths of the performance valleys, rather than mitigate them.
 
 
 
#### Variations to Perception

![Layout](https://i.imgur.com/EsMIhCZ.png)




