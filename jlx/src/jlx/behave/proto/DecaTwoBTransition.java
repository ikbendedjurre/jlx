package jlx.behave.proto;

import java.util.*;

import jlx.common.FileLocation;

public class DecaTwoBTransition {
	private Set<DecaTwoTransition> legacy;
	private DecaTwoBVertex src;
	private Set<DecaTwoBVertex> tgts;
	private Set<PulsePackMap> inputVals;
	
	public DecaTwoBTransition(Set<DecaTwoTransition> legacy, DecaTwoBVertex src, Set<DecaTwoBVertex> tgts, Set<PulsePackMap> inputVals) {
		this.legacy = legacy;
		this.src = src;
		this.tgts = tgts;
		this.inputVals = inputVals;
	}
	
	public Set<DecaTwoTransition> getLegacy() {
		return legacy;
	}
	
	public List<FileLocation> getFileLocations() {
		List<FileLocation> result = new ArrayList<FileLocation>();
		
		for (DecaTwoTransition t : legacy) {
			result.addAll(t.getFileLocations());
		}
		
		return result;
	}
	
	public DecaTwoBVertex getSrc() {
		return src;
	}
	
	public Set<DecaTwoBVertex> getTgts() {
		return tgts;
	}
	
	public Set<PulsePackMap> getInputVals() {
		return inputVals;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		Set<ProtoTransition> result = new HashSet<ProtoTransition>();
		
		for (DecaTwoTransition t : legacy) {
			result.addAll(t.getProtoTrs());
		}
		
		return result;
	}
}

