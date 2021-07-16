/**
 * The class with the main method to compare the fitness of locks
 */
import java.util.Random;
import java.util.Scanner;

public class FitnessGenerator {
    /*************************************************
     *  PARAMETERS TO CONTROL THE FITNESS PROCESS
     **************************************************/
    static int SEED = 4; // A seed for the random number generator to produce consistent results
    public static Random prng; // The prng that should be used throughout the optimization process

    static int MIN_DISTANCE_FROM_START_WORD = 0; // The minimum distance from the starting word for a word to
    // be considered as a combination

    /**
     * The driver method for the optimization process
     * @param args not used
     */
    public static void main(String[] args) {
        prng = new Random(SEED);
        // Read wheel configurations
        Scanner sc = new Scanner(System.in);
        int wheelCount = Integer.valueOf(sc.nextLine());
        String[] wheels = new String[wheelCount];
        for (int i = 0; i < wheelCount; i++) {
            wheels[i] = sc.nextLine().toLowerCase();
        }
        Lock lock = new Lock(wheels, MIN_DISTANCE_FROM_START_WORD);

        double bestSolutionFitness = lock.getFitness();
        System.out.println("Lock Lower Bound Fitness Score: " + bestSolutionFitness);
    }
}
