package jlx.utils;

import java.io.PrintStream;
import java.time.LocalTime;
import java.util.*;

public class DistinguisherMap<E, D> {
	private Map<Set<E>, Set<Set<D>>> distinguishersPerSubset;
	private Map<D, Set<E>> subsetPerDistinguisher;
	private Set<E> distinguishedElems; 
	private int distinguisherSetCount;
	
	public DistinguisherMap() {
		distinguishersPerSubset = new HashMap<Set<E>, Set<Set<D>>>();
		subsetPerDistinguisher = new HashMap<D, Set<E>>();
		distinguishedElems = new HashSet<E>();
		distinguisherSetCount = 0;
	}
	
	public DistinguisherMap<E, D> minimize() {
		Set<D> used = new HashSet<D>();
		
		for (Map.Entry<Set<E>, Set<Set<D>>> e : distinguishersPerSubset.entrySet()) {
			if (e.getKey().size() == 1) {
				used.addAll(getMinSize(e.getValue()));
			}
		}
		
		DistinguisherMap<E, D> result = new DistinguisherMap<E, D>();
		
		for (D d : used) {
			result.add(d, subsetPerDistinguisher.get(d));
		}
		
		return result;
	}
	
	private static <T> Set<T> getMinSize(Set<Set<T>> dds) {
		Set<T> result = null;
		
		for (Set<T> ds : dds) {
			if (result == null || ds.size() < result.size()) {
				result = ds;
			}
		}
		
		return result;
	}
	
	public Map<Set<E>, Set<Set<D>>> getDistinguishersPerSubset() {
		return distinguishersPerSubset;
	}
	
	public Map<D, Set<E>> getSubsetPerDistinguisher() {
		return subsetPerDistinguisher;
	}
	
	public Set<E> getDistinguishedElems() {
		return distinguishedElems;
	}
	
	public boolean distinguishesAll(Set<E> elems) {
		for (E elem : elems) {
			if (!distinguishersPerSubset.containsKey(Collections.singleton(elem))) {
				return false;
			}
		}
		
		return true;
	}
	
	public void clear() {
		distinguishersPerSubset.clear();
		subsetPerDistinguisher.clear();
		distinguishedElems.clear();
		distinguisherSetCount = 0;
	}
	
	public boolean add(D distinguisher, Set<E> resultingElems) {
		if (subsetPerDistinguisher.containsValue(resultingElems)) {
			return false; //Distinguisher does not add new information => ignore!!
		}
		
		subsetPerDistinguisher.put(distinguisher, resultingElems);
		return put(resultingElems, Collections.singleton(distinguisher));
	}
	
	private boolean put(Set<E> elems, Set<D> distinguishers) {
		boolean result = false;
		
//		System.out.println("[" + LocalTime.now() + "] #elems = " + elems.size() + " -> " + distinguishers.size() + "; #distinguishers = " + subsetPerDistinguisher.size() + "; #dist-sets = " + distinguisherSetCount);
		
		Map<Set<E>, Set<Set<D>>> fringe = new HashMap<Set<E>, Set<Set<D>>>();
		Map<Set<E>, Set<Set<D>>> newFringe = new HashMap<Set<E>, Set<Set<D>>>();
		put(elems, distinguishers, fringe);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
//			System.out.println("[" + LocalTime.now() + ":A] #xs = " + distinguishersPerSubset.size() + " (+" + fringe.size() + "); #distinguishers = " + subsetPerDistinguisher.size() + "; #dist-sets = " + distinguisherSetCount);
			
			for (Map.Entry<Set<E>, Set<Set<D>>> e : fringe.entrySet()) {
				for (Set<D> ds : e.getValue()) {
					put(e.getKey(), ds, newFringe);
				}
			}
			
			fringe.clear();
			fringe.putAll(newFringe);
			result = true;
			
			System.out.println("[" + LocalTime.now() + "] #xs = " + distinguishersPerSubset.size() + " (+" + fringe.size() + "); #distinguishers = " + subsetPerDistinguisher.size() + "; #dist-sets = " + distinguisherSetCount + "; #distinguishedElems = " + distinguishedElems.size());
		}
		
		return result;
	}
	
	private boolean union(Set<D> distinguishers, Set<Set<D>> dest) {
		if (dest.add(distinguishers)) {
			return true;
		}
		
		return false;
	}
	
	private boolean union2(Set<D> distinguishers, Set<Set<D>> dest) {
		if (dest.isEmpty()) {
			dest.add(distinguishers);
			return true;
		}
		
		if (distinguishers.size() < dest.iterator().next().size()) {
			dest.clear();
			dest.add(distinguishers);
			return true;
		}
		
		return false;
	}
	
	private void put(Set<E> elems, Set<D> distinguishers, Map<Set<E>, Set<Set<D>>> dest) {
//		System.out.println("[" + LocalTime.now() + "] j = 88");
		Set<Set<D>> dds = distinguishersPerSubset.get(elems);
		
		if (dds == null) {
			dds = new HashSet<Set<D>>();
			distinguishersPerSubset.put(elems, dds);
			
			if (elems.size() == 1) {
				distinguishedElems.add(elems.iterator().next());
			}
		}
		
		if (union(distinguishers, dds)) {
//			System.out.println("[" + LocalTime.now() + "] j = 99");
			distinguisherSetCount++;
//			int i = 0;
			
			for (Map.Entry<Set<E>, Set<Set<D>>> e : distinguishersPerSubset.entrySet()) {
//				System.out.println("[" + LocalTime.now() + "] j = 111");
				
				if (!e.getKey().equals(elems)) {
//					System.out.println("[" + LocalTime.now() + "] j = 222");
					
					Set<E> xy = new HashSet<E>(e.getKey());
					xy.retainAll(elems);
					
					if (xy.size() > 0) {
//						System.out.println("[" + LocalTime.now() + "] j = 000");
//						int j = 0;
						
						for (Set<D> ds : e.getValue()) {
							Set<D> newDs = new HashSet<D>(ds);
							
							if (newDs.addAll(distinguishers)) {
								HashMaps.inject(dest, xy, newDs);
							}
							
//							j++;
//							
//							if (j % 1 == 0) {
//								System.out.println("[" + LocalTime.now() + "] j = " + j + " / " + ds.size());
//							}
						}
					}
				}
				
//				System.out.println("[" + LocalTime.now() + "] j = 22222222");
//				
//				i++;
//				
//				if (i % 100 == 0) {
//					System.out.println("[" + LocalTime.now() + "] i = " + i + " / " + distinguishersPerSubset.size());
//				}
			}
		}
	}
	
	public void print(PrintStream out) {
		out.println("subsetPerDistinguisher -> {");
		
		for (Map.Entry<D, Set<E>> e : subsetPerDistinguisher.entrySet()) {
			out.println("\t" + e.getKey().toString() + " -> { " + Texts.concat(e.getValue(), ", ") + " }");
		}
		
		out.println("}");
		out.println("distinguishersPerSubset -> {");
		
		for (Map.Entry<Set<E>, Set<Set<D>>> e : distinguishersPerSubset.entrySet()) {
			out.println("\t{ " + Texts.concat(e.getKey(), ", ") + " } -> {");
			
			for (Set<D> ds : e.getValue()) {
				out.println("\t\t{ " + Texts.concat(ds, ", ") + " }");
			}
			
			out.println("\t}");
		}
		
		out.println("}");
	}
	
	private static Set<Integer> makeSet(Integer... xs) {
		Set<Integer> result = new HashSet<Integer>();
		
		for (Integer x : xs) {
			result.add(x);
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		DistinguisherMap<Integer, Set<Integer>> dmap = new DistinguisherMap<Integer, Set<Integer>>();
		
		dmap.print(System.out);
		dmap.add(makeSet(1, 2), makeSet(1, 2));
		dmap.print(System.out);
		dmap.add(makeSet(2, 3), makeSet(2, 3));
		dmap.print(System.out);
		
		if (!dmap.getDistinguishersPerSubset().containsKey(makeSet(2))) {
			throw new Error("");
		}
		
		if (dmap.getDistinguishersPerSubset().containsKey(makeSet(1))) {
			throw new Error("");
		}
		
		if (dmap.getDistinguishersPerSubset().containsKey(makeSet(3))) {
			throw new Error("");
		}
		
		dmap.add(makeSet(1, 3), makeSet(1, 3));
		dmap.print(System.out);
		
	}
}
