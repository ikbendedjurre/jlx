package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

public class DecaStableFileDistinguished4 extends DecaStableFileMinimized {
	private final Map<EquivClz, Set<InputChanges>> splittersPerEquivClz;
	private final Set<EquivClz> undistinguished;
	
	public DecaStableFileDistinguished4() {
		splittersPerEquivClz = new HashMap<EquivClz, Set<InputChanges>>();
		undistinguished = new HashSet<EquivClz>();
	}
	
	@Override
	public void init() {
		super.init();
		
		Map<InitState, Set<EquivClz>> vtxsPerInitState2 = new HashMap<InitState, Set<EquivClz>>();
		
		for (EquivClz v : getEquivClzs()) {
			HashMaps.inject(vtxsPerInitState2, new InitState(v.someVtx()), v);
		}
		
		Set<EquivClz> largest = null;
		
		for (Map.Entry<InitState, Set<EquivClz>> e : vtxsPerInitState2.entrySet()) {
			if (largest == null || e.getValue().size() > largest.size()) {
				largest = e.getValue();
			}
		}
		
		splitClz(largest);
	}
	
	public void splitClz(Set<EquivClz> xs) {
		splittersPerEquivClz.clear();
		
		for (EquivClz c : getEquivClzs()) {
			splittersPerEquivClz.put(c, new HashSet<InputChanges>());
		}
		
		System.out.println("[" + LocalTime.now() + "] begin.#undistinguished = " + xs.size());
		
		undistinguished.clear();
		undistinguished.addAll(xs);
		
		DistinguisherMap<EquivClz, Set<EquivClz>> dmap = new DistinguisherMap<EquivClz, Set<EquivClz>>();
		
		while (undistinguished.size() > 0) {
			distinguish2(new Bla(undistinguished), 10000, dmap);
			
			if (undistinguished.removeAll(dmap.getDistinguishedElems())) {
				int stepCount = 0;
				
				for (EquivClz c : dmap.getDistinguishedElems()) {
					int minDsSize = Integer.MAX_VALUE;
					
					for (Set<Set<EquivClz>> ds : dmap.getDistinguishersPerSubset().get(Collections.singleton(c))) {
						minDsSize = Math.min(minDsSize, ds.size());
					}
					
					stepCount += minDsSize;
				}
				
				System.out.println("[" + LocalTime.now() + "] #undistinguished = " + undistinguished.size() + " / " + xs.size() + "; #UIOs = " + stepCount);
			} else {
				throw new Error("No more distinctions!!");
			}
			
			dmap = dmap.minimize();
		}
		
		System.out.println("Done!!");
	}
	
	private void distinguish2(Bla start, int maxDepth, DistinguisherMap<EquivClz, Set<EquivClz>> dmap) {
		Set<Map<EquivClz, EquivClz>> beenHere = new HashSet<Map<EquivClz, EquivClz>>();
		beenHere.add(start.potPerInit);
		
		Set<Bla> fringe = new HashSet<Bla>();
		Set<Bla> newFringe = new HashSet<Bla>();
		fringe.add(start);
		
		for (int depth = 0; fringe.size() > 0 && depth < maxDepth; depth++) {
			newFringe.clear();
			
			for (Bla bla : fringe) {
				addBla(dmap, bla);
				
				Set<EquivClz> potentials = new HashSet<EquivClz>(bla.potPerInit.values());
				
				if (potentials.size() > 1) {
					newFringe.add(bla);
				} else {
					//Empty.
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			newFringe.clear();
			
			for (Bla bla : fringe) {
				for (BlaTr t : computeBestOutgoing(bla)) {
					if (beenHere.add(t.tgt.potPerInit)) {
						newFringe.add(t.tgt);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #beenHere = " + beenHere.size() + " (+" + fringe.size() + ")");
		}
	}
	
	private Set<BlaTr> computeBestOutgoing(Bla start) {
		Set<InputChanges> ics = new HashSet<InputChanges>();
		
		for (EquivClz c : start.potPerInit.values()) {
			for (InputChanges ic : c.distInputChanges) {
				if (!splittersPerEquivClz.get(c).contains(ic)) {
					ics.add(ic);
				}
			}
		}
		
		InputChanges bestInputChanges = null;
		Map<Response, Map<EquivClz, EquivClz>> bestPerResponse = null;
		
		for (InputChanges ic : ics) {
			Map<Response, Map<EquivClz, EquivClz>> perResponse = new HashMap<Response, Map<EquivClz, EquivClz>>();
			
			for (Map.Entry<EquivClz, EquivClz> e : start.potPerInit.entrySet()) {
				Transition t = e.getValue().someVtx().getOutgoingTransition(ic);
				EquivClz newPot = getEquivClzPerVtx().get(t.getTgt());
				HashMaps.injectSet(perResponse, new Response(t), e.getKey(), newPot);
			}
			
			if (bestPerResponse == null || perResponse.size() > bestPerResponse.size()) {
				bestPerResponse = perResponse;
				bestInputChanges = ic; 
			}
		}
		
		Set<BlaTr> result = new HashSet<BlaTr>();
		
		if (bestPerResponse != null) {
			for (Map.Entry<Response, Map<EquivClz, EquivClz>> e : bestPerResponse.entrySet()) {
				result.add(new BlaTr(start, e.getValue(), bestInputChanges));
			}
			
			for (EquivClz v : start.potPerInit.values()) {
				HashMaps.inject(splittersPerEquivClz, v, bestInputChanges);
			}
		}
		
		return result;
	}
	
	private static class Bla {
		public final Map<EquivClz, EquivClz> potPerInit;
		public final BlaTr incoming;
		
		public Bla(Map<EquivClz, EquivClz> potPerInit, BlaTr incoming) {
			this.potPerInit = potPerInit;
			this.incoming = incoming;
		}
		
		public Bla(Set<EquivClz> inits) {
			potPerInit = new HashMap<EquivClz, EquivClz>();
			incoming = null;
			
			for (EquivClz init : inits) {
				potPerInit.put(init, init);
			}
		}
	}
	
	private static class BlaTr {
		public final Bla src;
		public final Bla tgt;
		public final InputChanges inputChanges;
		
		public BlaTr(Bla src, Map<EquivClz, EquivClz> tgtPotPerInit, InputChanges inputChanges) {
			this.src = src;
			this.tgt = new Bla(tgtPotPerInit, this);
			this.inputChanges = inputChanges;
		}
	}
	
	private void addBla(DistinguisherMap<EquivClz, Set<EquivClz>> dmap, Bla bla) {
		Map<InitState, Set<EquivClz>> initStates = new HashMap<InitState, Set<EquivClz>>();
		
		for (Map.Entry<EquivClz, EquivClz> e2 : bla.potPerInit.entrySet()) {
			InitState initState = new InitState(e2.getValue().someVtx());
			HashMaps.inject(initStates, initState, e2.getKey());
		}
		
		for (Set<EquivClz> qs : initStates.values()) {
//			if (qs.size() < 1000) {
				dmap.add(qs, qs);
//			}
		}
	}
	
	public static void main(String[] args) {
		DecaStableFileDistinguished4 x = new DecaStableFileDistinguished4();
//		x.loadFromFile("models", "all.reduced.3.stable", true);
		
		x.loadFromFile("models", "all.reduced.2.stable", true);
		x.init();
		x.saveToFile("models", "all.reduced.3.stable");
		
//		System.out.println("#vtxs = " + x.getVertices().size());
//		System.out.println("#reduced = " + x.equivClzs.size());
	}
}
