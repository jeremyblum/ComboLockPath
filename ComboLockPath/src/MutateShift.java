import java.util.HashSet;

/**
 * Select a random sequence of words and move it elsewhere
 */
public class MutateShift extends MutationOperator {

	@Override
	public Solution run(Lock lock, Solution input) {
		int length = 1 + Optimizer.prng.nextInt(lock.getWordCount()-2);
		int start = Optimizer.prng.nextInt(lock.getWordCount()-length);
		int target = Optimizer.prng.nextInt(lock.getWordCount()-length);
		while (target == start) {
			target = Optimizer.prng.nextInt(lock.getWordCount()-length);
		}
		Solution retVal = new Solution(lock);
		HashSet<Integer> words = new HashSet<>();

		int index = 0;
		// Add start of words in order
		for (int i = 0; target > 0 && i < start; i++) {
			int word = input.getIthWord(index);
			if (words.contains(word))
				throw new RuntimeException("Attempting to add duplicate word");
			retVal.addWord(word);
			words.add(word);
			target--;
			index++;
		}
		
		while (target > 0) {
			int word = input.getIthWord(index + length);
			if (words.contains(word))
				throw new RuntimeException("Attempting to add duplicate word");
			retVal.addWord(word);
			words.add(word);
			target--;
			index++;
		}
		
		// Add shifted list
		for (int i = start; i < start + length; i++) {
			int word = input.getIthWord(i);
			if (words.contains(word))
				throw new RuntimeException("Attempting to add duplicate word");
			retVal.addWord(word);
			words.add(word);
			index++;
		}
		
		// Add remaining words
		for (int i = 0; i < lock.getWordCount(); i++) {
			int word = input.getIthWord(i);
			if (!words.contains(word)) {
				retVal.addWord(word);
				words.add(word);
			}
		}
		if (words.size() != lock.getWordCount()) {
			throw new RuntimeException("Failed to add correct number of words");
		}
		return retVal;
	}

}
