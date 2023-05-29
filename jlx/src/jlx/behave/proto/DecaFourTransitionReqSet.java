package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.utils.*;

public class DecaFourTransitionReqSet {
	private Set<Map<JScope, DecaFourVertex>> reqCfgs;
	private int reqCfgsHashCode;
	
	public LDDMapFactory.LDDMap<JScope, DecaFourVertex> x;
	
	private DecaFourTransitionReqSet(Set<Map<JScope, DecaFourVertex>> reqCfgs, LDDMapFactory.LDDMap<JScope, DecaFourVertex> x) {
		this.reqCfgs = reqCfgs;
		this.x = x;
		
		reqCfgsHashCode = Objects.hash(reqCfgs);
	}
	
	//public final static DecaFourTransitionReqSet NO_REQS = create(Collections.singleton(Collections.emptyMap()));
	
	public static DecaFourTransitionReqSet create(Set<Map<JScope, DecaFourVertex>> reqCfgs, LDDMapFactory.LDDMap<JScope, DecaFourVertex> x) {
		return reqCfgs.isEmpty() ? null : new DecaFourTransitionReqSet(reqCfgs, x);
		//return new DecaFourTransitionReqSet(reqCfgs, x);
	}
	
	/**
	 * Partial configurations that must be in place for a transition to be enabled.
	 * Based on the internal output valuations of state machines.
	 * Never empty; "emptiest" value is a set that contains an empty map.
	 */
	public Set<Map<JScope, DecaFourVertex>> getReqCfgs() {
		return reqCfgs;
	}
	
	public boolean isEmpty() {
		return reqCfgs.isEmpty();
	}
	
	private static boolean matchesReqCfg(Map<JScope, DecaFourVertex> subCfg, Map<JScope, DecaFourVertex> reqCfg) {
		for (Map.Entry<JScope, DecaFourVertex> e : subCfg.entrySet()) {
			DecaFourVertex v = reqCfg.get(e.getKey());
			
			if (v != null && v != e.getValue()) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean matchesReqCfg2(Map<JScope, Set<DecaFourVertex>> subCfg, Map<JScope, DecaFourVertex> reqCfg) {
		for (Map.Entry<JScope, Set<DecaFourVertex>> e : subCfg.entrySet()) {
			DecaFourVertex v = reqCfg.get(e.getKey());
			
			if (v != null && !e.getValue().contains(v)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean containsMatch(Map<JScope, DecaFourVertex> subCfg) {
		for (Map<JScope, DecaFourVertex> reqCfg : reqCfgs) {
			if (matchesReqCfg(subCfg, reqCfg)) {
				return true;
			}
		}
		
		return false;
	}
	
//	private static int getMatchCount(Map<JScope, Set<DecaFourVertex>> subCfg, Map<JScope, DecaFourVertex> reqCfg) {
//		int result = 1;
//		
//		for (Map.Entry<JScope, Set<DecaFourVertex>> e : subCfg.entrySet()) {
//			DecaFourVertex v = reqCfg.get(e.getKey());
//			
//			if (v != null) {
//				if (!e.getValue().contains(v)) {
//					return 0;
//				}
//			} else {
//				result = result * e.getValue().size();
//			}
//		}
//		
//		return result;
//	}
	
	public boolean containsMatch2(Map<JScope, Set<DecaFourVertex>> subCfg) {
		for (Map<JScope, DecaFourVertex> reqCfg : reqCfgs) {
			if (matchesReqCfg2(subCfg, reqCfg)) {
				return true;
			}
		}
		
		return false;
	}
	
	public DecaFourTransitionReqSet selectReachable() {
		Set<Map<JScope, DecaFourVertex>> result = new HashSet<Map<JScope, DecaFourVertex>>();
		
		for (Map<JScope, DecaFourVertex> reqCfg : reqCfgs) {
			boolean add = true;
			
			for (Map.Entry<JScope, DecaFourVertex> e : reqCfg.entrySet()) {
				if (!e.getValue().isReachable) {
					add = false;
					break;
				}
			}
			
			if (add) {
				result.add(reqCfg);
			}
		}
		
		return DecaFourTransitionReqSet.create(result, null);
	}
	
	public DecaFourTransitionReqSet selectMatches(Map<JScope, DecaFourVertex> subCfg) {
		Set<Map<JScope, DecaFourVertex>> result = new HashSet<Map<JScope, DecaFourVertex>>();
		
		for (Map<JScope, DecaFourVertex> reqCfg : reqCfgs) {
			if (matchesReqCfg(subCfg, reqCfg)) {
				result.add(reqCfg);
			}
		}
		
		return DecaFourTransitionReqSet.create(result, null);
	}
	
	public static DecaFourTransitionReqSet tryCombination(Map<JScope, DecaFourVertex> currentSubCfg, DecaFourTransitionReqSet currentReqSet, DecaFourTransitionReqSet additionalReqSet) {
//		System.out.println("currentSubCfg = " + Texts.concat(currentSubCfg.values(), ","));
//		System.out.println("currentReqSet = " + currentReqSet.toString());
//		System.out.println("additionalReqSet = " + additionalReqSet.toString());
		
		DecaFourTransitionReqSet additionalReqSet1 = additionalReqSet.selectMatches(currentSubCfg);
		
		if (additionalReqSet1 == null) {
			return null;
		}
		
//		DecaFourTransitionReqSet currentReqSet2 = currentReqSet.excludeScopes(currentSubCfg.keySet());
//		DecaFourTransitionReqSet additionalReqSet2 = additionalReqSet1.excludeScopes(currentSubCfg.keySet());
		Set<Map<JScope, DecaFourVertex>> result = new HashSet<Map<JScope, DecaFourVertex>>();
		
		for (Map<JScope, DecaFourVertex> currentReqCfg2 : currentReqSet.reqCfgs) {
			for (Map<JScope, DecaFourVertex> additionalReqCfg2 : additionalReqSet1.reqCfgs) {
				Map<JScope, DecaFourVertex> newReqCfg = new HashMap<JScope, DecaFourVertex>();
				newReqCfg.putAll(currentReqCfg2);
				newReqCfg.putAll(additionalReqCfg2);
				result.add(newReqCfg);
			}
		}
		
		return DecaFourTransitionReqSet.create(result, null);
	}
	
//	public DecaFourTransitionReqSet excludeScopes(Set<JScope> excludedScopes) {
//		Set<Map<JScope, DecaFourVertex>> result = new HashSet<Map<JScope, DecaFourVertex>>();
//		
//		for (Map<JScope, DecaFourVertex> reqCfg : reqCfgs) {
//			Map<JScope, DecaFourVertex> newReqCfg = new HashMap<JScope, DecaFourVertex>();
//			
//			for (Map.Entry<JScope, DecaFourVertex> e : reqCfg.entrySet()) {
//				if (!excludedScopes.contains(e.getKey())) {
//					newReqCfg.put(e.getKey(), e.getValue());
//				}
//			}
//			
//			result.add(newReqCfg);
//		}
//		
//		return DecaFourTransitionReqSet.create(result);
//	}
	
	@Override
	public int hashCode() {
		return reqCfgsHashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DecaFourTransitionReqSet)) {
			return false;
		}
		DecaFourTransitionReqSet other = (DecaFourTransitionReqSet) obj;
		return Objects.equals(reqCfgs, other.reqCfgs);
	}
	
	@Override
	public String toString() {
		List<String> elems = new ArrayList<String>();
		
		for (Map<JScope, DecaFourVertex> reqCfg : reqCfgs) {
			elems.add("{ " + Texts.concat(reqCfg.values(), ", ") + " }");
		}
		
		return "{" + Texts.concat(elems, ", ") + "}";
	}
}

