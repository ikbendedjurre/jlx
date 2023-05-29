package jlx.utils;

import java.util.*;

public class IdMap<T> {
	private SortedMap<Integer, T> elemPerId;
	private Map<T, Integer> idPerElem;
	private final int delta;
	private int nextId;
	
	public IdMap() {
		this(Collections.emptySet(), 1);
	}
	
	public IdMap(Collection<T> elems) {
		this(elems, 1);
	}
	
	public IdMap(Collection<T> elems, int delta) {
		this.delta = delta;
		
		elemPerId = new TreeMap<Integer, T>();
		idPerElem = new HashMap<T, Integer>();
		nextId = 0;
		
		for (T elem : new HashSet<T>(elems)) {
			elemPerId.put(nextId, elem);
			idPerElem.put(elem, nextId);
			nextId += delta;
		}
	}
	
	public int getOrAdd(T elem) {
		Integer result = idPerElem.get(elem);
		
		if (result != null) {
			return result.intValue();
		}
		
		result = nextId;
		elemPerId.put(nextId, elem);
		idPerElem.put(elem, nextId);
		nextId += delta;
		return result;
	}
	
	public Set<Integer> addAll(Collection<? extends T> elems) {
		Set<Integer> result = new HashSet<Integer>();
		
		for (T elem : elems) {
			result.add(getOrAdd(elem));
		}
		
		return result;
	}
	
	public int get(T elem) {
		return idPerElem.get(elem).intValue();
	}
	
	public Collection<T> sortedElems() {
		return elemPerId.values();
	}
	
	public SortedMap<Integer, T> getElemPerId() {
		return elemPerId;
	}
	
	public Map<T, Integer> getIdPerElem() {
		return idPerElem;
	}
	
	public int size() {
		return elemPerId.size();
	}
}
