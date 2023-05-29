package jlx.behave.proto;

import java.util.*;

public class DecaThreeBVertex {
	private DecaThreeOutputRun legacy;
	private Map<PulsePackMap, Set<DecaThreeBTransition>> outgoing;
	
	public DecaThreeBVertex(DecaThreeOutputRun legacy) {
		this.legacy = legacy;
		
		outgoing = new HashMap<PulsePackMap, Set<DecaThreeBTransition>>();
	}
	
	public DecaThreeOutputRun getLegacy() {
		return legacy;
	}
	
	public Map<PulsePackMap, Set<DecaThreeBTransition>> getOutgoing() {
		return outgoing;
	}
	
	public int getId() {
		return legacy.getVertex().getId();
	}
	
	public String getName() {
		return legacy.getVertex().getName();
	}
}

