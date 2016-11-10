package me.fru1t.fanfiction.process;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Session.SessionName;
import me.fru1t.util.Consumer;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Converts rows from a table in the database into something else.
 */
public class ConvertProcess<T extends DatabaseProducer.Row<?>> implements Runnable {
	private DatabaseProducer<T, ?> producer;
	private Consumer<T> converter;
	private SessionName convertSession;

	public ConvertProcess(DatabaseProducer<T, ?> producer, Consumer<T> converter,
			SessionName convertProfilePages161110) {
		this.producer = producer;
		this.converter = converter;
		this.convertSession = convertProfilePages161110;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ConvertProcess with session name: " + convertSession, true);
		@Nullable T obj = producer.take();
		while (obj != null) {
			converter.eat(obj);
			obj = producer.take();
		}
		Boot.getLogger().log("Finished ConvertProcess with session name: " + convertSession, true);
	}
}
