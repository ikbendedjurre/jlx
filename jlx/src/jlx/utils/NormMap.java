package jlx.utils;

import java.util.*;

public class NormMap<T> implements Set<T> {
	private Map<T, T> elems;
	private int reuseCount;
	
	public NormMap() {
		elems = new HashMap<T, T>();
		reuseCount = 0;
	}
	
	public Collection<T> uniqueElems() {
		return elems.values();
	}
	
	public void retainUnique() {
		Set<T> unique = new HashSet<T>(elems.values());
		elems.keySet().retainAll(unique);
	}
	
	public int getReuseCount() {
		return reuseCount;
	}
	
	@Override
	public int size() {
		return elems.size();
	}
	
	@Override
	public boolean isEmpty() {
		return elems.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return elems.containsKey(o);
	}
	
	@Override
	public Iterator<T> iterator() {
		return elems.keySet().iterator();
	}
	
	@Override
	public Object[] toArray() {
		return elems.keySet().toArray();
	}
	
	@Override
	public <X> X[] toArray(X[] a) {
		return elems.keySet().toArray(a);
	}
	
	public T getNoAdd(T e) {
		T result = elems.get(e);
		
		if (result != null) {
			reuseCount++;
			return result;
			
		}
		
		return e;
	}
	
	public T get(T e) {
		T result = elems.get(e);
		
		if (result != null) {
			reuseCount++;
			return result;
			
		}
		
		elems.put(e, e);
		return e;
	}
	
	@Override
	public boolean add(T e) {
		if (elems.containsKey(e)) {
			return false;
		}
		
		elems.put(e, e);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		return elems.remove(o, o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return elems.keySet().containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = false;
		
		for (T e : c) {
			if (add(e)) {
				result = true;
			}
		}
		
		return result;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return elems.keySet().retainAll(c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return elems.keySet().removeAll(c);
	}
	
	@Override
	public void clear() {
		elems.clear();
	}
	
	public <K> Map<K, T> normalize(Map<K, T> x) {
		Map<K, T> result = new HashMap<K, T>();
		
		for (Map.Entry<K, T> e : x.entrySet()) {
			result.put(e.getKey(), get(e.getValue()));
		}
		
		return result;
	}
	
	public Set<T> normalize(Set<T> x) {
		Set<T> result = new HashSet<T>();
		
		for (T e : x) {
			result.add(get(e));
		}
		
		return result;
	}
}

