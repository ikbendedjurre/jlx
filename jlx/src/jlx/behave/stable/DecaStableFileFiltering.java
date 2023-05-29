package jlx.behave.stable;

import java.util.*;

import jlx.utils.*;

public class DecaStableFileFiltering extends DecaStableFileMinimized {
	private Map<InitState, Set<Vertex>> undistVtxsPerInitState;
	
	public static class DistVertex {
		private final Vertex reprVtx;
		private final Map<Vertex, Vertex> potPerOrig;
		
		public DistVertex(Vertex reprVtx, Map<Vertex, Vertex> potPerOrig) {
			this.reprVtx = reprVtx;
			this.potPerOrig = potPerOrig;
		}
	}
	
	public static class Filter {
		
	}
	
	public DecaStableFileFiltering() {
		undistVtxsPerInitState = new HashMap<InitState, Set<Vertex>>();
	}
	
	@Override
	public void init() {
		super.init();
		
		undistVtxsPerInitState.clear();
		
		for (EquivClz v : getEquivClzs()) {
			if (v.vtxs.size() != 1) {
				throw new Error("Should not happen!");
			}
			
			HashMaps.inject(undistVtxsPerInitState, new InitState(v.someVtx()), v.someVtx());
		}
		
		
	}
	
	public void distinguish(DistVertex dv) {
//		Set<InputChanges> ics = new HashSet<InputChanges>();
		
		
	}
	
	public Map<InitState, Filter> getFilters(int length) {
		Map<InitState, Filter> result = new HashMap<InitState, Filter>();
		
		for (Map.Entry<InitState, Set<Vertex>> e : undistVtxsPerInitState.entrySet()) {
			result.put(e.getKey(), getFilter(e.getValue(), length));
		}
		
		return result;
	}
	
	public Filter getFilter(Set<Vertex> undistVtxs, int length) {
		Map<Vertex, Vertex> potPerOrig = new HashMap<Vertex, Vertex>();
		
		for (Vertex v : undistVtxs) {
			potPerOrig.put(v, v);
		}
		
		Set<DistVertex> beenHere = new HashSet<DistVertex>();
		beenHere.add(new DistVertex(undistVtxs.iterator().next(), potPerOrig));
		
		Set<DistVertex> fringe = new HashSet<DistVertex>();
		Set<DistVertex> newFringe = new HashSet<DistVertex>();
		fringe.addAll(beenHere);
		
		int dist = 0;
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DistVertex v : fringe) {
				for (Transition t : computeOutgoing(v)) {
					
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return null;
	}
	
	public Set<Transition> computeOutgoing(DistVertex v) {
//		EquivClz ec = getEquivClzPerVtx().get(v.reprVtx);
//		
//		for (SplitReason sr : ec.splitReasons) {
//			Map<Vertex, Vertex> newPotPerOrig = new HashMap<Vertex, Vertex>();
//			
//			for (Map.Entry<Vertex, Vertex> e : v.potPerOrig.entrySet()) {
//				if (!sr.rejectedVtxs.contains(e.getValue())) {
//					Transition outgoing = e.getValue().getOutgoingTransition(sr.inputChanges);
//					newPotPerOrig.put(e.getKey(), outgoing.getTgt());
//				}
//			}
//			
//			new DistTransition(sr.inputChanges, sr.response, new DistVertex(newPotPerOrig));
//		}
//		
//		for (InputChanges ic : v.reprVtx.getOutgoingInputChanges()) {
//			
//			
//			Map<Vertex, Map<Response, Vertex> newPotPerOrig = new HashMap<Vertex, Vertex>();
//			
////			for (Map.Entry<Vertex, Vertex> e : v.potPerOrig.entrySet()) {
////				Transition t = e.getValue().getOutgoingTransition(ic);
////				newPotPerOrig.put(e.getKey(), t.getTgt());
////				
////			}
//		}
		
		return null;
	}
}

