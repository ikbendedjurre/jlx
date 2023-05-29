package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

public class DecaStableFileDistinguished3 extends DecaStableFileMinimized {
	
	private static class Bla {
		public final Map<EquivClz, EquivClz> potPerInit;
		public final BlaTr incoming;
		
		public Bla(Map<EquivClz, EquivClz> potPerInit, BlaTr incoming) {
			this.potPerInit = potPerInit;
			this.incoming = incoming;
			
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
	
	private final Map<EquivClz, Bla> blaPerEquivClz;
	private final Set<EquivClz> distinguished;
	private final Set<EquivClz> undistinguished;
	
	public DecaStableFileDistinguished3() {
		blaPerEquivClz = new HashMap<EquivClz, Bla>();
		distinguished = new HashSet<EquivClz>();
		undistinguished = new HashSet<EquivClz>();
	}
	
	@Override
	public void init() {
		super.init();
		
		blaPerEquivClz.clear();
		distinguished.clear();
		undistinguished.clear();
		
		for (EquivClz v : getEquivClzs()) {
			undistinguished.add(v);
		}
		
//		Map<InitState, Set<EquivClz>> vtxsPerInitState2 = new HashMap<InitState, Set<EquivClz>>();
//		
//		for (EquivClz v : undistinguished) {
//			Permutations.inject(vtxsPerInitState2, new InitState(v.someVtx()), v);
//		}
//		
//		for (Map.Entry<InitState, Set<EquivClz>> e : vtxsPerInitState2.entrySet()) {
//			distinguish(e.getValue(), 10000, "");
//		}
//		
//		System.exit(0);
		
		int countx = 0;
		
//		while (undistinguished.size() > 0) {
			Map<InitState, Set<EquivClz>> vtxsPerInitState = new HashMap<InitState, Set<EquivClz>>();
			
			for (EquivClz v : undistinguished) {
				HashMaps.inject(vtxsPerInitState, new InitState(v.someVtx()), v);
			}
			
			Map<InitState, InputChanges> inputPerInitState = new HashMap<InitState, InputChanges>();
			Map<InitState, Map<Response, Set<EquivClz>>> perInputPerInitState = new HashMap<InitState, Map<Response, Set<EquivClz>>>();
			
			for (Map.Entry<InitState, Set<EquivClz>> e : vtxsPerInitState.entrySet()) {
				Map<Response, Set<EquivClz>> bestPerResponse = new HashMap<Response, Set<EquivClz>>();
				InputChanges bestInput = null;
				
				for (InputChanges ic : e.getValue().iterator().next().getOutgoingInputChanges()) {
					Map<Response, Set<EquivClz>> perResponse = new HashMap<Response, Set<EquivClz>>();
					
					for (EquivClz e2 : e.getValue()) {
						Response r = new Response(e2.someVtx().getOutgoingTransition(ic));
						HashMaps.inject(perResponse, r, e2);
					}
					
					if (bestInput == null || bestPerResponse.size() < perResponse.size()) {
						bestPerResponse = perResponse;
						bestInput = ic;
					}
				}
				
				inputPerInitState.put(e.getKey(), bestInput);
				perInputPerInitState.put(e.getKey(), bestPerResponse);
				countx += bestPerResponse.size();
			}
			
			int indexx = 0;
			
			for (Map.Entry<InitState, Map<Response, Set<EquivClz>>> e : perInputPerInitState.entrySet()) {
				for (Map.Entry<Response, Set<EquivClz>> e2 : e.getValue().entrySet()) {
					indexx++;
//					int d = distinguished.size();
					distinguish(e2.getValue(), 10000, "[" + indexx + " / " + countx + "]");
//					System.out.println((distinguished.size() - d) + " / " + e2.getValue().size());
				}
			}
			
//			for (int depth = 80; depth <= 100; depth++) {
//				int distinguishedSize = distinguished.size();
//				
//				for (Map.Entry<InitState, Set<EquivClz>> e : vtxsPerInitState.entrySet()) {
//					System.out.println("[" + LocalTime.now() + "] Working on " + e.getValue().size() + " eq-classes . . .");
//					distinguish(e.getValue(), depth);
//				}
//				
//				if (distinguished.size() > distinguishedSize) {
//					break;
//				}
//			}
			
			undistinguished.removeAll(distinguished);
//		}
		
		int transitionCount = 0;
		Map<EquivClz, List<BlaTr>> tracePerEquivClz = new HashMap<EquivClz, List<BlaTr>>();
		Map<EquivClz, List<Response>> responsesPerEquivClz = new HashMap<EquivClz, List<Response>>();
		
		for (EquivClz c : getEquivClzs()) {
			List<BlaTr> trace = createTrace(c);
			tracePerEquivClz.put(c, trace);
			transitionCount += trace.size();
			
			responsesPerEquivClz.put(c, applyTrace(c.someVtx(), trace));
		}
		
		System.out.println("#undistinguished = " + undistinguished.size());
		System.out.println("#transitions = " + transitionCount);
		
//		{
//			Map<InitState, Set<EquivClz>> vtxsPerInitState = new HashMap<InitState, Set<EquivClz>>();
//			
//			for (EquivClz v : getEquivClzs()) {
//				Permutations.inject(vtxsPerInitState, new InitState(v.someVtx()), v);
//			}
//			
//			for (Map.Entry<InitState, Set<EquivClz>> e : vtxsPerInitState.entrySet()) {
//				for (EquivClz z : e.getValue()) {
//					List<BlaTr> trace = tracePerEquivClz.get(z);
//					List<Response> responses = applyTrace(z.someVtx(), trace);
//					
//					for (EquivClz z2 : e.getValue()) {
//						if (z2 != z) {
//							List<Response> responses2 = applyTrace(z2.someVtx(), trace);
//							
//							if (responses2.equals(responses)) {
//								throw new Error("Should not happen!");
//							}
//						}
//					}
//				}
//			}
//		}
	}
	
//	private void checkTrace(EquivClz c, List<BlaTr> trace) {
//		for (EquivClz e : getEquivClzs()) {
//			if (e != c) {
//				e.someVtx().
//			}
//		}
//	}
	
	private List<Response> applyTrace(Vertex v, List<BlaTr> trace) {
		List<Response> result = new ArrayList<Response>();
		Vertex curr = v;
		
		for (BlaTr tr : trace) {
			Transition outgoing = curr.getOutgoingTransition(tr.inputChanges);
			result.add(new Response(outgoing));
			curr = outgoing.getTgt();
		}
		
		return result;
	}
	
	private List<BlaTr> createTrace(EquivClz c) {
		if (c == null) {
			throw new Error("Should not happen!");
		}
		
		Bla bla = blaPerEquivClz.get(c);
		List<BlaTr> trace = new ArrayList<BlaTr>();
		BlaTr incoming = bla.incoming;
		
		while (incoming != null) {
			trace.add(0, incoming);
			incoming = incoming.src.incoming;
		}
		
//		if (!bla.potPerInit.containsKey(c)) {
//			return trace;
//		}
		
		if (bla.potPerInit.size() == 1) {
			return trace;
		}
		
		trace.addAll(createTrace(bla.potPerInit.get(c)));
		
//		List<BlaTr> trace = createTrace(bla.potPerInit.get(c));
//		trace.add(0, );
		return trace;
	}
	
	private static <T> Map<T, T> createIdMap(Set<T> elems) {
		Map<T, T> result = new HashMap<T, T>();
		
		for (T elem : elems) {
			result.put(elem, elem);
		}
		
		return result;
	}
	
	private void distinguish(Set<EquivClz> xs, int maxDepth, String prefix) {
		Map<EquivClz, EquivClz> startPos = createIdMap(xs);
		Map<Map<EquivClz, EquivClz>, Bla> blaPerPos = new HashMap<Map<EquivClz, EquivClz>, Bla>();
		blaPerPos.put(startPos, new Bla(startPos, null));
		
		Set<Bla> fringe = new HashSet<Bla>();
		Set<Bla> newFringe = new HashSet<Bla>();
		fringe.addAll(blaPerPos.values());
		
		int depth = 0;
		
		while (fringe.size() > 0 && depth < maxDepth) {
			//updateDistinguished(fringe);
			depth++;
			
//			newFringe.clear();
//			
//			for (Bla bla : fringe) {
//				if (!distinguished.containsAll(bla.potPerInit.keySet())) {
//					newFringe.add(bla);
//				}
//			}
//			
//			fringe.clear();
//			fringe.addAll(newFringe);
			newFringe.clear();
			
			for (Bla bla : fringe) {
				for (BlaTr t : computeOutgoing(bla)) {
					if (!blaPerPos.containsKey(t.tgt.potPerInit)) {
						blaPerPos.put(t.tgt.potPerInit, t.tgt);
						
//						addBla(dmap, bla);
						//bla.outgoing.put(t);
						newFringe.add(t.tgt);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println(prefix + "[" + LocalTime.now() + "] #xs = " + xs.size() + "; depth = " + depth + " / " + maxDepth + "; #beenHere = " + blaPerPos.size() + " (+" + fringe.size() + "); #distinguished = " + distinguished.size());
		}
		
		DistinguisherMap<EquivClz, Set<EquivClz>> dmap = new DistinguisherMap<EquivClz, Set<EquivClz>>();
		
		for (Map.Entry<Integer, Map<Bla, Set<Set<EquivClz>>>> e : sortBlas(blaPerPos.values()).entrySet()) {
			for (Map.Entry<Bla, Set<Set<EquivClz>>> e2 : e.getValue().entrySet()) {
				for (Set<EquivClz> e3 : e2.getValue()) {
					dmap.add(e3, e3);
				}
			}
			
			if (dmap.distinguishesAll(xs)) {
				break;
			}
		}
		
//		int zix = 0;
		
//		for (Map.Entry<Map<EquivClz, EquivClz>, Bla> e : blaPerPos.entrySet()) {
//			addBla(dmap, e.getValue());
////			Map<InitState, Set<EquivClz>> initStates = new HashMap<InitState, Set<EquivClz>>();
////			
////			for (Map.Entry<EquivClz, EquivClz> e2 : e.getValue().potPerInit.entrySet()) {
////				InitState initState = new InitState(e2.getValue().someVtx());
////				Permutations.inject(initStates, initState, e2.getKey());
////			}
////			
////			for (Set<EquivClz> qs : initStates.values()) {
////				dmap.add(qs, qs);
////			}
////			
////			zix++;
////			System.out.println("[" + LocalTime.now() + "] " + zix + " / " + blaPerPos.size());
//		}
//		
//		for (EquivClz q : xs) {
//			if (!dmap.getDistinguishersPerSubset().containsKey(Collections.singleton(q))) {
//				throw new Error(":-(");
//			}
//		}
		
		if (!dmap.distinguishesAll(xs)) {
			throw new Error(":-(");
		}
	}
	
	private SortedMap<Integer, Map<Bla, Set<Set<EquivClz>>>> sortBlas(Collection<Bla> blas) {
		SortedMap<Integer, Map<Bla, Set<Set<EquivClz>>>> result = new TreeMap<Integer, Map<Bla, Set<Set<EquivClz>>>>();
		
		for (Bla bla : blas) {
			Map<InitState, Set<EquivClz>> initStates = new HashMap<InitState, Set<EquivClz>>();
			
			for (Map.Entry<EquivClz, EquivClz> e2 : bla.potPerInit.entrySet()) {
				InitState initState = new InitState(e2.getValue().someVtx());
				HashMaps.inject(initStates, initState, e2.getKey());
			}
			
			HashMaps.injectSet(result, initStates.size(), bla, new HashSet<Set<EquivClz>>(initStates.values()));
		}
		
		return result;
	}
	
	private void addBla(DistinguisherMap<EquivClz, Set<EquivClz>> dmap, Bla bla) {
		Map<InitState, Set<EquivClz>> initStates = new HashMap<InitState, Set<EquivClz>>();
		
		for (Map.Entry<EquivClz, EquivClz> e2 : bla.potPerInit.entrySet()) {
			InitState initState = new InitState(e2.getValue().someVtx());
			HashMaps.inject(initStates, initState, e2.getKey());
		}
		
		for (Set<EquivClz> qs : initStates.values()) {
			if (qs.size() < 1000) {
				dmap.add(qs, qs);
			}
		}
	}
	
	private void updateDistinguished(Set<Bla> blas) {
		boolean done = false;
		
		while (!done) {
			done = true;
			
			for (Bla bla : blas) {
				Set<InitState> initStates = new HashSet<InitState>();
				
				for (Map.Entry<EquivClz, EquivClz> e : bla.potPerInit.entrySet()) {
					initStates.add(new InitState(e.getValue().someVtx()));
				}
				
				if (initStates.size() == bla.potPerInit.size()) {
					if (distinguished.addAll(bla.potPerInit.keySet())) {
						
					}
				} else {
					
				}
				
//				if (bla.potPerInit.size() == 1) {
//					EquivClz c = bla.potPerInit.keySet().iterator().next();
//					
//					if (distinguished.add(c)) {
//						blaPerEquivClz.put(c, bla);
//						done = false;
//					}
//				} else {
//					Map<EquivClz, Set<EquivClz>> undistPerDist = new HashMap<EquivClz, Set<EquivClz>>();
//					
//					for (Map.Entry<EquivClz, EquivClz> e3 : bla.potPerInit.entrySet()) {
//						if (distinguished.contains(e3.getValue())) {
//							Permutations.inject(undistPerDist, e3.getValue(), e3.getKey());
//						}
//					}
//					
//					for (Map.Entry<EquivClz, Set<EquivClz>> e3 : undistPerDist.entrySet()) {
//						if (e3.getValue().size() == 1) {
//							EquivClz c = e3.getValue().iterator().next();
//							
//							if (distinguished.add(c)) {
//								blaPerEquivClz.put(c, bla);
//								done = false;
//							}
//						}
//					}
//				}
			}
		}
	}
	
	private Set<BlaTr> computeOutgoing(Bla start) {
		Set<InputChanges> ics = new HashSet<InputChanges>();
		
		for (EquivClz c : start.potPerInit.values()) {
			ics.addAll(c.distInputChanges);
		}
		
		Set<BlaTr> result = new HashSet<BlaTr>();
		
		for (InputChanges ic : ics) {
			Map<Response, Map<EquivClz, EquivClz>> perResponse = new HashMap<Response, Map<EquivClz, EquivClz>>();
			
			for (Map.Entry<EquivClz, EquivClz> e : start.potPerInit.entrySet()) {
				Transition t = e.getValue().someVtx().getOutgoingTransition(ic);
				EquivClz newPot = getEquivClzPerVtx().get(t.getTgt());
				HashMaps.injectSet(perResponse, new Response(t), e.getKey(), newPot);
			}
			
			for (Map.Entry<Response, Map<EquivClz, EquivClz>> e : perResponse.entrySet()) {
				result.add(new BlaTr(start, e.getValue(), ic));
			}
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		DecaStableFileDistinguished3 x = new DecaStableFileDistinguished3();
//		x.loadFromFile("models", "all.reduced.3.stable", true);
		
		x.loadFromFile("models", "all.reduced.2.stable", true);
//		x.bla();
		x.init();
		x.saveToFile("models", "all.reduced.3.stable");
		
//		System.out.println("#vtxs = " + x.getVertices().size());
//		System.out.println("#reduced = " + x.equivClzs.size());
	}
}
