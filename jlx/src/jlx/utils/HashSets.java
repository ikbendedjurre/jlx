package jlx.utils;

import java.util.*;
import java.util.function.*;

public class HashSets {
	public static <V> void searchSubsets(Set<V> values, Consumer<Set<V>> fct) {
		if (values.size() > 31) {
			System.err.println("Not supported for " + values.size() + " elements!");
			return;
			//throw new Error("Not supported for " + values.size() + " elements!");
		}
		
		final int maxIndex = 1 << values.size();
		
		for (int index = 0; index <= maxIndex; index++) {
			Set<V> subset = new HashSet<V>();
			Iterator<V> q = values.iterator();
			
			for (int i = 0; q.hasNext(); i++) {
				if (((index >> i) & 1) == 1) {
					subset.add(q.next());
				} else {
					q.next();
				}
			}
			
			fct.accept(subset);
		}
	}
	
	public static <V> Set<Set<V>> getSubsets(Set<V> values) {
		Set<Set<V>> result = new HashSet<Set<V>>();
		addSubsets(new ArrayList<V>(values), new HashSet<V>(), 0, result);
		return result;
	}
	
	private static <V> void addSubsets(List<V> orderedValues, Set<V> soFar, int valueIndex, Set<Set<V>> destination) {
		if (valueIndex < orderedValues.size()) {
			V value = orderedValues.get(valueIndex);
			
			Set<V> newSoFar1 = new HashSet<V>(soFar);
			newSoFar1.add(value);
			addSubsets(orderedValues, newSoFar1, valueIndex + 1, destination);
			
			Set<V> newSoFar2 = new HashSet<V>(soFar);
			addSubsets(orderedValues, newSoFar2, valueIndex + 1, destination);
		} else {
			destination.add(soFar);
		}
	}
	
	public static <X, Y> Set<Y> map(Set<X> xs, Map<X, Y> m) {
		Set<Y> result = new HashSet<Y>();
		
		for (X x : xs) {
			result.add(m.get(x));
		}
		
		return result;
	}
	
	public static <X, Y> Set<Y> map(Set<X> xs, Function<X, Y> f) {
		Set<Y> result = new HashSet<Y>();
		
		for (X x : xs) {
			result.add(f.apply(x));
		}
		
		return result;
	}
	
//	public static <T> Set<T> combine(Collection<
}
