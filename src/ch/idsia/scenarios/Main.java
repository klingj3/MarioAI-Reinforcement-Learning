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
        final String argsString = "-vis on -fps 100 -tl 200 -ld 0 -ag ch.idsia.agents.controllers.QLearningAgent";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        final MarioCustomSystemOfValues sov = new MarioCustomSystemOfValues();

        cmdLineOptions.setVisualization(false);
        QLearningAgent agent = new QLearningAgent();
        cmdLineOptions.setAgent(agent);
        basicTask.reset(cmdLineOptions);
        double epsilon = 0.03;
        double minEpsilon = 0.01;
        for (int i = 0; i < generations; ++i) {
            cmdLineOptions.setVisualization((i)%7894132 == 0);
            epsilon = Math.max(minEpsilon, epsilon-(0.001));

            agent.setEpsilon(epsilon);
            basicTask.reset(cmdLineOptions);
            basicTask.runOneEpisode();
            float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(sov);
            System.out.println(i + ",\t" + tempVal);
        }

//        write(bestEver, "best.txt");


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}