package jlx.utils;

import java.util.*;

public class NameMap<T> {
	private UnusedNames unusedNames;
	private Map<T, String> namePerElem;
	private SortedMap<String, T> elemPerName;
	
	public NameMap(UnusedNames unusedNames) {
		this.unusedNames = unusedNames;
		
		namePerElem = new HashMap<T, String>();
		elemPerName = new TreeMap<String, T>();
	}
	
	public void add(T elem, String base) {
		generate(elem, base);
	}
	
	public String generate(T elem, String base) {
		if (namePerElem.containsKey(elem)) {
			throw new Error("Should not happen!");
		}
		
		String result = unusedNames.generateUnusedName(base);
		namePerElem.put(elem, result);
		elemPerName.put(result, elem);
		return result;
	}
	
	public String get(T elem) {
		String result = namePerElem.get(elem);
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	public T reverseGet(String name) {
		T result = elemPerName.get(name);
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	public Collection<String> orderedNames() {
		return elemPerName.keySet();
	}
	
	public Collection<T> orderedElems() {
		return elemPerName.values();
	}
	
	public Map<T, String> getNamePerElem() {
		return namePerElem;
	}
	
	public SortedMap<String, T> getElemPerName() {
		return elemPerName;
	}
}

