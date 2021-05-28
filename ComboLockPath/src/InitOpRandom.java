import java.util.ArrayList;
import java.util.Collections;

/**
 * Create an initial path at random
 * 
 */
public class InitOpRandom extends InitializationOperator {
	
	@Override
	public Solution run(Lock lock) {
		Solution retVal = new Solution(lock);
		
		ArrayList<Integer> ordering = new ArrayList<>();
		for (int i = 0; i < lock.getWordCount(); i++) {
			ordering.add(i);
		}
		Collections.shuffle(ordering);

		for (int i: ordering) {
			retVal.addWord(i);
		}

		return retVal;
	}
}
