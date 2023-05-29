package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.common.FileLocation;

public class DecaTransition {
	private DecaVertex sourceVertex;
	private DecaVertex targetVertex;
	private ASALSymbolicValue guard;
	private NextStateFct nextStateFct;
	private NonaVertex legacyVertex;
	private NonaTransition legacyTransition;
	private boolean idle;
	
	public DecaTransition(NonaVertex legacyVertex, NonaTransition legacyTransition, DecaVertex sourceVertex, DecaVertex targetVertex, ASALSymbolicValue guard, NextStateFct nextStateFct, boolean idle) {
		this.sourceVertex = sourceVertex;
		this.targetVertex = targetVertex;
		this.guard = guard;
		this.nextStateFct = nextStateFct;
		this.legacyVertex = legacyVertex;
		this.legacyTransition = legacyTransition;
		this.idle = idle;
	}
	
	public NonaVertex getLegacyVertex() {
		return legacyVertex;
	}
	
	public NonaTransition getLegacyTransition() {
		return legacyTransition;
	}
	
	public List<FileLocation> getFileLocations() {
		if (legacyTransition != null) {
			return legacyTransition.getFileLocations();
		}
		
		return new ArrayList<FileLocation>(legacyVertex.getFileLocations());
	}
	
	public DecaVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public DecaVertex getTargetVertex() {
		return targetVertex;
	}
	
	public ASALSymbolicValue getGuard() {
		return guard;
	}
	
	public NextStateFct getNextStateFct() {
		return nextStateFct;
	}
	
	public List<ASALSymbolicValue> getEqns() {
		List<ASALSymbolicValue> eqns = new ArrayList<ASALSymbolicValue>();
		
		for (NextStateFct.Entry expr : getNextStateFct().getEntries()) {
			eqns.add(expr.getValue());
		}
		
		return eqns;
	}
	
	public boolean isIdle() {
		return idle;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		if (legacyTransition != null) {
			return legacyTransition.getProtoTrs();
		}
		
		return Collections.emptySet();
	}
}



