import java.util.ArrayList;
import java.util.HashSet;

/**
 * Choose a random sequence of words in the path and reverse it, placing it in a random spot
 */
public class MutateReverse extends MutationOperator {

	@Override
	public Solution run(Lock lock, Solution input) {
		int length = 1 + Optimizer.prng.nextInt(lock.getWordCount()-2);
		boolean ok = false;
		int tries = lock.getWordCount() - length;
		int start = Optimizer.prng.nextInt(lock.getWordCount()-length);
		
		ArrayList<Integer> reversedList = new ArrayList<>();
		HashSet<Integer> words = new HashSet<>();
		for (int i = start + length - 1; i >= start; i--) {
			int word = input.getIthWord(i);
			reversedList.add(word);
			if (words.contains(word)) throw new RuntimeException("Added word twice");
			words.add(word);
		}
		ArrayList<Integer> inorderList = new ArrayList<>();
		for (int i = 0; i < lock.getWordCount(); i++) {
			if (i < start || i >= start+length) {
				int word = input.getIthWord(i);
				inorderList.add(word);
				if (words.contains(word)) throw new RuntimeException("Added word twice");
				words.add(word);
			}
		}
		int bestInsertionPoint = Optimizer.prng.nextInt(inorderList.size()+1);
		
		Solution retVal = new Solution(lock);
		int inOrderNdx = 0;
		// Add start of words in order
		while (inOrderNdx < bestInsertionPoint) {
			int word = inorderList.get(inOrderNdx);
			retVal.addWord(word);
			inOrderNdx++;
		}
		// Add reversed list
		for (int word: reversedList) {
			retVal.addWord(word);			
		}
		// Add start of words in order
		while (inOrderNdx < inorderList.size()) {
			int word = inorderList.get(inOrderNdx);
			retVal.addWord(word);
			inOrderNdx++;
		}

		if (words.size() != lock.getWordCount()) {
			throw new RuntimeException("Failed to add correct number of words");
		}
		return retVal;
	}

}
