/**
 * The class with the main method to drive the optimization process
 */
import java.util.Random;
import java.util.Scanner;

public class Optimizer {
	/*************************************************
    *  PARAMETERS TO CONTROL THE OPTIMIZATION PROCESS
	**************************************************/
	static int SEED = 4; // A seed for the random number generator to produce consistent results
	public static Random prng; // The prng that should be used throughout the optimization process
	public static int VERBOSITY = 0; // A variable that controls how much output the optimization
									// process produces
	static int populationSize = 100; // The size of a generation in the GA
	static int generations = 100; // The number of generations in the optimization process
	
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

		double score = findShortestPath(lock);
	}
	
	private static double findShortestPath(Lock lock) {
		// The operators used to produce the initial solutions
		InitializationOperator[] initOps = { new InitOpNextClosest()};
		// The weight that determines how likely each initialization operator will be used
		double[] initOpWeights = {1.0};
		
		// A list of mutation operators to apply to the solutions
		MutationOperator[] mutationOperators = { new MutateReverse(), new MutateShift(), 
												 new MutateReverse(), new MutateShift(), 
												 new MutateReverse(), new MutateShift()};
		// The maximum number of times each mutation operator will run
		int[] mutationMaxTimes = {1000, 1000, 1000, 1000, 1000, 1000};

		Population population = new Population(lock, initOps, initOpWeights, populationSize);

		for (int g = 0; g < generations; g++) {
			System.out.println("Generation " + (g+1));
			population.runGeneration(mutationOperators, mutationMaxTimes);
		}
		System.out.print("Best solution, score: ");
		System.out.println(population.getBest().getScore());
		return population.getBest().getScore();
	}
	
}
