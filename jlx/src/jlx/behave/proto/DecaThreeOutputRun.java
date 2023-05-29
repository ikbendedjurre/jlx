package jlx.behave.proto;

import java.util.*;

import jlx.models.UnifyingBlock.ReprPort;

public class DecaThreeOutputRun {
	private final DecaThreeVertex vertex;
	private final PulsePackMap output;
	private final int hashCode;
	
	public DecaThreeOutputRun(DecaThreeVertex vertex, PulsePackMap output) {
		this.vertex = vertex;
		this.output = output;
		
		hashCode = Objects.hash(output, vertex);
	}
	
	public DecaThreeVertex getVertex() {
		return vertex;
	}
	
	public PulsePackMap getOutput() {
		return output;
	}
	
	public Set<ReprPort> getChangedOutputs() {
		return output.getPackPerPort().keySet();
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
		DecaThreeOutputRun other = (DecaThreeOutputRun) obj;
		return Objects.equals(output, other.output) && Objects.equals(vertex, other.vertex);
	}
	
	/**
	 * Computes all vertices that are reachable from this vertex via a given input, and
	 * which only change output ports if they are in the given set.
	 */
	public Set<DecaThreeOutputRun> computeOutputClosure(PulsePackMap input) {
		Set<DecaThreeOutputRun> beenHere = new HashSet<DecaThreeOutputRun>();
		beenHere.add(this);
		
		Set<DecaThreeOutputRun> fringe = new HashSet<DecaThreeOutputRun>();
		Set<DecaThreeOutputRun> newFringe = new HashSet<DecaThreeOutputRun>();
		fringe.add(this);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaThreeOutputRun v : fringe) {
				for (DecaThreeTransition t : v.getVertex().getOutgoing()) {
					if (t.contains(input)) {
						for (DecaThreeVertex tgt : t.getTargetVertices()) {
							PulsePackMap changes = tgt.getStateConfig().getOutputVal().extractEventMap(v.getVertex().getStateConfig().getOutputVal());
							
							if (getChangedOutputs().containsAll(changes.getPackPerPort().keySet())) {
								DecaThreeOutputRun newRun = new DecaThreeOutputRun(tgt, getOutput().combine(changes));
								
								if (beenHere.add(newRun)) {
									newFringe.add(newRun);
								}
							}
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return beenHere;
	}
	
	public Set<DecaThreeOutputRun> computeNewOutputSuccs(PulsePackMap input) {
		Set<DecaThreeOutputRun> result = new HashSet<DecaThreeOutputRun>();
		
		for (DecaThreeTransition t : getVertex().getOutgoing()) {
			if (t.contains(input)) {
				for (DecaThreeVertex tgt : t.getTargetVertices()) {
					PulsePackMap changes = tgt.getStateConfig().getOutputVal().extractEventMap(getVertex().getStateConfig().getOutputVal());
					
					if (!getChangedOutputs().containsAll(changes.getPackPerPort().keySet())) {
						result.add(new DecaThreeOutputRun(tgt, getOutput().combine(changes)));
					}
				}
			}
		}
		
		return result;
	}
}

