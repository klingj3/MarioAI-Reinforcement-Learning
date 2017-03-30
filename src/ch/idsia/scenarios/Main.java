package ch.idsia.scenarios;

import ch.idsia.agents.controllers.QLearningAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.CmdLineOptions;

import java.io.*;
import java.io.FileWriter;
import java.util.Scanner;
/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class Main {

    public static void write(double[] a, String filename) {
        try {
            Writer wr = new FileWriter(filename);
            for (int i = 0; i < a.length; i++) {
                wr.write(Double.toString(a[i]) + " ");
            }
            wr.close();
        } catch (IOException e) {

        }
    }

    private static int nb2i(boolean b){
        if (b)
            return 0;
        return 1;
    }

    public static double[] fileTodouble(String filename) {
        double[] ret = new double[1500];
        try {
            int i = 0;
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextDouble() && i < ret.length) {
                double temp = scanner.nextDouble();
                ret[i++] = temp;
            }
        } catch (IOException e) {

        }
        return ret;
    }

    public static void main(String[] args) {

        int generations = 10000;

        //Establishing junk.
        final String argsString = "-vis on -fps 100 -tl 100 -ld 0 -ag ch.idsia.agents.controllers.QLearningAgent";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        final MarioCustomSystemOfValues sov = new MarioCustomSystemOfValues();

        cmdLineOptions.setLevelRandSeed(6);
        cmdLineOptions.setVisualization(false);
        QLearningAgent agent = new QLearningAgent();
        cmdLineOptions.setAgent(agent);
        basicTask.reset(cmdLineOptions);
        double epsilon = (float)0.01;
        double minEpsilon = (float)0.0005;
        float average = 0;
        for (int i = 0; i < generations; ++i) {
            //epsilon = Math.max(minEpsilon, epsilon-(0.00001));
            //cmdLineOptions.setVisualization((i+1)%50 == 0);
            agent.setEpsilon(epsilon);
            basicTask.reset(cmdLineOptions);
            basicTask.runOneEpisode();
            float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(sov);
            average += tempVal;
            System.out.println(i + ",\t" + tempVal + ",\t " + average/(float)(i)); //+ ",\t" + epsilon);
        }

//        write(bestEver, "best.txt");


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}