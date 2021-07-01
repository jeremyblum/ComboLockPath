import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class whose objects represent lock configurations
 */
public class Solution implements Comparable<Solution> {
	private Lock lock;
	private ArrayList<Integer> path = new ArrayList<>(); // The order in which we visit values
	private Double score = null; // The score
	private Double fitness = null; // The fitness
	
	public Solution(Lock lock) {
		this.lock = lock;
	}
	
	public Integer getIthWord(int i) {
		if (i == -1) return -1;
		else return path.get(i);
	}

	public void setIthWord(int i, Integer wordIndex) {
		score = null;
		path.set(i, wordIndex);
	}

	public void addWord(Integer wordIndex) {
		score = null;
		path.add(wordIndex);
	}

	/**
	 * Get the fitness of the lock configuration
	 * @return a score, which is currently the number of words that can be made from the 
	 *         lock configuration
	 */
	double getScore() {
		if (score == null) {
			score = 0.0;
			int distSoFar = 0;
			int lastWord = -1;
			for (int i = 0; i < path.size(); i++) {
				distSoFar += 1 + lock.distance(lastWord, path.get(i)); // One to try this word
				score += distSoFar;
				lastWord = path.get(i);
			}
		}
		return score;
	}

	//V2
	/**
	 * Get the lower bound fitness of the lock configuration with lower bound
	 * @return a lower bound fitness, which is the sum of the total running distance by looking
	 *         at the distance of the closest word and next closest word of all the words the lock can create
	 */
	double getFitness() {
		if (fitness == null) {
			double[] aveMinusClosest = new double[path.size()];
			double[] overallAve = new double[path.size()];
			double [] runTotal = new double[path.size()];
			double aveSoFar = 0.0;
			for (int i = 0; i < path.size(); i++) {
				int closestDist = Integer.MAX_VALUE;
				int closestNextDist = Integer.MAX_VALUE;
				int closestWord = -1;
				for (int j = 0; j < path.size(); j++) {
					if (j == i) continue;
					int wordDist = lock.distance(path.get(i), path.get(j));
					if (wordDist < closestDist) {
						closestNextDist = closestDist;
						closestDist = wordDist;
						closestWord = path.get(j);
					}
					if (wordDist < closestNextDist && path.get(j) != closestWord) {
						closestNextDist = wordDist;
					}
				}
				double ave = (closestDist + closestNextDist) / 2.0;
				aveMinusClosest[i] = ave - closestDist;
				overallAve[i] = 1 + ave;
			}
			Arrays.sort(overallAve);
			for(int i = 0; i < overallAve.length; i++){
				runTotal[i] = overallAve[i] + aveSoFar;
				aveSoFar = runTotal[i];
			}

			fitness = 0.0;
			double adjustment = -1.0;
			for (int i = 0; i < runTotal.length; i++) {
				fitness += runTotal[i];
				if(aveMinusClosest[i] > adjustment){
					adjustment = aveMinusClosest[i];
				}
			}
			fitness -= adjustment;
		}
		return fitness;
	}

	/**
	 * @return a string representation of the lock, with the letter that have been selected
	 */
	@Override 
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getScore());
		
		return sb.toString();
	}
	
	/**
	 * @return true iff the two solutions have exactly the same letters selected for each wheel
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (this.getClass() != obj.getClass()) return false;
		Solution that = (Solution) obj;
		for (int w = 0; w < path.size(); w++) {
			if (!this.path.get(w).equals(that.path.get(w))) return false;
		}
		return true;
	}

	/**
	 * @return an appropriate hash code for the lock
	 */
	@Override
	public int hashCode() {
		int retVal = 0;
		for (int w = 0; w < path.size(); w++) {
			retVal = retVal * 31 + path.get(w);
		}
		return retVal;
	}

	@Override
	public int compareTo(Solution that) {
		if (this.getScore() < that.getScore()) return -1;
		else if (this.getScore() > that.getScore()) return 1;
		return 0;
	}

	
}
