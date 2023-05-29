package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.behave.stable.files.DecaStableFileWriter;
import jlx.utils.*;

//Based on "A. Gill, Introduction to The Theory of Finite State Machines, McGraw-Hill, New York, 1962."
public class DecaStableFileMinimized extends DecaStableFileWriter {
	private final Map<Vertex, EquivClz> equivClzPerVtx;
	private final Set<EquivClz> equivClzs;
	
	public static class EquivClz {
		public final Set<Vertex> vtxs;
//		public final Set<SplitReason> splitReasons;
		public final Set<InputChanges> distInputChanges;
//		public final Map<Pair<Vertex>, Set<InputChanges>> reasonsPerPair;
//		public final Map<InputChanges, Set<Vertex>> rejectedPerInputChanges;
		public final Map<Vertex, Set<InputChanges>> icsPerPreserved;
		
		public EquivClz(Set<Vertex> vtxs, Set<Vertex> excludedVtxs) {
			this.vtxs = vtxs;
			
//			splitReasons = new HashSet<SplitReason>();
			distInputChanges = new HashSet<InputChanges>();
//			reasonsPerPair = new HashMap<Pair<Vertex>, Set<InputChanges>>();
			
			icsPerPreserved = new HashMap<Vertex, Set<InputChanges>>();
//			rejectedPerInputChanges = new HashMap<InputChanges, Set<Vertex>>();
		}
		
		public EquivClz(Set<Vertex> vtxs, Set<Vertex> excludedVtxs, EquivClz baseEquivClz, SplitReason splitReason) {
			this.vtxs = vtxs;
			
//			splitReasons = new HashSet<SplitReason>();
//			splitReasons.addAll(baseEquivClz.splitReasons);
//			splitReasons.add(splitReason);
			distInputChanges = new HashSet<InputChanges>();
//			reasonsPerPair = new HashMap<Pair<Vertex>, Set<InputChanges>>();
			
			icsPerPreserved = new HashMap<Vertex, Set<InputChanges>>();
			
			for (Map.Entry<Vertex, Set<InputChanges>> e : baseEquivClz.icsPerPreserved.entrySet()) {
				icsPerPreserved.put(e.getKey(), new HashSet<InputChanges>(e.getValue()));
			}
			
//			for (Map.Entry<Pair<Vertex>, Set<InputChanges>> e : baseEquivClz.reasonsPerPair.entrySet()) {
//				if (vtxs.contains(e.getKey().getElem1())) {
//					reasonsPerPair.put(e.getKey(), new HashSet<InputChanges>(e.getValue()));
//				}
//			}
			
//			for (Vertex v1 : vtxs) {
//				for (Vertex v2 : excludedVtxs) {
//					Permutations.inject(reasonsPerPair, new Pair<Vertex>(v1, v2), splitReason.inputChanges); 
//				}
//			}
			
//			rejectedPerInputChanges = new HashMap<InputChanges, Set<Vertex>>();
			
//			for (Map.Entry<InputChanges, Set<Vertex>> e : baseEquivClz.rejectedPerInputChanges.entrySet()) {
//				rejectedPerInputChanges.put(e.getKey(), new HashSet<Vertex>(e.getValue()));
//			}
//			
//			Permutations.injectAll(rejectedPerInputChanges, splitReason.inputChanges, excludedVtxs);
		}
		
		public Set<InputChanges> getOutgoingInputChanges() {
			Set<InputChanges> result = new HashSet<InputChanges>();
			
			for (Vertex vtx : vtxs) {
				result.addAll(vtx.getOutgoingInputChanges());
			}
			
			return result;
		}
		
		public Vertex someVtx() {
			return vtxs.iterator().next();
		}
	}
	
	public static class SplitReason {
		public final InputChanges inputChanges;
		public final Response response;
		
		/** If the given response is detected, then we could NOT have been in these vertices. */
		public final Set<Vertex> rejectedVtxs;
		
		public SplitReason(InputChanges inputChanges, Response response, Set<Vertex> rejectedVtxs) {
			this.inputChanges = inputChanges;
			this.response = response;
			this.rejectedVtxs = rejectedVtxs;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(inputChanges, response, rejectedVtxs);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SplitReason other = (SplitReason) obj;
			return Objects.equals(inputChanges, other.inputChanges) && Objects.equals(response, other.response) && Objects.equals(rejectedVtxs, other.rejectedVtxs);
		}
	}
	
	public DecaStableFileMinimized() {
		equivClzPerVtx = new HashMap<Vertex, EquivClz>();
		equivClzs = new HashSet<EquivClz>();
	}
	
	public Map<Vertex, EquivClz> getEquivClzPerVtx() {
		return equivClzPerVtx;
	}
	
	public Set<EquivClz> getEquivClzs() {
		return equivClzs;
	}
	
	public void init() {
		Map<InitState, Set<Vertex>> vtxsPerInitState = new HashMap<InitState, Set<Vertex>>();
		
		for (Vertex v : getVertices().values()) {
			HashMaps.inject(vtxsPerInitState, new InitState(v), v);
		}
		
		Set<EquivClz> fringe = new HashSet<EquivClz>();
		Set<EquivClz> newFringe = new HashSet<EquivClz>();
		equivClzPerVtx.clear();
		
		for (Map.Entry<InitState, Set<Vertex>> e : vtxsPerInitState.entrySet()) {
			EquivClz equivClz = new EquivClz(e.getValue(), Collections.emptySet());
			fringe.add(equivClz);
			
			for (Vertex vtx : e.getValue()) {
				equivClzPerVtx.put(vtx, equivClz);
			}
		}
		
		boolean done = false;
		int iterationIndex = 1;
		
		while (!done) {
			System.out.println("[" + LocalTime.now() + "] Iteration " + iterationIndex + "; #remaining-equiv-clzs = " + fringe.size());
			iterationIndex++;
			
			done = true;
			newFringe.clear();
			
			for (EquivClz equivClz : fringe) {
				Set<EquivClz> parts = split(equivClz);
				
				if (parts.size() == 1) {
					newFringe.addAll(parts);
				} else {
					done = false;
					
					for (EquivClz newEquivClz : parts) {
						if (newEquivClz.vtxs.size() > 1) {
							newFringe.add(newEquivClz);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		equivClzs.clear();
		equivClzs.addAll(equivClzPerVtx.values());
		
		System.out.println("#equiv-clzs = " + equivClzs.size());
		
		int distInputChangeCount = 0;
		int totalInputChangeCount = 0;
		
		for (EquivClz c : equivClzs) {
			distInputChangeCount += c.distInputChanges.size();
			totalInputChangeCount += c.getOutgoingInputChanges().size();
		}
		
		System.out.println("#dist-input-changes = " + distInputChangeCount + " / " + totalInputChangeCount);
	}
	
//	public static class Response2 {
//		public final Response response;
//		public final EquivClz tgtEquivClz;
//		
//		public Response2(Response response, EquivClz tgtEquivClz) {
//			this.response = response;
//			this.tgtEquivClz = tgtEquivClz;
//		}
//		
//		@Override
//		public int hashCode() {
//			return Objects.hash(response, tgtEquivClz);
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			Response2 other = (Response2) obj;
//			return Objects.equals(response, other.response) && Objects.equals(tgtEquivClz, other.tgtEquivClz);
//		}
//	}
	
	private Set<EquivClz> split(EquivClz equivClz) {
		Set<EquivClz> fringe = new HashSet<EquivClz>();
		Set<EquivClz> newFringe = new HashSet<EquivClz>();
		fringe.add(equivClz);
		
		for (InputChanges inputChanges : equivClz.getOutgoingInputChanges()) {
			newFringe.clear();
			
			for (EquivClz c : fringe) {
				Map<Response, Set<Vertex>> vtxsPerResponse = new HashMap<Response, Set<Vertex>>();
				
				for (Vertex vtx : c.vtxs) {
					Transition outgoing = vtx.getOutgoingTransition(inputChanges);
					HashMaps.inject(vtxsPerResponse, new Response(outgoing), vtx);
				}
				
				for (Map.Entry<Response, Set<Vertex>> e : vtxsPerResponse.entrySet()) {
					Map<EquivClz, Set<Vertex>> vtxsPerNewEquivClz = new HashMap<EquivClz, Set<Vertex>>();
					
					for (Vertex vtx : e.getValue()) {
						Transition outgoing = vtx.getOutgoingTransition(inputChanges);
						EquivClz newEquivClz = equivClzPerVtx.get(outgoing.getTgt());
						HashMaps.inject(vtxsPerNewEquivClz, newEquivClz, vtx);
					}
					
//					Set<Set<Vertex>> vtxsCompatibleWithResponse = new HashSet<Set<Vertex>>();
//					vtxsCompatibleWithResponse.addAll(newVtxsPerNewEquivClz.values());
//					Set<Set<Vertex>> vtxsExcludedByResponse = new HashSet<Set<Vertex>>();
					
//					for (Vertex v : c.vtxs) {
//						if (!e.getValue().contains(v)) {
//							vtxsExcludedByResponse.add(Collections.singleton(v));
//						}
//					}
					
//					if (e.getValue().equals(c.vtxs)) {
//						newFringe.add(c);
//					} else {
						for (Map.Entry<EquivClz, Set<Vertex>> e3 : vtxsPerNewEquivClz.entrySet()) {
							if (e3.getValue().equals(c.vtxs)) {
								newFringe.add(c);
								
//								for (Vertex v : e3.getValue()) {
//									Permutations.inject(c.icsPerPreserved, v, inputChanges);
//								}
							} else {
								Set<Vertex> rejectedVtxs = new HashSet<Vertex>(c.vtxs);
								rejectedVtxs.removeAll(e3.getValue());
								
								SplitReason r = new SplitReason(inputChanges, e.getKey(), rejectedVtxs);
								EquivClz newC = new EquivClz(e3.getValue(), rejectedVtxs, c, r);
								newC.distInputChanges.addAll(c.distInputChanges);
								newC.distInputChanges.add(inputChanges);
								newFringe.add(newC);
								
								for (Vertex v : e3.getValue()) {
									HashMaps.inject(newC.icsPerPreserved, v, inputChanges);
								}
								
//								for (Vertex v : e3.getKey().vtxs) {
//									Permutations.inject(newC.icsPerPreserved, v, inputChanges);
//								}
							}
						}
//					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		for (EquivClz c : fringe) {
			for (Vertex vtx : c.vtxs) {
				equivClzPerVtx.put(vtx, c);
			}
		}
		
		return fringe;
	}
	
	public DecaStableFileWriter createWriter() {
		DecaStableFileWriter result = new DecaStableFileWriter();
		
		result.getScopes().putAll(getScopes());
		result.getInputPorts().putAll(getInputPorts());
		result.getOutputPorts().putAll(getOutputPorts());
		result.getInputChanges().putAll(getInputChanges());
		result.getOutputEvolutions().putAll(getOutputEvolutions());
		
		Set<Transition> transitions = new HashSet<Transition>();
		
		Map<EquivClz, Vertex> vtxPerEquivClz = new HashMap<EquivClz, Vertex>();
		
		for (EquivClz equivClz : equivClzs) {
			int id = result.getVertices().size();
			Vertex reprVtx = equivClz.vtxs.iterator().next();
			Vertex newVtx = new Vertex(id, reprVtx.getStatePerScope(), reprVtx.getClzsPerScope());
			
			result.getVertices().put(id, newVtx);
			vtxPerEquivClz.put(equivClz, newVtx);
			
			for (Transition outgoing : reprVtx.getOutgoing()) {
				transitions.add(outgoing);
			}
		}
		
		{
			Vertex initVtx = vtxPerEquivClz.get(equivClzPerVtx.get(getInitialTransition().getTgt()));
			Transition t = new Transition(-1, null, initVtx, getInitialTransition().getInputChanges(), getInitialTransition().getOutputEvolutions());
			result.setInitialTransition(t);
		}
		
		for (Transition outgoing : transitions) {
			Vertex srcVtx = vtxPerEquivClz.get(equivClzPerVtx.get(outgoing.getSrc()));
			Vertex tgtVtx = vtxPerEquivClz.get(equivClzPerVtx.get(outgoing.getTgt()));
			Transition t = new Transition(result.getTransitions().size(), srcVtx, tgtVtx, outgoing.getInputChanges(), outgoing.getOutputEvolutions());
			result.getTransitions().add(t);
			srcVtx.getOutgoing().add(t);
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		DecaStableFileMinimized x = new DecaStableFileMinimized();
		x.loadFromFile("models", "all.reduced.stable", true);
		x.init();
//		x.createWriter().saveToFile("models", "all.reduced.stable");
		
		System.out.println("#vtxs = " + x.getVertices().size());
		System.out.println("#reduced = " + x.equivClzs.size());
	}
}
