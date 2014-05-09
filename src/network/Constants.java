package network;

/**
 * A class encapsulating constants to be used throughout the project.
 * @author Bitidork
 *
 */
public final class Constants {
	/**
	 * The number of contiguous time units that form a single frame.
	 */
	public static final int FRAME_SIZE = 320;
	
	/**
	 * The amount of capacity that is safe to reserve ( FRAME_SIZE * 0.53 ).
	 */
	public static final int SAFE_CAPACITY = (int)((float)FRAME_SIZE * 0.53f);
	
	/**
	 * The default number of iterations to run in parallel iterative matching.
	 */
	public static final int PARALLEL_ITERATIVE_ITERATIONS = 4;
	
	/**
	 * The number of iterations to run statistical matching for.
	 * Note: This isn't an iteration as described in the paper.
	 */
	public static final int STATISTICAL_ITERATIONS = 1;

	/**
	 * The number of iterations to run parallel-iterative matching after statistical matching.
	 */
	public static final int STATISICAL_ADDITIONAL_PARALLEL_ITERATIONS = 3;
}
