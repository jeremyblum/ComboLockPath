import java.util.ArrayList;

/**
 * A class whose objects represent lock configurations
 */
public class Solution implements Comparable<Solution> {
	private Lock lock;
	private ArrayList<Integer> path = new ArrayList<>(); // The order in which we visit values
	private Double score = null; // The score
	
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
	 FIX
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
				distSoFar += lock.distance(lastWord, path.get(i)); // One to try this word
				score += 1 + distSoFar; 
				lastWord = path.get(i);
			}
		}
		return score;
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
