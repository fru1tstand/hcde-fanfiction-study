package me.fru1t.util.concurrent;

import me.fru1t.util.Producer;

public abstract class ConcurrentProducer<T> extends Producer<T> {
	/**
	 * Returns if this producer is currently blocked.
	 */
	public abstract Boolean isBlocked();
}
