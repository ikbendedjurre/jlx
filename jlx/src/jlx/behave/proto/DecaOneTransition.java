package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;
import jlx.common.FileLocation;

public class DecaOneTransition {
	private DecaOneVertex sourceVertex;
	private DecaOneVertex targetVertex;
	private ASALSymbolicValue guard;
	private NextStateFct nextStateFct;
	private DecbTransition legacy;
	
	public DecaOneTransition(DecbTransition source, Map<DecbVertex, DecaOneVertex> newVertexPerOldVertex) {
		sourceVertex = newVertexPerOldVertex.get(source.getSourceVertex());
		targetVertex = newVertexPerOldVertex.get(source.getTargetVertex());
		guard = source.getGuard();
		nextStateFct = new NextStateFct(source.getNextStateFct());
		legacy = source;
	}
	
	/**
	 * Transition from which this transition has been derived.
	 * May reference properties that have been rewritten in this transition.
	 */
	public DecbTransition getLegacy() {
		return legacy;
	}
	
	public boolean isIdle() {
		return legacy.isIdle();
	}
	
	public List<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public DecaOneVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public DecaOneVertex getTargetVertex() {
		return targetVertex;
	}
	
	public ASALSymbolicValue getGuard() {
		return guard;
	}
	
	public NextStateFct getNextStateFct() {
		return nextStateFct;
	}
	
	public boolean removeIdentityAssignments() {
		NextStateFct temp = new NextStateFct();
		boolean result = false;
		
		for (NextStateFct.Entry e : nextStateFct.getEntries()) {
			if (e.getValue().equals(ASALSymbolicValue.from(e.getVariable()))) {
				result = true;
			} else {
				temp.put(e.getVariable(), e.getValue(), e.getDebugText());
			}
		}
		
		nextStateFct = temp;
		return result;
	}
	
	public boolean removeWrittenVars(Collection<ASALVariable> vars) {
		NextStateFct temp = nextStateFct.removeVariables(vars);
		boolean result = temp.getVariables().size() < nextStateFct.getVariables().size();
		nextStateFct = temp;
		return result;
	}
	
	public boolean substitute(Map<ASALVariable, ASALSymbolicValue> subst) {
		boolean result = false;
		ASALSymbolicValue newGuard = guard.substitute(subst);
		
		if (!newGuard.equals(guard)) {
			guard = newGuard;
			result = true;
		}
		
		if (nextStateFct.substitute(subst)) {
			result = true;
		}
		
		return result;
	}
	
	public boolean applyRestr(Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> restrPerVar) {
		return nextStateFct.applyRestrPerVar(restrPerVar);
	}
}




