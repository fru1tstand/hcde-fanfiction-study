package me.fru1t.util;

import java.util.Random;

public class ThreadUtils {
	private static final Random random = new Random();
	
	/**
	 * Pauses the current execution for a distrubuted amount of time.
	 * 
	 * @param milliseconds
	 * @throws InterruptedException
	 */
	public static void waitGauss(int milliseconds) throws InterruptedException {
		int waitTime = Math.max(0, 
				(int) ((random.nextGaussian() * milliseconds)
						+ milliseconds));
		Thread.sleep(waitTime);
	}

}
