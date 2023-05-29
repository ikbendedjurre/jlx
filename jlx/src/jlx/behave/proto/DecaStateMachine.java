package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.common.reflection.ClassReflectionException;
import jlx.models.UnifyingBlock.*;
import jlx.utils.*;

/**
 * This state machine eliminates transitions of which the guard is known to be FALSE.
 * 
 * Timeout events in transitions are converted to (helper) input pulse ports.
 * We preserve the timeout EXPRESSIONS (i.e. integers) that correspond with those ports.
 * 
 * Idle self-loops are are added to each state, which are enabled when no other transition is enabled.
 * These also model a situation in which a timeout transition has not happened yet.
 * They change data, namely to reset pulses.
 * Idle self-loops are flagged with `isIdle()'.
 * 
 * (Currently disabled:)
 * For transitions, we populate the "entry rewrite functions", which
 * map the value of an input port to another value based on the future use of that input port.
 */
public class DecaStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final DecaVertex initialVertex;
	public final Set<DecaVertex> vertices;
	public final Set<DecaTransition> transitions;
	public final Map<ASALVariable, ASALSymbolicValue> initialization;
//	public final Map<ASALPort, ASALPort> timePerTimeoutPort;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final NonaStateMachine legacy;
	
	public DecaStateMachine(NonaStateMachine source) throws ClassReflectionException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		initialization = new HashMap<ASALVariable, ASALSymbolicValue>(source.initialization);
//		timePerTimeoutPort = new HashMap<ASALPort, ASALPort>();
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>();
		
		Map<NonaVertex, DecaVertex> newVertexPerOldVertex = new HashMap<NonaVertex, DecaVertex>();
		
		initialVertex = new DecaVertex(source.initialVertex);
		newVertexPerOldVertex.put(source.initialVertex, initialVertex);
		vertices = new HashSet<DecaVertex>();
		vertices.add(initialVertex);
		transitions = new HashSet<DecaTransition>();
		
		Set<DecaVertex> fringe = new HashSet<DecaVertex>();
		Set<DecaVertex> newFringe = new HashSet<DecaVertex>();
		fringe.add(initialVertex);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaVertex v : fringe) {
				for (DecaTransition t : computeTransitions(v, newVertexPerOldVertex)) {
					transitions.add(t);
					
					if (vertices.add(t.getTargetVertex())) {
						newFringe.add(t.getTargetVertex());
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		//computeEntryRewriteFcts();
	}
	
	private Set<DecaTransition> computeTransitions(DecaVertex sourceVertex, Map<NonaVertex, DecaVertex> newVertexPerOldVertex) {
		Set<DecaTransition> result = new HashSet<DecaTransition>();
		//Set<DecaTransition> timeoutTransitions = new HashSet<DecaTransition>();
		//Set<DecaTransition> nonTimeoutTransitions = new HashSet<DecaTransition>();
		ASALSymbolicValue nothingHappensGuard = ASALSymbolicValue.TRUE;
		
		for (NonaTransition t : legacy.transitions) {
			if (t.getSourceVertex() == sourceVertex.getLegacy()) {
				DecaVertex targetVertex = newVertexPerOldVertex.get(t.getTargetVertex());
				
				if (targetVertex == null) {
					targetVertex = new DecaVertex(t.getTargetVertex());
					newVertexPerOldVertex.put(t.getTargetVertex(), targetVertex);
				}
				
				ASALSymbolicValue guard = t.getGuard();
				
				if (guard.couldBeTrue()) {
					ASALSymbolicValue newGuard = guard;
					
					if (t.getTimeoutEvent() != null) {
						ASALPort s = t.getTimeoutEvent().getResolvedDurationPort();
						ASALPort timeoutPort = timeoutPortPerDurationPort.get(s);
						
						if (timeoutPort == null) {
							String prefix = "T" + (1000 + timeoutPortPerDurationPort.size()) + "_";
							ASALPortFields fields = new ASALPortFields();
							fields.dir = Dir.IN;
							fields.priority = s.getPriority();
							fields.initialValue = JPulse.FALSE;
							fields.verificationActions = s.getVerificationActions();
							fields.executionTime = s.getExecutionTime();
							timeoutPort = (ReprPort)scope.generateHelperPort(prefix + s.getName(), fields);
							timeoutPortPerDurationPort.put(s, timeoutPort);
							initialization.put(timeoutPort, ASALSymbolicValue.FALSE);
						}
						
						newGuard = ASALSymbolicValue.and(newGuard, ASALSymbolicValue.from(timeoutPort));
					}
					
					nothingHappensGuard = ASALSymbolicValue.and(nothingHappensGuard, newGuard.negate());
					DecaTransition t2 = new DecaTransition(null, t, sourceVertex, targetVertex, newGuard, t.getNextStateFct(), false);
					result.add(t2); //TODO avoid duplicates?
				}
			}
		}
		
		if (nothingHappensGuard.couldBeTrue()) {
			NextStateFct nextStateFct = new NextStateFct();
			
			for (ASALVariable w : scope.getVariablePerName().values()) {
				if (w instanceof ReprPort) {
					ReprPort rp = (ReprPort)w;
					
					if (rp.getType().equals(JPulse.class)) {
						if (rp.getDir() == Dir.IN) {
							//Inputs do not need to be reset.
						} else {
							if (!nextStateFct.containsKey(rp)) {
								nextStateFct.put(rp, ASALSymbolicValue.FALSE, "OUTPUT_RESET_NH");
								
								for (ReprPort drp : rp.getDataPorts()) {
									if (initialization.containsKey(rp)) {
										nextStateFct.put(drp, initialization.get(drp), "OUTPUT_RESET_NH");
									} else {
										nextStateFct.put(drp, ASALSymbolicValue.from(drp.getInitialValue()), "OUTPUT_RESET_NH");
									}
									
									//nextStateFct.put(drp, ASALSymbolicValue.unassigned(drp), "OUTPUT_RESET_NH");
								}
							}
						}
					} else {
						if (rp.getPulsePort() != null) {
							//DT-output => We reset DT-outputs if we reset their pulse port (see above). 
						} else {
							//D-input/output => Do not touch!
						}
					}
				} else {
					//Do not change property values.
				}
			}
			
			result.add(new DecaTransition(sourceVertex.getLegacy(), null, sourceVertex, sourceVertex, nothingHappensGuard, nextStateFct, true));
		}
		
		return result;
	}
	
//	/**
//	 * Returns transitions reachable from a starting transition.
//	 * Stops BEFORE transitions that use the specified variable as an input (when the environment has full control over its value).
//	 * Does not move BEYOND transitions that write to the specified variable (after which the current value is "lost").
//	 * This function is used to determine "until when" the value of the specified variable could be relevant.
//	 */
//	private Set<DecaTransition> getReachableTransitionsUntilWritten(DecaVertex start, ASALVariable v, Map<DecaTransition, Set<DecaTransition>> succPerTransition) {
//		Set<DecaTransition> beenHere = new HashSet<DecaTransition>();
//		Set<DecaTransition> fringe = new HashSet<DecaTransition>();
//		Set<DecaTransition> newFringe = new HashSet<DecaTransition>();
//		
//		for (DecaTransition succ : transitions) {
//			if (succ.getSourceVertex() == start) {
////				if (!succ.getInputVars().contains(v)) {
//					beenHere.add(succ);
//					
//					if (!succ.getNextStateFct().containsKey(v)) {
//						fringe.add(succ);
//					}
////				}
//			}
//		}
//		
//		while (fringe.size() > 0) {
//			newFringe.clear();
//			
//			for (DecaTransition f : fringe) {
//				for (DecaTransition succ : succPerTransition.get(f)) {
////					if (!succ.getInputVars().contains(v)) {
//						if (beenHere.add(succ)) {
//							if (!succ.getNextStateFct().containsKey(v)) {
//								newFringe.add(succ);
//							}
//						}
////					}
//				}
//			}
//			
//			fringe.clear();
//			fringe.addAll(newFringe);
//		}
//		
//		return beenHere;
//	}
//	
//	private void computeEntryRewriteFcts() {
//		Map<DecaTransition, Set<DecaTransition>> succPerTransition = getSuccPerTransition();
//		
//		List<ASALSymbolicValue> orderedValues = new ArrayList<ASALSymbolicValue>();
//		
//		for (ASALVariable v : scope.getVariablePerName().values()) {
//			for (JType t : scope.getPossibleValues(v)) {
//				orderedValues.add(ASALSymbolicValue.from(scope, t));
//			}
//		}
//		
//		//throw new Error("got here");
//		
//		int vi = 1;
//		
//		for (DecaVertex vtx : vertices) {
//			System.out.println("vertex " + vi + " / " + vertices.size());
//			
//			for (ASALVariable v : scope.getVariablePerName().values()) {
//				if (canBeRewritten(v) && scope.getPossibleValues(v).size() > 1) {
//					VarValueEquivDistr distr = new VarValueEquivDistr();
//					Set<DecaTransition> ts = getReachableTransitionsUntilWritten(vtx, v, succPerTransition);
//					int ti = 1;
//					
//					for (DecaTransition x : ts) {
//						System.out.println("vertex " + vi + " / " + vertices.size() + "; transition " + ti + " / " + ts.size());
//						distr.add(new VarValueEquivDistr(scope, v, x.getGuard(), x.getEqns(), ""));
//						ti++;
//					}
//					
//					Map<ASALSymbolicValue, ASALSymbolicValue> vpv = new HashMap<ASALSymbolicValue, ASALSymbolicValue>();
//					vpv.putAll(distr.computeRemapFct(scope, v, orderedValues));
//					vtx.addToEntryRewriteFct(v, vpv);
//				}
//			}
//			
//			vi++;
//		}
//		
//		//throw new Error("got here");
//	}
//	
//	private static boolean canBeRewritten(ASALVariable v) {
//		//Properties can be rewritten, based on how they are used in the future:
//		if (v instanceof ReprProperty) {
//			//return false; //Actually no, because we cannot rely on having finite values for properties . . .
//			return true; //We can do this because we removed SMI, which defines a JInt property TODO why is SMI working?!
//		}
//		
//		if (v instanceof ReprPort) {
//			ReprPort rp = (ReprPort)v;
//			
//			if (rp.getDir() == Dir.IN) {
//				//Input T ports are always set to false after having been read:
//				if (JPulse.class.equals(rp.getType())) {
//					return false;
//				}
//				
//				//Input DT ports can be rewritten, based on how they are used in the future, because
//				//whether their value matters is determined primarily by their pulse port, anyway.
//				
//				
//				//Input D ports can be rewritten, based on how they are used in the future.
//				return true;
//			}
//			
//			//Even if the model does no longer need an output value, the user may want to inspect it.
//			//So always keep output values:
//			if (rp.getDir() == Dir.OUT) {
//				return false;
//			}
//		}
//		
//		throw new Error("Should not happen!");
//	}
//	
//	private Map<DecaTransition, Set<DecaTransition>> getSuccPerTransition() {
//		Map<DecaVertex, Set<DecaTransition>> transitionsPerSourceVertex = new HashMap<DecaVertex, Set<DecaTransition>>();
//		
//		for (DecaVertex v : vertices) {
//			transitionsPerSourceVertex.put(v, new HashSet<DecaTransition>());
//		}
//		
//		for (DecaTransition t : transitions) {
//			transitionsPerSourceVertex.get(t.getSourceVertex()).add(t);
//		}
//		
//		Map<DecaTransition, Set<DecaTransition>> result = new HashMap<DecaTransition, Set<DecaTransition>>();
//		
//		for (DecaTransition t : transitions) {
//			result.put(t, transitionsPerSourceVertex.get(t.getTargetVertex()));
//		}
//		
//		return result;
//	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<DecaVertex, String> namePerVertex = new HashMap<DecaVertex, String>();
		Map<DecaTransition, String> namePerTransition = new HashMap<DecaTransition, String>();
		Map<DecaTransition, String> colorPerTransition = new HashMap<DecaTransition, String>();
		
		for (DecaVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaTransition t : transitions) {
			namePerTransition.put(t, "T" + namePerTransition.size());
			colorPerTransition.put(t, Dot.getRandomColor());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		
		{
			String s = "(initial)";
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : initialization.entrySet()) {
				s += "\\n" + entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\"");
			}
			
			out.println("\tI0 [label=\"" + s + "\", shape=circle, fontsize=10];");
		}
		
		for (DecaVertex v : vertices) {
			String s = Texts.concat(v.getLegacy().getLegacy().getStateConfig().states, " + ", (x) -> { return x.getSysmlClz().getSimpleName(); });
			//out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\\nEntry / " + v.getEntryRewriteFctStr(scope, LOD.DOT_MIN) + "\", shape=ellipse];");
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
		}
		
		for (DecaTransition t : transitions) {
			String s = "[ " + Texts._break(t.getGuard().toString().replace("\"", "\\\""), "\\n", 70) + " ]";
			
			for (ASALVariable v : t.getNextStateFct().getVariables()) {
				s += "\\n" + TextOptions.GRAPHVIZ_FULL.id(v.getName()) + " := " + t.getNextStateFct().get(v).getValue().toString().replace("\"", "\\\"") + " //" + t.getNextStateFct().get(v).getDebugText();
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, fontsize=10, fontcolor=\"" + colorPerTransition.get(t) + "\", color=\"" + colorPerTransition.get(t) + "\"];");
		}
		
		for (DecaTransition t : transitions) {
			String t_name = namePerTransition.get(t);
			String sv_name = namePerVertex.get(t.getSourceVertex());
			String tv_name = namePerVertex.get(t.getTargetVertex());
			
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

