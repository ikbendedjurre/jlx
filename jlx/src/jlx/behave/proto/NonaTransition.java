package jlx.behave.proto;

import java.util.*;

import jlx.asal.controlflow.ASALFlattening;
import jlx.asal.j.JPulse;
import jlx.asal.parsing.api.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;
import jlx.common.FileLocation;
import jlx.common.reflection.ModelException;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Dir;

public class NonaTransition {
	private NonaVertex sourceVertex;
	private NonaVertex targetVertex;
	private ASALTimeout timeoutEvent;
	private ASALSymbolicValue guard;
	private NextStateFct nextStateFct;
	private OctoTransition legacy;
	
	public NonaTransition(OctoTransition source, NonaStateMachine owner, NonaVertex sourceVertex, NonaVertex targetVertex, Map<ASALVariable, ASALSymbolicValue> restrictedInputs) throws ModelException {
		legacy = source;
		
		this.sourceVertex = sourceVertex;
		this.targetVertex = targetVertex;
		
		if (source.getEvent() instanceof ASALTrigger) {
			guard = ASALFlattening.getSymbolicValue(owner.scope, source.getGuard(), ASALSymbolicValue.FALSE);
			guard = ASALSymbolicValue.and(guard, ASALFlattening.getSymbolicValue(owner.scope, ((ASALTrigger)source.getEvent()).getExpr(), ASALSymbolicValue.FALSE));
			timeoutEvent = null;
		} else {
			guard = ASALFlattening.getSymbolicValue(owner.scope, source.getGuard(), ASALSymbolicValue.FALSE);
			timeoutEvent = (ASALTimeout)source.getEvent();
		}
		
		guard = guard.substitute(restrictedInputs);
		
//		for (ASALVariable v : guard.getReferencedVars()) {
//			guard = ASALSymbolicValue.and(guard, ASALSymbolicValue.assigned(v));
//		}
		
		if (source.getEffectSeq().isEmpty()) {
			nextStateFct = new NextStateFct();
		} else {
			Map<ASALVariable, ASALSymbolicValue> initialValuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>();
			
			for (ASALVariable v : owner.scope.getVariablePerName().values()) {
				if (v instanceof ReprPort) {
					ReprPort rp = (ReprPort)v;
					
					if (rp.getDir() == Dir.OUT) {
						if (rp.getType().equals(JPulse.class)) {
							initialValuePerVar.put(rp, ASALSymbolicValue.FALSE);
						} else {
							if (rp.getPulsePort() != null) {
								if (owner.initialization.containsKey(rp)) {
									initialValuePerVar.put(rp, owner.initialization.get(rp));
								} else {
									initialValuePerVar.put(rp, ASALSymbolicValue.from(rp.getInitialValue()));
								}
								
//								if (initialValuePerVar.get(rp) == null) {
//									throw new Error("?? " + rp.getName());
//								}
								
								//initialValuePerVar.put(rp, ASALSymbolicValue.unassigned(rp));
							} else {
								initialValuePerVar.put(rp, ASALSymbolicValue.from(rp));
							}
						}
					} else {
						initialValuePerVar.put(rp, ASALSymbolicValue.from(rp));
					}
				} else {
					initialValuePerVar.put(v, ASALSymbolicValue.from(v));
				}
			}
			
			Map<ASALVariable, ASALStatement> statPerChangedVar = new HashMap<ASALVariable, ASALStatement>();
			Map<ASALVariable, ASALSymbolicValue> exprPerChangedVar = ASALFlattening.getNextStateFct2(owner.scope, initialValuePerVar, source.getEffectSeq(), statPerChangedVar);
			
			nextStateFct = new NextStateFct();
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : exprPerChangedVar.entrySet()) {
				nextStateFct.put(entry.getKey(), entry.getValue(), statPerChangedVar.get(entry.getKey()).getFileLocation().tiny());
			}
		}
		
//		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : owner.initialization.entrySet()) {
//			System.out.println("init " + entry.getKey().getName() + " -> " + entry.getValue().toString());
//		}
//		
//		for (NextStateFct.Entry entry : nextStateFct.getEntries()) {
//			String s = "update " + entry.getVariable().getName() + " -> " + entry.getValue().toString();
//			System.out.println(s);
//		}
//		
//		for (ASALStatement eff : source.getEffectSeq()) {
//			System.out.println(eff.textify(LOD.ONE_LINE));
//		}
		
//		for (NextStateFct.Entry entry : nextStateFct.getEntries()) {
//			String s = "update " + entry.getVariable().getName() + " -> " + entry.getValue().toString();
//			
//			if (s.equals("update D50_PDI_Connection_State -> \"PDI_Connection_State.READY_FOR_INITIALISATION\"")) {
//				throw new Error();
//			}
//		}
		
		//Check that when a DT/T-output is set, all associated DT/T-outputs are also set:
		checkExplicitOutputParamWriting();
		
		//Check that output pulses are never set to FALSE.
		checkNoFalseOutputPulseWriting();
		
		//Check that reading a DT-input means that we also read the corresponding T-input:
		checkInputParamReading();
		
		//Check that DT/T-outputs are never READ:
		checkNoOutputReading();
		
		//Check that DT/T-inputs are never SET:
		checkNoInputWriting();
		
		//All T/DT-inputs are immediately reset.
		//All T/DT-outputs that are not set by this transition are reset.
		//D-inputs are left unchanged here, although we may rewrite them later.
		//D-outputs are never touched.
		//Properties are left unchanged here, although we may rewrite them later.
		for (ASALVariable v : owner.scope.getVariablePerName().values()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getType().equals(JPulse.class)) {
					if (rp.getDir() == Dir.IN) {
						
					} else {
						if (!nextStateFct.containsKey(rp)) {
							nextStateFct.put(rp, ASALSymbolicValue.FALSE, "OUTPUT_RESET");
							
							for (ReprPort drp : rp.getDataPorts()) {
								if (owner.initialization.containsKey(rp)) {
									nextStateFct.put(drp, owner.initialization.get(drp), "OUTPUT_RESET");
								} else {
									nextStateFct.put(drp, ASALSymbolicValue.from(drp.getInitialValue()), "OUTPUT_RESET");
								}
								
								//nextStateFct.put(drp, ASALSymbolicValue.unassigned(drp), "OUTPUT_RESET");
							}
						}
					}
				} else {
					if (rp.getPulsePort() != null) {
						//DT-input/output => We reset these if we reset their pulse port (see above). 
					} else {
						//D-input/output => Do not touch!
					}
				}
			} else {
				//Property => Do nothing.
			}
		}
		
		nextStateFct.substitute(restrictedInputs);
	}
	
	private void checkExplicitOutputParamWriting() throws ModelException {
		Set<ReprPort> portsThatShouldBeAssigned = new HashSet<ReprPort>();
		
		for (ASALVariable v : nextStateFct.getVariables()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.OUT) {
					if (rp.getPulsePort() != null) {
						portsThatShouldBeAssigned.add(rp.getPulsePort());
					}
					
					if (rp.getDataPorts().size() > 0) {
						portsThatShouldBeAssigned.addAll(rp.getDataPorts());
					}
				}
			}
		}
		
		for (ReprPort rp : portsThatShouldBeAssigned) {
			if (!nextStateFct.containsKey(rp)) {
				throw new ModelException(legacy.getFileLocations(), "Expected assignment to output port " + rp.getName() + " because of assignments to a related port!");
			}
		}
	}
	
	private void checkNoFalseOutputPulseWriting() throws ModelException {
		for (ASALVariable v : nextStateFct.getVariables()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getType().equals(JPulse.class)) {
					if (nextStateFct.get(rp).getValue().equals(ASALSymbolicValue.FALSE)) {
						throw new ModelException(legacy.getFileLocations(), "Cannot assign FALSE to " + rp.getName() + " because it is a pulse port!");
					}
				}
			}
		}
	}
	
	private void checkInputParamReading() throws ModelException {
		Set<ReprPort> portsThatShouldBeInGuard = new HashSet<ReprPort>();
		
		for (ASALVariable v : getReadVariables()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.IN) {
					if (rp.getPulsePort() != null) {
						portsThatShouldBeInGuard.add(rp.getPulsePort());
					}
				}
			}
		}
		
		portsThatShouldBeInGuard.removeAll(guard.getReferencedVars());
		
		for (ReprPort rp : portsThatShouldBeInGuard) {
			throw new ModelException(legacy.getFileLocations(), "Expected check on input port " + rp.getName() + " because you read from a related port!");
		}
	}
	
	private void checkNoOutputReading() throws ModelException {
		for (ASALVariable v : guard.getReferencedVars()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.OUT) {
					throw new ModelException(legacy.getFileLocations(), rp.getName() + " cannot be read because it is an output port (" + guard.toString() + ")!");
				}
			}
		}
		
		for (ASALVariable w : nextStateFct.getVariables()) {
			NextStateFct.Entry e = nextStateFct.get(w);
			
			for (ASALVariable v : e.getValue().getReferencedVars()) {
				if (v instanceof ReprPort) {
					ReprPort rp = (ReprPort)v;
					
					if (rp.getDir() == Dir.OUT) {
						throw new ModelException(legacy.getFileLocations(), rp.getName() + " cannot be read because it is an output port (" + w.getName() + " := " + e.getValue().toString() + ")!");
					}
				}
			}
		}
	}
	
	private void checkNoInputWriting() throws ModelException {
		for (ASALVariable v : nextStateFct.getVariables()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.IN) {
					if (!nextStateFct.get(rp).getValue().equals(ASALSymbolicValue.from(rp))) {
						throw new ModelException(legacy.getFileLocations(), rp.getName() + " cannot be assigned because it is an input port!");
					}
				}
			}
		}
	}
	
	/**
	 * Read AND written variables!
	 */
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(guard.getReferencedVars());
		result.addAll(nextStateFct.getReferencedVars());
		return result;
	}
	
	public Set<ASALVariable> getReadVariables() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(guard.getReferencedVars());
		result.addAll(nextStateFct.getReadVariables());
		return result;
	}
	
	public OctoTransition getLegacy() {
		return legacy;
	}
	
	public List<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public NonaVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public NonaVertex getTargetVertex() {
		return targetVertex;
	}
	
	public ASALTimeout getTimeoutEvent() {
		return timeoutEvent;
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



