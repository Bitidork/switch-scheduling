package network;

/**
 * Implements a switch that uses the FIFO policy.
 * <p>
 * A switch may not receive messages (will exception if it receives a message destined for it).
 * A switch will not perform any user-defined operations on an update.
 * @author Bitidork
 *
 * @param <T> A type of message.
 * @see #onReceiveMessage(int, Message)
 * @see #onUpdate(int)
 */
public final class FIFOSwitch<T extends Message> extends FIFONode<T> {
	/**
	 * Constructs a switch that uses FIFO policy with the supplied scheduler.
	 * @param scheduler The scheduler this switch will use.
	 */
	public FIFOSwitch(FIFOScheduler<T> scheduler) {
		super(scheduler);
	}

	@Override
	protected void onReceiveMessage(int time, T message) {
		throw new IllegalStateException("FIFOSwitch received a message destined for it");
	}

	@Override
	protected void onUpdate(int time) {
		// empty
	}

}
