import java.util.ArrayList;
import java.util.HashSet;

/** 
 * Choose a path by selecting the next closest word.  Break ties randomly
 * 
 */
public class InitOpNextClosest extends InitializationOperator {
	
	@Override
	public Solution run(Lock lock) {
		HashSet<Integer> used = new HashSet<>();
		Solution retVal = new Solution(lock);
		
		int lastWord = -1;
		used.add(lastWord);
		while (used.size() <= lock.getWordCount()) {
			ArrayList<Integer> bestNextWords = new ArrayList<>();
			double bestWeightedDistance = Double.MAX_VALUE;
			for (int i = 0; i < lock.getWordCount(); i++){
				if (!used.contains(i)) {
					double w_dist = lock.distance(i, lastWord);
					if (w_dist < bestWeightedDistance) {
						bestWeightedDistance = w_dist;
						bestNextWords.clear();
					}
					if (w_dist == bestWeightedDistance) {
						bestNextWords.add(i);
					}
				}
			}
			/*System.err.print("lastword: " + lock.getWord(lastWord));
			for (int i: bestNextWords) {
				System.err.print(" " + lock.getWord(i));
			}
			System.err.println(" dist: " + bestWeightedDistance + " candidates: " + bestNextWords.size());*/
			lastWord = bestNextWords.get(Optimizer.prng.nextInt(bestNextWords.size()));
			retVal.addWord(lastWord);
			used.add(lastWord);
		}
		return retVal;
	}

}
