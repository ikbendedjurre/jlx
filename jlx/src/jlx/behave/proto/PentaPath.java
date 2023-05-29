package jlx.behave.proto;

import java.util.*;

import jlx.behave.*;
import jlx.common.reflection.ModelException;
import jlx.utils.HashMaps;

public abstract class PentaPath {
	public static class Stable extends PentaPath {
		private final PentaVertex target;
		
		public Stable(PentaVertex target) {
			this.target = target;
		}
		
		@Override
		public Map<List<PentaTransition>, Set<PentaVertex>> getTgtsPerTrSeq() {
			return Collections.singletonMap(Collections.emptyList(), Collections.singleton(target));
		}
	}
	
	public static class Choice extends PentaPath {
		private Map<PentaTransition, PentaPath> succs;
		
		public Choice(Set<PentaTransition> encounteredTrs) {
			succs = new HashMap<PentaTransition, PentaPath>();
		}
		
		public Map<PentaTransition, PentaPath> getSuccs() {
			return succs;
		}
		
		@Override
		public Map<List<PentaTransition>, Set<PentaVertex>> getTgtsPerTrSeq() {
			Map<List<PentaTransition>, Set<PentaVertex>> result = new HashMap<List<PentaTransition>, Set<PentaVertex>>();
			
			for (Map.Entry<PentaTransition, PentaPath> e1 : succs.entrySet()) {
				for (Map.Entry<List<PentaTransition>, Set<PentaVertex>> e2 : e1.getValue().getTgtsPerTrSeq().entrySet()) {
					List<PentaTransition> seq = new ArrayList<PentaTransition>(e2.getKey());
					seq.add(0, e1.getKey());
					result.put(seq, e2.getValue());
				}
			}
			
			return result;
		}
	}
	
	public static class Parallel extends PentaPath {
		private Map<PentaTransition, PentaPath> succs;
		
		public Parallel(Set<PentaTransition> encounteredTrs) {
			succs = new HashMap<PentaTransition, PentaPath>();
		}
		
		public Map<PentaTransition, PentaPath> getSuccs() {
			return succs;
		}
		
		@Override
		public Map<List<PentaTransition>, Set<PentaVertex>> getTgtsPerTrSeq() {
			Map<PentaTransition, Set<Map<List<PentaTransition>, Set<PentaVertex>>>> seqsPerTr = new HashMap<PentaTransition, Set<Map<List<PentaTransition>, Set<PentaVertex>>>>();
			
			for (Map.Entry<PentaTransition, PentaPath> e1 : succs.entrySet()) {
				Set<Map<List<PentaTransition>, Set<PentaVertex>>> newSeqs = new HashSet<Map<List<PentaTransition>, Set<PentaVertex>>>();
				
				for (Map.Entry<List<PentaTransition>, Set<PentaVertex>> e2 : e1.getValue().getTgtsPerTrSeq().entrySet()) {
					newSeqs.add(Collections.singletonMap(e2.getKey(), e2.getValue()));
				}
				
				seqsPerTr.put(e1.getKey(), newSeqs);
			}
			
			Map<List<PentaTransition>, Set<PentaVertex>> result = new HashMap<List<PentaTransition>, Set<PentaVertex>>();
			
			for (Map<PentaTransition, Map<List<PentaTransition>, Set<PentaVertex>>> combo : HashMaps.allCombinations(seqsPerTr)) {
				Set<PentaVertex> tgts = new HashSet<PentaVertex>();
				List<PentaTransition> seq = new ArrayList<PentaTransition>();
				
				for (Map.Entry<PentaTransition, Map<List<PentaTransition>, Set<PentaVertex>>> e : combo.entrySet()) {
					Map.Entry<List<PentaTransition>, Set<PentaVertex>> entry = e.getValue().entrySet().iterator().next();
					seq.add(e.getKey());
					seq.addAll(entry.getKey());
					tgts.addAll(entry.getValue());
				}
				
				result.put(seq, tgts);
			}
			
			return result;
		}
	}
	
	public abstract Map<List<PentaTransition>, Set<PentaVertex>> getTgtsPerTrSeq();
	
	private static boolean isStable(Set<PentaVertex> vtxs) {
		switch (vtxs.size()) {
			case 0:
				throw new Error("Should not happen!");
			case 1:
				return State.class.isAssignableFrom(vtxs.iterator().next().getSysmlClz());
			default:
				for (PentaVertex v : vtxs) {
					if (State.class.isAssignableFrom(v.getSysmlClz())) {
						throw new Error("Should not happen!"); //TODO what about multi-region composite states?!
					}
				}
				
				return false;
		}
	}
	
	private static boolean isParallel(Set<PentaVertex> vtxs) {
		switch (vtxs.size()) {
			case 0:
				throw new Error("Should not happen!");
			case 1:
				return InitialState.class.isAssignableFrom(vtxs.iterator().next().getSysmlClz());
			default:
				boolean result = false;
				
				for (PentaVertex v : vtxs) {
					if (InitialState.class.isAssignableFrom(v.getSysmlClz())) {
						result = true;
						break;
					}
				}
				
				if (result) {
					for (PentaVertex v : vtxs) {
						if (!InitialState.class.isAssignableFrom(v.getSysmlClz())) {
							throw new Error("Should not happen!");
						}
					}
				}
				
				return false;
		}
	}
	
	private static Set<PentaTransition> add(Set<PentaTransition> soFar, PentaTransition t) throws ModelException {
		Set<PentaTransition> newSoFar = new HashSet<PentaTransition>(soFar);
		
		if (!newSoFar.add(t)) {
			throw new ModelException(t.getFileLocation(), "Circular paths of transitions are forbidden!");
		}
		
		return newSoFar;
	}
	
	public static PentaPath createFromTransition(PentaStateMachine sm, PentaTransition t, Set<PentaTransition> encounteredTrs) throws ModelException {
		Choice result = new Choice(encounteredTrs);
		result.getSuccs().put(t, createFromVtxs(sm, t.getTargetVertices(), add(encounteredTrs, t)));
		return result;
	}
	
	public static PentaPath createFromVtxs(PentaStateMachine sm, Set<PentaVertex> vtxs, Set<PentaTransition> encounteredTrs) throws ModelException {
		if (isStable(vtxs)) {
			return new Stable(vtxs.iterator().next());
		}
		
		if (isParallel(vtxs)) {
			Parallel result = new Parallel(encounteredTrs);
			
			for (PentaTransition outgoing : sm.transitions) {
				if (!Collections.disjoint(outgoing.getSourceVertices(), vtxs)) {
					if (result.getSuccs().containsKey(outgoing)) {
						throw new ModelException(outgoing.getFileLocation(), "Cannot start more than one transition from an initial vertex!");
					}
					
					result.getSuccs().put(outgoing, createFromVtxs(sm, outgoing.getTargetVertices(), add(encounteredTrs, outgoing)));
				}
			}
			
			return result;
		}
		
		Choice result = new Choice(encounteredTrs);
		
		for (PentaTransition outgoing : sm.transitions) {
			if (!Collections.disjoint(outgoing.getSourceVertices(), vtxs)) {
				if (outgoing.getSourceVertices().size() > 1) {
					throw new Error("Should not happen!");
				}
				
				result.getSuccs().put(outgoing, createFromVtxs(sm, outgoing.getTargetVertices(), add(encounteredTrs, outgoing)));
			}
		}
		
		return result;
	}
}
