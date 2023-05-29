package jlx.behave.verify;

import java.util.*;

public class IntRanges {
	private final SortedMap<Integer, IntRange> rangePerMin;
	
	public IntRanges() {
		rangePerMin = new TreeMap<Integer, IntRange>();
	}
	
	public Collection<IntRange> getRanges() {
		return rangePerMin.values();
	}
	
	public void add(int value) {
		for (Map.Entry<Integer, IntRange> e : rangePerMin.entrySet()) {
			if (e.getKey() > value) {
				
				return;
			}
			
			
		}
	}
}
