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
*Simplified Diagram of the Network with Reduced Input Size*

In this section, we revisit the neural network, but instead of taking in information in a 5 by 5 grid, it instead observes the scene in a 3 by 3 grid. In one of the paper **A Comparison of Genetic Algorithms using Super Mario Bros.** by Ross Foley and Karl Kuhn of WPI, their 3 by 3 feature set yielded far stronger results than their 5 by 5, but that did not repeat itself here. In fact, while performance was more or less equivalent on level zero, Mario's decreased visibility led to a significant decrease in performance on levels 5 and 25.

![3by3Graph](https://i.imgur.com/wYjmN02.png)

### Pt 2. Reinforecement Learning

Reinforcement learning is an AI development strategy in which the action taken by the agent is based on which action yielded the best result in the past. If it has never seen this particular state before, and thus has no basis under which to pick the best action, it makes it's choice randomly. In this way, it has some similarity to neural networks, in that numeric values driven in part by randomness fuel decision making.

There are a multitude of methods for reinforcement learning. In this paper, we examine the differences in fitness and learned behaviours stemming from implementation of the methods Sarsa and Q-Learning.

#### Testing Procedure and Variables

The agent's values were updated over the course of 1,000 generations, each taking place on ten maps. For both Sarsa and QLearning, the learning rate α was set to 0.1, 0.2, and 0.3, the discount factor γ was set to 0.6, 0.7, and 0.8. In addition, both methods utilize the ε-greedy method of choosing a random action ε percentage of the time. For both Sarsa and QLearning ε is 0.15 (with the section of the variable ε test), meaning that fifteen percent of actions undertaken by Mario are random. While this may seem like a very high number, due to the staggering number of actions undertaken by Mario in an episode (30,000 in a 200s episode) it actually doesn't have a noticeable negative impact on his ultimate performance. 

#### Reward/Punishment Calculation

In order to meaningfully update the values used to make decisions, an agent must be able to accurately assess if it's actions are having a positive or negative impact on overall performance. While in real-world scenarios rewards can be a complicated multi-variable formula, fortunately Mario's goals are simple enough that reward can be calculated by a linear combination of variables and values. 

At his core, Mario has a singular primary goal: move right towards the end of the level. He also has the secondary goal of destroying enemies and collecting coins. Because of this, reward is calculated as 10*(horizontal speed) + 5*(number of enemies killed since the last frame). For the sake of limiting state spaces, and due to their relative unimportance in fitness calculation, coins were not a part of the state space nor the ultimate reward formula.

In addition, all of the enemies in this level fall off ledges as they walk, and are unable to jump, so naturally (as Mario takes no fall damage) it is always advantageous to be on a higher level. In order to incentives this behaviour, if Mario's vertical coordinates are greater than they were last time Mario was standing on something, the fifty times the change in height is added to his reward.  

The reward is only negative if Mario's size is less than it was the previous frame (meaning that he has collided with an enemy) or if he has moved to the left. Though, the punishment for left movement is mild enough that if Mario needs to move left in order to get an enemy, the overall reward is still positive.

Lastly, if Mario is currently stuck, or has been recently stuck (more on this variable later), then the *xChange* is set to the absolute value of *xChange*, as if Mario is stuck then it makes sense to move left and reassess the situation. 

#### State Space Reduction

The state space for mario is determined by ten boolean variables, and two valeus in the range 0-9, for a total number of possible states 100*2^10. While there exist 102,500 possibile combinations of these variables, in actuality the number of encountered states was typically around 8,000 when ten seeds were being evaluated simultaneously. 

The ten boolean values values used in state evaluation are:

* **SoftObstacle** - True if there is a soft obstacle (semi-permeable barrier such as the top of a hill) with jumping distance of Mario.
* **isFire** - True if Mario is in his "fire" condition. Other solutions to variants on Mario reinforcement learning also include variables for "Big" and "Small" states, but these are irrelevant here as on level 0 Mario should be able to reach the end of the level in his fire state relatively easily.
* **enemyInRadius1** - True if there is an enemy in a box extending 1 above, 1 below, 1 in-front, and 1 behind Mario.
* **enemyInRadius3** - True if there is an enemy in a box extending 3 above, 3 below, 3 in-front, and 3 behind Mario.
* **enemyInRadius5** - True if there is an enemy in a box extending 5 above, 5 below, 5 in-front, and 5 behind Mario.
* **stuck** - True if Mario has had an xSpeed of less than 0.1 for 5 continuous cycles.
* **onGround** - True if Mario is standing on a solid surface.
* **ableToJump** - True if Mario is able to jump.
* **onPipe** - True if Mario is on a pipe. 
* **ObstacleAhead** - True if there is an obstacle ahead of Mario that would prevent him walking forwards.

In addition, we have the two variables which return a value in the range 0-9.

* **Direction** - Returns a value between 0 and 9 representing the angle of Mario's movement.
* **DistanceToEnemy** - Returns the the x position of Mario's closest forward enemy in the right direction. If no enemy is ahead, then returns 9.

These values are hashed into a value between 0 and 102499, which are then stored in a hashmap, linking to an array of twelve double values. These twelve values refer to the twelve possible viable key combinations, which are some combination of **(NO DIRECTION, LEFT, RIGHT, DOWN)*(JUMP, RUN, NONE)**.

#### QLearning Evalutation

QLearning is a style of updating state-action values according to the following formula:
![QLearnignFormula](https://i.imgur.com/y5HT4X8.png)

This algorithm was used for 500 generations of training, both on 10 randomly generated seeds and on seed 0, on level 0 with epsilon 0.3, learning rate 0.2, and discount factor 0.7.

![MultiSeedResults](https://i.imgur.com/vU23r8L.png)

![SingleSeedResult](https://i.imgur.com/wyVdlbU.png)

In both scenarios, the average value approached a consistent limit within 100 generations, while the average over the last 30 generations fluctuated within a fitness evaluating of 200 of this value. Again, in both scenarios, the generation fitness varied wildly, and the maximum fitness of any single generation maxed out at 7500. 

The key different between the two is in the proximity of the average over the last 30 generations to the maximum generation value, and in the value of the average. While the average across ten seeds hovered around 5900, when evaluated on exclusively seed 0 the average was at 7100, which is an excellent score considering the night-impassible wall at the end of seed 0 on difficulty 0.

#### SARSA Evaluation

SARSA is a style of updating state-action values according to the following formula:

![SarsaFormula](https://i.imgur.com/VObtdsX.png)

Like QLearning, this algorithm was used for 500 generations of training, both on 10 randomly generated seeds and on seed 0, on level 0 with epsilon 0.3, learning rate 0.2, and discount factor 0.7.

![MultiSarsa](https://i.imgur.com/undefined.jpg)
![SingleSarsa](https://i.imgur.com/UJSftIs.png)

 While SARSA and Q-Learning had the very similar maximum value attained by any single generation, their performance differed by about 2500 when evaluated across ten seeds, and about 500 evaluated on a single seed. In addition, SARSA's curve of total average didn't normalize around a single value for about 300 generations.
 
 #### Variable Alpha
 
Alpha, also known as the learning rate, is a variable which effects the weight of the most recent action on the state-action value relative to past actions.

![AlphaScores](https://i.imgur.com/GalaMLr.png)

The results in this section generally conform to expectations, with the higher alpha values having a greater degree of mutability, while the lower alpha values are unable to reach any particular heights. Overall, the alpha value of 0.2 had the greatest consistent value, but it was consistently within 300 of alpha values 0.1 and 0.3, with 0.4 having the worst overall performance.

#### Variable Discount Factor

The discount factor determines the importance of a potential upcoming action in determining the ultimate value of a state-action pair.

![Discount Factor Values](https://i.imgur.com/gLHwZbs.png)

Unlike the learning rate, the discount factor value here had no impact on the rate of learning increase, or on the general range of values. The discount rate of 0.6 had the best performance, while the discount rate of 0.9 had the worst overall performance.

#### Variable Epsilon

With epsilon greedy selection, epsilon signifies the percentage likelihood of a random action being selected. While this initially seems as though it would detrimental to overall performance, on multiple levels, where states which hash to the same value may have varying optimal actions, a high epsilon value is essential to exploring new opportunities and continually storing the correct values for a given state-action pair. Random actions are also selected when a new state is encountered for the first time. Due to the high number of states encountered (about 7,000) over the course of 500 generations, this randomness ensures that even when epsilon is set to 0, a pattern of some simplicity develops. 
 
 ![EpsilonValues](https://i.imgur.com/gLHwZbs.png)
 
Surprisingly, the lowest epsilon value reached the highest peak, 7300, of any epsilon value. While on one hand it makes sense, as an epsilon of 0 means that the best actions will be selected in every situation, it still comes as a surprise that an accurate enough state-action values were ever developed to reach such a lofty peak. The other values all fell within 500 of one another, with 0.1 and 0.2 being about equal for optimal values.

#### Conclusion
 
 Ultimately, this rule based agent was better than or equal to all previously explored agents, with the exception of the 1500 node neural network on difficulty 0. There are several reasons this may be:
* The neural network was designed for optimally performance on this custom system of values, while the genetic algorithm was tuned for the other method.
* This neural network had 1500 nodes, meaning that actions were based on a far more accurate representation of the state space than is present in this graph. This, combined with it taking roughly ten times as long to cmpute, gives it a distinct advantage. 

 In this assignment, it was shown that the best values for epsilon, learning rate, and discount factor as linear non-evolving variables was 0.2, 0.2, and 0.6. Under these scenarios, on the level of difficulty 0 on variable seeds the average sat at a comfortable 5800, while on a single level it was just over 7500. Considering that this single level had a large wall at the end, and in the reinforcement learning paper which was read in class their average was just over 8000 in a level without a wall at the end, this is an excellent overall result.
 
 Going forward, it would be worthwhile to examine better ways of tuning state-action values across multiple levels simultaneously. Presently, the agent works off of a single table across all ten levels, updating it's table each time the ten levels are complete. This gives a noticeable bias to the first level, was it first creates the initial state-action values. It would be more effective if at the beginning of each generation a different copy of the current state-action values table was made for each level, and then at the end averages of all of their q values for shared state-action pairs were made into the new values. I assume that in this case, there would ultimately be improved overall performance. 

![TotalComparisons](https://i.imgur.com/Z9o6tvw.png)






