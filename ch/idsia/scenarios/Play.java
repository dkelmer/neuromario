package ch.idsia.scenarios;

import ch.idsia.ai.agents.human.HumanAgentForPlayTrace;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import competition.cig.robinbaumgarten.AStarAgent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 5, 2009
 * Time: 12:46:43 PM
 */

/**
 * The <code>Play</code> class shows how simple is to run an iMario benchmark.
 * It shows how to set up some parameters, create a task,
 * use the CmdLineParameters class to set up options from command line if any.
 * Defaults are used otherwise.
 *
 * @author  Julian Togelius, Sergey Karakovskiy
 * @version 1.0, May 5, 2009
 * @since   JDK1.0
 */

public class Play {
    /**
     * <p>An entry point of the class.
     *
     * @param args input parameters for customization of the benchmark.
     *
     * @see ch.idsia.scenarios.MainRun
     * @see ch.idsia.tools.CmdLineOptions
     * @see ch.idsia.tools.EvaluationOptions
     *
     * @since   iMario1.0
     */

    public static void main(String[] args) {



//        EvaluationOptions options = new CmdLineOptions(args);
//        Task task = new ProgressTask(options);
////        options.setMaxFPS(false);
////        options.setVisualization(true);
////        options.setNumberOfTrials(1);
//     //   options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
//
//        options.setLevelDifficulty(1);
//        task.setOptions(options);
//
//        HumanAgentForPlayTrace agent = (HumanAgentForPlayTrace) options.getAgent();

//        for (int i = 0; i < 20; i++) {
//            System.out.println("Starting game " + i + " with seed " + i%5);
//            agent.feature = new StringBuilder();
//            agent.target = new StringBuilder();
//
//            options.setLevelRandSeed(i%5); //10 is the def
//
//            double d = task.evaluate(options.getAgent())[0];
//
//            try {
//
//                File feature = new File("/Users/giorgio/projects/neuromario/competition/research/kp/traces/features/human5level/5-humanF" + i);
//                File target = new File("/Users/giorgio/projects/neuromario/competition/research/kp/traces/targets/human5level/5-humanT" + i);
//
//                // if file doesnt exists, then create it
//                //            if (!feature.exists()) {
//                //                feature.createNewFile();
//                //                target.createNewFile();
//                //            }
//
//                FileWriter featurefw = new FileWriter(feature.getAbsoluteFile());
//                BufferedWriter featurebw = new BufferedWriter(featurefw);
//                featurebw.write(agent.feature.toString());
//                featurebw.close();
//
//                FileWriter targetfw = new FileWriter(target.getAbsoluteFile());
//                BufferedWriter targetbw = new BufferedWriter(targetfw);
//                targetbw.write(agent.target.toString());
//                targetbw.close();
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

//        System.out.println("Simulation/Play finished");

        EvaluationOptions options = new CmdLineOptions(args);
        Task task = new ProgressTask(options);
//        options.setMaxFPS(false);
//        options.setVisualization(true);
//        options.setNumberOfTrials(1);
//        options.setLevelRandSeed((int) (Math.random () * Integer.MAX_VALUE));
        options.setLevelRandSeed(10);
        options.setLevelDifficulty(1);
        task.setOptions(options);

        System.out.println ("Score: " + task.evaluate (options.getAgent())[0]);
        System.out.println("Simulation/Play finished");
    }
}
