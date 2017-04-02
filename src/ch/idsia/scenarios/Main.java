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

        int generations = 1000;

        //Establishing junk.
        final String argsString = "-vis on -fps 30 -tl 200 -ld 0 -ag ch.idsia.agents.controllers.QLearningAgent";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        final MarioCustomSystemOfValues sov = new MarioCustomSystemOfValues();

        cmdLineOptions.setLevelRandSeed(6);
        cmdLineOptions.setVisualization(false);
        double epsilon = 0.3;
        double minEpsilon = 0.1;

        double learningRate = 0.2;
        double discountFactor = 0.7;

        float average = 0;

        QLearningAgent agent = new QLearningAgent();
        agent.setLearning(learningRate);
        agent.setDiscount(discountFactor);
        cmdLineOptions.setAgent(agent);
        basicTask.reset(cmdLineOptions);

        for (int i = 0; i < generations; ++i) {
            //cmdLineOptions.setVisualization((i+1)%50 == 0);
            agent.setEpsilon(epsilon);
            basicTask.reset(cmdLineOptions);
            basicTask.runOneEpisode();
            float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness();
            average += tempVal;
            System.out.println(i + "\t" + tempVal + "\t" + average/(i+1));
        }

//        write(bestEver, "best.txt");


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}