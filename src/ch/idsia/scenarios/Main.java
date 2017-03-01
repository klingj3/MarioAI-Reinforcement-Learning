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
    public static byte[] breed(byte[] a, byte[] b, byte[] bestToDate, double percentCrossover, double percentMutate) {
        //Crossover
        Random r = new Random();
        Random rDoub = new Random();
        byte[] ret;
        if (rDoub.nextDouble() < .9)
             ret = a.clone();
        else
            ret = bestToDate.clone();

        double crossover = percentCrossover * (double)(a.length);
        while (rDoub.nextDouble() < crossover) {
            crossover -= 1;
            int crossPos = r.nextInt(ret.length);
            ret[crossPos] = b[crossPos];
        }
        //Mutate
        double mutate = (percentMutate)*(double)(a.length);
        while (rDoub.nextDouble() < mutate) {
            mutate -= 1;
            int mutPos = r.nextInt(ret.length);
            ret[mutPos] = (byte) Math.abs(ret[mutPos] - 1);
        }
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

    public static byte[] randomParent(byte[][] parents){
        return parents[new Random().nextInt(parents.length)];
    }

    public static float average(float[] scores){
        float ret = 0;
        for (int i = 0; i < scores.length; i++){
            ret += scores[i];
        }
        return ret/scores.length;
    }

    public static void main(String[] args) {
        boolean genesGiven = false;
        boolean elitism = false;

        byte[] parentA;
        byte[] parentB;
        int len = 450;

        if (genesGiven) {
            parentA = fileToByte("best.txt");
            parentB = parentA.clone();
        } else {
            parentA = new byte[len];
            parentB = new byte[len];
            for (int i = 0; i < len; i++) {
                parentA[i] = 0;
                parentB[i] = 1;
            }
        }

        int generations = 500;
        int c = 50;
        int p = 8;
        Random r2 = new Random();


        double mutateLevel = 0.0015;
        double crossoverLevel = 0.25;
        byte[][] parents = new byte[p][len];
        float[] scores = new float[p];
        for (int i = 0; i < p; i++){
            scores[i] = (float)0;
        }
        byte[][] children = new byte[c][len];

        //Establishes seeds.
        int numSeeds = 5;
        int[] seeds = new int[numSeeds];
        for (int i = 0; i < numSeeds; i++){
            seeds[i] = r2.nextInt(1000);
        }

        //Establishing junk.
        final String argsString = "-vis on -fps 100 -tl 100 -ld 0 -ag ch.idsia.agents.controllers.jk";
        final CmdLineOptions cmdLineOptions = new CmdLineOptions(argsString);
        final BasicTask basicTask = new BasicTask(cmdLineOptions);
        final MarioCustomSystemOfValues sov = new MarioCustomSystemOfValues();

        int tempMutate;
        float value = 0;
        float prevAverage = 0;

        int timelimit = 5;


        /**For saving best species*/
        byte[] bestEver = parentA.clone();
        float bestValue = 0;

        //Creates the innitial chidlren
        for (int i = 0; i < p; i++){
            children[i] = breed(parentA, parentB, bestEver, crossoverLevel, mutateLevel);
        }


        for (int i = 0; i < generations; ++i) {
            cmdLineOptions.setVisualization(false);
            cmdLineOptions.setLevelDifficulty(0);
            timelimit = Math.min(Math.max(5, i/3), 100);
            if (!genesGiven)
                cmdLineOptions.setTimeLimit(timelimit);
            if (average(scores) == prevAverage && i%10 != 0)
                mutateLevel += 0.0002;
            else
                mutateLevel = 0.0015;
            System.out.println(i + "   " + average(scores));// + " with mutation rate " + mutateLevel*100 + "% and crossover rate " + crossoverLevel*100 + "%");
            prevAverage = average(scores);
            for (int j = 0; j < c; j++){
                children[j] = breed(randomParent(parents), randomParent(parents), bestEver, crossoverLevel, mutateLevel);
            }

            if (!elitism) {
                parents = new byte[p][len];
                scores = new float[p];
            }
            for (int j = 0; j < c; j++) {
                value = 0;
                jk currAgent = new jk(children[j]);
                for (int k = 0; k < numSeeds; k++) {
                    cmdLineOptions.setVisualization(j==0 && i%3 == 0 && k == 0);
                    cmdLineOptions.setLevelRandSeed(seeds[k]);
                    cmdLineOptions.setAgent(currAgent);
                    basicTask.reset(cmdLineOptions);
                    basicTask.runOneEpisode();
                    float tempVal = basicTask.getEnvironment().getEvaluationInfo().computeWeightedFitness(sov);
                    value +=  nb2i(tempVal%500 - (float)(6.4) < .02) * tempVal;
                }
                value = value / numSeeds;
                if (value > bestValue){
                    bestEver = children[j].clone();
                    bestValue = value;
                }
                if (!elitism && j < p){
                   parents[j] = children[j].clone();
                   scores[j] = value;
                }
                else{
                    int minIndex = -1;
                    float minValue = value;
                    for (int m = 0; m < p; m++){
                        if ((minValue - scores[m]) > 0.05){
                            minIndex = m;
                            minValue = scores[m];
                        }
                    }
                    if (minIndex >= 0){
                        parents[minIndex] = children[j].clone();
                        scores[minIndex] = value;
                    }
                }
            }
        }

        write(bestEver, "best.txt");;


        //System.out.println("Average fitness is " + average / totalCount + "\n");
        System.exit(0);

    }
}