package ch.idsia.benchmark.tasks;

import ch.idsia.agents.Agent;
import ch.idsia.tools.CmdLineOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Apr 8, 2009
 * Time: 11:20:41 AM
 * Package: ch.idsia.maibe.tasks
 */

public interface Task
{
float[] evaluate(Agent controller);

void setOptions(CmdLineOptions options);

CmdLineOptions getOptions();

void doEpisodes(int amount, boolean verbose);

boolean isFinished();

void reset();

String getName();
}
