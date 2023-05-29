package jlx.utils;

import java.util.*;

public class EqClzMap<T> {
	public static class Entry<T> {
		private Set<T> equivElems;
		private Object userData;
		
		public final T someElem;
		
		private Entry(Set<T> eqElems) {
			if (eqElems.isEmpty()) {
				throw new Error("At least 1 element expected!");
			}
			
			equivElems = new HashSet<T>(eqElems);
			someElem = equivElems.iterator().next();
		}
		
		public Set<T> getEquivElems() {
			return Collections.unmodifiableSet(equivElems);
		}
		
		public <Q> Q getUserData(Class<Q> clz) {
			return clz.cast(userData);
		}
		
		public void setUserData(Object userData) {
			this.userData = userData;
		}
		
		private String getText(String indent) {
			String result;
			
			if (equivElems.size() > 0) {
				result = indent + "Equivalence class [" + equivElems.size() + "]:";
				
				for (T equivElem : equivElems) {
					result += "\n" + indent + "\t" + equivElem;
				}
			} else {
				result = "Equivalence class [0]";
			}
			
			return result;
		}
	}
	
	private Set<T> elems;
	private Map<T, Entry<T>> eqClzPerElem;
	private Set<Entry<T>> eqClzs;
	
	public EqClzMap(Map<T, Set<T>> equivElemsPerElem) {
		elems = new HashSet<T>();
		eqClzPerElem = new HashMap<T, Entry<T>>();
		eqClzs = new HashSet<Entry<T>>();
		
		Set<Set<T>> equivElemsSet = new HashSet<Set<T>>();
		equivElemsSet.addAll(equivElemsPerElem.values());
		
		for (Set<T> equivElems : equivElemsSet) {
			Entry<T> eqClz = new Entry<T>(equivElems);
			
			for (T equivElem : equivElems) {
				Entry<T> existingEqClz = eqClzPerElem.get(equivElem);
				
				if (existingEqClz != null) {
					String msg = "Element may belong to only one equivalence class!";
					msg += "\n\tElement: " + equivElem;
					msg += "\n" + existingEqClz.getText("\t");
					msg += "\n" + eqClz.getText("\t");
					throw new Error(msg);
				}
				
				elems.add(equivElem);
				eqClzPerElem.put(equivElem, eqClz);
			}
			
			eqClzs.add(eqClz);
		}
	}
	
	public Set<T> getElems() {
		return Collections.unmodifiableSet(elems);
	}
	
	public Entry<T> getEqClz(T elem) {
		return eqClzPerElem.get(elem);
	}
	
	public Map<T, Entry<T>> getEqClzPerElem() {
		return Collections.unmodifiableMap(eqClzPerElem);
	}
	
	public Set<Entry<T>> getEqClzs() {
		return Collections.unmodifiableSet(eqClzs);
	}
}
