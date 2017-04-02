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
	HashMap hm;

	double epsilon;
	double learningRate;
	double discountFactor;

	int previousState;
	int previousKillsTotal;
	int previousAction;
	int constXChange;
	int previousStatus;

	int randomCount;
	int totalCount;


	float previousX;
	float previousY;
	float previousGroundedY;

	boolean stuck;

	ArrayList<boolean[]> actionSets;

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

	/**NUM BUTTONS*/
	private final int nb = 5;
	private final int numActions = 12;

	public QLearningAgent()
	{
		super("jk");
		hm = new HashMap<Integer, ArrayList<Double>>();
		epsilon = 0;
		previousState = -1;
		previousKillsTotal = -1;
		previousX = -1;
		previousY = -1;
		previousGroundedY = -1;
		previousAction = -1;
		constXChange = 0;
		previousStatus = 0;
		stuck = false;
		learningRate = 0.3;
		discountFactor = 0.9;

		randomCount = 0;
		totalCount = 0;

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

		boolean obstacleAhead = obstacleAhead(scene); //|| (!isMarioOnGround && isObstacle(10, 10, scene));

		boolean[] arr = {isFire,
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
		float xChange = 0;
		float yChange = 0;
		float yGroundedChange = 0;

		if (previousState != -1){

			xChange = marioFloatPos[0] - previousX;
			yChange = marioFloatPos[1] - previousY;
			yGroundedChange = marioFloatPos[1] - previousGroundedY;

			int numEnemiesKilled = getKillsTotal - previousKillsTotal;

			direction = speedToDirection(xChange, yChange);
			hash += direction;

			reward +=  xChange*100 + 10*numEnemiesKilled;
			if (marioStatus < previousStatus || previousAction == -1){
				reward -= 5000; //Punishment for being hurt.
			}
			if (Math.max(0.00, xChange) > 0 && isMarioOnGround){
				reward += Math.max(0.00, yGroundedChange);
			}
			if (xChange < 0.1)
				constXChange++;
			else
				constXChange = 0;
			reward -= 10*b2i(stuck);

			ArrayList<Double> arrList = (ArrayList<Double>)hm.get(previousState);
			hm.remove(previousState);
			ArrayList<Double> newList = new ArrayList<Double>();

			for (int i = 0; i < numActions; i++){
				if (i != previousAction){
					newList.add(arrList.get(i));
				}
				else{
					double maxValue = 0;
					if (hm.containsKey(hash)){
						ArrayList<Double> temp = (ArrayList<Double>)hm.get(hash);
						for (int j = 0; j < numActions; j++){
							maxValue = Math.max(maxValue, temp.get(j));
						}
					}
					double newQ = (1-learningRate)*(arrList.get(previousAction)) + learningRate*
							(reward+discountFactor*maxValue);
					newList.add(newQ);
				}
			}

			hm.put(previousState, newList);

			stuck = (constXChange > 5);
		}


		/* END OF REWARD **/


		//If we've seen this state before, we use Q values to make an assessment.
		if (hm.containsKey(hash)){
			if (Math.random() > epsilon) {
				double maxVal = -1000;
				int maxValIndex = 1;
				ArrayList<Integer> maxLink = new ArrayList<Integer>();
				ArrayList<Double> arrList = (ArrayList<Double>) hm.get(hash);
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
					previousAction = maxValIndex;
				}
			}
			else{
				previousAction = (int)(Math.random() * numActions);
				randomCount++;
			}
		}
		else{
			//If this is a new state, select a random action.
			ArrayList<Double> qValues = new ArrayList<Double>();
			for (int i = 0; i < numActions; i++){
				qValues.add(0.0);
			}
			previousAction = (int)(Math.random() * numActions);
			randomCount++;
			//Add this blank array to the hashmap.
			hm.put(hash, qValues);
		}

		previousState = hash;
		previousKillsTotal = getKillsTotal;
		previousX = marioFloatPos[0];
		previousStatus = marioStatus;
		previousY = marioFloatPos[1];
		if (isMarioOnGround)
			previousGroundedY = marioFloatPos[1];
		if (previousAction == -1){
			previousAction = (int)(Math.random()*numActions);
		}
		// Left = 0 |  Right = 1  | Down = 2 | Jump = 3 | Speed = 4 | Up = 5
		if (previousAction == 0){return s2ba("000000");}
		if (previousAction == 1){return s2ba("100000");}
		if (previousAction == 2){return s2ba("100100");}
		if (previousAction == 3){return s2ba("100010");}
		if (previousAction == 4){return s2ba("010000");}
		if (previousAction == 5){return s2ba("010010");}
		if (previousAction == 6){return s2ba("010100");}
		if (previousAction == 7){return s2ba("001000");}
		if (previousAction == 8){return s2ba("000100");}
		if (previousAction == 9){return s2ba("000010");}
		if (previousAction == 11){return s2ba("100110");}
		if (previousAction == 10){return s2ba("010110");}

		return action;
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


