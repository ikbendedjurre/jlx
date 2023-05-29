package jlx.behave.stable.files;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

//Based on "A. Gill, Introduction to The Theory of Finite State Machines, McGraw-Hill, New York, 1962."
public class DecaStableFileBisimReducer extends DecaStableFileWriter {
	private static class EqClzResponse {
		private final InputChanges inputChanges;
		private final Set<OutputEvolution> evos;
		private final int hashCode;
		private final EquivClz tgt;
		
		public EqClzResponse(InputChanges inputChanges, Set<OutputEvolution> evos, EquivClz tgt) {
			this.inputChanges = inputChanges;
			this.evos = evos;
			this.tgt = tgt;
			
			hashCode = Objects.hash(evos, inputChanges, tgt);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EqClzResponse other = (EqClzResponse) obj;
			return Objects.equals(evos, other.evos) && Objects.equals(inputChanges, other.inputChanges) && Objects.equals(tgt, other.tgt);
		}
	}
	
	public static class EquivClz {
		private final Set<Vertex> vtxs;
		
		public EquivClz(Set<Vertex> vtxs) {
			this.vtxs = vtxs;
		}
		
		public Set<EquivClz> split(Map<Vertex, EquivClz> clzPerVtx) {
			Map<Set<EqClzResponse>, Set<Vertex>> vtxsPerResponse = new HashMap<Set<EqClzResponse>, Set<Vertex>>();
			
			for (Vertex vtx : vtxs) {
				Set<EqClzResponse> responses = new HashSet<EqClzResponse>();
				
				for (Transition t : vtx.getOutgoing()) {
					EquivClz tgt = clzPerVtx.get(t.getTgt());
					EqClzResponse response = new EqClzResponse(t.getInputChanges(), t.getOutputEvolutions(), tgt);
					responses.add(response);
				}
				
				HashMaps.inject(vtxsPerResponse, responses, vtx);
			}
			
			Set<EquivClz> result = new HashSet<EquivClz>();
			
			for (Set<Vertex> vtxs : vtxsPerResponse.values()) {
				result.add(new EquivClz(vtxs));
			}
			
			return result;
		}
		
		public Vertex someVtx() {
			return vtxs.iterator().next();
		}
	}
	
	private Set<EquivClz> createInitialFringe() {
		Map<InitState, Set<Vertex>> vtxsPerInitState = new HashMap<InitState, Set<Vertex>>();
		
		for (Vertex v : getVertices().values()) {
			HashMaps.inject(vtxsPerInitState, new InitState(v), v);
		}
		
		Set<EquivClz> result = new HashSet<EquivClz>();
		
		for (Set<Vertex> vtxs : vtxsPerInitState.values()) {
			result.add(new EquivClz(vtxs));
		}
		
		return result;
	}
	
	public void writeTo(DecaStableFileReader dest) {
		Set<EquivClz> fringe = createInitialFringe();
		Set<EquivClz> newFringe = new HashSet<EquivClz>();
		
		System.out.println("[" + LocalTime.now() + "] #eq-clzs = " + fringe.size() + " (+" + fringe.size() + ") / " + getVertices().size());
		
		int newClzCount = 1;
		
		while (newClzCount > 0) {
			newClzCount = 0;
			
			Map<Vertex, EquivClz> clzPerVtx = new HashMap<Vertex, EquivClz>();
			
			for (EquivClz equivClz : fringe) {
				for (Vertex vtx : equivClz.vtxs) {
					clzPerVtx.put(vtx, equivClz);
				}
			}
			
			newFringe.clear();
			
			for (EquivClz equivClz : fringe) {
				Set<EquivClz> newClzs = equivClz.split(clzPerVtx);
				newFringe.addAll(newClzs);
				newClzCount += newClzs.size() - 1;
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #eq-clzs = " + fringe.size() + " (+" + newClzCount + ") / " + getVertices().size());
		}
		
		dest.clear();
		dest.getScopes().putAll(getScopes());
		dest.getInputPorts().putAll(getInputPorts());
		dest.getOutputPorts().putAll(getOutputPorts());
		dest.getInputChanges().putAll(getInputChanges());
		dest.getOutputEvolutions().putAll(getOutputEvolutions());
		
		Map<Vertex, Vertex> newVtxPerOldVtx = new HashMap<Vertex, Vertex>();
		
		for (EquivClz eqClz : fringe) {
			Vertex oldVtx = eqClz.vtxs.iterator().next();
			Vertex newVtx = new Vertex(dest.getVertices().size(), oldVtx.getStatePerScope(), oldVtx.getClzsPerScope());
			dest.getVertices().put(newVtx.getId(), newVtx);
			
			for (Vertex vtx : eqClz.vtxs) {
				newVtxPerOldVtx.put(vtx, newVtx);
			}
		}
		
		{
			Transition t = getInitialTransition();
			dest.setInitialTransition(new Transition(-1, null, newVtxPerOldVtx.get(t.getTgt()), t.getInputChanges(), t.getOutputEvolutions()));
		}
		
		for (EquivClz eqClz : fringe) {
			Vertex oldVtx = eqClz.vtxs.iterator().next();
			Vertex newSrc = newVtxPerOldVtx.get(oldVtx);
			
			for (Transition t : oldVtx.getOutgoing()) {
				Vertex newTgt = newVtxPerOldVtx.get(t.getTgt());
				Transition newTransition = new Transition(dest.getTransitions().size(), newSrc, newTgt, t.getInputChanges(), t.getOutputEvolutions());
				dest.getTransitions().add(newTransition);
				newSrc.getOutgoing().add(newTransition);
				newTgt.getIncoming().add(newTransition);
			}
		}
	}
}
