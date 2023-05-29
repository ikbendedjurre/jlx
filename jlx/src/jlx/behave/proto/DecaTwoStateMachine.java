package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALPort;
import jlx.asal.vars.ASALVariable;
import jlx.behave.StateMachine;
import jlx.common.reflection.ClassReflectionException;
import jlx.common.reflection.ModelException;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * This state machine eliminates properties by substitution and, iff necessary, by instantiating inputs.
 * (Consequently, transitions may be partially or entirely instantiated for concrete input values.)
 * Values of inputs are ENTIRELY determined by guards.
 * Values of outputs and properties are ENTIRELY determined by state.
 * 
 * Given two vertices V1 and V2, there may be multiple transitions from V1 to V2 (even with the same attributes).
 */
public class DecaTwoStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Map<ReprPort, Set<ASALSymbolicValue>> pvsPerInput;
	public final Map<DecaTwoStateConfig, DecaTwoVertex> vertices;
	public final DecaTwoVertex initialVertex;
	public final Map<ReprPort, ASALSymbolicValue> initialInputs;
	public final Set<DecaTwoTransition> transitions;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final Set<ASALVariable> propsAndOutputs;
	public final DecaOneStateMachine legacy;
	
	public DecaTwoStateMachine(DecaOneStateMachine source) throws ClassReflectionException, ModelException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(source.timeoutPortPerDurationPort);
		
		pvsPerInput = new HashMap<ReprPort, Set<ASALSymbolicValue>>();
		
		for (ASALVariable v : source.scope.getVariablePerName().values()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.IN) {
					if (rp.getIncomingFlows().size() > 0) {
						pvsPerInput.put(rp, ASALSymbolicValue.from(Collections.unmodifiableSet(JType.createAllValues(rp.getType()))));
					} else {
						pvsPerInput.put(rp, ASALSymbolicValue.from(Collections.unmodifiableSet(scope.getPossibleValues(rp))));
					}
				}
			}
		}
		
		propsAndOutputs = new HashSet<ASALVariable>();
		
		for (ASALVariable v : scope.getVariablePerName().values()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.OUT) {
					propsAndOutputs.add(v);
				}
			} else {
				propsAndOutputs.add(v);
			}
		}
		
		DecaTwoStateConfig initialCfg = new DecaTwoStateConfig(source.initialVertex, filterPropsAndOutputs(source.initialization));
		initialVertex = new DecaTwoVertex(initialCfg, scope.getName() + "[0][" + initialCfg.getClzsStr() + "]", 0);
		initialInputs = filterInputs(source.initialization); //(Outputs may also appear here, and are no longer needed.)
		
		vertices = new HashMap<DecaTwoStateConfig, DecaTwoVertex>();
		vertices.put(initialVertex.getStateConfig(), initialVertex);
		transitions = new HashSet<DecaTwoTransition>();
		
		Set<DecaTwoVertex> fringe = new HashSet<DecaTwoVertex>();
		Set<DecaTwoVertex> newFringe = new HashSet<DecaTwoVertex>();
		fringe.add(initialVertex);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaTwoVertex v : fringe) {
				for (DecaTwoTransition t : computeTransitions(v.getStateConfig())) {
					transitions.add(t);
					
					if (!vertices.containsKey(t.getTargetStateConfig())) {
						DecaTwoVertex newVertex = new DecaTwoVertex(t.getTargetStateConfig(), scope.getName() + "[" + vertices.size() + "][" + t.getTargetStateConfig().getClzsStr() + "]", vertices.size());
						vertices.put(t.getTargetStateConfig(), newVertex);
						newFringe.add(newVertex);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		for (DecaTwoVertex v : vertices.values()) {
			v.getStateConfig().getVertex().getNames().add(v.getName());
		}
		
		Set<ASALSymbolicValue> guards = new HashSet<ASALSymbolicValue>();
		
		for (DecaTwoTransition t : transitions) {
			vertices.get(t.getSourceStateConfig()).getOutgoing().add(t);
			guards.add(t.getGuard());
		}
		
//		if (scope.getName().equals("p3")) {
//			Set<String> elems = new TreeSet<String>();
//			
//			for (Map.Entry<ReprPort, Set<ASALSymbolicValue>> e : pvsPerInput.entrySet()) {
//				elems.add(e.getKey().getName() + " (x" + e.getValue().size() + ")");
//			}
//			
//			for (String elem : elems) {
//				System.out.println(elem);
//			}
//			
//			System.out.println("#vertices = " + vertices.size());
//			System.out.println("#transitions = " + transitions.size());
//			System.out.println("#guards = " + guards.size());
//			
//			System.exit(0);
//		}
	}
	
	private Map<ASALVariable, ASALSymbolicValue> filterPropsAndOutputs(Map<ASALVariable, ASALSymbolicValue> valuation) {
		Map<ASALVariable, ASALSymbolicValue> result = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : valuation.entrySet()) {
			if (propsAndOutputs.contains(entry.getKey())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		
		return result;
	}
	
	private Map<ReprPort, ASALSymbolicValue> filterInputs(Map<ASALVariable, ASALSymbolicValue> valuation) {
		Map<ReprPort, ASALSymbolicValue> result = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : valuation.entrySet()) {
			if (!propsAndOutputs.contains(entry.getKey())) {
				result.put((ReprPort)entry.getKey(), entry.getValue());
			}
		}
		
		return result;
	}
	
	private Set<DecaTwoTransition> computeTransitions(DecaTwoStateConfig sourceStateConfig) throws ModelException {
		Set<DecaTwoTransition> result = new HashSet<DecaTwoTransition>();
		
		for (DecaOneTransition t : legacy.transitions) {
			if (t.getSourceVertex() == sourceStateConfig.getVertex()) {
				Set<ASALVariable> referencedInputs = new HashSet<ASALVariable>();
				
				//Find all inputs that are used to compute new values of properties/outputs:
				for (ASALVariable prop : propsAndOutputs) {
					NextStateFct.Entry e = t.getNextStateFct().get(prop);
					
					if (e != null) {
						referencedInputs.addAll(e.getValue().getReferencedVars());
					}
				}
				
				referencedInputs.removeAll(propsAndOutputs);
				
				//Sanity check that all found variables are indeed inputs:
				for (ASALVariable referencedInput : referencedInputs) {
					if (referencedInput instanceof ReprPort) {
						ReprPort rp = (ReprPort)referencedInput;
						
						if (rp.getDir() == Dir.OUT) {
							throw new Error("Should not happen!");
						}
					} else {
						throw new Error("Should not happen!");
					}
				}
				
				//Determine possible values per referenced input: 
				Map<ASALVariable, Set<ASALSymbolicValue>> inputPvs = new HashMap<ASALVariable, Set<ASALSymbolicValue>>();
				
				for (ASALVariable referencedInput : referencedInputs) {
					Set<ASALSymbolicValue> pvs = new HashSet<ASALSymbolicValue>();
					
					for (JType pv : scope.getPossibleValues(referencedInput)) {
						pvs.add(ASALSymbolicValue.from(pv));
					}
					
					inputPvs.put(referencedInput, pvs);
				}
				
				//Compute possible permutations of possible values per input:
				Map<DecaTwoStateConfig, Set<Map<ASALVariable, ASALSymbolicValue>>> inputValsPerNewStateConfig = new HashMap<DecaTwoStateConfig, Set<Map<ASALVariable, ASALSymbolicValue>>>();
				
				for (Map<ASALVariable, ASALSymbolicValue> inputVal : HashMaps.allCombinations(inputPvs)) {
					Map<ASALVariable, ASALSymbolicValue> newValuePerProp = new HashMap<ASALVariable, ASALSymbolicValue>();
					newValuePerProp.putAll(sourceStateConfig.getValuation());
					
					NextStateFct f = new NextStateFct(t.getNextStateFct());
					f.substitute(inputVal);
					f.substitute(sourceStateConfig.getValuation());
					
					for (ASALVariable prop : propsAndOutputs) {
						NextStateFct.Entry e = f.get(prop);
						
						if (e != null) {
							newValuePerProp.put(prop, e.getValue());
						}
					}
					
//					Map<ReprPort, ASALSymbolicValue> valuePerPort = new HashMap<ReprPort, ASALSymbolicValue>();
					
					//Sanity check that all properties have a closed value:
					for (Map.Entry<ASALVariable, ASALSymbolicValue> e : newValuePerProp.entrySet()) {
						if (e.getValue().getReferencedVars().size() > 0) {
							throw new Error("Should not happen!");
						}
						
//						if (e.getKey() instanceof ReprPort) {
//							ReprPort rp = (ReprPort)e.getKey();
//							
//							if (rp.getDir() != Dir.OUT) {
//								throw new Error("Should not happen!");
//							}
//							
//							if (JPulse.class.isAssignableFrom(rp.getType())) {
//								if (e.getValue().toBoolean()) {
//									if (e.getValue().getReferencedVars().size() > 0) {
//										throw new Error("Should not happen!");
//									}
//									
//									for (ReprPort drp : rp.getDataPorts()) {
//										if (newValuePerProp.get(drp).getReferencedVars().size() > 0) {
//											throw new Error("Should not happen!");
//										}
//									}
//								}
//							} else {
//								if (rp.getPulsePort() == null) {
//									if (e.getValue().getReferencedVars().size() > 0) {
//										throw new Error("Should not happen!");
//									}
//								}
//							}
//							
//							valuePerPort.put(rp, e.getValue());
//						} else {
//							if (e.getValue().getReferencedVars().size() > 0) {
//								throw new Error("Should not happen!");
//							}
//						}
					}
					
					HashMaps.inject(inputValsPerNewStateConfig, new DecaTwoStateConfig(t.getTargetVertex(), newValuePerProp), inputVal);
				}
				
				ASALSymbolicValue guardRelToState = t.getGuard().substitute(sourceStateConfig.getValuation());
				
				for (Map.Entry<DecaTwoStateConfig, Set<Map<ASALVariable, ASALSymbolicValue>>> entry : inputValsPerNewStateConfig.entrySet()) {
					ASALSymbolicValue newGuard = ASALSymbolicValue.FALSE;
					
					for (Map<ASALVariable, ASALSymbolicValue> inputVal : entry.getValue()) {
						ASALSymbolicValue g = guardRelToState;
						
						for (Map.Entry<ASALVariable, ASALSymbolicValue> p : inputVal.entrySet()) {
							ASALSymbolicValue restriction = ASALSymbolicValue.eq(ASALSymbolicValue.from(p.getKey()), p.getValue());
							g = ASALSymbolicValue.and(g, restriction);
						}
						
						newGuard = ASALSymbolicValue.or(newGuard, g);
					}
					
					newGuard = ASALSymbolicValue.simplify(newGuard, pvsPerInput);
					
					if (newGuard.couldBeTrue()) {
						result.add(new DecaTwoTransition(this, t, newGuard, sourceStateConfig, entry.getKey()));
					}
				}
			}
		}
		
		return result;
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-two-" + scope.getName() + ".gv");
//			printGraphvizFile(ps, extractStoppedTrs());
			printGraphvizFile(ps, transitions);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	private Set<DecaTwoTransition> extractStoppedTrs() {
		Set<DecaTwoTransition> result = new HashSet<DecaTwoTransition>();
		
		for (DecaTwoTransition t : transitions) {
			if (t.getSourceStateConfig().getClzsStr().contains("STOPPED")) {
				if (t.getSourceStateConfig().getClzsStr().contains("ALL_LEFT")) {
					result.add(t);
				}
			}
		}
		
		return result;
	}
	
	public void printGraphvizFile(String filename, Collection<DecaTwoTransition> trs) {
		try {
			PrintStream ps = new PrintStream(filename);
			printGraphvizFile(ps, trs);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out, Collection<DecaTwoTransition> trs) {
		Map<DecaTwoVertex, String> namePerVertex = new HashMap<DecaTwoVertex, String>();
		Map<DecaTwoTransition, String> namePerTransition = new HashMap<DecaTwoTransition, String>();
		Map<DecaTwoTransition, String> colorPerTransition = new HashMap<DecaTwoTransition, String>();
		Set<DecaTwoVertex> vtxs = new HashSet<DecaTwoVertex>();
		
		for (DecaTwoTransition t : trs) {
			vtxs.add(vertices.get(t.getSourceStateConfig()));
			vtxs.add(vertices.get(t.getTargetStateConfig()));
		}
		
		for (DecaTwoVertex v : vtxs) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaTwoTransition t : trs) {
			namePerTransition.put(t, "T" + namePerTransition.size());
			colorPerTransition.put(t, Dot.getRandomColor());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		
		{
			String s = "(initial)";
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : initialVertex.getStateConfig().getValuation().entrySet()) {
				s += "\\n" + entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\"");
			}
			
			out.println("\tI0 [label=\"" + s + "\", shape=circle, fontsize=10];");
		}
		
		for (DecaTwoVertex v : vtxs) {
			String s = v.getName();
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : v.getStateConfig().getValuation().entrySet()) {
				if (!JPulse.class.isAssignableFrom(entry.getKey().getType()) || entry.getValue().toBoolean()) {
					s += "\\n" + entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\"");
				}
			}
			
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
			
			boolean foundSelfloop = false;
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> e : v.getStateConfig().getValuation().entrySet()) {
				if (JPulse.class.isAssignableFrom(e.getKey().getType()) && e.getValue().toBoolean()) {
					foundSelfloop = true; //Resetting pulses prevents a selfloop from happening...
					break;
				}
			}
			
			if (!foundSelfloop) {
				for (DecaTwoTransition t : transitions) {
					if (vertices.get(t.getSourceStateConfig()) == v && vertices.get(t.getTargetStateConfig()) == v) {
						foundSelfloop = true;
						break;
					}
				}
			}
			
			if (!foundSelfloop) {
				out.println("\t" + namePerVertex.get(v) + "X [label=\"No self loop!\", shape=ellipse];");
				out.println("\t" + namePerVertex.get(v) + " -> " + namePerVertex.get(v) + "X [style=dashed];");
			}
		}
		
		for (DecaTwoTransition t : trs) {
			String s = "[ " + Texts._break(t.getGuard().toString().replace("\"", "\\\""), "\\n", 70) + " ]";
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, color=\"" + colorPerTransition.get(t) + "\", fontsize=10, fontcolor=\"" + colorPerTransition.get(t) + "\"];");
		}
		
		for (DecaTwoTransition t : trs) {
			String t_name = namePerTransition.get(t);
			String sv_name = namePerVertex.get(vertices.get(t.getSourceStateConfig()));
			String tv_name = namePerVertex.get(vertices.get(t.getTargetStateConfig()));
			
			if (sv_name.equals(tv_name)) {
				out.println("\t" + sv_name + " -> " + t_name + " [color=\"" + colorPerTransition.get(t) + "\", style=dashed, dir=both];");
			} else {
				out.println("\t" + sv_name + " -> " + t_name + " [color=\"" + colorPerTransition.get(t) + "\"];");
				out.println("\t" + t_name + " -> " + tv_name + " [color=\"" + colorPerTransition.get(t) + "\"];");
			}
		}
		
		out.println("\tI0 -> " + namePerVertex.get(initialVertex) + ";");
		out.println("}");
	}
}








