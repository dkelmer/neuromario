package competition.research.kp;

import com.heatonresearch.book.introneuralnet.neural.feedforward.FeedforwardLayer;
import com.heatonresearch.book.introneuralnet.neural.feedforward.FeedforwardNetwork;
import com.heatonresearch.book.introneuralnet.neural.feedforward.train.Train;
import com.heatonresearch.book.introneuralnet.neural.feedforward.train.anneal.NeuralSimulatedAnnealing;
import com.heatonresearch.book.introneuralnet.neural.feedforward.train.backpropagation.Backpropagation;
import com.heatonresearch.book.introneuralnet.neural.feedforward.train.genetic.TrainingSetNeuralGeneticAlgorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by K&P bitches on 4/24/16.
 */
public class KPTrainer {

    static List<double[]> actual = new ArrayList<double[]>();
    static List<double[]> ideal = new ArrayList<double[]>();

    public static void main(String[] args) throws Exception{
        // this list will store all the created arrays
        // use a BufferedReader to get the handy readLine() function
        BufferedReader reader = new BufferedReader(new FileReader("competition/research/kp/traces/01actual.txt"));

        // this reads in all the lines. If you only want the first thousand, just
        // replace these loop conditions with a regular counter variable
        // use a BufferedReader to get the handy readLine() function

        // this reads in all the lines. If you only want the first thousand, just
        // replace these loop conditions with a regular counter variable
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] doubleStrings = line.split(",");
            double[] doubles = new double[doubleStrings.length];
            for (int i = 0; i < doubles.length; ++i) {
                doubles[i] = (double) Integer.parseInt(doubleStrings[i]);
            }
            actual.add(doubles);

        }
        reader.close();

        reader = new BufferedReader(new FileReader("competition/research/kp/traces/01ideal.txt"));

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] doubleStrings = line.split(",");
            double[] doubles = new double[doubleStrings.length];
            for (int i = 0; i < doubleStrings.length; ++i) {
                if(doubleStrings[i].equals("false")){
                    doubles[i] = -1.0;
                }
                else{
                    doubles[i] = 1.0;
                }
            }
            ideal.add(doubles);
        }
        reader.close();

        double[][] actualArr = new double[actual.size()][actual.get(0).length];
        double[][] idealArr = new double[ideal.size()][ideal.get(0).length];

        for(int i = 0; i < actual.size(); i++){
            actualArr[i] = actual.get(i);
        }

        for(int i = 0; i < ideal.size(); i++){
            idealArr[i] = ideal.get(i);
        }

        final FeedforwardNetwork network = new FeedforwardNetwork();
        network.addLayer(new FeedforwardLayer(28));
        network.addLayer(new FeedforwardLayer(16));
        network.addLayer(new FeedforwardLayer(5));
        network.reset();
        //final Train train = new Backpropagation(network, actualArr, idealArr, 0.7, 0.9);
        final NeuralSimulatedAnnealing train = new NeuralSimulatedAnnealing(network, actualArr, idealArr, 10,2,100);
        //final TrainingSetNeuralGeneticAlgorithm train = new TrainingSetNeuralGeneticAlgorithm(network, true, actualArr,
             //   idealArr, 500, 0.1, 0.25);
        int epoch = 1;
        do {
            train.iteration();
            System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;
        } while ((epoch < 100) && train.getError() > 0.001);

        System.out.println("Neural Network Results:");
        for(int i = 0; i < idealArr[0].length; i++){
            final double results[] = network.computeOutputs(actualArr[i]);
            System.out.println(actualArr[i][0] + "," + actualArr[i][1] + ", results =" + results[0] + ",ideal ="
            + idealArr[i][0]);
        }

    }
}
