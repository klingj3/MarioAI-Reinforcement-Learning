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

public class NeuralNetwork2 extends BasicMarioAIAgent implements Agent
{

	/**NUM BUTTONS*/
	private final int nb = 5;

	//Num square types
	private final int nst = 9;

	int jump;
	private double[] genes;

	public NeuralNetwork2()
	{
		super("jk");
		reset();
		jump = 0;
	}

	public NeuralNetwork2(double[] geneList)
	{
		super("jk");
		genes = geneList;
		jump = 0;
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
		if (action[Mario.KEY_JUMP])
			jump++;
		else
			jump = 0;
		byte[][] scene = mergedObservation;


		for (int i = 0; i < nb; i++){
			action[i] = false;
		}
		int gene = 0;
		int horWidth = 2;
		int verWidth = 2;
		int geneSize = 6*nb;

		double hiddenLayer[] = new double[nb];
		//System.out.println(geneSize);
		if (isMarioAbleToJump) {
			for (int l = 0; l < nb; ++l) {
				hiddenLayer[l] += (genes[gene++]*b2i(gapAhead(scene)));
				hiddenLayer[l] += (genes[gene++]*b2i(enemyAhead(scene)));
				hiddenLayer[l] += (genes[gene++]*b2i(obstacleAhead(scene)));
				hiddenLayer[l] += (genes[gene++]*b2i(goalAhead(scene)));
				hiddenLayer[l] += (genes[gene++])*b2i(safeToJump(scene));
			}
		}
		else{
			for (int l = 0; l < nb; ++l) {
				hiddenLayer[l] += (genes[gene++]*b2i(gapAhead(scene)));
				hiddenLayer[l] += (genes[gene++]*b2i(enemyAhead(scene)));
				hiddenLayer[l] += (genes[gene++]*b2i(obstacleAhead(scene)));
				hiddenLayer[l] += (genes[gene++]*b2i(goalAhead(scene)));
				hiddenLayer[l] += (genes[gene++])*b2i(safeToJump(scene));
				hiddenLayer[l] += (genes[gene++])*1;
			}
		}
		for (int i = 0; i < nb; i++){
			//System.out.println(genes[genes.length-5 + 1]);
			if (hiddenLayer[i] >  0.1){
				action[i] = true;
			}
			//if (hiddenLayer[i] < -0.2){
			//	action[i] = false;
			//}
		}
		action[Mario.KEY_JUMP] = action[Mario.KEY_JUMP] && !(++jump >= 30);
		action[Mario.KEY_DOWN] = false;
		return action;
	}

	public boolean safeToJump(byte[][] scene){
		boolean ret = true;
		for (int i = 9 ; i < 12; i++){
			for (int j = 7; j < 9; j++){
				ret = ret && (scene[i][j] != 80 && scene[i][j] != 93);
			}
		}
		return ret;
	}

	public boolean gapAhead(byte[][] scene){
		for (int j = 5; j < 18; j++){
			if (scene[10][j] == -60 || scene[10][j] == -85 || scene[10][j] == -24)
				return false;
		}
		return true;
	}

	public boolean enemyAhead(byte[][] scene){
		for (int i = 9; i < 12; i++){
			for (int j = 7; j < 10; j++){
				if (scene[i][j] == 80 || scene[i][j] == 93)
					return true;
			}
		}
		return false;
	}

	public boolean goalAhead(byte[][] scene){
		for (int i = 9; i < 12; i++){
			for (int j = 7; j < 10; j++){
				if (scene[i][j] == 2 || scene[i][j] == 3)
					return true;
			}
		}
		return false;
	}

	public boolean obstacleAhead(byte[][] scene){
		boolean ret = (scene[10][9] != 0);
		return ret;
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


