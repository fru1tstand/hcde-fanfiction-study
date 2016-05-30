package me.fru1t.fanfiction.process;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.concurrent.DatabaseProducer;
import me.fru1t.util.concurrent.GenericConsumer;

/**
 * Converts rows from a table in the database into something else.
 */
public class ConvertProcess<T extends DatabaseProducer.Row<?>> implements Runnable {
	private DatabaseProducer<T, ?> producer;
	private GenericConsumer<T> converter;
	private String sessionName;

	public ConvertProcess(DatabaseProducer<T, ?> producer, GenericConsumer<T> converter,
			String sessionName) {
		this.producer = producer;
		this.converter = converter;
		this.sessionName = sessionName;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ConvertProcess with session name: " + sessionName);
		@Nullable T obj = producer.take();
		while (obj != null) {
			converter.eat(obj);
			obj = producer.take();
		}
		Boot.getLogger().log("Finished ConvertProcess with session name: " + sessionName);
	}
}
