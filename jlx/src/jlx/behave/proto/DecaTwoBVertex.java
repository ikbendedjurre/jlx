package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;
import jlx.common.FileLocation;
import jlx.models.UnifyingBlock.ReprPort;

public class DecaTwoBVertex {
	private DecaTwoVertex legacy;
	private Set<DecaTwoBTransition> outgoing;
	private Map<ReprPort, ASALSymbolicValue> outputVal;
	
	public DecaTwoBVertex(DecaTwoVertex legacy) {
		this.legacy = legacy;
		
		outgoing = new HashSet<DecaTwoBTransition>();
		
		Map<ReprPort, ASALSymbolicValue> m = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> e : legacy.getStateConfig().getValuation().entrySet()) {
			if (e.getKey() instanceof ReprPort) {
				m.put((ReprPort)e.getKey(), e.getValue());
			}
		}
		
		outputVal = m;
	}
	
	public DecaTwoVertex getLegacy() {
		return legacy;
	}
	
	public Set<DecaTwoBTransition> getOutgoing() {
		return outgoing;
	}
	
	public Map<ReprPort, ASALSymbolicValue> getOutputVal() {
		return outputVal;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return legacy.getStateConfig().getSysmlClzs();
	}
	
	public Set<FileLocation> getFileLocations() {
		return legacy.getStateConfig().getFileLocations();
	}
	
	public String getName() {
		return legacy.getName();
	}
	
	public int getId() {
		return legacy.getId();
	}
	
	@Override
	public String toString() {
		return legacy.getName();
	}
}
