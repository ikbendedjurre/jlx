package jlx.utils;

import java.util.*;

public class EquivDistr<S, Y> {
	private Map<Y, Set<S>> sourcesPerOutcome;
	private Set<Set<S>> sourceEqClzs;
	
	public EquivDistr() {
		sourcesPerOutcome = new HashMap<Y, Set<S>>();
		sourceEqClzs = null;
	}
	
	public void addOutcome(S source, Y outcome) {
		HashMaps.inject(sourcesPerOutcome, outcome, source);
		sourceEqClzs = null;
	}
	
	public void addDistr(EquivDistr<S, Y> distr) {
		HashMaps.merge(sourcesPerOutcome, distr.sourcesPerOutcome);
		sourceEqClzs = null;
	}
	
	public Set<Set<S>> getSourceEqClzs() {
		if (sourceEqClzs == null) {
			sourceEqClzs = getSourceEqClzs(new HashSet<Set<S>>(sourcesPerOutcome.values()));
		}
		
		return sourceEqClzs;
	}
	
	public static <S> Set<Set<S>> getSourceEqClzs(Set<Set<S>> groupedSources) {
		Set<Set<S>> result = new HashSet<Set<S>>();
		
		Set<Set<S>> fringe = new HashSet<Set<S>>();
		Set<Set<S>> newFringe = new HashSet<Set<S>>();
		fringe.addAll(groupedSources);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (Set<S> c1 : fringe) {
				Iterator<Set<S>> q = result.iterator(); 
				boolean disjoint = true;
				
				while (q.hasNext()) {
					Set<S> c2 = q.next();
					
					if (!Collections.disjoint(c1, c2)) {
						Set<S> x1 = new HashSet<S>(c1);
						x1.removeAll(c2);
						Set<S> x2 = new HashSet<S>(c2);
						x2.removeAll(c1);
						Set<S> x12 = new HashSet<S>(c1);
						x12.retainAll(c2);
						
						if (x1.size() > 0) {
							newFringe.add(x1);
						}
						
						if (x2.size() > 0) {
							newFringe.add(x2);
						}
						
						if (x12.size() > 0) {
							newFringe.add(x12);
						}
						
						disjoint = false;
						q.remove();
					}
				}
				
				if (disjoint) {
					result.add(c1);
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return result;
	}
	
	public Set<Y> getOutcomes() {
		return sourcesPerOutcome.keySet();
	}
	
	public Y getOutcome(Set<S> eqClz) {
		for (Map.Entry<Y, Set<S>> e : sourcesPerOutcome.entrySet()) {
			if (!Collections.disjoint(e.getValue(), eqClz)) {
				return e.getKey();
			}
		}
		
		throw new Error("Should not happen!");
	}
}
