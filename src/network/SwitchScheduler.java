package network;

/**
 * A generic scheduler.
 * @author Bitidork
 *
 * @param <Switch> The type of Node this scheduler is used for.
 * @param <T> The type of message this scheduler is used for.
 */
public abstract class SwitchScheduler<Switch extends Node<T>, T extends Message> {
	/**
	 * Schedules the supplied <i>node</i> at the supplied <i>time</i> by determining which messages to send out from that node and transmitting them.
	 * @param time The time at which to schedule.
	 * @param node The node to schedule.
	 */
	public abstract void scheduleSwitch( final int time, final Switch node );
}
