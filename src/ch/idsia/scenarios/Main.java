package ch.idsia.scenarios;

import ch.idsia.agents.controllers.QLearningAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.CmdLineOptions;

import java.io.*;
import java.io.FileWriter;
import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;

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

        int generations = 1000;

        //Establishing junk.
        final String argsString = "-vis on -fps 100 -tl 200 -ld 0 -ag ch.idsia.agents.controllers.QLearningAgent";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
        final BasicTask basicTask = new BasicTask(cmdLineOptions);

        cmdLineOptions.setVisualization(false);

        double epsilon = 0.15;
        double learningRate = 0.2;
        double discountFactor = 0.6;
        boolean visualizeEvery100 = false;

        String learningType = "QLEARNING"; //Must be SARSA or QLEARNING

        QLearningAgent agent = new QLearningAgent(learningType);
        agent.setEpsilon(epsilon);
        agent.setLearning(learningRate);
        agent.setDiscount(discountFactor);
        cmdLineOptions.setAgent(agent);
        basicTask.reset(cmdLineOptions);

        HashMap<Integer, ArrayList<Double>> hm;

        int numSeeds = 10;
        Integer seeds[] = new Integer[10];
        for (int i = 0; i < numSeeds; i++){
            seeds[i] = (int)(Math.random()*Integer.MAX_VALUE);
        }

        float generationAverages[] = new float[generations];
        float cumulativeAverage = 0;

        for (int i = 0; i < generations; ++i) {
            generationAverages[i] = 0;
            for (int s = 0; s < numSeeds; s++) {
                cmdLineOptions.setLevelRandSeed(seeds[s]);
                if (visualizeEvery100)
                    cmdLineOptions.setVisualization((i-5) % 100 == 0);
                basicTask.reset(cmdLineOptions);
                basicTask.runOneEpisode();
                float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
                generationAverages[i] += tempVal;
            }
            hm = agent.getHashMap();
            agent = new QLearningAgent(learningType, hm, learningRate, discountFactor, epsilon);
            cmdLineOptions.setAgent(agent);
            generationAverages[i] = generationAverages[i]/numSeeds;
            cumulativeAverage += generationAverages[i];
            if (i < 30)
                System.out.println(i + "\t" + generationAverages[i] + "\t" + cumulativeAverage/(i+1) + "\t" + cumulativeAverage/(i+1));
            else{
                float temp = 0;
                for (int k = i-30; k<i; k++){
                    temp += generationAverages[k];
                }
                System.out.println(i + "\t" + generationAverages[i] + "\t" + temp/(30) + "\t" + cumulativeAverage/(i+1));
            }
        }

//        write(bestEver, "best.txt");


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}