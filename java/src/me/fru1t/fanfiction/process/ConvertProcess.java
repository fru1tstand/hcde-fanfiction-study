package me.fru1t.fanfiction.process;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.Consumer;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Converts rows from a table in the database into something else.
 */
public class ConvertProcess<T extends DatabaseProducer.Row<?>> implements Runnable {
	private DatabaseProducer<T, ?> producer;
	private Consumer<T> converter;
	
	public ConvertProcess(DatabaseProducer<T, ?> producer, Consumer<T> converter) throws InterruptedException {
		this.producer = producer;
		this.converter = converter;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ConvertProcess with session name: " + Boot.getSessionOfThisRun().getName(), true);
		@Nullable T obj = producer.take();
		while (obj != null) {
			converter.eat(obj);
			obj = producer.take();
		}
		Boot.getLogger().log("Finished ConvertProcess with session name: " + Boot.getSessionOfThisRun().getName(), true);
	}
}
