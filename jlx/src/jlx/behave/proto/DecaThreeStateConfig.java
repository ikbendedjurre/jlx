package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;
import jlx.common.FileLocation;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Dir;

public class DecaThreeStateConfig {
	private DecaTwoVertex vertex;
	private PulsePackMap outputVal;
	
	public DecaThreeStateConfig(DecaTwoVertex vertex, Map<ASALVariable, ASALSymbolicValue> outputVal) {
		this.vertex = vertex;
		
		Map<ReprPort, ASALSymbolicValue> m = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> e : outputVal.entrySet()) {
			if (e.getKey() instanceof ReprPort) {
				m.put((ReprPort)e.getKey(), e.getValue());
			}
		}
		
		this.outputVal = PulsePackMap.from(m, Dir.OUT);
	}
	
	public DecaTwoVertex getVertex() {
		return vertex;
	}
	
	public PulsePackMap getOutputVal() {
		return outputVal;
	}
	
	public Set<FileLocation> getFileLocations() {
		return vertex.getStateConfig().getFileLocations();
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return vertex.getStateConfig().getSysmlClzs();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(vertex, outputVal);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DecaThreeStateConfig)) {
			return false;
		}
		DecaThreeStateConfig other = (DecaThreeStateConfig) obj;
		return Objects.equals(vertex, other.vertex) && Objects.equals(outputVal, other.outputVal);
	}
}

