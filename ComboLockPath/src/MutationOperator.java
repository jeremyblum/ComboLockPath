/**
 * A base class for an operator that induces a mutation in a
 * child solution.
 */
public abstract class MutationOperator {
	public abstract Solution run(Lock lock, Solution input);

	public String getName() {
		return getClass().getSimpleName();
	}
}
