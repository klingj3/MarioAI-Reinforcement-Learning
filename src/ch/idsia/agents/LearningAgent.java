package ch.idsia.agents;

import ch.idsia.benchmark.tasks.ProgressTask;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey@idsia.ch
 * Date: May 19, 2010
 * Time: 3:45:19 PM
 */

public interface LearningAgent extends Agent
{
    void learn();

    void giveReward(float reward);

    void newEpisode();

    void setTask(ProgressTask task);

    void setNumberOfTrials(int num);

    Agent getBestAgent();

    void init();
}
