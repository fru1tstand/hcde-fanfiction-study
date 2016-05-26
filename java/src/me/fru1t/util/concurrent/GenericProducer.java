package me.fru1t.util.concurrent;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines a generic producer for use in systems that may or may not require concurrency.
 *
 * @param <T> The type produced by this producer.
 */
public abstract class GenericProducer<T> {
	/**
	 * Retrieves the next item of this producer.
	 */
	@Nullable
	public abstract T take();
}
