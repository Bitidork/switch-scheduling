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
	public static final int FRAME_SIZE = 200;
	
	/**
	 * The amount of capacity that is safe to reserve ( FRAME_SIZE * 0.6 ).
	 */
	public static final int SAFE_CAPACITY = (FRAME_SIZE * 5) / 3;
	
	/**
	 * The default number of iterations to run in parallel iterative matching.
	 */
	public static final int PARALLEL_ITERATIVE_ITERATIONS = 4;
}
