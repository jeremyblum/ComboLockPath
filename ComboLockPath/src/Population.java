/**
 * The population of lock configurations for the GA
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Population {
	private enum ResultCode {DUPLICATE, SUCCESS, BEST_SO_FAR}; // the result of adding a path configuration to the population
	private ArrayDeque<Solution> populationList = new ArrayDeque<>(); // a queue of solutions, used to remove the old generation
	private HashSet<Solution> populationSet = new HashSet<>(); // the current set of solutions, used to prevent duplicate
															   // configurations
	private Solution bestSolutionSoFar; // The best configuration so far
	private int targetSize; // The target population size
	private Long lastGenerationTime;
	private Lock lock;

	private Double bestSolutionFitness; //The fitness of the best configuration so far
	
	/**
	 * Seed the population with initial solutions
	 * @param agents an array of initialization agents that generate initial solutions
	 * @param weights weights[i]/[Sum of weights] is the probability that agents[i] will run
	 * @param targetSize the size of the population 
	 */
	Population(Lock lock, InitializationOperator[] agents, double[] weights, int targetSize) {
		this.lock = lock;
		this.targetSize = targetSize;
		if (agents.length == 0) {
			throw new IllegalArgumentException("You must pass at least one initialization operator to seed the population.");
		}
		if (agents.length != weights.length) {
			throw new IllegalArgumentException("The weights array must be the same length as the agents array.");
		}
		double sum = 0.0;
		for (int i = 0; i < agents.length; i++) {
			if (weights[i] <= 0.0) {
				throw new IllegalArgumentException("The weights array must contain positive values.");
			}
			sum += weights[i];
		}
		
		// Seed the population with initial solutions
		while (populationList.size() != targetSize) {
			InitializationOperator agent = rouletteSelection(agents, weights, sum);
			Solution solution = agent.run(lock);
			switch (insert(solution)) {
			case SUCCESS:
				if (Optimizer.VERBOSITY > 1) System.out.println(agent.getName() + ": " + solution.getScore() + " " + solution.toString());
				break;
			case DUPLICATE:
				if (Optimizer.VERBOSITY > 0) System.out.println(agent.getName() + ": produced duplicate solution." + " " + solution.toString());
				break;
			case BEST_SO_FAR:
				System.out.println("BEST " + agent.getName() + ": " + solution.getScore());
				break;
			}
		}
	}

	/**
	 * Create a new generation of child solutions
	 * 
	 * @param mutationOperators an array of mutation operators to apply to the children
	 * @param maxMutations maxMutations[i] is the maximum number of mutations to apply mutationOperator[i]
	 */
	public void runGeneration(MutationOperator[] mutationOperators, int[] maxMutations) {
		// Create children
		for (int i = 0; i < targetSize; i += 2)  {
			generateChildren(mutationOperators, maxMutations);
		}
		
		// Kill the parents
		/*while (populationList.size() > targetSize) {
			populationSet.remove(populationList.remove());
		}*/
		// Use elitism and kill the least fit solutions
		PriorityQueue<Solution> pq = new PriorityQueue<>();
		for (Solution solution: populationList) {
			pq.add(solution);
		}
		for (int i = 0; i < targetSize; i++) pq.remove();
		while(pq.size() > 0) {
			Solution unfit = pq.remove();
			populationSet.remove(unfit);
		}
		double bestInPopulation = Double.MAX_VALUE;
		double worstInPopulation = 0.0;
		double sumOfPopulation = 0.0;
		populationList.clear();
		for (Solution solution: populationSet) {
			populationList.add(solution);
			bestInPopulation = Math.min(bestInPopulation, solution.getScore());
			worstInPopulation = Math.max(worstInPopulation, solution.getScore());
			sumOfPopulation += solution.getScore();
		}
		if (lastGenerationTime != null)
			System.out.println("Time (ms): " + (System.currentTimeMillis() - lastGenerationTime));
		lastGenerationTime = System.currentTimeMillis();
		System.out.println("Population Stats: best: " + bestInPopulation + " worst: " + worstInPopulation + 
				" ave: " + sumOfPopulation / populationList.size() + " bsf: " + bestSolutionFitness);
	}
	
	/**
	 * Go through the selection, crossover, and mutation phases
	 * @param mutationOperators the mutation operators to apply to offspring
	 * @param maxMutations we choose a random number in [0..maxMutations[i]) to apply operator at mutation[i]
	 */
	public void generateChildren(MutationOperator[] mutationOperators, int[] maxMutations) {
		Solution[] parents = selectParents();
		Solution[] children = crossover(parents);
		if (Optimizer.VERBOSITY == 2) System.out.println("Parent fitness " + parents[0].getScore() + " " + parents[1].getScore());
		for (int i = 0; i < children.length; i++) {
			// Run a greedy mutation process for child
			Solution solution = children[i];
			double startScore = children[i].getScore();
			for (int j = 0; j < mutationOperators.length; j++) {
				MutationOperator mutation =  mutationOperators[j];
				int times = Optimizer.prng.nextInt(maxMutations[j]);
				for (int t = 0; t < times; t++) {
					solution = mutation.run(lock,children[i]); // This would be another way to generate the mutations by always
															// starting with the best mutation seen so far
					//solution = mutation.run(lock, solution);
					if (solution.getScore() < children[i].getScore()) {
						children[i] = solution;
					}
				}
			}
			if (Optimizer.VERBOSITY == 2) System.out.println("mutations improved from " + startScore + " to " + children[i].getScore());
			switch(insert(children[i])) {
			case DUPLICATE:
				if (Optimizer.VERBOSITY > 0) System.out.println("Offspring produced was duplicate. " + children[i].toString());
				break;
			case BEST_SO_FAR:
				System.out.println("BEST " + children[i].getScore());
				break;
			case SUCCESS:
				if (Optimizer.VERBOSITY > 0) System.out.println(children[i].getScore());
			}
		}
	}

	/** 
	 * Accessor method 
	 * @return the best solution seen so far
	 */
	public Solution getBest() {
		return bestSolutionSoFar;
	}
	
	/**
	 * Accessor method
	 * @return the number of children in the population
	 */
	public int getSize() {
		return populationList.size();
	}
	
	/**
	 * Select an agent using a roulette wheel selection
	 * @param agents the array of agents
	 * @param weights weights[i]/sum is the probability that agents[i] will run
	 * @param sum the sum of values in the weights array
	 * @return
	 */
	private InitializationOperator rouletteSelection(InitializationOperator[] agents, double[] weights, double sum) {
		double selector = Optimizer.prng.nextDouble() * sum;
		int index = 0;
		while (selector > weights[index] && index < agents.length - 1) {
			selector -= weights[index];
			index++;
		}
		return agents[index];
	}
	
	/**
	 * Use roulette wheel selection to select fit parents
	 * @return an arraylist with two parents
	 */
	private Solution[] selectParents() {
		Solution[] parents = new Solution[2];
		double sum = 0;
		int count = 0;
		double maxScore = 0;
		for (Solution solution: populationList) {
			if (count == targetSize) break;
			sum += solution.getScore();
			maxScore = Math.max(maxScore, solution.getScore() + 1);
			count++;
		}
		double selector = (count * maxScore - sum) * Optimizer.prng.nextDouble();
		count = 0;
		for (Solution solution: populationList) {
			selector -= (maxScore - solution.getScore());
			if (selector <= 0 || count == targetSize - 1) {
				parents[0] = solution;
				break;
			}
			count++;
		}
		sum = 0;
		count = 0;
		for (Solution solution: populationList) {
			if (count == targetSize) break;
			if (solution != parents[0]) {
				sum += solution.getScore();
				count++;
			}
		}
		selector = sum * Optimizer.prng.nextDouble();
		count = 0;
		for (Solution solution: populationList) {
			if (solution != parents[0]) {
				selector -= solution.getScore();
				if (selector <= 0) {
					parents[1] = solution;
					break;
				}
			}
			count++;
			if (count == targetSize) {
				parents[1] = solution;
				if (parents[1] == parents[0])
					parents[1] = populationList.getFirst();
				break;
			}
		}
		return parents;
	}


	private Solution[] crossover(Solution[] parents) {
		Solution[] children = {new Solution(lock), new Solution(lock)};
		if (parents[0].equals(parents[1])) {
			throw new RuntimeException("Duplicate parents: " + parents[0] + "\n" + parents[1]);
		}
		// Find a point where the solutions diverge
		HashSet<Integer> child0Words = new HashSet<>();
		HashSet<Integer> child1Words = new HashSet<>();
		int index = 0;
		while(parents[0].getIthWord(index) == parents[1].getIthWord(index)) {
			int word = parents[0].getIthWord(index);
			children[0].addWord(word);
			children[1].addWord(word);
			child0Words.add(word);
			child1Words.add(word);
			index++;
		}
		
		if (index == lock.getWordCount()) {
			throw new RuntimeException("Duplicate solutions were found in the population");
		}
		
		// Add words from parent 0 to child 1 until we get to the next word queued up in parent 1
		int nextWordIn1 = parents[1].getIthWord(index);
		int index1 = index;
		while (parents[0].getIthWord(index1) != nextWordIn1 && index1 < lock.getWordCount()) {
			int word = parents[0].getIthWord(index1);
			if (child1Words.contains(word)) throw new RuntimeException("Error in algorithm");
			children[1].addWord(word);
			child1Words.add(word);
			index1++;
		}
		
		// Add words from parent 1 to child 0 until we get to the next word queued up in parent 0
		int index2 = index;
		int nextWordIn0 = parents[0].getIthWord(index2);
		while (parents[1].getIthWord(index2) != nextWordIn0 && index2 < lock.getWordCount()) {
			int word = parents[1].getIthWord(index2);
			if (child0Words.contains(word)) throw new RuntimeException("Error in algorithm");
			children[0].addWord(word);
			child0Words.add(word);
			index2++;
		}
		
		// Add remaining words to children using closest city heuristic
		while (index < lock.getWordCount()) {
			int word = parents[0].getIthWord(index);
			if (!child0Words.contains(word)) {
				children[0].addWord(word);
			}
			word = parents[1].getIthWord(index);
			if (!child1Words.contains(word)) {
				children[1].addWord(word);
			}
			index++;
		}
		
		return children;
	}
	
	/**
	 * Create 2 children by applying a crossover operator to the parents.
	 * At each wheel we choose a random half of the letters for one offspring, and use
	 * the other letters for the other offspring.
	 * 
	 * @param parents the parents
	 * @return an array of two child lock configurations
	 */
	private Solution[] crossover_v1(Solution[] parents) {
		Solution[] children = {new Solution(lock), new Solution(lock)};
		
		int point = Optimizer.prng.nextInt(lock.getWordCount()-1);
		
		HashSet<Integer> child1Words = new HashSet<>();
		HashSet<Integer> child2Words = new HashSet<>();

		for (int i = 0; i <= point; i++) {
			int word = parents[0].getIthWord(i);
			children[0].addWord(word);
			child1Words.add(word);
			word = parents[1].getIthWord(i);
			children[1].addWord(word);
			child2Words.add(word);
		}
		for (int i = 0; i < lock.getWordCount(); i++) {
			int word = parents[1].getIthWord(i);
			if (!child1Words.contains(word)) {
				children[0].addWord(word);
				child1Words.add(word);
			}
			word = parents[0].getIthWord(i);
			if (!child2Words.contains(word)) {
				children[1].addWord(word);
				child2Words.add(word);
			}
		}
		if (child1Words.size() != lock.getWordCount() || child1Words.size() != lock.getWordCount()) {
			throw new RuntimeException("Child has an incorrect number of words.");
		}
		
		return children;
	}

	/**
	 * Add a solution to the population, if there is not an identical solution already present
	 * @param solution the solution to be added
	 * @return whether the solution was not added because it is a duplicate, whether it was added, and whether
	 *         it is the best solution seen so far
	 */
	private ResultCode insert(Solution solution) {
		if (populationSet.contains(solution)) return ResultCode.DUPLICATE;
		double score = solution.getScore();
		populationList.add(solution);
		populationSet.add(solution);
		if (bestSolutionSoFar == null || bestSolutionSoFar.getScore() > score) {
			bestSolutionSoFar = solution;
			bestSolutionFitness = solution.getFitness(); //added JP
			return ResultCode.BEST_SO_FAR;
		}
		return ResultCode.SUCCESS;
	}
	
	
}
