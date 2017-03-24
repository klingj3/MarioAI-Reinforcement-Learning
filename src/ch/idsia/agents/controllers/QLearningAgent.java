package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.agents.controllers;
 */

public class QLearningAgent extends BasicMarioAIAgent implements Agent
{

	/** Boolean functions */


	/**NUM BUTTONS*/
	private final int nb = 5;

	//Num square types
	private final int nst = 9;
	private double[] genes;

	public QLearningAgent()
	{
		super("jk");
		reset();
	}

	public QLearningAgent(double[] QLearningTable)
	{
		super("jk");
		genes = QLearningTable;
		reset();
	}

	//Boolean to int
	private double b2i(boolean b){
		if (b)
			return 1;
		return 0;
	}

	public boolean[] getAction()
	{
		byte[][] scene = mergedObservation;


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


