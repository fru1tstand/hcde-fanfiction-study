package me.fru1t.util.concurrent;

import me.fru1t.util.Producer;

public abstract class ConcurrentProducer<T> extends Producer<T> {
	/**
	 * Returns if this producer will not produce any more.
	 * @return
	 */
	public abstract boolean isComplete();
}
