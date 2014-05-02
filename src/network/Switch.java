package network;

/**
 * Implements a schedule-based switch.
 * <p>
 * A switch will perform no action on an update, and will exception upon processing a message destined for it.
 * @author Bitidork
 *
 * @param <T>
 */
public class Switch<T extends Message> extends DeferredSchedulingNode<T> {

	@Override
	protected void onReceiveMessage(int time, T message) {
		throw new IllegalStateException("message was destined for switch");
	}

	@Override
	protected void onUpdate(int time) {
		// empty
	}

	/**
	 * Constructs a switch following the supplied scheduler.
	 * @param scheduler The scheduler the switch should use.
	 */
	public Switch( final Scheduler<T> scheduler ) {
		super( scheduler );
	}
}
