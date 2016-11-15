package me.fru1t.util;

import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;

/**
 * Defines a generic consumer.
 *
 * @param <T> The type of data consumed.
 */
public abstract class BatchConsumer<T> extends Consumer<Scrape>{
	/**
	 * Consumes whatever is given to it. But save it for a batch processing
	 */
	public abstract void eatBatch(T food, boolean flush);
}
