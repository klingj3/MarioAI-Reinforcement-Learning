package ch.idsia.scenarios;

import ch.idsia.agents.Agent;
import ch.idsia.agents.controllers.jk;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.MarioCustomSystemOfValues;
import ch.idsia.tools.CmdLineOptions;

import java.nio.file.Files;
import java.util.Random;
import java.io.*;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class Main {
    public static byte[] breed(byte[] a, byte[] b, float value) {
        byte[] ret = new byte[a.length];
        int percentCrossover = 70;
        Random r = new Random();
        ret = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            if (r.nextInt(10000) < percentCrossover) {
                ret[i] = b[i];
            } else
                ret[i] = a[i];
        }
        return ret;
    }

    public static byte[] mutate(byte[] a, int percentMutate) {
        byte[] ret = new byte[a.length];
        Random r = new Random();
        for (int i = 0; i < a.length; i++) {
            ret[i] = a[i];
            if (r.nextInt(10000) < percentMutate) {
                ret[i] = (byte) Math.abs(((int) a[i] - 1));
            }
        }
        return ret;
    }

    public static byte[] flipOneByte(byte[] a) {
        byte[] ret = new byte[a.length];
        Random r = new Random();
        int randInt = r.nextInt(a.length);
        ret[randInt] = (byte) Math.abs(((int) a[randInt] - 1));
        return ret;
    }

    public static void write(byte[] a, String filename) {
        try {
            Writer wr = new FileWriter(filename);
            for (int i = 0; i < a.length; i++) {
                wr.write(Byte.toString(a[i]) + " ");
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

    public static byte[] fileToByte(String filename) {
        byte[] ret = new byte[960];
        try {
            int i = 0;
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextInt()) {
                ret[i++] = (byte) scanner.nextInt();
            }
        } catch (IOException e) {

        }
        return ret;
    }

    public static void main(String[] args) {
        boolean genesGiven = true;
        byte[] parentA;
        byte[] parentB;

        if (genesGiven) {
            parentA = fileToByte("parentA.txt");
            parentB = fileToByte("parentB.txt");
        } else {
            int len = 540;
            parentA = new byte[len];
            parentB = new byte[len];
            for (int i = 0; i < len; i++) {
                parentA[i] = 0;
                parentB[i] = 0;
            }
        }
        float prevFitness = 0;
        float bestFitness = 0;
        float secondBest = 0;

        int generations = 20;
        int genSize = 100;
        Random r2 = new Random();

        int numSeeds = 1;
        int[] seeds = new int[numSeeds];
        for (int i = 0; i < numSeeds; i++){
            seeds[i] = i;
        }


        //final String argsString = "-vis off -ld 25 -ag ch.idsia.agents.controllers.ScaredShooty";
        final String argsString = "-vis on -fps 100 -tl 85 -ld 0 -ag ch.idsia.agents.controllers.jk";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
//        final Environment environment = new MarioEnvironment();
        Agent mutatingAgent = new jk(parentA);
//        final Agent agent = cmdLineOptions.getAgent();
//        final Agent a = AgentsPool.load("ch.idsia.controllers.agents.controllers.ForwardJumpingAgent");
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        int totalCount = 1;
        float average = 0;
        final MarioCustomSystemOfValues sov = new MarioCustomSystemOfValues();
        byte[] crossover;
        byte[] tempCrossover;


        int tempMutate;
        int mutateLevel = 20; //Out of 10000
        float value = 0;

        float prevBest = 0;
        int immobileCounter = 0;
        int timelimit = 5;

        for (int i = 0; i < generations; ++i) {
            if (i < 12 && !genesGiven)
                mutateLevel--;
            else
                mutateLevel = 8;

            cmdLineOptions.setVisualization(false);
            cmdLineOptions.setLevelDifficulty(0);
            if (!genesGiven)
                cmdLineOptions.setTimeLimit(timelimit);
            System.out.print("Breeding generation " + i + " via specimens of fitness " + bestFitness + " and " + secondBest);
            crossover = breed(parentA, parentB, value);
            if (Math.abs(prevBest - bestFitness) > 0.5 || immobileCounter++ > 6)
                immobileCounter = 0;
            if (!genesGiven)
                timelimit = Math.min(timelimit+1, 80);
            prevBest = bestFitness;
            tempMutate = mutateLevel + immobileCounter;
            System.out.println( " with mutation " + tempMutate);

            for (int j = 0; j < genSize; j++) {
                value = 0;
                tempCrossover = mutate(crossover, tempMutate);
                cmdLineOptions.setVisualization(j%(genSize/-1)==0);
                mutatingAgent = new jk(tempCrossover);
                for (int k = 0; k < numSeeds; k++) {
                    cmdLineOptions.setLevelRandSeed(seeds[k]);
                    cmdLineOptions.setAgent(mutatingAgent);
                    basicTask.reset(cmdLineOptions);
                    basicTask.runOneEpisode();
                    float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(sov);
                    value +=  nb2i(tempVal%500 - (float)(6.4) < .02) * tempVal;
                    cmdLineOptions.setVisualization(false);
                }
                value = value / numSeeds;
                if (value > bestFitness) {
                    secondBest = bestFitness;
                    parentB = parentA.clone();
                    parentA = tempCrossover.clone();
                    bestFitness = value;
                    basicTask.runOneEpisode();
                } else if (value > secondBest) {
                    parentB = tempCrossover.clone();
                    secondBest = value;
                    //System.out.println(value);
                }
            }
        }

        write(parentA, "parentA.txt");
        write(parentB, "parentB.txt");


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}