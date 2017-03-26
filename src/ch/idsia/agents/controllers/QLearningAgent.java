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
	int previousState;
	int previousKillsTotal;
	int previousAction;
	int constXChange;

	int randomCount;
	int totalCount;

	float previousX;
	float previousY;

	// 0 			 = nothing
	//-60, -24, -85  = can't pass through
	// 2, 3 		 = coin, mushroom, fire flower
	// 80			 = jumpable enemy
	// -62			 = Soft obstacle
	// 93			 = Spiky -- Irrlevant for level 0

	private boolean isEnemy(int x, int y, byte[][] scene){
		return (scene[x][y] == 80);
	}

	private boolean isObstacle(int x, int y, byte[][] scene){
		return (scene[x][y] == -60 || scene[x][y] == -85 || scene[x][y] == -24);
	}


	private boolean isGoal(int x, int y, byte[][] scene){
		return (scene[x][y] == 2 || scene[x][y] == 3);
	}

	private boolean isSoftObstacle(int x, int y, byte[][] scene){
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
		return (isObstacle(10, 8, scene)) || (isObstacle(10, 9, scene)) || (isObstacle(10, 7, scene) || (isObstacle(10, 10, scene)));
	}

	/**NUM BUTTONS*/
	private final int nb = 5;

	public QLearningAgent()
	{
		super("jk");
		hm = new HashMap<Integer, ArrayList<Double>>();
		epsilon = 0.0;
		previousState = -1;
		previousKillsTotal = -1;
		previousX = -1;
		previousY = -1;
		previousAction = -1;
		constXChange = 0;

		randomCount = 0;
		totalCount = 0;

		reset();
	}

	public void setEpsilon(double newEpsilon){
		epsilon = newEpsilon;
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
	public int hash(boolean[] arr){
		String s = "";
		for (int i = 0; i < arr.length; i++){
			s += b2s(arr[i]);
		}
		return Integer.parseInt(s, 2);
	}

	public boolean[] getAction()
	{

		totalCount++;

		for (int i = 0; i < nb; i++){
			action[i] = false;
		}
		int numBooleans = 8;

		double learningRate = 0.7;
		double discountFactor = 0.3;

		byte[][] scene = mergedObservation;

		boolean enemyInRadius1 = enemyInRadius(1, scene);
		boolean enemyInRadius2 = enemyInRadius(2, scene);
		boolean enemyInRadius3 = enemyInRadius(3, scene);
		boolean onPipe = scene[9][10] == -85;

		boolean obstacleAhead = obstacleAhead(scene) || (!isMarioOnGround && isObstacle(10, 10, scene));
		boolean stuck = false;

		boolean[] arr = {enemyInRadius1, enemyInRadius2, enemyInRadius3, obstacleAhead, stuck, isMarioOnGround, isMarioAbleToJump, onPipe};
		int hash = hash(arr);


		/* REWARD SECTION **/
		double reward = 0;
		if (previousState != -1){
			float xChange = marioFloatPos[0] - previousX;
			float yChange = marioFloatPos[1] - previousY;
			int numEnemiesKilled = getKillsTotal - previousKillsTotal;
			reward += Math.max(0.00, xChange) + numEnemiesKilled;
			if (Math.max(0.00, xChange) > 0 && isMarioOnGround){
				reward += Math.max(0.00, yChange);
			}

			ArrayList<Double> arrList = (ArrayList<Double>)hm.get(previousState);
			hm.remove(previousState);
			ArrayList<Double> newList = new ArrayList<Double>();

			for (int i = 0; i < nb; i++){
				if (i != previousAction){
					newList.add(arrList.get(i));
				}
				else{
					double maxValue = 0;
					if (hm.containsKey(hash)){
						ArrayList<Double> temp = (ArrayList<Double>)hm.get(hash);
						for (int j = 0; j < nb; j++){
							maxValue = Math.max(maxValue, temp.get(j));
						}
					}
					newList.add(arrList.get(i) + learningRate*(reward + discountFactor*(maxValue) - arrList.get(i)));
				}
			}

			hm.put(previousState, newList);
			if (xChange < 0.001)
				constXChange++;
			else
				constXChange = 0;

			stuck = constXChange > 10;
		}


		/* END OF REWARD **/


		//If we've seen this state before, we use Q values to make an assessment.
		if (hm.containsKey(hash)){
			if (Math.random() > epsilon) {
				double maxVal = -100;
				int maxValIndex = 0;
				ArrayList<Integer> maxLink = new ArrayList<Integer>();
				ArrayList<Double> arrList = (ArrayList<Double>) hm.get(hash);
				for (int i = 0; i < nb; i++) {
					double temp = arrList.get(i);
					if (temp > maxVal) {
						maxVal = temp;
						maxLink.clear();
						maxLink.add(i);
					} else if (temp == maxVal) {
						maxLink.add(i);
					}
				}
				maxValIndex = maxLink.get((int) (maxLink.size() * Math.random()));
				action[maxValIndex] = true;
			}
			else{
				action[(int) (Math.random() * nb)] = true;
				randomCount++;
			}
		}
		else{
			//If this is a new state, select a random action.
			ArrayList<Double> rewards = new ArrayList<Double>();
			for (int i = 0; i < nb; i++){
				rewards.add(0.0);
			}
			if (Math.random() < epsilon) {
				action[(int) (Math.random() * nb)] = true;
				randomCount++;
			}
			//Add this blank array to the hashmap.
			hm.put(hash, rewards);
		}

		previousState = hash;
		previousKillsTotal = getKillsTotal;
		previousX = marioFloatPos[0];
		if (isMarioOnGround)
			previousY = marioFloatPos[1];
		for (int i = 0; i < nb; i++){
			if (action[i]){
				previousAction = i;
			}
		}
		action[0] = false;

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


