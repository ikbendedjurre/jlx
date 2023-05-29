package jlx.asal.controlflow;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;

public class ASALCF {
	public static ASALSymbolicValue getSymbolicValue(JScope scope, ASALExpr expr, ASALSymbolicValue fallbackExpr) {
		ASALCFVisitor v = new ASALCFVisitor(scope);
		ASALCFState s = new ASALCFState(scope, fallbackExpr);
		return v.visitExpr(s, expr).valueSoFar;
	}
	
	public static boolean couldBeSat(JScope scope, ASALExpr expr) {
		return getSymbolicValue(scope, expr, ASALSymbolicValue.FALSE).couldBeTrue();
	}
	
	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct2(JScope scope, ASALStatement stat) {
		ASALCFVisitor v = new ASALCFVisitor(scope);
		ASALCFState s = new ASALCFState(scope, ASALSymbolicValue.NONE);
		return v.visitStat(s, stat).getValuePerAssignedVar();
	}
	
	/**
	 * Note that no return statements may occur (nested or otherwise) in the given list of statements!!
	 */
	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct2(JScope scope, List<ASALStatement> stats) {
		ASALCFVisitor v = new ASALCFVisitor(scope);
		ASALCFState s = new ASALCFState(scope, ASALSymbolicValue.NONE);
		
		for (int index = 0; index < stats.size(); index++) {
			s = v.visitStat(s, stats.get(index));
		}
		
		return s.getValuePerAssignedVar();
	}
	
	public static Map<ASALVariable, ASALStatement> getStatPerVarUpdate(JScope scope, List<ASALStatement> stats) {
		ASALCFVisitor v = new ASALCFVisitor(scope);
		List<ASALCFState> states = new ArrayList<ASALCFState>();
		states.add(new ASALCFState(scope, ASALSymbolicValue.NONE));
		
		for (int index = 0; index < stats.size(); index++) {
			states.add(v.visitStat(states.get(states.size() - 1), stats.get(index)));
		}
		
		Map<ASALVariable, ASALStatement> result = new HashMap<ASALVariable, ASALStatement>();
		
		for (int index = 1; index < states.size(); index++) {
			ASALCFState s1 = states.get(index - 1);
			ASALCFState s2 = states.get(index);
			
			for (ASALVariable assignedVar : s2.assignedVars) {
				ASALSymbolicValue v1 = s1.valuePerVar.get(assignedVar);
				ASALSymbolicValue v2 = s2.valuePerVar.get(assignedVar);
				
				if (v2.equals(v1)) {
					if (!result.containsKey(assignedVar)) {
						result.put(assignedVar, stats.get(index - 1)); //First statement in which (possibly completely unchanged) variable was ASSIGNED.
					}
				} else {
					result.put(assignedVar, stats.get(index - 1)); //Last statement in which assigned variable was CHANGED.
				}
			}
		}
		
		return result;
	}
}

