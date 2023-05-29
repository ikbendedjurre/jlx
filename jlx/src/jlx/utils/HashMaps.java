package jlx.utils;

import java.util.*;
import java.util.function.Function;

public class HashMaps {
	public static <K1, K2, V> void injectInjectAll(Map<K1, Map<K2, Set<V>>> dest, K1 key1, K2 key2, Set<V> newValues) {
		Map<K2, Set<V>> values = dest.get(key1);
		
		if (values == null) {
			values = new HashMap<K2, Set<V>>();
			dest.put(key1, values);
		}
		
		injectAll(values, key2, newValues);
	}
	
	public static <K1, K2, V> void injectMerge(Map<K1, Map<K2, Set<V>>> dest, K1 key1, Map<K2, Set<V>> newValue) {
		Map<K2, Set<V>> values = dest.get(key1);
		
		if (values == null) {
			values = new HashMap<K2, Set<V>>();
			dest.put(key1, values);
		}
		
		merge(values, newValue);
	}
	
	public static <K1, K2, V> void injectInject(Map<K1, Map<K2, Set<V>>> dest, K1 key1, K2 key2, V newValue) {
		Map<K2, Set<V>> values = dest.get(key1);
		
		if (values == null) {
			values = new HashMap<K2, Set<V>>();
			dest.put(key1, values);
		}
		
		inject(values, key2, newValue);
	}
	
	public static <K1, K2, V> void injectInject1(Map<K1, Map<K2, Set<V>>> dest, K1 key1, K2 key2, V newValue) {
		Map<K2, Set<V>> values = dest.get(key1);
		
		if (values == null) {
			values = new HashMap<K2, Set<V>>();
			dest.put(key1, values);
		}
		
		inject1(values, key2, newValue);
	}
	
	public static <K1, K2, V> void injectSet(Map<K1, Map<K2, V>> dest, K1 key1, K2 key2, V newValue) {
		Map<K2, V> values = dest.get(key1);
		
		if (values == null) {
			values = new HashMap<K2, V>();
			dest.put(key1, values);
		}
		
		values.put(key2, newValue);
	}
	
	public static <K, V> boolean containsValue(Map<K, Set<V>> dest, K key, V value) {
		Set<V> values = dest.get(key);
		
		if (values == null) {
			return false;
		}
		
		return values.contains(value);
	}
	
	public static <K, V> boolean inject(Map<K, Set<V>> dest, K key, V newValue) {
		Set<V> values = dest.get(key);
		
		if (values == null) {
			values = new HashSet<V>();
			dest.put(key, values);
		}
		
		return values.add(newValue);
	}
	
	public static <K, V> void inject1(Map<K, Set<V>> dest, K key, V newValue) {
		Set<V> values = dest.get(key);
		
		if (values == null) {
			values = new HashSet<V>();
			dest.put(key, values);
			values.add(newValue);
		}
	}
	
	public static <K, V> void injectAll(Map<K, Set<V>> dest, K key, Collection<V> newValues) {
		Set<V> values = dest.get(key);
		
		if (values == null) {
			values = new HashSet<V>();
			dest.put(key, values);
		}
		
		values.addAll(newValues);
	}
	
	public static <K, V> void inject(Map<K, Set<V>> dest, Map<K, V> newValues) {
		for (Map.Entry<K, V> e : newValues.entrySet()) {
			Set<V> values = dest.get(e.getKey());
			
			if (values == null) {
				values = new HashSet<V>();
				dest.put(e.getKey(), values);
			}
			
			values.add(e.getValue());
		}
	}
	
	public static <K> int increment(Map<K, Integer> dest, K key, int delta, int initialValue) {
		int newValue = dest.getOrDefault(key, initialValue) + 1;
		dest.put(key, newValue);
		return newValue;
	}
	
	public static <K, V> void merge(Map<K, Set<V>> dest, Map<K, Set<V>> other) {
		for (Map.Entry<K, Set<V>> e : other.entrySet()) {
			Set<V> values = dest.get(e.getKey());
			
			if (values == null) {
				values = new HashSet<V>();
				dest.put(e.getKey(), values);
			}
			
			values.addAll(e.getValue());
		}
	}
	
	public static <K, V> Map<Set<K>, V> mergeKeysByValues(Map<K, V> source) {
		Map<V, Set<K>> keysPerValue = new HashMap<V, Set<K>>();
		
		for (Map.Entry<K, V> e : source.entrySet()) {
			inject(keysPerValue, e.getValue(), e.getKey());
		}
		
		Map<Set<K>, V> result = new HashMap<Set<K>, V>();
		
		for (Map.Entry<V, Set<K>> e : keysPerValue.entrySet()) {
			result.put(e.getValue(), e.getKey());
		}
		
		return result;
	}
	
	public static <K, V> Map<V, Set<K>> splitValuesByKeys(Map<K, Set<V>> source) {
		Map<V, Set<K>> result = new HashMap<V, Set<K>>();
		
		for (Map.Entry<K, Set<V>> e : source.entrySet()) {
			for (V v : e.getValue()) {
				inject(result, v, e.getKey());
			}
		}
		
		return result;
	}
	
	public static <K, V> Map<K, V> submap(Map<K, V> source, Collection<K> includedKeys) {
		Map<K, V> result = new HashMap<K, V>();
		
		for (Map.Entry<K, V> e : source.entrySet()) {
			if (includedKeys.contains(e.getKey())) {
				result.put(e.getKey(), e.getValue());
			}
		}
		
		return result;
	}
	
	public static <K, V> Map<K, V> exclusionSubmap(Map<K, V> source, Collection<K> excludedKeys) {
		Map<K, V> result = new HashMap<K, V>();
		
		for (Map.Entry<K, V> e : source.entrySet()) {
			if (!excludedKeys.contains(e.getKey())) {
				result.put(e.getKey(), e.getValue());
			}
		}
		
		return result;
	}
	
	public static <K, V> long getCombinationCount(Map<K, ? extends Collection<V>> valuesPerKey, boolean canAssignNoValueToKey) {
		long result = 1L;
		
		if (canAssignNoValueToKey) {
			for (Collection<V> values : valuesPerKey.values()) {
				result = result * (values.size() + 1);
			}
		} else {
			for (Collection<V> values : valuesPerKey.values()) {
				result = result * values.size();
			}
		}
		
		return result;
	}
	
	public static <K, V> Set<Map<K, V>> allCombinations(Map<K, ? extends Collection<V>> choicesPerKey) {
		Set<Map<K, V>> result = new HashSet<Map<K, V>>();
		addCombinations(choicesPerKey, new HashMap<K, V>(), new ArrayList<K>(choicesPerKey.keySet()), 0, result);
		return result;
	}
	
	private static <K, V> void addCombinations(Map<K, ? extends Collection<V>> choicesPerKey, Map<K, V> soFar, List<K> orderedKeys, int keyIndex, Set<Map<K, V>> dest) {
		if (keyIndex < orderedKeys.size()) {
			K key = orderedKeys.get(keyIndex);
			
			for (V value : choicesPerKey.get(key)) {
				Map<K, V> newSoFar = new HashMap<K, V>(soFar);
				newSoFar.put(key, value);
				
				addCombinations(choicesPerKey, newSoFar, orderedKeys, keyIndex + 1, dest);
			}
		} else {
			dest.add(soFar);
		}
	}
	
	public static <K1, K2, V> Map<K1, Map.Entry<K2, V>> searchEntrySetCombos(Map<K1, ? extends Map<K2, V>> entrySetMapPerKey, Function<Map<K1, Map.Entry<K2, V>>, Boolean> fct) {
		Map<K1, Set<Map.Entry<K2, V>>> entrySetPerKey = new HashMap<K1, Set<Map.Entry<K2, V>>>();
		
		for (Map.Entry<K1, ? extends Map<K2, V>> e : entrySetMapPerKey.entrySet()) {
			entrySetPerKey.put(e.getKey(), e.getValue().entrySet());
		}
		
		return searchCombinations(entrySetPerKey, fct);
	}
	
	public static <K, V> Map<K, V> searchCombinations(Map<K, ? extends Iterable<V>> choicesPerKey, Function<Map<K, V>, Boolean> fct) {
		List<K> orderedKeys = new ArrayList<K>(choicesPerKey.keySet());
		Map<K, Iterator<V>> iters = new HashMap<K, Iterator<V>>();
		Map<K, V> current = new HashMap<K, V>();
		
//		System.out.println("choicesPerKey.size() = " + choicesPerKey.size());
//		
//		for (Map.Entry<K, ? extends Iterable<V>> e : choicesPerKey.entrySet()) {
//			Iterator<V> q = e.getValue().iterator();
//			int size = 0;
//			
//			while (q.hasNext()) {
//				size++;
//				q.next();
//			}
//			
//			System.out.println("choicesPerKey.get(" + e.getKey().toString() + ").size() = " + size);
//		}
		
		for (Map.Entry<K, ? extends Iterable<V>> e : choicesPerKey.entrySet()) {
			Iterator<V> q = e.getValue().iterator();
			
			if (q.hasNext()) {
				iters.put(e.getKey(), q);
				current.put(e.getKey(), q.next());
			} else {
//				System.out.println("lvlkrfvuidfv");
//				System.exit(0);
				return null;
			}
		}
		
		while (true) {
			Boolean b = fct.apply(current);
			
			if (b != null) {
				return b.booleanValue() ? current : null;
			}
			
			if (!hasNext(iters)) {
				return null;
			}
			
			current = getNext(choicesPerKey, orderedKeys, iters, current);
		}
	}
	
	private static <K, V> Map<K, V> getNext(Map<K, ? extends Iterable<V>> choicesPerKey, List<K> orderedKeys, Map<K, Iterator<V>> iters, Map<K, V> base) {
		Map<K, V> result = new HashMap<K, V>(base);
		
		for (int index = orderedKeys.size() - 1; index >= 0; index--) {
			K key = orderedKeys.get(index);
			Iterator<V> q = iters.get(key);
			
			if (q.hasNext()) {
				result.put(key, q.next());
				return result;
			}
			
			q = choicesPerKey.get(key).iterator();
			iters.put(key, q);
			result.put(key, q.next());
		}
		
		throw new Error("Should not happen!");
	}
	
	private static <K, V> boolean hasNext(Map<K, Iterator<V>> iters) {
		for (Iterator<V> i : iters.values()) {
			if (i.hasNext()) {
				return true;
			}
		}
		
		return false;
	}
}
