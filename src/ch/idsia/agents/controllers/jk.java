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

public class jk extends BasicMarioAIAgent implements Agent
{
	private int[] allBlockTypes = {80, 3, -24, -60, -62, -85};

	/**NUM BUTTONS*/
	private final int nb = 5;

	//Num square types
	private final int nst = 9;

	int jump;
	private byte[] genes;

	public jk()
	{
		super("jk");
		reset();
		jump = 0;
	}

	public jk(byte[] geneList)
	{
		super("jk");
		genes = geneList;
		jump = 0;
		reset();
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
		//printSceneCodes(scene);
		int gene = 0;
		int horWidth = 4;
		int verWidth = 3;
		int geneSize = 2*verWidth*2*horWidth*allBlockTypes.length*nb;
		for (int i = 11-horWidth; i < 11+horWidth; ++i){
			for (int j = 9-(verWidth); j < 9+(verWidth); ++j){
				for (int k = 0; k < allBlockTypes.length; ++k){
					for (int l = 0; l < nb; ++l){
						action[l] = action[l] || (genes[gene++] == 1 && scene[i][j] == allBlockTypes[k]);
					}
				}
			}
		}

		action[Mario.KEY_JUMP] = action[Mario.KEY_JUMP] && !(++jump >= 30);
		action[0] = false;
		action[2] = false;
		if (action[Mario.KEY_RIGHT])
			action[Mario.KEY_LEFT ] = false;
		//System.out.println(gene);
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
				if ((i == 11) && (j == 11)) {
					System.out.print("   M");
				}
				else {
					//if (scene[i][j] != 1 && scene[i][j] != 25 && scene[i][j] != 80 && scene[i][j] !=  93 && scene[i][j] !=  2 && scene[i][j] !=  3&& scene[i][j] !=  -24 && scene[i][j] !=  2 && scene[i][j] !=  -60 && scene[i][j] !=  -62 && scene[i][j] != 0)
						System.out.print(String.format("%4d", scene[i][j]));
				}
			}
			System.out.println();
		}
		System.out.println(" ...........................................................................................");
	}

}


