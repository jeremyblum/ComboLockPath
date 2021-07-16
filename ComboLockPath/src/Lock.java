import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

/**
 * A lock configuration
 */
public class Lock {
	private String[] wheels; // The wheels.  wheel[i] contains all of the letters on the ith wheel
	private String initialWord; // The initial word shown on the lock
	private ArrayList<String> words = new ArrayList<>(); // The list of words
	private HashSet<String> wordSet = new HashSet<>(); // The set of words that can be made
	private int [][] distances; // Stored distances between locks and words to speed up computation
	private Double fitness = null; // The fitness
	
	/**
	 * Initialize a lock
	 * 
	 * @param wheels - the letters on the lock wheels
	 * @param minDistanceFromStart - the minimum distance from the initial word for a combination to be considered
	 */
	public Lock(String[] wheels, int minDistanceFromStart) {
		this.wheels = wheels; 

		initialWord = "";
		for (int i = 0; i < wheels.length; i++) {
			initialWord += wheels[i].charAt(0);
		}

		Scanner sc;
		try {
			sc = new Scanner(new File("words_with_frequency.txt"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Missing word list");
		}
		while (sc.hasNext()) {
			String word = sc.next().toLowerCase();
			Long count = sc.nextLong();
			
			boolean ok = true;
			if (word.length() > wheels.length) { 
				ok = false;
			}
			else {
				word = String.format("%-"+ wheels.length + "s", word);
			}
			for (int i = 0; ok && i < word.length(); i++) {
				if (!wheels[i].toLowerCase().contains(word.substring(i,i+1))) ok = false;
			}
			
			if (ok && distance(initialWord, word) >= minDistanceFromStart) {
				if (!wordSet.contains(word)) {
					wordSet.add(word);
					words.add(word);
				}
			}
		}
		System.out.println("Total words: " + words.size());
		distances = new int[words.size()][words.size()];
		sc.close();
	}

	/**
	 * Get the ith word that can be made with this lock
	 * @param i
	 * @return
	 */
	String getWord(int i) {
		return words.get(i);
	}

	/**
	 * 
	 * @return the word that is shown when the lock is in the default position
	 */
	String getInitialWord() {
		return initialWord;
	}
	
	/**
	 * 
	 * @return the total number of valid words that can be made from this lock
	 */
	int getWordCount() {
		return words.size();
	}

	/**
	 * Get the lower bound fitness of the lock configuration
	 * @return a lower bound fitness, which is the sum of the total running distance by looking
	 *         at the distance of the closest word and next closest word of all the words the lock can create
	 */
	double getFitness() {
		if (fitness == null) {
			//System.out.println("Can make " + words.size());
			double[] overallAve = new double[words.size()];
			double aveSoFar = 0.0;
			double adjustment = -1.0;
			for (int i = 0; i < words.size(); i++) {
				int closestDist = Integer.MAX_VALUE;
				int closestNextDist = Integer.MAX_VALUE;
				int closestWord = -1;
				for (int j = 0; j < words.size(); j++) {
					if (j == i) continue;
					int wordDist = distance(i, j);
					if (wordDist < closestDist) {
						closestNextDist = closestDist;
						closestDist = wordDist;
						closestWord = j;
					}
					if (wordDist < closestNextDist && j != closestWord) {
						closestNextDist = wordDist;
					}
					if (closestNextDist == 2) break; // Already found two words very close
				}
				double ave = (closestDist + closestNextDist) / 2.0;
				double aveMinusClsDist = ave - closestDist;
				if(aveMinusClsDist > adjustment){
					adjustment = aveMinusClsDist;
				}
				overallAve[i] = 1 + ave;
			}
			Arrays.sort(overallAve);
			fitness = 0.0;
			for(int i = 0; i < overallAve.length; i++){
				aveSoFar = overallAve[i] + aveSoFar;
				fitness += aveSoFar;
			}

			fitness -= adjustment;
			fitness /= words.size();
			//System.out.println("Score: " + score + " in " + (System.currentTimeMillis() - time) + " ms");
		}
		return fitness;
	}
	
	/**
	 * The distance between two words
	 * @param index1 the position of the first word in the list. A value of -1 indicates the starting point.
	 * @param index2 the position of the second word in the list. A value of -1 indicates the starting point.
	 * @return the number of moves needed to change from the first word to the second
	 */
	public int distance(int index1, int index2) {
		// Return stored value if we have already computed this distance
		if (index1 >= 0 && index2 >= 0 && distances[index1][index2] != 0) return distances[index1][index2];
		
		String word1;
		if (index1 == -1) word1 = getInitialWord();
		else word1 = words.get(index1);
		
		String word2;
		if (index2 == -1) word2 = getInitialWord();
		else word2 = words.get(index2);

		int retVal = distance(word1, word2);

		if (index1 >= 0 && index2 >= 0) {
			distances[index1][index2] = retVal;
			distances[index2][index1] = retVal;
		}
		return retVal;
	}
	
	/**
	 * The distance between two words
	 * @param word1 the first word 
	 * @param word2 the second word
	 * @return the number of moves needed to change from the first word to the second
	 */
	public int distance(String word1, String word2) {
		int retVal = 0;
		for (int i = 0; i < word1.length(); i++) {
			retVal += distance(i, word1.charAt(i), word2.charAt(i));
		}
		return retVal;
	}
	
	/** 
	 * Return number of turns needed to move from one character to another
	 * on a particular wheel
	 * @param wheel Which wheel we are turning
	 * @param c1 The starting character
	 * @param c2 The ending character
	 * @return the number of moves needed
	 */
	private int distance(int wheel, char c1, char c2) {
		int pos1 = wheels[wheel].indexOf(c1);
		int pos2 = wheels[wheel].indexOf(c2);
		
		int steps = Math.abs(pos1-pos2);
		steps = Math.min(steps, Math.abs((wheels[wheel].length() + pos1) - pos2));
		steps = Math.min(steps, Math.abs((wheels[wheel].length() + pos2) - pos1));
		return steps;
	}
}
