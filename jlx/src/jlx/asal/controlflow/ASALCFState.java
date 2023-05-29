package jlx.asal.controlflow;

import java.util.*;

import jlx.asal.j.JPulse;
import jlx.asal.j.JScope;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Dir;

public class ASALCFState {
	public final Set<ASALVariable> assignedVars; /* Potentially! */
	public final Map<ASALVariable, ASALSymbolicValue> valuePerVar;
	public final Map<ASALVariable, ASALSymbolicValue> valuePerParam;
	public ASALSymbolicValue valueSoFar;
	public ASALSymbolicValue hasEncounteredReturn;
	public ASALSymbolicValue returnExpr;
	
	public ASALCFState(JScope scope, ASALSymbolicValue defaultReturnValue) {
		assignedVars = new HashSet<ASALVariable>();
		valuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (Map.Entry<String, ASALVariable> e : scope.getVariablePerName().entrySet()) {
			if (e.getValue() instanceof ReprPort) {
				ReprPort rp = (ReprPort)e.getValue();
				
				if (rp.getDir() == Dir.OUT) {
					if (rp.getType().equals(JPulse.class)) {
						valuePerVar.put(rp, ASALSymbolicValue.FALSE);
					} else {
						valuePerVar.put(rp, ASALSymbolicValue.from(scope.getDefaultValue(rp)));
					}
				} else {
					valuePerVar.put(e.getValue(), ASALSymbolicValue.from(rp));
				}
			} else {
				valuePerVar.put(e.getValue(), ASALSymbolicValue.from(e.getValue()));
			}
		}
		
		valuePerParam = new HashMap<ASALVariable, ASALSymbolicValue>();
		valueSoFar = defaultReturnValue;
		hasEncounteredReturn = ASALSymbolicValue.FALSE;
		returnExpr = defaultReturnValue;
	}
	
	public ASALCFState(ASALCFState source) {
		assignedVars = new HashSet<ASALVariable>(source.assignedVars);
		valuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>(source.valuePerVar);
		valuePerParam = new HashMap<ASALVariable, ASALSymbolicValue>(source.valuePerParam);
		valueSoFar = source.valueSoFar;
		hasEncounteredReturn = source.hasEncounteredReturn;
		returnExpr = source.returnExpr;
	}
	
	private ASALCFState() {
		assignedVars = new HashSet<ASALVariable>();
		valuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>();
		valuePerParam = new HashMap<ASALVariable, ASALSymbolicValue>();
		valueSoFar = null;
		hasEncounteredReturn = null;
		returnExpr = null;
	}
	
	public Map<ASALVariable, ASALSymbolicValue> getValuePerAssignedVar() {
		Map<ASALVariable, ASALSymbolicValue> result = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (ASALVariable assignedVar : assignedVars) {
			result.put(assignedVar, valuePerVar.get(assignedVar));
		}
		
		return result;
	}
	
	public static ASALCFState ite(ASALSymbolicValue condition, ASALCFState thenBranch, ASALCFState elseBranch) {
		ASALCFState result = new ASALCFState();
		result.assignedVars.addAll(thenBranch.assignedVars);
		result.assignedVars.addAll(elseBranch.assignedVars);
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> e : thenBranch.valuePerVar.entrySet()) {
			result.valuePerVar.put(e.getKey(), ASALSymbolicValue.ite(condition, e.getValue(), elseBranch.valuePerVar.get(e.getKey())));
		}
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> e : thenBranch.valuePerParam.entrySet()) {
			result.valuePerParam.put(e.getKey(), ASALSymbolicValue.ite(condition, e.getValue(), elseBranch.valuePerParam.get(e.getKey())));
		}
		
		result.valueSoFar = ASALSymbolicValue.ite(condition, thenBranch.valueSoFar, elseBranch.valueSoFar);
		result.hasEncounteredReturn = ASALSymbolicValue.ite(condition, thenBranch.hasEncounteredReturn, elseBranch.hasEncounteredReturn);
		result.returnExpr = ASALSymbolicValue.ite(condition, thenBranch.returnExpr, elseBranch.returnExpr);
		return result;
	}
}

