package me.fru1t.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class SizedHashMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 3112031005060334210L;
	private int maxSize;

	public SizedHashMap(int maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
