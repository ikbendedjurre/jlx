package jlx.utils;

import java.io.*;
import java.util.*;

public class LDDMapFactory<K, V> {
	private LDDFactory factory;
	private IdMap<K> uniqueKeys;
	private IdMap<V> values;
	
	public LDDMapFactory(Collection<K> keys) {
		uniqueKeys = new IdMap<K>(keys);
		factory = new LDDFactory(uniqueKeys.size());
		values = new IdMap<V>();
	}
	
	public LDDFactory getFactory() {
		return factory;
	}
	
	public LDDMap<K, V> empty() {
		return new LDDMap<K, V>(this, factory.emptyLDD());
	}
	
	public LDDMap<K, V> create(Map<K, V> m) {
		return new LDDMap<K, V>(this, factory.mapToLDD(fromValuePerKey(m)));
	}
	
	public LDDMap<K, V> create(Collection<Map<K, V>> ms) {
		return new LDDMap<K, V>(this, factory.mapsToLDD(fromMultipleValuePerKey(ms)));
	}
	
	public LDDMap<K, V> fromPvsPerKey(Map<K, ? extends Collection<V>> pvsPerKey) {
		return new LDDMap<K, V>(this, factory.pvsMapToLDD(fromPvsMap(pvsPerKey)));
	}
	
	private Map<Integer, Collection<Integer>> fromPvsMap(Map<K, ? extends Collection<V>> pvsMap) {
		Map<Integer, Collection<Integer>> result = new HashMap<Integer, Collection<Integer>>();
		
		for (Map.Entry<K, ? extends Collection<V>> e : pvsMap.entrySet()) {
			result.put(uniqueKeys.get(e.getKey()), fromValues(e.getValue()));
		}
		
		return result;
	}
	
	private Collection<Integer> fromValues(Collection<V> elems) {
		Set<Integer> result = new HashSet<Integer>();
		
		for (V elem : elems) {
			result.add(values.getOrAdd(elem));
		}
		
		return result;
	}
	
	private Map<Integer, Integer> fromValuePerKey(Map<K, V> m) {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		
		for (Map.Entry<K, V> e : m.entrySet()) {
			result.put(uniqueKeys.get(e.getKey()), values.getOrAdd(e.getValue()));
		}
		
		return result;
	}
	
	private Collection<Map<Integer, Integer>> fromMultipleValuePerKey(Collection<Map<K, V>> ms) {
		Set<Map<Integer, Integer>> result = new HashSet<Map<Integer, Integer>>();
		
		for (Map<K, V> m : ms) {
			result.add(fromValuePerKey(m));
		}
		
		return result;
	}
	
//	private Map<K, V> fromIntMap(Map<Integer, Integer> m) {
//		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
//		
//		for (Map.Entry<K, V> e : m.entrySet()) {
//			result.put(uniqueKeys.get(e.getKey()), values.getOrAdd(e.getValue()));
//		}
//		
//		return result;
//	}
	
	public static class LDDMap<K, V> {
		private LDDMapFactory<K, V> owner;
		private LDDFactory.LDD ldd;
		
		private LDDMap(LDDMapFactory<K, V> owner, LDDFactory.LDD ldd) {
			this.owner = owner;
			this.ldd = ldd;
		}
		
		public LDDMapFactory<K, V> getOwner() {
			return owner;
		}
		
		public void add(Map<K, V> m) {
			ldd = ldd.union(owner.create(m).ldd);
		}
		
		public void addAll(Collection<Map<K, V>> ms) {
			ldd = ldd.union(owner.factory.mapsToLDD(owner.fromMultipleValuePerKey(ms)));
		}
		
		public void addAll(LDDMap<K, V> y) {
			ldd = ldd.union(y.ldd);
		}
		
		public boolean contains(Map<K, V> m) {
			return ldd.contains(owner.fromValuePerKey(m));
		}
		
		public boolean containsAll(Collection<Map<K, V>> ms) {
			return containsAll(owner.create(ms));
		}
		
		public boolean containsAll(LDDMap<K, V> y) {
			return ldd.containsAll(y.ldd);
		}
		
		public void printGraphvizFile(String filename) {
			ldd.printGraphvizFile(filename);
		}
		
		public void printGraphvizFile(PrintStream out, String header) {
			ldd.printGraphvizFile(out, header);
		}
	}
}

