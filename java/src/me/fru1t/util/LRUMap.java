package me.fru1t.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A "least recently used" cache implemented as a map.
 *
 * @param <K>
 * @param <V>
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 3112031005060334210L;
	private int maxSize;

	/**
	 * Creates a new LRU cache with the given maximum size.
	 * 
	 * @param maxSize
	 */
	public LRUMap(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
