package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.common.FileLocation;

public class DecbTransition {
	private DecbVertex sourceVertex;
	private DecbVertex targetVertex;
	private ASALSymbolicValue guard;
	private NextStateFct nextStateFct;
	private DecaTransition legacy;
	
	public DecbTransition(DecaTransition source, ASALSymbolicValue guard, NextStateFct nextStateFct, Map<DecaVertex, DecbVertex> newVertexPerOldVertex) {
		sourceVertex = newVertexPerOldVertex.get(source.getSourceVertex());
		targetVertex = newVertexPerOldVertex.get(source.getTargetVertex());
		
		this.guard = guard;
		this.nextStateFct = nextStateFct;
		
		legacy = source;
	}
	
	/**
	 * Transition from which this transition has been derived.
	 * May reference properties that have been rewritten in this transition.
	 */
	public DecaTransition getLegacy() {
		return legacy;
	}
	
	public boolean isIdle() {
		return legacy.isIdle();
	}
	
	public List<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public DecbVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public DecbVertex getTargetVertex() {
		return targetVertex;
	}
	
	public ASALSymbolicValue getGuard() {
		return guard;
	}
	
	public NextStateFct getNextStateFct() {
		return nextStateFct;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
}
