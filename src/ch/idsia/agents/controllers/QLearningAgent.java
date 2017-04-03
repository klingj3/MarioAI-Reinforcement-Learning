package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.agents.controllers;
 */

public class QLearningAgent extends BasicMarioAIAgent implements Agent
{
	boolean qLearning;
	boolean sarsa;

	HashMap updatingHM; //
	HashMap origHM;

	double epsilon;
	double learningRate;
	double discountFactor;
	double previousReward;

	int previousState;
	int previousKillsTotal;
	int currAction;
	int constXChange;
	int previousStatus;
	int previousAction;

	int randomCount;
	int totalCount;


	float previousX;
	float previousY;
	float previousGroundedY;

	boolean stuck;

	// 0 			 = nothing
	//-60, -24, -85  = can't pass through
	// 2, 3 		 = coin, mushroom, fire flower
	// 80			 = jumpable enemy
	// -62			 = Soft obstacle
	// 93			 = Spiky -- Irrlevant for level 0

	private boolean isEnemy(int y, int x, byte[][] scene){
		return (scene[x][y] == 80);
	}

	private boolean isObstacle(int y, int x, byte[][] scene){
		return (scene[x][y] == -60 || scene[x][y] == -85 || scene[x][y] == -24);
	}


	private boolean isGoal(int y, int x, byte[][] scene){
		return (scene[x][y] == 2 || scene[x][y] == 3);
	}

	private boolean isSoftObstacle(int y, int x, byte[][] scene){
		return (scene[x][y] == -62);
	}

	/** Boolean functions */
	private boolean enemyInRadius(int radius, byte[][] scene){
		for (int i = 9-radius; i < 9+radius; i++){
			for (int j = 9-radius; j < 9+radius; j++){
				if (isEnemy(i, j, scene)){
					return true;
				}
			}
		}
		return false;
	}

	private boolean obstacleAhead(byte[][] scene){
		return (isObstacle(10, 8, scene)) || (isObstacle(10, 9, scene)) || (isObstacle(10, 7, scene));
	}

	private boolean softObstacleAbove(byte[][] scene){
		for (int i = 0; i < 9; i++){
			if (scene[i][9] == -62){
				return true;
			}
		}
		return false;
	}

	/**NUM BUTTONS*/
	private final int nb = 5;
	private final int numActions = 12;

	public QLearningAgent()
	{
		super("jk");
		updatingHM = new HashMap<Integer, ArrayList<Double>>();
		origHM = new HashMap<Integer, ArrayList<Double>>();
		genericInitializers();
	}

	public QLearningAgent(String learningType)
	{
		super("jk");

		if (learningType.equals("SARSA")){
			System.out.println("Sarsa Performance");
			sarsa = true;
			qLearning = false;
		}
		else if (learningType.equals("QLEARNING")){
			System.out.println("QLearning Performance");
			sarsa = false;
			qLearning = true;
		}

		System.out.println("Generation" + "\t" + "Generation Fitness" + "\t" + "Average last 30 Generations" + "\t" + "Average to Date");

		updatingHM = new HashMap<Integer, ArrayList<Double>>();
		origHM = new HashMap<Integer, ArrayList<Double>>();
		genericInitializers();
	}

	public QLearningAgent(String learningType, HashMap<Integer, ArrayList<Double>> newHash, double lr, double df, double e)
	{
		super("jk");

		if (learningType.equals("SARSA")){
			sarsa = true;
			qLearning = false;
		}
		else if (learningType.equals("QLEARNING")){
			sarsa = false;
			qLearning = true;
		}

		updatingHM = (HashMap<Integer, ArrayList<Double>>)newHash.clone();
		origHM = (HashMap<Integer, ArrayList<Double>>)newHash.clone();
		genericInitializers();
		learningRate = lr;
		discountFactor = df;
		epsilon = e;
	}

	public HashMap<Integer, ArrayList<Double>> getHashMap(){ return updatingHM; }

	//Generic stuff for setting up the function
	private void genericInitializers() {
		epsilon = 0;
		previousState = -1;
		previousKillsTotal = -1;
		previousX = -1;
		previousY = -1;
		previousGroundedY = -1;
		currAction = -1;
		constXChange = 0;
		previousStatus = 0;
		stuck = false;
		learningRate = 0.3;
		discountFactor = 0.9;
		previousReward = 0.0;
		randomCount = 0;
		totalCount = 0;
		previousAction = 0;

		reset();
	}

	/*Modifying core parameters*/

	public void setDiscount(double newD) {discountFactor = newD;}

	public void setLearning(double newL) {learningRate = newL;}

	/*End of parameters*/

	public void setEpsilon(double newEpsilon){
		epsilon = newEpsilon;
	}

	//String boolean array
	private boolean[] s2ba(String s){
		boolean ret[] = {false, false, false, false, false, false};
		for (int i = 0; i < s.length(); i++){
			if (s.charAt(i) =='1'){
				ret[i] = true;
			}
		}
		return ret;
	}


	//boolean to string
	private String b2s(boolean b){
		if (b)
			return "1";
		else
			return "0";
	}

	//Boolean to int
	private double b2i(boolean b){
		if (b)
			return 1;
		return 0;
	}

	//Hashes the boolean values to an integer
	private int hash(boolean[] arr){
		String s = "";
		for (int i = 0; i < arr.length; i++){
			s += b2s(arr[i]);
		}
		return Integer.parseInt(s, 2);
	}

	//Converts the x and y speeds into a single direction, 1-9
	private int speedToDirection(float x, float y){
		double angle = Math.atan2(y, x);
		double temp = (360*angle/Math.PI+180);
		int ret = (int)temp/(360/9);
		return ret;
	}

	//distance to closet enemy on right;
	private int distanceToClosestEnemy(byte[][] scene){
		for (int x = 9; x < 19; x++){
			for (int y = 0; y < 18; y++){
				if (isEnemy(x, y, scene)){
					return x-9;
				}
			}
		}
		return 9;
	}

	public boolean[] getAction()
	{

		totalCount++;

		for (int i = 0; i < nb; i++){
			action[i] = false;
		}

		byte[][] scene = mergedObservation;

		boolean enemyInRadius1 = enemyInRadius(1, scene);
		boolean enemyInRadius3 = enemyInRadius(3, scene);
		boolean enemyInRadius5 = enemyInRadius(5, scene);
		boolean onPipe = scene[9][10] == -85;
		boolean isFire = marioStatus == 2;
		int direction = 0;

		boolean obstacleAhead = obstacleAhead(scene) || scene[9][10] != 0;
		boolean softObstacle = softObstacleAbove(scene);

		boolean[] arr = {softObstacle, isFire,
				enemyInRadius1,
				enemyInRadius3,
				enemyInRadius5,
				obstacleAhead,
				stuck,
				isMarioOnGround,
				isMarioAbleToJump,
				onPipe};

		int hash = hash(arr)*10;

		/* REWARD SECTION **/
		double reward = 0;
		float xChange =  marioFloatPos[0] - previousX;
		float yChange = marioFloatPos[1] - previousY;
		direction = speedToDirection(xChange, yChange);
		hash += direction;
		hash *= 10;
		hash += distanceToClosestEnemy(scene);

		float yGroundedChange = 0;

		selectAction(hash);

		if (previousState != -1){
			reward = getReward(hash, reward, xChange, scene);
		}



		previousAction = currAction;
		previousReward = reward;
		previousState = hash;
		previousKillsTotal = getKillsTotal;
		previousX = marioFloatPos[0];
		previousStatus = marioStatus;
		previousY = marioFloatPos[1];
		if (isMarioOnGround)
			previousGroundedY = marioFloatPos[1];
		if (currAction == -1){
			currAction = (int)(Math.random()*numActions);
		}
		// Left = 0 |  Right = 1  | Down = 2 | Jump = 3 | Speed = 4 | Up = 5
		if (currAction == 0){return s2ba("000000");}
		if (currAction == 1){return s2ba("100000");}
		if (currAction == 2){return s2ba("100100");}
		if (currAction == 3){return s2ba("100010");}
		if (currAction == 4){return s2ba("010000");}
		if (currAction == 5){return s2ba("010010");}
		if (currAction == 6){return s2ba("010100");}
		if (currAction == 7){return s2ba("001000");}
		if (currAction == 8){return s2ba("000100");}
		if (currAction == 9){return s2ba("000010");}
		if (currAction == 10){return s2ba("010110");}
		if (currAction == 11){return s2ba("100110");}

		return action;
	}

	private void selectAction(int hash) {
		//If we've seen this state before, we use Q values to make an assessment.
		if (origHM.containsKey(hash)){
			if (Math.random() > epsilon) {
				double maxVal = -1000;
				int maxValIndex = 1;
				ArrayList<Integer> maxLink = new ArrayList<Integer>();
				ArrayList<Double> arrList = (ArrayList<Double>) origHM.get(hash);
				for (int i = 0; i < numActions; i++) {
						double temp = arrList.get(i);
						if (temp > maxVal) {
							maxVal = temp;
							maxLink.clear();
							maxLink.add(i);
						} else if (temp == maxVal) {
							maxLink.add(i);
						}
				}
				if (maxVal >= 0) {
					maxValIndex = maxLink.get((int) (maxLink.size() * Math.random()));
					currAction = maxValIndex;
				}
			}
			else{
				currAction = (int)(Math.random() * numActions);
			}
		}
		else{
			//If this is a new state, select a random action.
			ArrayList<Double> qValues = new ArrayList<Double>();
			for (int i = 0; i < numActions; i++){
				qValues.add(0.0);
			}
			currAction = (int)(Math.random() * numActions);
			//Add this blank array to the hashmap.
			updatingHM.put(hash, qValues);
		}
	}

	private double getReward(int hash, double reward, float xChange, byte[][] scene) {
		float yGroundedChange;
		yGroundedChange = marioFloatPos[1] - previousGroundedY;
		int numEnemiesKilled = getKillsTotal - previousKillsTotal;
		reward +=  xChange*10 + 2*numEnemiesKilled;

		if (marioStatus < previousStatus){
            reward -= 500; //Punishment for being hurt.
        }
		if (Math.max(0.00, xChange) > 0 && isMarioOnGround){
			if (scene[10][9] == -62)
				reward += 50;
            reward += 10*Math.max(0.00, yGroundedChange);
        }
		if (xChange < 0.1)
            constXChange++;
        else
            constXChange = 0;
		reward -= 5*b2i(stuck);

		ArrayList<Double> previousStateValues = (ArrayList<Double>) updatingHM.get(previousState);
		updatingHM.remove(previousState);
		ArrayList<Double> newList = new ArrayList<Double>();
		//Copies all of the values for the old, except for that action being updated.
		for (int i = 0; i < numActions; i++) {
            if (i != previousAction) {
                newList.add(previousStateValues.get(i));
            } else {
                //At the updated action, the nature of the update depends on Qlearning or Sarsa
                if (qLearning) {
                    double maxValue = 0;
                    if (updatingHM.containsKey(hash)) {
                        ArrayList<Double> temp = (ArrayList<Double>) updatingHM.get(hash);
                        for (int j = 0; j < numActions; j++) {
                            maxValue = Math.max(maxValue, temp.get(j));
                        }
                    }
                    //QLearning formula
                    double newQ = (previousStateValues.get(previousAction)) + learningRate *
                            (reward + discountFactor * maxValue - previousStateValues.get(previousAction));
                    newList.add(newQ);
                }
                else if (sarsa){
                    double currentActionValue = 0;
                    if (updatingHM.containsKey(hash)){
                        ArrayList<Double> temp = (ArrayList<Double>) updatingHM.get(hash);
                        currentActionValue = temp.get(currAction);
                    }
                    double newSarsa = previousStateValues.get(previousAction) + learningRate*(previousReward +
                                discountFactor*currentActionValue - previousStateValues.get(previousAction));
                    newList.add(newSarsa);
                }
                else
                	System.out.println("ERROR!: Neither QLearning nor SARSA selected!");
            }
        }


		updatingHM.put(previousState, newList);

		stuck = (constXChange > 5);
		return reward;
	}

	public void reset()
	{
		action = new boolean[Environment.numberOfButtons];
		action[Mario.KEY_RIGHT] = false;
		action[Mario.KEY_SPEED] = false;
	}

	public void printSceneCodes(byte[][] scene) {
		System.out.println(" ...........................................................................................");
		System.out.print("     ");
		for (int j=0; j < scene[0].length; j++) {
			System.out.print(String.format("%3d.", j));
		}
		System.out.println();
		for (int i=0; i < scene.length; i++) {
			System.out.print(String.format("%3d.", i));
			for (int j=0; j < scene[i].length; j++) {
					//if (scene[i][j] != 1 && scene[i][j] != 25 && scene[i][j] != 80 && scene[i][j] !=  93 && scene[i][j] !=  2 && scene[i][j] !=  3&& scene[i][j] !=  -24 && scene[i][j] !=  2 && scene[i][j] !=  -60 && scene[i][j] !=  -62 && scene[i][j] != 0)
						System.out.print(String.format("%4d", scene[i][j]));

			}
			System.out.println();
		}
		System.out.println(" ...........................................................................................");
	}

}


