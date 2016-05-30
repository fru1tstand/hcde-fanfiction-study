package me.fru1t.util.concurrent;

/**
 * Defines a generic consumer.
 *
 * @param <T> The type of data consumed.
 */
public abstract class GenericConsumer<T> {
	/**
	 * Consumes whatever is given to it.
	 */
	public abstract void eat(T food);
}
