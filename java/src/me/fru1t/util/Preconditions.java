package me.fru1t.util;

public class Preconditions {
	/**
	 * Returns true if and only if the original object matches any of the comparison objects. False
	 * otherwise.
	 * 
	 * @param value
	 * @param possibilities
	 * @return
	 */
	public static <T> boolean isAnyOf(T value, @SuppressWarnings("unchecked") T... possibilities) {
		for (T comparison : possibilities) {
			if (value.equals(comparison)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if and only if the value passed is within the given bounds, both inclusive.
	 * 
	 * @param value
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static boolean isWithin(int value, int lower, int upper) {
		return (value >= lower && value <= upper);
	}
}
