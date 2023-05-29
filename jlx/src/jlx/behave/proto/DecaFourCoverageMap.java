package jlx.behave.proto;

import java.util.*;

import jlx.utils.*;

public class DecaFourCoverageMap {
	private Map<DecaFourVertex, Set<DecaFourStateConfig>> cfgsPerVtx;
	private Map<DecaFourTransition, Set<DecaFourStateConfig>> cfgsPerTr;
	
	public DecaFourCoverageMap() {
		cfgsPerVtx = new HashMap<DecaFourVertex, Set<DecaFourStateConfig>>();
		cfgsPerTr = new HashMap<DecaFourTransition, Set<DecaFourStateConfig>>();
	}
	
	public DecaFourCoverageMap(DecaFourCoverageMap source) {
		cfgsPerVtx = new HashMap<DecaFourVertex, Set<DecaFourStateConfig>>();
		cfgsPerTr = new HashMap<DecaFourTransition, Set<DecaFourStateConfig>>();
		
		for (Map.Entry<DecaFourVertex, Set<DecaFourStateConfig>> e : source.cfgsPerVtx.entrySet()) {
			cfgsPerVtx.put(e.getKey(), new HashSet<DecaFourStateConfig>(e.getValue()));
		}
		
		for (Map.Entry<DecaFourTransition, Set<DecaFourStateConfig>> e : source.cfgsPerTr.entrySet()) {
			cfgsPerTr.put(e.getKey(), new HashSet<DecaFourStateConfig>(e.getValue()));
		}
	}
	
	public Map<DecaFourVertex, Set<DecaFourStateConfig>> getCfgsPerVtx() {
		return cfgsPerVtx;
	}
	
	public Map<DecaFourTransition, Set<DecaFourStateConfig>> getCfgsPerTr() {
		return cfgsPerTr;
	}
	
	public void add(DecaFourCoverageMap other) {
		HashMaps.merge(cfgsPerVtx, other.cfgsPerVtx);
		HashMaps.merge(cfgsPerTr, other.cfgsPerTr);
	}
	
	public void remove(DecaFourCoverageMap other) {
		cfgsPerVtx.keySet().removeAll(other.cfgsPerVtx.keySet());
		cfgsPerTr.keySet().removeAll(other.cfgsPerTr.keySet());
	}
	
	public Set<DecaFourStateConfig> getVtxCfgs() {
		Set<DecaFourStateConfig> result = new HashSet<DecaFourStateConfig>();
		
		for (Set<DecaFourStateConfig> cfgs : cfgsPerVtx.values()) {
			result.addAll(cfgs);
		}
		
		return result;
	}
	
	public Set<DecaFourStateConfig> getTrCfgs() {
		Set<DecaFourStateConfig> result = new HashSet<DecaFourStateConfig>();
		
		for (Set<DecaFourStateConfig> cfgs : cfgsPerVtx.values()) {
			result.addAll(cfgs);
		}
		
		return result;
	}
}

