package jlx.behave.proto;

import java.io.*;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALPort;
import jlx.asal.vars.ASALVariable;
import jlx.behave.StateMachine;
import jlx.models.UnifyingBlock.*;
import jlx.utils.*;

/**
 * 
 */
public class DecaFourStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<InputEquivClz> inputEquivClzs;
	public final Map<DecaFourTransitionReqSet, Set<InputEquivClz>> inputEquivClzsPerReqSet;
	public final Set<DecaFourVertex> vertices;
	public final DecaFourVertex initialVertex;
	public final PulsePackMap initialInputs;
	public final Set<DecaFourTransition> transitions;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final DecaThreeStateMachine legacy;
	public final Set<DecaFourTransitionReqSet> reqSets;
	public final Map<Set<DecaFourVertex>, DecaFourTgtGrp> tgtGrpPerVtxs;
	
	public DecaFourStateMachine(DecaTwoBStateMachine sm, Map<JScope, DecaTwoBStateMachine> smPerScope, Map<DecaTwoBVertex, DecaFourVertex> newVertexPerOldVertex, LDDMapFactory<JScope, DecaFourVertex> cfgFactory) {
		instance = sm.instance;
		scope = sm.scope;
		legacy = null;
		
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(sm.timeoutPortPerDurationPort);
		vertices = new HashSet<DecaFourVertex>();
		
		for (DecaTwoBVertex oldVertex : sm.vertices.values()) {
			vertices.add(newVertexPerOldVertex.get(oldVertex));
		}
		
		inputEquivClzs = new HashSet<InputEquivClz>();
		initialInputs = sm.initialInputs;
		initialVertex = newVertexPerOldVertex.get(sm.initialVertex);
		
		//Determine all "output state machines", i.e. state machines that generate outputs that become inputs for this state machine.
		//Per output state machine X, determine the input ports of this state machine to which X's output ports are connected:
		Map<DecaTwoBStateMachine, Map<ReprPort, Set<ReprPort>>> inputsPerOutputPerOutputSm = new HashMap<DecaTwoBStateMachine, Map<ReprPort, Set<ReprPort>>>();
		
		for (DecaTwoBStateMachine outputSm : smPerScope.values()) {
			Map<ReprPort, Set<ReprPort>> inputsPerOutput = new HashMap<ReprPort, Set<ReprPort>>();
			
			for (ASALVariable v : outputSm.scope.getVariablePerName().values()) {
				if (v instanceof ReprPort) {
					ReprPort rp = (ReprPort)v;
					
					for (ReprFlow rf : rp.getOutgoingFlows()) {
						if (rf.target.getReprOwner() == scope) {
							HashMaps.inject(inputsPerOutput, rp, rf.target);
						}
					}
				}
			}
			
			if (inputsPerOutput.size() > 0) {
				inputsPerOutputPerOutputSm.put(outputSm, inputsPerOutput);
			}
		}
		
		System.out.println("2");
		
		//Group vertices of each output state machine by the (internal) output valuation that it generates for ONLY THIS state machine:
		Map<DecaTwoBStateMachine, Map<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>>> vtxsPerInternalOutputValPerOutputSm = new HashMap<DecaTwoBStateMachine, Map<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>>>();
		
		for (Map.Entry<DecaTwoBStateMachine, Map<ReprPort, Set<ReprPort>>> e : inputsPerOutputPerOutputSm.entrySet()) {
			Map<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>> vtxsPerInternalOutputVal = new HashMap<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>>();
			Set<ReprPort> outputs = e.getValue().keySet();
			
			for (DecaTwoBVertex v : e.getKey().vertices.values()) {
				Map<ReprPort, ASALSymbolicValue> internalOutputVal = new HashMap<ReprPort, ASALSymbolicValue>();
				
				for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : v.getOutputVal().entrySet()) {
					if (outputs.contains(e2.getKey())) {
						internalOutputVal.put(e2.getKey(), e2.getValue());
					}
				}
				
				if (newVertexPerOldVertex.get(v) == null) {
					for (Class<?> c : v.getSysmlClzs()) {
						System.out.println("error clz = " + c.getCanonicalName());
					}
					
					throw new Error("Should not happen!");
				}
				
				internalOutputVal = PulsePackMap.from(internalOutputVal, Dir.OUT).extractValuation();
				HashMaps.inject(vtxsPerInternalOutputVal, internalOutputVal, newVertexPerOldVertex.get(v));
			}
			
			vtxsPerInternalOutputValPerOutputSm.put(e.getKey(), vtxsPerInternalOutputVal);
		}
		
		System.out.println("3");
		
		Map<Map<JScope, DecaFourVertex>, Set<PulsePackMap>> inputValsPerVtxCfg = new HashMap<Map<JScope, DecaFourVertex>, Set<PulsePackMap>>();
		
		for (PulsePackMap input : sm.allInputs) {
			Map<ReprPort, ASALSymbolicValue> inputVal = input.extractValuation();
			
			//Compute per output state machine the vertices that generate outputs that match the currently considered input valuation:
			Map<JScope, Set<DecaFourVertex>> vtxsPerOutputSm = new HashMap<JScope, Set<DecaFourVertex>>();
			boolean isValid = true;
			
			for (Map.Entry<DecaTwoBStateMachine, Map<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>>> e : vtxsPerInternalOutputValPerOutputSm.entrySet()) {
				Map<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>> vtxsPerInternalOutputVal = e.getValue();
				Map<ReprPort, Set<ReprPort>> inputsPerOutput = inputsPerOutputPerOutputSm.get(e.getKey());
				Set<DecaFourVertex> vtxs = new HashSet<DecaFourVertex>();
				
				for (Map.Entry<Map<ReprPort, ASALSymbolicValue>, Set<DecaFourVertex>> e2 : vtxsPerInternalOutputVal.entrySet()) {
					Map<ReprPort, ASALSymbolicValue> internalOutputVal = e2.getKey();
					boolean matches = true;
					
					for (Map.Entry<ReprPort, ASALSymbolicValue> e3 : internalOutputVal.entrySet()) {
						for (ReprPort inputRp : inputsPerOutput.get(e3.getKey())) {
							ASALSymbolicValue v = inputVal.get(inputRp);
							
							//Two possibilities:
							// - Value is not null.
							//    Then the value must match the output value.
							// - Value is null.
							//    Then it must be a DT-port of which the T-port is FALSE.
							//    We will encounter a mismatch for the T-port values, or
							//    the output value is a dummy DT-port value that accompanies a FALSE T-port.
							if (v != null && !e3.getValue().equals(v)) {
								matches = false;
								break;
							}
						}
						
						if (!matches) {
							break;
						}
					}
					
					if (matches) {
						vtxs.addAll(e2.getValue());
					}
				}
				
				if (vtxs.isEmpty()) {
//					System.out.println("No output matches input " + input.extractInternalMap());
//					
//					CLI.waitForEnter();
					
					isValid = false;
					break;
				}
				
				vtxsPerOutputSm.put(e.getKey().scope, vtxs);
			}
			
			if (isValid) {
				for (Map<JScope, DecaFourVertex> vtxCfg : HashMaps.allCombinations(vtxsPerOutputSm)) {
					HashMaps.inject(inputValsPerVtxCfg, vtxCfg, input);
				}
			}
		}
		
		System.out.println("4");
		
		Map<PulsePackMap, Set<Map<JScope, DecaFourVertex>>> vtxCfgsPerInputVal = HashMaps.splitValuesByKeys(inputValsPerVtxCfg);
		
		System.out.println("#deca-four-allInputs = " + vtxCfgsPerInputVal.size());
		
		System.out.println("#deca-four-vtxCfgsPerInputVal = " + vtxCfgsPerInputVal.size());
		
		Set<Set<PulsePackMap>> inputEquivClzSets = new HashSet<Set<PulsePackMap>>();
		
		for (DecaTwoBTransition t : sm.transitions) {
			inputEquivClzSets.add(t.getInputVals());
		}
		
		System.out.println("#deca-four-inputEquivClzSets = " + inputEquivClzSets.size());
		
		reqSets = new HashSet<DecaFourTransitionReqSet>();
		Map<Set<PulsePackMap>, DecaFourTransitionReqSet> reqSetPerInputEquivClzSet = new HashMap<Set<PulsePackMap>, DecaFourTransitionReqSet>();
		
		for (Set<PulsePackMap> is : inputEquivClzSets) {
			Set<Map<JScope, DecaFourVertex>> enablingVtxCfgs = new HashSet<Map<JScope, DecaFourVertex>>();
			LDDMapFactory.LDDMap<JScope, DecaFourVertex> x = cfgFactory.empty();
			
			for (PulsePackMap i : is) {
				Set<Map<JScope, DecaFourVertex>> vtxCfgs = vtxCfgsPerInputVal.get(i);
				
				if (vtxCfgs != null) {
					enablingVtxCfgs.addAll(vtxCfgs);
				}
			}
			
			DecaFourTransitionReqSet reqSet = DecaFourTransitionReqSet.create(enablingVtxCfgs, x);
			
			if (reqSet != null) {
				reqSetPerInputEquivClzSet.put(is, reqSet);
				reqSets.add(reqSet);
			} else {
//				System.out.println("scope = " + scope.getName());
//				
//				Set<PulsePackMap> z = new HashSet<PulsePackMap>();
//				
//				for (PulsePackMap i : is) {
//					z.add(i.extractInternalMap());
//				}
//				
//				System.out.println("Forbidden set of input valuations:");
//				
//				for (PulsePackMap i : z) {
//					System.out.println("\t" + i.toString());
//				}
//				
//				CLI.waitForEnter();
			}
		}
		
		System.out.println("#deca-four-reqSets = " + reqSets.size());
		
		tgtGrpPerVtxs = new HashMap<Set<DecaFourVertex>, DecaFourTgtGrp>();
		
		//Transitions:
		transitions = new HashSet<DecaFourTransition>();
		
		for (DecaTwoBTransition t : sm.transitions) {
			DecaFourVertex srcVertex = newVertexPerOldVertex.get(t.getSrc());
			DecaFourTransitionReqSet reqSet = reqSetPerInputEquivClzSet.get(t.getInputVals());
			
//			if (isStoppedTransition(t)) {
//				System.out.println("Found special transition!");
//				
//				if (reqSet == null) {
//					System.out.println("Blocked!");
//				}
//				
//				System.exit(0);
//			}
			
//			if (contains(t.getSrc(), "STOPPED")) {
//				if (contains(t.getTgts(), "MOVING_LEFT")) {
//					if (reqSet != null) {
//						Set<PulsePackMap> zs = new HashSet<PulsePackMap>();
//						
//						for (PulsePackMap z : t.getInputVals()) {
//							zs.add(z);
//						}
//						
//						for (PulsePackMap z : zs) {
//							System.out.println("z = " + z.extractInternalMap());
//						}
//						
//						if (reqSet == null) {
//							System.out.println("Found STOPPED->MOVING_LEFT, but no req set!");
//						}
//						
//						CLI.waitForEnter();
//					}
//				}
//			}
			
			if (reqSet == null) {
				//throw new Error("Cannot happen?");
				//This *CAN* happen: This means that other state machines prevent this transition from ever being enabled!
				continue;
			}
			
			Set<DecaFourVertex> tgtVertices = new HashSet<DecaFourVertex>();
			
			for (DecaTwoBVertex c : t.getTgts()) {
				tgtVertices.add(newVertexPerOldVertex.get(c));
			}
			
			DecaFourTgtGrp tgtGrp = tgtGrpPerVtxs.get(tgtVertices);
			
			if (tgtGrp == null) {
				tgtGrp = new DecaFourTgtGrp(scope, tgtVertices, tgtGrpPerVtxs.size() + 1);
				tgtGrpPerVtxs.put(tgtVertices, tgtGrp);
			}
			
			DecaFourTransition newTr = new DecaFourTransition(t, reqSet, srcVertex, tgtGrp);
			
//			srcVertex.getOutgoing().put(newTr.getInputs(), newTr);
//			HashMaps.injectAll(srcVertex.getProtoTrsPerReqSet(), reqSet, t.getProtoTrs());
			HashMaps.inject(srcVertex.getNondetOutgoingPerReqSet(), reqSet, newTr);
			HashMaps.inject(srcVertex.getOutgoing(), t.getInputVals(), newTr);
			
			for (PulsePackMap i : newTr.getInputs()) {
				HashMaps.injectInject(srcVertex.getInputsPerNonDetTgtsPerReqSet(), reqSet, tgtGrp, i);
				PulsePackMap e = i.extractExternalMap();
				
				if (e != null) { //Never null . . .
					HashMaps.injectInject(srcVertex.getTrsPerNonDetTgtsPerReqSet(), reqSet, e, newTr);
					HashMaps.injectInject(srcVertex.getExternalInputsPerNonDetTgtsPerReqSet(), reqSet, tgtGrp, e);
				}
			}
			
			for (DecaFourVertex tgtVertex : tgtVertices) {
				tgtVertex.getIncoming().add(newTr);
			}
			
			transitions.add(newTr);
		}
		
		System.out.println("5");
		
		for (DecaFourVertex v : vertices) {
			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e : v.getInputsPerNonDetTgtsPerReqSet().entrySet()) {
				for (Map.Entry<DecaFourTgtGrp, Set<PulsePackMap>> e2 : e.getValue().entrySet()) {
					HashMaps.injectSet(v.getInputsPerNonDetTgtsPerReqSet2(), e.getKey(), e2.getKey(), new DecaFourPulsePackMaps(e2.getValue(), false));
				}
			}
			
			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e : v.getExternalInputsPerNonDetTgtsPerReqSet().entrySet()) {
				for (Map.Entry<DecaFourTgtGrp, Set<PulsePackMap>> e2 : e.getValue().entrySet()) {
					HashMaps.injectSet(v.getExternalInputsPerNonDetTgtsPerReqSet2(), e.getKey(), e2.getKey(), new DecaFourPulsePackMaps(e2.getValue(), true));
				}
			}
		}
		
		System.out.println("6");
		
		inputEquivClzsPerReqSet = Collections.emptyMap();
	}
	
	private static boolean contains(Collection<DecaTwoBVertex> vs, String s) {
		for (DecaTwoBVertex v : vs) {
			if (contains(v, s)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean contains(DecaTwoBVertex v, String s) {
		return clzSetToStr(v.getSysmlClzs()).contains(s);
	}
	
	private static String clzSetToStr(Set<Class<?>> clzSet) {
		return Texts.concat(clzSet, "+", (c) -> { return c.getSimpleName(); });
	}
	
	public DecaFourStateMachine(DecaThreeStateMachine sm, Map<JScope, DecaThreeStateMachine> smPerScope, Map<DecaThreeVertex, DecaFourVertex> newVertexPerOldVertex, LDDMapFactory<JScope, DecaFourVertex> cfgFactory) {
		instance = sm.instance;
		scope = sm.scope;
		legacy = sm;
		
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(sm.timeoutPortPerDurationPort);
		vertices = new HashSet<DecaFourVertex>();
		
		for (DecaThreeVertex oldVertex : sm.vertices.values()) {
			vertices.add(newVertexPerOldVertex.get(oldVertex));
		}
		
		inputEquivClzs = new HashSet<InputEquivClz>(sm.inputEquivClzs);
		initialInputs = sm.initialInputs;
		initialVertex = newVertexPerOldVertex.get(sm.initialVertex);
		
		System.out.println("1");
		
		//Determine all "output state machines", i.e. state machines that generate outputs that become inputs for this state machine.
		//Per output state machine X, determine the input ports of this state machine to which X's output ports are connected:
		Map<DecaThreeStateMachine, Map<ReprPort, Set<ReprPort>>> inputsPerOutputPerOutputSm = new HashMap<DecaThreeStateMachine, Map<ReprPort, Set<ReprPort>>>();
		
		for (DecaThreeStateMachine outputSm : smPerScope.values()) {
			Map<ReprPort, Set<ReprPort>> inputsPerOutput = new HashMap<ReprPort, Set<ReprPort>>();
			
			for (ASALVariable v : outputSm.scope.getVariablePerName().values()) {
				if (v instanceof ReprPort) {
					ReprPort rp = (ReprPort)v;
					
					for (ReprFlow rf : rp.getOutgoingFlows()) {
						if (rf.target.getReprOwner() == scope) {
							HashMaps.inject(inputsPerOutput, rp, rf.target);
						}
					}
				}
			}
			
			if (inputsPerOutput.size() > 0) {
				inputsPerOutputPerOutputSm.put(outputSm, inputsPerOutput);
			}
		}
		
		System.out.println("2");
		
		//Group vertices of each output state machine by the (internal) output valuation that it generates for ONLY THIS state machine:
		Map<DecaThreeStateMachine, Map<Map<ReprPort, PulsePack>, Set<DecaFourVertex>>> vtxsPerInternalOutputValPerOutputSm = new HashMap<DecaThreeStateMachine, Map<Map<ReprPort, PulsePack>, Set<DecaFourVertex>>>();
		
		for (Map.Entry<DecaThreeStateMachine, Map<ReprPort, Set<ReprPort>>> e : inputsPerOutputPerOutputSm.entrySet()) {
			Map<Map<ReprPort, PulsePack>, Set<DecaFourVertex>> vtxsPerInternalOutputVal = new HashMap<Map<ReprPort, PulsePack>, Set<DecaFourVertex>>();
			Set<ReprPort> outputs = e.getValue().keySet();
			
			for (DecaThreeVertex v : e.getKey().vertices.values()) {
				Map<ReprPort, PulsePack> internalOutputVal = new HashMap<ReprPort, PulsePack>();
				
				for (Map.Entry<ReprPort, PulsePack> e2 : v.getStateConfig().getOutputVal().getPackPerPort().entrySet()) {
					if (outputs.contains(e2.getKey())) {
						internalOutputVal.put(e2.getKey(), e2.getValue());
					}
				}
				
				if (newVertexPerOldVertex.get(v) == null) {
					for (Class<?> c : v.getStateConfig().getSysmlClzs()) {
						System.out.println("error clz = " + c.getCanonicalName());
					}
					
					throw new Error("Should not happen!");
				}
				
				HashMaps.inject(vtxsPerInternalOutputVal, internalOutputVal, newVertexPerOldVertex.get(v));
			}
			
			vtxsPerInternalOutputValPerOutputSm.put(e.getKey(), vtxsPerInternalOutputVal);
		}
		
		System.out.println("3");
		
		//Compute the input valuations that would be matched per vertex configuration:
		Map<Map<JScope, DecaFourVertex>, Set<PulsePackMap>> inputValsPerVtxCfg = new HashMap<Map<JScope, DecaFourVertex>, Set<PulsePackMap>>();
		
		for (InputEquivClz i : inputEquivClzs) {
			for (PulsePackMap inputVal : i.getInputVals()) {
				//Compute per output state machine the vertices that generate outputs that match the currently considered input valuation:
				Map<JScope, Set<DecaFourVertex>> vtxsPerOutputSm = new HashMap<JScope, Set<DecaFourVertex>>();
				boolean isValid = true;
				
				for (Map.Entry<DecaThreeStateMachine, Map<Map<ReprPort, PulsePack>, Set<DecaFourVertex>>> e : vtxsPerInternalOutputValPerOutputSm.entrySet()) {
					Map<Map<ReprPort, PulsePack>, Set<DecaFourVertex>> vtxsPerInternalOutputVal = e.getValue();
					Map<ReprPort, Set<ReprPort>> inputsPerOutput = inputsPerOutputPerOutputSm.get(e.getKey());
					Set<DecaFourVertex> vtxs = new HashSet<DecaFourVertex>();
					
					for (Map.Entry<Map<ReprPort, PulsePack>, Set<DecaFourVertex>> e2 : vtxsPerInternalOutputVal.entrySet()) {
						Map<ReprPort, PulsePack> internalOutputVal = e2.getKey();
						boolean matches = true;
						
						for (Map.Entry<ReprPort, PulsePack> e3 : internalOutputVal.entrySet()) {
							for (ReprPort inputRp : inputsPerOutput.get(e3.getKey())) {
								if (!e3.getValue().equals(inputVal.getPackPerPort().get(inputRp))) {
									matches = false;
									break;
								}
							}
							
							if (!matches) {
								break;
							}
						}
						
						if (matches) {
							vtxs.addAll(e2.getValue());
						}
					}
					
					if (vtxs.isEmpty()) {
						isValid = false;
						break;
					}
					
					vtxsPerOutputSm.put(e.getKey().scope, vtxs);
				}
				
				if (isValid) {
					for (Map<JScope, DecaFourVertex> vtxCfg : HashMaps.allCombinations(vtxsPerOutputSm)) {
						HashMaps.inject(inputValsPerVtxCfg, vtxCfg, inputVal);
					}
				}
			}
		}
		
		System.out.println("4");
		
		//Per input valuation, compute the vertex configurations that would match it:
		Map<PulsePackMap, Set<Map<JScope, DecaFourVertex>>> vtxCfgsPerInputVal = HashMaps.splitValuesByKeys(inputValsPerVtxCfg);
		Set<Set<InputEquivClz>> inputEquivClzSets = new HashSet<Set<InputEquivClz>>();
		
		for (DecaThreeTransition t : sm.transitions) {
			inputEquivClzSets.add(t.getInputs());
		}
		
		reqSets = new HashSet<DecaFourTransitionReqSet>();
		Map<Set<InputEquivClz>, DecaFourTransitionReqSet> reqSetPerInputEquivClzSet = new HashMap<Set<InputEquivClz>, DecaFourTransitionReqSet>();
		
		for (Set<InputEquivClz> is : inputEquivClzSets) {
			Set<Map<JScope, DecaFourVertex>> enablingVtxCfgs = new HashSet<Map<JScope, DecaFourVertex>>();
			LDDMapFactory.LDDMap<JScope, DecaFourVertex> x = cfgFactory.empty();
			
			for (InputEquivClz i : is) {
				for (PulsePackMap inputVal : i.getInputVals()) {
					Set<Map<JScope, DecaFourVertex>> vtxCfgs = vtxCfgsPerInputVal.get(inputVal);
					
					if (vtxCfgs != null) {
						enablingVtxCfgs.addAll(vtxCfgs);
					}
				}
			}
			
			DecaFourTransitionReqSet reqSet = DecaFourTransitionReqSet.create(enablingVtxCfgs, x);
			
			if (reqSet != null) {
				reqSetPerInputEquivClzSet.put(is, reqSet);
				reqSets.add(reqSet);
			}
		}
		
		System.out.println("#inputEquivClzSets = " + inputEquivClzSets.size());
//		CLI.waitForEnter();
		
		inputEquivClzsPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<InputEquivClz>>();
		Map<InputEquivClz, DecaFourTransitionReqSet> reqSetPerInputClz = new HashMap<InputEquivClz, DecaFourTransitionReqSet>();
		
		for (InputEquivClz i : inputEquivClzs) {
			Set<Map<JScope, DecaFourVertex>> enablingVtxCfgs = new HashSet<Map<JScope, DecaFourVertex>>();
			
			for (PulsePackMap inputVal : i.getInputVals()) {
				Set<Map<JScope, DecaFourVertex>> vtxCfgs = vtxCfgsPerInputVal.get(inputVal);
				
				if (vtxCfgs != null) {
					enablingVtxCfgs.addAll(vtxCfgs);
				}
			}
			
			if (enablingVtxCfgs.isEmpty()) {
				//This can happen, some inputs are prevented from happening by other components!
			}
			
			DecaFourTransitionReqSet reqSet = DecaFourTransitionReqSet.create(enablingVtxCfgs, null);
			
			if (reqSet != null) {
				HashMaps.inject(inputEquivClzsPerReqSet, reqSet, i);
				reqSetPerInputClz.put(i, reqSet);
			}
		}
		
//		SortedMap<Integer, Set<InputEquivClz>> clzsPerScore = new TreeMap<Integer, Set<InputEquivClz>>();
//		
//		for (InputEquivClz i : inputEquivClzs) {
//			HashMaps.inject(clzsPerScore, i.getMinInputValScore(), i);
//		}
//		
//		List<InputEquivClz> sortedClzs = new ArrayList<InputEquivClz>();
//		
//		for (Map.Entry<Integer, Set<InputEquivClz>> e : clzsPerScore.entrySet()) {
//			sortedClzs.addAll(e.getValue());
//		}
//		
//		for (DecaFourVertex v : vertices) {
//			v.orderedInputClzs = sortedClzs;
//		}
		
		tgtGrpPerVtxs = new HashMap<Set<DecaFourVertex>, DecaFourTgtGrp>();
		
		//Transitions:
		transitions = new HashSet<DecaFourTransition>();
		
		for (DecaThreeTransition t : sm.transitions) {
			DecaFourVertex srcVertex = newVertexPerOldVertex.get(sm.vertices.get(t.getSourceStateConfig()));
			DecaFourTransitionReqSet reqSet = reqSetPerInputEquivClzSet.get(t.getInputs());
			
			if (reqSet != null) {
				Set<DecaFourVertex> tgtVertices = new HashSet<DecaFourVertex>();
				
				for (DecaThreeStateConfig c : t.getTargetStateConfigs()) {
					tgtVertices.add(newVertexPerOldVertex.get(sm.vertices.get(c)));
				}
				
				DecaFourTgtGrp tgtGrp = tgtGrpPerVtxs.get(tgtVertices);
				
				if (tgtGrp == null) {
					tgtGrp = new DecaFourTgtGrp(scope, tgtVertices, tgtGrpPerVtxs.size() + 1);
					tgtGrpPerVtxs.put(tgtVertices, tgtGrp);
				}
				
				//HashMaps.injectAll(srcVertex.getProtoTrsPerReqSet(), reqSet, t.getEnabledProtoLegacy());
				
				DecaFourTransition newTr = new DecaFourTransition(t, reqSet, srcVertex, tgtGrp);
				
//				srcVertex.getOutgoing().put(newTr.getInputs(), newTr);
				HashMaps.inject(srcVertex.getOutgoingPerReqSet(), newTr.getReqs(), newTr);
				
				if (tgtVertices.size() == 1) {
//					srcVertex.getDetOutgoing().put(newTr.getInputs(), newTr);
					HashMaps.inject(srcVertex.getDetOutgoingPerReqSet(), newTr.getReqs(), newTr);
					HashMaps.inject(srcVertex.getNondetOutgoingPerReqSet(), newTr.getReqs(), newTr);
					
//					for (InputEquivClz i : newTr.getInputs()) {
//						if (reqSetPerInputClz.get(i) != null) {
//							HashMaps.injectInject(srcVertex.getInputsPerDetTgtPerReqSet(), reqSetPerInputClz.get(i), tgtGrp, i);
//							
////							if (i.getExternal() != null) {
////								HashMaps.injectInject(srcVertex.getExternalInputsPerDetTgtPerReqSet(), reqSetPerInputClz.get(i), tgtGrp, i.getExternal());
////							}
//						}
//					}
					
//					for (InputEquivClz i : newTr.getInputs()) {
//						if (reqSetPerInputClz.get(i) != null) {
//							HashMaps.injectInject(srcVertex.getInputsPerNonDetTgtsPerReqSet(), reqSetPerInputClz.get(i), newTr.getTgtGrp(), i);
//							InputEquivClz e = i.extractDeactivatedExternal();
//							
//							if (e != null) {
//								HashMaps.injectInject(srcVertex.getExternalInputsPerNonDetTgtsPerReqSet(), reqSetPerInputClz.get(i), tgtGrp, e);
//							}
//						}
//					}
				} else {
					HashMaps.inject(srcVertex.getNondetOutgoingPerReqSet(), newTr.getReqs(), newTr);
					
//					for (InputEquivClz i : newTr.getInputs()) {
//						if (reqSetPerInputClz.get(i) != null) {
//							HashMaps.injectInject(srcVertex.getInputsPerNonDetTgtsPerReqSet(), reqSetPerInputClz.get(i), newTr.getTgtGrp(), i);
//							InputEquivClz e = i.extractDeactivatedExternal();
//							
//							if (e != null) {
//								HashMaps.injectInject(srcVertex.getExternalInputsPerNonDetTgtsPerReqSet(), reqSetPerInputClz.get(i), tgtGrp, e);
//							}
//						}
//					}
				}
				
				for (DecaFourVertex tgtVertex : tgtVertices) {
					tgtVertex.getIncoming().add(newTr);
				}
				
				transitions.add(newTr);
			}
		}
		
		System.out.println("5");
	}
	
	public boolean updateReachableVertices() {
		boolean hasChanged = false;
		
		Set<DecaFourVertex> fringe = new HashSet<DecaFourVertex>();
		Set<DecaFourVertex> newFringe = new HashSet<DecaFourVertex>();
		
		for (DecaFourVertex v : vertices) {
			if (v.isReachable) {
				fringe.add(v);
			}
		}
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaFourVertex v : fringe) {
//				for (DecaFourTransition t : v.getOutgoing().values()) {
//					if (t.isReachable || isEnabled(t)) {
//						t.isReachable = true;
//						
//						for (DecaFourVertex tgt : t.getTgtGrp().getVtxs()) {
//							if (!tgt.isReachable) {
//								tgt.isReachable = true;
//								newFringe.add(tgt);
//								hasChanged = true;
//							}
//						}
//					}
//				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return hasChanged;
	}
	
	private boolean isEnabled(DecaFourTransition t) {
		for (Map<JScope, DecaFourVertex> req : t.getReqs().getReqCfgs()) {
			boolean pass = true;
			
			for (Map.Entry<JScope, DecaFourVertex> e : req.entrySet()) {
				if (!e.getValue().isReachable) {
					pass = false;
					break;
				}
			}
			
			if (pass) {
				return true;
			}
		}
		
		return false;
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-four-" + scope.getName() + ".gv");
//			printGraphvizFile(ps, extractStoppedTransitions());
			printGraphvizFile(ps, transitions);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	private boolean isStoppedTransition(DecaTwoBTransition t) {
		if (contains(t.getSrc(), "STOPPED", "ALL_LEFT")) {
			if (contains(t.getTgts(), "MOVING_RIGHT", "ALL_LEFT")) {
				if (t.getTgts().size() == 1) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isStoppedTransition(DecaFourTransition t) {
		if (contains(t.getSourceVertex(), "STOPPED", "ALL_LEFT")) {
			if (contains(t.getTgtGrp(), "MOVING_RIGHT", "ALL_LEFT")) {
				if (t.getTgtGrp().getVtxs().size() == 1) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private Set<DecaFourTransition> extractStoppedTransitions() {
		Set<DecaFourTransition> result = new HashSet<DecaFourTransition>();
		
		for (DecaFourTransition t : transitions) {
			if (isStoppedTransition(t)) {
				result.add(t);
			}
		}
		
		return result;
	}
	
	private static boolean contains(DecaFourTgtGrp v, String... elems) {
		for (DecaFourVertex w : v.getVtxs()) {
			if (contains(w, elems)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean contains(DecaFourVertex v, String... elems) {
		String s = v.getName();
		
		for (String elem : elems) {
			if (!s.contains(elem)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean contains(Collection<DecaTwoBVertex> v, String... elems) {
		for (DecaTwoBVertex w : v) {
			if (contains(w, elems)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean contains(DecaTwoBVertex v, String... elems) {
		String s = v.getName();
		
		for (String elem : elems) {
			if (!s.contains(elem)) {
				return false;
			}
		}
		
		return true;
	}
	
	public void printGraphvizFile(PrintStream out, Collection<DecaFourTransition> trs) {
		Set<DecaFourVertex> vtxs = new HashSet<DecaFourVertex>();
		
		for (DecaFourTransition t : trs) {
			vtxs.add(t.getSourceVertex());
			vtxs.addAll(t.getTgtGrp().getVtxs());
		}
		
		Map<DecaFourVertex, String> namePerVertex = new HashMap<DecaFourVertex, String>();
		Map<DecaFourTransition, String> namePerTransition = new HashMap<DecaFourTransition, String>();
		Map<DecaFourTransition, String> colorPerTransition = new HashMap<DecaFourTransition, String>();
//		Map<DecaFourTransition, Set<DecaFourTransition>> notePerTransition = new HashMap<DecaFourTransition, Set<DecaFourTransition>>();
		
		for (DecaFourVertex v : vtxs) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaFourTransition t : trs) {
			namePerTransition.put(t, "T" + namePerTransition.size());
			colorPerTransition.put(t, Dot.getRandomColor());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		
		{
			String s = "(initial)";
			
			for (Map.Entry<ReprPort, ASALSymbolicValue> entry : initialVertex.getOutputVal().extractValuation().entrySet()) {
				s += "\\n" + entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\"");
			}
			
			out.println("\tI0 [label=\"" + s + "\", shape=circle, fontsize=10];");
		}
		
		for (DecaFourVertex v : vtxs) {
			String s = "ID " + v.getName();
			s += "\\n" + Texts.concat(v.getSysmlClzs(), " + ", (c) -> { return c.getSimpleName(); });
			
			for (Map.Entry<ReprPort, ASALSymbolicValue> entry : v.getOutputVal().extractValuation().entrySet()) {
				if (!entry.getKey().getType().equals(JPulse.class) || entry.getValue().equals(ASALSymbolicValue.TRUE)) {
					s += "\\n" + Texts._break(entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\""), "\\n", 70);
				}
			}
			
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
		}
		
		for (DecaFourTransition t : trs) {
			{
				TextOptions.select(TextOptions.GRAPHVIZ_MIN);
				
				String s = namePerTransition.get(t);
				
				Set<PulsePackMap> zz = new HashSet<PulsePackMap>();
				
				for (PulsePackMap i : t.getInputs()) {
					zz.add(i.extractMultiplePvsMap());
				}
				
				if (zz.size() <= 12) {
					for (PulsePackMap i : zz) {
						s += "\\n" + i.toString();
					}
				}
				
				String colors = "color=\"" + colorPerTransition.get(t) + "\", fontcolor=\"" + colorPerTransition.get(t) + "\"";
				out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, fontsize=10, " + colors + ", style=dashed];");
			}
			
//			for (DecaFourTransition t : notePerTransition.get(e.getKey())) {
//				String s = e.getValue();
//				s += "\\n" + t.getInput().createCombinedGuardStr().replace("\"", "\\\"");
//				out.println("\t% " + s);
//			}
		}
		
		for (DecaFourTransition t : trs) {
			String s = namePerTransition.get(t);
			
			if (t.getTgtGrp().getVtxs().size() > 1) {
				String prev_name = namePerVertex.get(t.getSourceVertex());
				out.println("\t" + prev_name + " -> " + s + " [color=\"" + colorPerTransition.get(t) + "\", style=dashed];");
				
				for (DecaFourVertex tgt : t.getTgtGrp().getVtxs()) {
					String next_name = namePerVertex.get(tgt);
					out.println("\t" + s + " -> " + next_name + " [color=\"" + colorPerTransition.get(t) + "\", style=dashed];");
				}
			} else {
				String prev_name = namePerVertex.get(t.getSourceVertex());
				out.println("\t" + prev_name + " -> " + s + " [color=\"" + colorPerTransition.get(t) + "\", style=solid];");
				
				for (DecaFourVertex tgt : t.getTgtGrp().getVtxs()) {
					String next_name = namePerVertex.get(tgt);
					out.println("\t" + s + " -> " + next_name + " [color=\"" + colorPerTransition.get(t) + "\", style=solid];");
				}
			}
		}
		
		if (vtxs.contains(initialVertex)) {
			out.println("\tI0 -> " + namePerVertex.get(initialVertex) + ";");
		}
		
		out.println("}");
	}
}



