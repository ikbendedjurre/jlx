package jlx.utils;

import java.io.*;
import java.util.*;

public class LDDFactory {
	private final int levelCount;
	private final Map<Integer, Map<LDD, LDD>> cachePerLevel;
	private final LDD EMPTY_LDD = new LDD(this, true);
	private final LDD ABSENT = new LDD(this, true);
	private final LDD PRESENT = new LDD(this, true);
	private final static Integer WILDCARD = null;
	
	private int cacheSize;
	
	public LDDFactory(int levelCount) {
		this.levelCount = levelCount;
		
		cacheSize = 0;
		cachePerLevel = new HashMap<Integer, Map<LDD, LDD>>();
		
		for (int level = 0; level <= levelCount; level++) {
			Map<LDD, LDD> cache = new HashMap<LDD, LDD>();
			cache.put(ABSENT, ABSENT);
			cachePerLevel.put(level, cache);
		}
		
		addToCache(levelCount, PRESENT);
		mapToLDD(0, Collections.emptyMap());
	}
	
	private LDD addToCache(int topLevel, LDD ldd) {
		ldd.sanityCheck();
		
		Map<LDD, LDD> cache = cachePerLevel.get(topLevel);
		LDD result = cache.get(ldd);
		
		if (result != null) {
			return result;
		}
		
		cache.put(ldd, ldd);
		cacheSize++;
		
//		System.out.println("|cache| = " + cacheSize);
		
		return ldd;
	}
	
	public int getCacheSize() {
		return cacheSize;
	}
	
	/**
	 * The number of levels in the LDD.
	 * There is one level per "variable", so the level with the TRUE/FALSE nodes does not count.
	 */
	public int getLevelCount() {
		return levelCount;
	}
	
	public boolean contains(LDD x, Map<Integer, Integer> m) {
		if (x == EMPTY_LDD) {
			return false;
		}
		
		return contains(0, x, m);
	}
	
	private boolean contains(int topLevel, LDD x, Map<Integer, Integer> m) {
		if (topLevel == levelCount) {
			return true;
		}
		
		if (Objects.equals(x.value, WILDCARD)) {
			return contains(topLevel + 1, x.ifEqual, m);
		}
		
		Integer v = m.get(topLevel);
		
		if (v == null) {
			return false;
		}
		
		if (Objects.equals(v, x.value)) {
			return contains(topLevel + 1, x.ifEqual, m);
		}
		
		if (x.ifGreater != ABSENT && isOrdered(x.value, v)) {
			return contains(topLevel, x.ifGreater, m);
		}
		
		return false;
	}
	
	public boolean containsAll(LDD x, LDD y) {
		return intersection(x, y) != EMPTY_LDD;
	}
	
//	public boolean containsAll(LDD x, LDD y) {
//		return containsAll(0, x, y);
//	}
//	
//	private boolean containsAll(int topLevel, LDD x, LDD y) {
//		if (topLevel == levelCount) {
//			return true;
//		}
//		
//		if (Objects.equals(x.value, y.value)) {
//			
//		}
//	}
	
	public LDD emptyLDD() {
		return EMPTY_LDD;
	}
	
	/**
	 * Converts a map to an LDD.
	 * The keys of the map are assumed to correspond with the levels of the LDD (zero-indexed).
	 * If there is no mapping for a specific key, the LDD uses DONT_CARE.
	 */
	public LDD mapToLDD(Map<Integer, Integer> m) {
		return mapToLDD(0, m);
	}
	
	private LDD mapToLDD(int topLevel, Map<Integer, Integer> m) {
		if (topLevel == levelCount) {
			return PRESENT;
		}
		
		LDD ldd = new LDD(this);
		ldd.value = m.getOrDefault(topLevel, WILDCARD);
		ldd.ifEqual = mapToLDD(topLevel + 1, m);
		ldd.ifGreater = ABSENT;
		return addToCache(topLevel, ldd);
	}
	
	public LDD mapsToLDD(Collection<Map<Integer, Integer>> ms) {
		LDD result = EMPTY_LDD;
		System.out.println("mapsToLDD.begin");
		
		int mix = 1;
		
		for (Map<Integer, Integer> m : ms) {
			if (mix % 100 == 0) {
				System.out.println("mix = " + mix + " / " + ms.size());
			}
			
			mix++;
			
			result = union(result, mapToLDD(m));
		}
		
		System.out.println("mapsToLDD.end");
		return result;
	}
	
	public LDD pvsMapToLDD(Map<Integer, Collection<Integer>> pvsMap) {
		return pvsMapToLDD(0, pvsMap);
	}
	
	private LDD pvsMapToLDD(int topLevel, Map<Integer, Collection<Integer>> pvsMap) {
		if (topLevel == levelCount) {
			return PRESENT;
		}
		
		LDD nextLevelLDD = pvsMapToLDD(topLevel + 1, pvsMap);
		Collection<Integer> pvs = pvsMap.get(topLevel);
		
		if (pvs != null) {
			SortedSet<Integer> sortedPvs = new TreeSet<Integer>(pvs);
			Iterator<Integer> q = sortedPvs.iterator();
			
			LDD result = new LDD(this);
			result.value = q.next();
			result.ifEqual = nextLevelLDD;
			result.ifGreater = ABSENT;
			LDD prevSibling = result;
			
			while (q.hasNext()) {
				LDD sibling = new LDD(this);
				sibling.value = q.next();
				sibling.ifEqual = nextLevelLDD;
				sibling.ifGreater = ABSENT;
				prevSibling.ifGreater = sibling;
				prevSibling = sibling;
			}
			
			return result;
		} else {
			LDD result = new LDD(this);
			result.value = WILDCARD;
			result.ifEqual = nextLevelLDD;
			result.ifGreater = ABSENT;
			return result;
		}
	}
	
	/**
	 * Checks that e1 and e2 occur in the canonical order.
	 * This is simply numerical order, in addition to which DONT_CARE should always occur first.
	 * (WILDCARD should be first so that we do not have to search the entire level in order to check for its existence.)
	 */
	private boolean isOrdered(Integer e1, Integer e2) {
		if (Objects.equals(e1, WILDCARD)) {
			return true;
		}
		
		if (Objects.equals(e2, WILDCARD)) { //(Because e1 != WILDCARD.)
			return false;
		}
		
		return e1 <= e2;
	}
	
	/**
	 * Computes the union of two LDDs.
	 */
	public LDD union(LDD x, LDD y) {
		if (x == EMPTY_LDD) {
			return y;
		}
		
		if (y == EMPTY_LDD) {
			return x;
		}
		
		return union(0, x, y);
	}
	
	private LDD union(int topLevel, LDD x, LDD y) {
		// [ x != EMPTY_LDD && y != EMPTY_LDD ]
		
		if (topLevel == levelCount) {
			return PRESENT;
		}
		
		if (x == ABSENT) {
			return y;
		}
		
		if (y == ABSENT) {
			return x;
		}
		
		if (Objects.equals(x.value, WILDCARD) && Objects.equals(y.value, WILDCARD)) {
			LDD ldd = new LDD(this);
			ldd.value = WILDCARD;
			ldd.ifEqual = union(topLevel + 1, x.ifEqual, y.ifEqual);
			ldd.ifGreater = union(topLevel, x.ifGreater, y.ifGreater);
			return addToCache(topLevel, ldd);
		}
		
		if (Objects.equals(x.value, WILDCARD)) {
			LDD ldd = new LDD(this);
			ldd.value = WILDCARD;
			ldd.ifEqual = x.ifEqual;
			ldd.ifGreater = union(topLevel, x.ifGreater, y);
			return addToCache(topLevel, ldd);
		}
		
		if (Objects.equals(y.value, WILDCARD)) {
//			System.out.println("??");
			return union(topLevel, y, x); //Reuse the code above.
		}
		
		if (Objects.equals(x.value, y.value)) {
			// [ x.value == y.value ]
			LDD ldd = new LDD(this);
			ldd.value = x.value;
			ldd.ifEqual = union(topLevel + 1, x.ifEqual, y.ifEqual);
			ldd.ifGreater = union(topLevel, x.ifGreater, y.ifGreater);
			return addToCache(topLevel, ldd);
		} // [ x.value != y.value ]
		
		if (isOrdered(x.value, y.value)) {
			// [ x.value < y.value ]
			LDD ldd = new LDD(this);
			ldd.value = x.value;
			ldd.ifEqual = x.ifEqual;
			ldd.ifGreater = union(topLevel, x.ifGreater, y);
			return addToCache(topLevel, ldd);
		} else {
			// [ x.value > y.value ]
			LDD ldd = new LDD(this);
			ldd.value = y.value;
			ldd.ifEqual = y.ifEqual;
			ldd.ifGreater = union(topLevel, y.ifGreater, x);
			return addToCache(topLevel, ldd);
		}
	}
	
	/**
	 * Computes the intersection of two LDDs.
	 */
	public LDD intersection(LDD x, LDD y) {
		if (x == EMPTY_LDD || y == EMPTY_LDD) {
			return EMPTY_LDD;
		}
		
		return intersection(0, x, y);
	}
	
	private LDD intersection(int topLevel, LDD x, LDD y) {
		// [ x != EMPTY_LDD && y != EMPTY_LDD ]
		
		if (topLevel == levelCount) {
			return PRESENT;
		}
		
//		System.out.println("phase 1");
		
		LDD rootLDD = new LDD(this);
		rootLDD.value = WILDCARD;
		rootLDD.ifEqual = EMPTY_LDD;
		rootLDD.ifGreater = ABSENT;
		
		LDD parentLDD = rootLDD;
		LDD xIterator = x;
		
		while (xIterator != ABSENT) {
			LDD yIterator = y;
			
			while (yIterator != ABSENT && isOrdered(yIterator.value, xIterator.value)) {
				if (Objects.equals(xIterator.value, WILDCARD)) {
					LDD nextLDD = new LDD(this);
					nextLDD.value = yIterator.value;
					nextLDD.ifEqual = intersection(topLevel + 1, xIterator.ifEqual, yIterator.ifEqual);
					nextLDD.ifGreater = ABSENT;
					parentLDD.ifGreater = nextLDD;
					parentLDD = nextLDD;
				} else {
					if (Objects.equals(yIterator.value, WILDCARD) || Objects.equals(xIterator.value, yIterator.value)) {
						LDD nextLDD = new LDD(this);
						nextLDD.value = xIterator.value;
						nextLDD.ifEqual = intersection(topLevel + 1, xIterator.ifEqual, yIterator.ifEqual);
						nextLDD.ifGreater = ABSENT;
						parentLDD.ifGreater = nextLDD;
						parentLDD = nextLDD;
					}
				}
				
				yIterator = yIterator.ifGreater;
			}
			
			xIterator = xIterator.ifGreater;
		}
		
//		System.out.println("phase 2");
		
		//Remove empty nodes from the head of the linked list:
		while (rootLDD.ifEqual == EMPTY_LDD) {
			rootLDD = rootLDD.ifGreater;
			
			if (rootLDD == ABSENT) {
				return EMPTY_LDD;
			}
		}
		
//		System.out.println("phase 3");
		
		// [ rootLDD != ABSENT && rootLDD.ifEqual != EMPTY_LDD ]
		LDD currentLDD = rootLDD;
		
		while (currentLDD.ifGreater != ABSENT) {
			if (currentLDD.ifGreater.ifEqual == EMPTY_LDD) {
				currentLDD.ifGreater = currentLDD.ifGreater.ifGreater;
			} else {
				currentLDD = currentLDD.ifGreater;
			}
		}
		
		return addToCache(topLevel, rootLDD);
	}
	
//	/**
//	 * Computes the values in x that remain after removing the values in y.
//	 */
//	public LDD remove(LDD x, LDD y) {
//		return remove(0, x, y);
//	}
//	
//	private LDD remove(int topLevel, LDD x, LDD y) {
//		//TODO
//	}
	
	/**
	 * Removes from the cache all LDDs that are not "alive".
	 */
	public void cleanup() {
		for (Map.Entry<Integer, Map<LDD, LDD>> e : cachePerLevel.entrySet()) {
			Iterator<LDD> q = e.getValue().keySet().iterator();
			
			while (q.hasNext()) {
				LDD ldd = q.next();
				
				if (ldd.isAlive) {
					ldd.isAlive = false;
				} else {
					q.remove();
					cacheSize--;
				}
			}
		}
		
		EMPTY_LDD.isAlive = true;
		PRESENT.isAlive = true;
		ABSENT.isAlive = true;
	}
	
	public class LDD {
		private LDDFactory owner;
		private Integer value; //Can be NULL, indicating WILDCARD.
		private LDD ifEqual; //Can never be ABSENT; denoted with =
		private LDD ifGreater; //Can never be PRESENT; denoted with >
		private boolean isAlive;
//		private int userCount;
		
		private LDD(LDDFactory owner) {
			this.owner = owner;
			
			value = WILDCARD;
			ifEqual = PRESENT;
			ifGreater = ABSENT;
			isAlive = false;
		}
		
		private LDD(LDDFactory owner, boolean isAlive) {
			this.owner = owner;
			this.isAlive = isAlive;
			
			value = WILDCARD;
			ifEqual = PRESENT;
			ifGreater = ABSENT;
		}
		
		public LDDFactory getOwner() {
			return owner;
		}
		
		public void keepAlive() {
			if (!isAlive) {
				isAlive = true;
				
				if (ifEqual != null) {
					ifEqual.keepAlive();
				}
				
				if (ifGreater != null) {
					ifGreater.keepAlive();
				}
			}
		}
		
//		public void free() {
//			for (LDD node : getReachableNodes()) {
//				node.userCount--;
//			}
//		}
		
		public boolean contains(Map<Integer, Integer> m) {
			return owner.contains(this, m);
		}
		
		public boolean containsAll(LDD y) {
			return owner.containsAll(this, y);
		}
		
		public LDD union(LDD y) {
			return owner.union(this, y);
		}
		
		public LDD intersection(LDD y) {
			return owner.intersection(this, y);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(value, ifEqual, ifGreater);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == EMPTY_LDD || obj == EMPTY_LDD || this == PRESENT || obj == PRESENT || this == ABSENT || obj == ABSENT) {
				return this == obj;
			}
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LDD)) {
				return false;
			}
			LDD other = (LDD) obj;
			return Objects.equals(value, other.value) && Objects.equals(ifGreater, other.ifGreater) && Objects.equals(ifEqual, other.ifEqual);
		}
		
		private void sanityCheck() {
			if (this == PRESENT || this == ABSENT) {
				return;
			}
			
			if (ifEqual == null) {
				throw new Error();
			}
			
			if (ifGreater == null) {
				throw new Error();
			}
		}
		
		public Set<LDD> getReachableNodes() {
			Set<LDD> result = new HashSet<LDD>();
			result.add(this);
			
			if (ifEqual != null) {
				result.addAll(ifEqual.getReachableNodes());
			}
			
			if (ifGreater != null) {
				result.addAll(ifGreater.getReachableNodes());
			}
			
			return result;
		}
		
		public void printGraphvizFile(String filename) {
			try {
				PrintStream ps = new PrintStream(filename + ".gv");
				printGraphvizFile(ps, filename);
				ps.flush();
			} catch (FileNotFoundException e) {
				throw new Error(e);
			}
		}
		
		public void printGraphvizFile(PrintStream out, String header) {
			Map<LDD, String> namePerNode = new HashMap<LDD, String>();
			
			for (LDD node : getReachableNodes()) {
				namePerNode.put(node, "N" + namePerNode.size());
			}
			
			out.println("// " + getClass().getCanonicalName());
			out.println("// " + header);
			out.println("digraph G {");
			
			for (Map.Entry<LDD, String> e : namePerNode.entrySet()) {
				if (Objects.equals(e.getKey(), EMPTY_LDD)) {
					out.println("\t" + e.getValue() + " [label=\"EMPTY\", shape=ellipse];");
					continue;
				}
				
				if (Objects.equals(e.getKey(), ABSENT)) {
					out.println("\t" + e.getValue() + " [label=\"ABSENT\", shape=ellipse];");
					continue;
				}
				
				if (Objects.equals(e.getKey(), PRESENT)) {
					out.println("\t" + e.getValue() + " [label=\"PRESENT\", shape=ellipse];");
					continue;
				}
				
				if (Objects.equals(e.getKey().value, WILDCARD)) {
					out.println("\t" + e.getValue() + " [label=\"WILDCARD\", shape=ellipse];");
					continue;
				}
				
				out.println("\t" + e.getValue() + " [label=\"" + e.getKey().value + "\", shape=ellipse];");
			}
			
			for (Map.Entry<LDD, String> e : namePerNode.entrySet()) {
				if (e.getKey().ifEqual != null) {
					String tgt = namePerNode.get(e.getKey().ifEqual);
					out.println("\t" + e.getValue() + " -> " + tgt + " [style=dashed];");
				}
				
				if (e.getKey().ifGreater != null) {
					String tgt = namePerNode.get(e.getKey().ifGreater);
					out.println("\t" + e.getValue() + " -> " + tgt + " [style=solid];");
				}
			}
			
			out.println("\tI0 [label=\"( initial )\\n\\n\\\"" + header + "\\\"\", shape=ellipse];");
			out.println("\tI0 -> " + namePerNode.get(this) + " [style=dashed];");
			out.println("}");
		}
	}
}

