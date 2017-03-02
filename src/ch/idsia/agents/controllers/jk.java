package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 25, 2009
 * Time: 12:27:07 AM
 * Package: ch.idsia.agents.controllers;
 */

public class jk extends BasicMarioAIAgent implements Agent
{

	/**NUM BUTTONS*/
	private final int nb = 5;

	//Num square types
	private final int nst = 9;

	int jump;
	private double[] genes;

	public jk()
	{
		super("jk");
		reset();
		jump = 0;
	}

	public jk(double[] geneList)
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
		//	action[i] = false;
		}
		int gene = 0;
		int horWidth = 1;
		int verWidth = 1;
		int geneSize = 3*3*5*nb;

		double hiddenLayer[] = new double[nb];
		//System.out.println(geneSize);
		if (isMarioAbleToJump) {
			for (int i = 9 - horWidth; i <= 9 + horWidth; ++i) {
				for (int j = 9 - (verWidth); j <= 9 + (verWidth); ++j) {
					// 0 			 = nothing
					//-60, -24, -85  = can't pass through
					// 2, 3 		 = coin, mushroom, fire flower
					// 80			 = jumpable enemy
					// -62			 = Soft obstacle

					for (int l = 0; l < nb; ++l) {
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == 0));
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == -60 || scene[i][j] == -24 || scene[i][j] == -85));
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == 2 || scene[i][j] == 3));
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == 80));
						hiddenLayer[l] += (genes[gene++])*b2i(scene[i][j] == -62);
					}
				}
			}
		}
		else{
			gene += geneSize;
			for (int i = 9 - horWidth; i <= 9 + horWidth; ++i) {
				for (int j = 9 - (verWidth); j <= 9 + (verWidth); ++j) {
					// 0 			 = nothing
					//-60, -24, -85  = can't pass through
					// 2, 3 		 = coin, mushroom, fire flower
					// 80			 = jumpable enemy
					// -62			 = Soft obstacle

					for (int l = 0; l < nb; ++l) {
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == 0));
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == -60 || scene[i][j] == -24 || scene[i][j] == -85));
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == 2 || scene[i][j] == 3));
						hiddenLayer[l] += (genes[gene++]*b2i(scene[i][j] == 80));
						hiddenLayer[l] += (genes[gene++])*b2i(scene[i][j] == -62);
					}
				}
			}
		}
		for (int i = 0; i < nb; i++){
			if (hiddenLayer[i] > 0.15){
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


