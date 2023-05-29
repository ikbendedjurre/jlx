package jlx.behave.proto;

import java.util.*;

import jlx.utils.*;

public class DecaThreeVertex {
	private DecaThreeStateConfig stateConfig;
	private Set<DecaThreeTransition> outgoing;
	private int localLevel;
	private int id;
	
	public DecaThreeVertex(DecaThreeStateConfig stateConfig, int localLevel, int id) {
		this.stateConfig = stateConfig;
		this.localLevel = localLevel;
		this.id = id;
		
		outgoing = new HashSet<DecaThreeTransition>();
	}
	
	public DecaThreeStateConfig getStateConfig() {
		return stateConfig;
	}
	
	public Set<DecaThreeTransition> getOutgoing() {
		return outgoing;
	}
	
	/**
	 * I.e. the smallest number of transitions from the initial vertex of the same state machine.
	 */
	public int getLocalLevel() {
		return localLevel;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return stateConfig.getVertex().getName();
	}
	
	public Map<Set<DecaThreeOutputRun>, Set<PulsePackMap>> computeOutputRuns() {
		Map<Set<DecaThreeOutputRun>, Set<PulsePackMap>> result = new HashMap<Set<DecaThreeOutputRun>, Set<PulsePackMap>>();
		
		for (DecaThreeTransition t : getOutgoing()) {
			HashMaps.merge(result, t.computeOutputRuns());
		}
		
		return result;
	}
}

