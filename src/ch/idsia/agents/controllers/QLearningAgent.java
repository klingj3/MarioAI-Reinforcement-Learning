package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
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
		return (isObstacle(10, 8, scene)) || (isObstacle(10, 9, scene)) || (isObstacle(10, 7, scene));
	}

	/**NUM BUTTONS*/
	private final int nb = 5;

	public QLearningAgent()
	{
		super("jk");
		hm = new HashMap();
		reset();
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
		byte[][] scene = mergedObservation;

		boolean enemyInRadius1 = enemyInRadius(1, scene);
		boolean enemyInRadius2 = enemyInRadius(2, scene);
		boolean enemyInRadius3 = enemyInRadius(3, scene);

		boolean obstacleAhead = obstacleAhead(scene);

		boolean[] arr = {enemyInRadius1, enemyInRadius2, enemyInRadius3, obstacleAhead};
		int hash = hash(arr);

		if (hm.containsKey(hash)){

		}
		else{

		}

		for (int i = 0; i < nb; i++){
			action[i] = false;
		}

		action[Mario.KEY_DOWN] = true;
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


