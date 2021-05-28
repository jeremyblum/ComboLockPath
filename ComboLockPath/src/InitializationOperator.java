/** 
 * A base class for an operator that creates a lock configuration
 * for the initial population of the GA
 */
public abstract class InitializationOperator {
	public abstract Solution run(Lock lock);

	public String getName() {
		return getClass().getSimpleName();
	}
}
