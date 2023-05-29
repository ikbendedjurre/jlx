package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.*;
import jlx.asal.vars.ASALPort;
import jlx.asal.vars.ASALVariable;
import jlx.behave.StateMachine;
import jlx.common.reflection.ClassReflectionException;
import jlx.common.reflection.ModelException;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * This state machine is simplified with regard to PROPERTIES:
 *  1. Identity assignments are removed.
 *  2. Properties that are never read are removed.
 *  3. Properties are restricted to values that result in a different outcome SOMEWHERE in the state machine.
 *     (We currently do not do smarter control flow analysis.)
 *  4. Properties that only ever have 1 value are removed.
 */
public class DecaOneStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<DecaOneVertex> vertices;
	public final DecaOneVertex initialVertex;
	public final Map<ASALVariable, ASALSymbolicValue> initialization;
//	public final Map<ReprPort, ASALSymbolicValue> timePerTimeoutPort;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final Set<DecaOneTransition> transitions;
	public final DecbStateMachine legacy;
	
	public DecaOneStateMachine(DecbStateMachine source) throws ClassReflectionException, ModelException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		//timePerTimeoutPort = new HashMap<ReprPort, ASALSymbolicValue>(source.timePerTimeoutPort);
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(source.timeoutPortPerDurationPort);
		
		Map<DecbVertex, DecaOneVertex> newVertexPerOldVertex = new HashMap<DecbVertex, DecaOneVertex>();
		vertices = new HashSet<DecaOneVertex>();
		
		for (DecbVertex oldVertex : source.vertices) {
			DecaOneVertex newVertex = new DecaOneVertex(oldVertex);
			vertices.add(newVertex);
			newVertexPerOldVertex.put(oldVertex, newVertex);
		}
		
		initialVertex = newVertexPerOldVertex.get(source.initialVertex);
		initialization = new HashMap<ASALVariable, ASALSymbolicValue>(source.initialization);
		transitions = new HashSet<DecaOneTransition>();
		
		for (DecbTransition t : source.transitions) {
			transitions.add(new DecaOneTransition(t, newVertexPerOldVertex));
		}
		
		boolean done = false;
		
		while (!done) {
			done = true;
			
			System.out.println("================================ ITERATION ===============================");
			
			if (removeIdentityAssignments()) {
				System.out.println("1");
				done = false;
			}
			
			if (removeConstantProperties()) {
				System.out.println("2");
				done = false;
			}
			
			if (removeUnreadProps()) {
				System.out.println("3");
				done = false;
			}
			
//			if (updateRestrictions()) {
//				System.out.println("4");
//				//applyRestrictions();
//				done = false;
//			}
		}
		
//		for (DecaOneVertex v : vertices) {
//			boolean foundSelfloop = false;
//			
//			for (DecaOneTransition t : transitions) {
//				if (t.getSourceVertex() == v && t.getTargetVertex() == v) {
//					foundSelfloop = true;
//					break;
//				}
//			}
//			
//			if (!foundSelfloop) {
//				printGraphvizFile();
//				
//				throw new Error("No self-loop?!");
//			}
//		}
	}
	
	/**
	 * Returns true if ANY assignment was removed.
	 * If called twice in a row, it cannot return TRUE the second time.
	 */
	private boolean removeIdentityAssignments() {
		boolean result = false;
		
		for (DecaOneTransition t : transitions) {
			if (t.removeIdentityAssignments()) {
				result = true;
			}
		}
		
		return result;
	}
	
	private static boolean isConstant(ASALSymbolicValue sv) {
		if (sv instanceof ASALTrueSv) {
			return true;
		}
		
		if (sv instanceof ASALFalseSv) {
			return true;
		}
		
		if (sv instanceof ASALLitSv) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if ANY property was removed.
	 * If called twice in a row, it cannot return TRUE the second time.
	 */
	private boolean removeConstantProperties() {
		Map<ASALVariable, ASALSymbolicValue> valuePerCandidate = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (ASALVariable v : scope.getVariablePerName().values()) {
			if (!(v instanceof ReprPort)) {
				if (initialization.containsKey(v)) {
					valuePerCandidate.put(v, initialization.get(v));
				} else {
					valuePerCandidate.put(v, ASALSymbolicValue.from(v.getInitialValue()));
				}
			}
		}
		
		Set<ASALVariable> rejected = new HashSet<ASALVariable>();
		
		for (DecaOneTransition t : transitions) {
			for (NextStateFct.Entry e : t.getNextStateFct().getEntries()) {
				if (!rejected.contains(e.getVariable())) {
					if (e.getVariable() instanceof ReprPort) {
						//Also do not replace ports:
						rejected.add(e.getVariable());
					} else {
						if (isConstant(e.getValue())) {
							ASALSymbolicValue lit = valuePerCandidate.get(e.getVariable());
//							System.out.println("litvar = " + e.getVariable().getName());
//							System.out.println("litvar.expr = " + e.getValue().toString());
							
							if (!lit.equals(e.getValue())) {
								rejected.add(e.getVariable());
							}
						} else {
							//A non-constant expression is assigned to the property, so do not replace it:
							rejected.add(e.getVariable());
						}
					}
				}
			}
		}
		
		//Substitute variables, expect those that were marked 'rejected':
		Map<ASALVariable, ASALSymbolicValue> subst = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> e : valuePerCandidate.entrySet()) {
			if (!rejected.contains(e.getKey())) {
				subst.put(e.getKey(), e.getValue());
			}
		}
		
		if (subst.size() > 0) {
			boolean result = false;
			
			for (ASALVariable v : subst.keySet()) {
				if (initialization.remove(v) != null) {
					result = true;
				}
			}
			
			Set<DecaOneTransition> newTransitions = new HashSet<DecaOneTransition>();
			
			for (DecaOneTransition t : transitions) {
				if (t.substitute(subst)) {
					result = true;
				}
				
				if (t.getGuard().couldBeTrue()) {
					if (t.removeWrittenVars(subst.keySet())) {
						result = true;
					}
					
					newTransitions.add(t);
				} else {
					result = true;
				}
			}
			
			transitions.clear();
			transitions.addAll(newTransitions);
			return result;
		}
		
		return false;
	}
	
	/**
	 * Returns true if ANY property was removed.
	 * If called twice in a row, it cannot return TRUE the second time.
	 */
	private boolean removeUnreadProps() {
		//Determine all properties:
		Set<ASALVariable> properties = new HashSet<ASALVariable>();
		
		for (DecaOneTransition t : transitions) {
			for (ASALVariable v : t.getNextStateFct().getVariables()) {
				if (!(v instanceof ReprPort)) {
					properties.add(v);
				}
			}
		}
		
		//Determine which properties are read:
		Set<ASALVariable> readProps = new HashSet<ASALVariable>();
		readProps.addAll(properties);
		
		while (true) {
			Set<ASALVariable> readVars = new HashSet<ASALVariable>();
			
			for (DecaOneTransition t : transitions) {
				readVars.addAll(t.getGuard().getReferencedVars());
				readVars.addAll(t.getNextStateFct().getReadVariables());
			}
			
			readVars.retainAll(properties);
			
			if (!readProps.containsAll(readVars)) {
				throw new Error("Should not happen!");
			}
			
			if (readVars.size() == readProps.size()) {
				break;
			}
			
			readProps = readVars;
		}
		
		Set<ASALVariable> unreadProps = new HashSet<ASALVariable>();
		unreadProps.addAll(properties);
		unreadProps.removeAll(readProps);
		
		boolean result = false;
		
		for (ASALVariable v : unreadProps) {
			if (initialization.remove(v) != null) {
				result = true;
			}
		}
		
		for (DecaOneTransition t : transitions) {
			if (t.removeWrittenVars(unreadProps)) {
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Returns true if the restrictions changed.
	 * If called twice in a row, it cannot return TRUE the second time.
	 */
	private boolean updateRestrictions() {
		Set<ASALVariable> restrictedVars = new HashSet<ASALVariable>();
		
		for (ASALVariable v : scope.getVariablePerName().values()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (DecbStateMachine.ENABLED) {
					if (rp.getDir() == Dir.IN) {
						if (rp.getPulsePort() == null && !rp.getType().equals(JPulse.class)) { // --> D-input
							restrictedVars.add(rp);
						}
					}
				}
			} else {
				restrictedVars.add(v);
			}
		}
		
		Map<ASALVariable, Set<ASALSymbolicValue>> exprsPerVar = new HashMap<ASALVariable, Set<ASALSymbolicValue>>();
		
		for (ASALVariable v : restrictedVars) {
			exprsPerVar.put(v, new HashSet<ASALSymbolicValue>());
		}
		
		for (DecaOneTransition t : transitions) {
			for (ASALVariable v : t.getGuard().getReferencedVars()) {
				if (restrictedVars.contains(v)) {
					exprsPerVar.get(v).add(t.getGuard());
				}
			}
			
			for (NextStateFct.Entry e : t.getNextStateFct().getEntries()) {
				for (ASALVariable v : e.getValue().getReferencedVars()) {
					if (restrictedVars.contains(v)) {
//						ASALSymbolicValue unchangableExpr = ASALSymbolicValue.from(scope, v.getType());
//						exprsPerVar.get(v).add(ASALSymbolicValue.ite(t.getGuard(), e.getValue(), unchangableExpr));
						exprsPerVar.get(v).add(e.getValue());
					}
				}
			}
		}
		
		Map<ASALVariable, Set<ASALSymbolicValue>> pvsPerVar = new HashMap<ASALVariable, Set<ASALSymbolicValue>>();
		
		for (ASALVariable v : scope.getVariablePerName().values()) {
			pvsPerVar.put(v, ASALSymbolicValue.from(scope.getPossibleValues(v)));
		}
		
		Set<ASALSymbolicValue> allPropertyValues = new HashSet<ASALSymbolicValue>();
		
		for (ASALVariable v : restrictedVars) {
			allPropertyValues.addAll(pvsPerVar.get(v));
		}
		
		List<ASALSymbolicValue> orderedPropertyValues = new ArrayList<ASALSymbolicValue>();
		orderedPropertyValues.addAll(allPropertyValues);
		
//		for (Map.Entry<ASALVariable, Set<ASALSymbolicValue>> entry : exprsPerVar.entrySet()) {
//			
//			for (ASALSymbolicValue expr : exprsPerVar.get(entry.getKey())) {
//				Map<ASALVariable, Set<ASALSymbolicValue>> pvsPerRefVar = new HashMap<ASALVariable, Set<ASALSymbolicValue>>();
//				
//				for (ASALVariable v : expr.getReferencedVars()) {
//					pvsPerRefVar.put(v, pvsPerVar.get(v));
//				}
//				
//				for (Map<ASALVariable, ASALSymbolicValue> perm : Permutations.getPermutations(pvsPerRefVar, false)) {
//					d.addOutcome(perm.get(entry.getKey()), expr.substitute(perm));
//				}
//			}
//		}
		
		Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> newRestrPerVar = new HashMap<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>>();
		
		for (Map.Entry<ASALVariable, Set<ASALSymbolicValue>> entry : exprsPerVar.entrySet()) {
			Set<Set<ASALSymbolicValue>> groupedSources = new HashSet<Set<ASALSymbolicValue>>();
			
			for (ASALSymbolicValue expr : exprsPerVar.get(entry.getKey())) {
				EquivDistr<ASALSymbolicValue, ASALSymbolicValue> d = new EquivDistr<ASALSymbolicValue, ASALSymbolicValue>();
				Map<ASALVariable, Set<ASALSymbolicValue>> pvsPerRefVar = new HashMap<ASALVariable, Set<ASALSymbolicValue>>();
				
				for (ASALVariable v : expr.getReferencedVars()) {
					pvsPerRefVar.put(v, pvsPerVar.get(v));
				}
				
				for (Map<ASALVariable, ASALSymbolicValue> perm : HashMaps.allCombinations(pvsPerRefVar)) {
					d.addOutcome(perm.get(entry.getKey()), expr.substitute(perm));
				}
				
				groupedSources.addAll(d.getSourceEqClzs());
			}
			
			Set<Set<ASALSymbolicValue>> xs = EquivDistr.getSourceEqClzs(groupedSources);
			Map<ASALSymbolicValue, ASALSymbolicValue> m = new HashMap<ASALSymbolicValue, ASALSymbolicValue>();
			
			for (ASALSymbolicValue pv : pvsPerVar.get(entry.getKey())) {
				boolean found = false;
				
				for (Set<ASALSymbolicValue> evs : xs) {
					if (evs.contains(pv)) {
						m.put(pv, getFirstValue(evs, orderedPropertyValues));
						found = true;
						break;
					}
				}
				
				if (!found) {
					m.put(pv, pv);
				}
			}
			
			newRestrPerVar.put(entry.getKey(), m);
		}
		
		boolean result = false;
		
		for (DecaOneTransition t : transitions) {
			if (t.applyRestr(newRestrPerVar)) {
				result = true;
			}
		}
		
		for (ASALVariable v : newRestrPerVar.keySet()) {
			if (v.getName().equals("H_D21_S_SCI_EfeS_Gen_SR_State")) {
				System.out.println("Restriction on " + v.getName() + " now is:");
				
				for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> e : newRestrPerVar.get(v).entrySet()) {
					System.out.println("\t" + e.getKey() + " -> " + e.getValue());
				}
				
				System.out.println("pvsPerVar.get(v).size() = " + pvsPerVar.get(v).size());
				System.out.println("exprsPerVar.get(v).size() = " + exprsPerVar.get(v).size());
			}
		}
		
//		if (!newRestrPerVar.equals(restrPerVar)) {
//			if (!restrPerVar.keySet().equals(newRestrPerVar.keySet())) {
//				throw new Error("Should not happen!");
//			}
//			
//			for (ASALVariable v : newRestrPerVar.keySet()) {
//				if (!restrPerVar.get(v).values().containsAll(newRestrPerVar.get(v).values())) {
//					System.out.println("Restriction of " + v.getName() + " was:");
//					
//					for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> e : restrPerVar.get(v).entrySet()) {
//						System.out.println("\t" + e.getKey() + " -> " + e.getValue());
//					}
//					
//					System.out.println("Now is:");
//					
//					for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> e : newRestrPerVar.get(v).entrySet()) {
//						System.out.println("\t" + e.getKey() + " -> " + e.getValue());
//					}
//					
//					throw new Error("Should not happen!");
//				}
//			}
//			
//			restrPerVar.clear();
//			restrPerVar.putAll(newRestrPerVar);
//			return true;
//		}
		
		return result;
	}
	
//	private Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> createInitialRestrPerProp() {
//		Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> result = new HashMap<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>>();
//		
//		for (ASALVariable v : scope.getVariablePerName().values()) {
//			boolean add = false;
//			
//			if (v instanceof ReprPort) {
//				ReprPort rp = (ReprPort)v;
//				
//				if (rp.getDir() == Dir.IN) {
//					if (rp.getPulsePort() == null && !rp.getType().equals(JPulse.class)) {
//						add = true;
//					}
//				}
//			} else {
//				add = true;
//			}
//			
//			if (add) {
//				Map<ASALSymbolicValue, ASALSymbolicValue> m = new HashMap<ASALSymbolicValue, ASALSymbolicValue>();
//				
//				for (ASALSymbolicValue pv : ASALSymbolicValue.from(scope, scope.getPossibleValues(v))) {
//					m.put(pv, pv);
//				}
//				
//				result.put(v, m);
//			}
//		}
//		
//		return result;
//	}
//	
//	/**
//	 * Applies the variable restrictions for PROPERTIES (not for D-inputs).
//	 */
//	private void applyRestrictions() {
//		for (DecaOneTransition t : transitions) {
//			t.applyRestrictions(restrPerVar);
//		}
//	}
	
	private static ASALSymbolicValue getFirstValue(Set<ASALSymbolicValue> eqClz, List<ASALSymbolicValue> orderedValues) {
		for (int index = 0; index < orderedValues.size(); index++) {
			if (eqClz.contains(orderedValues.get(index))) {
				return orderedValues.get(index);
			}
		}
		
		throw new Error("Should not happen!");
	}
	
	
//	private Set<DecaOneCStateConfig> computeTransitions(DecaOneCStateConfig srcStateConfig) throws ModelException {
//		Set<DecaOneCTransition> result = new HashSet<DecaOneCTransition>();
//		
//		for (DecaTransition t : legacy.transitions) {
//			
//			
//			if (t.getSourceVertex() == sourceStateConfig.getVertex()) {
//				Set<ASALVariable> referencedInputs = new HashSet<ASALVariable>();
//				
//				//Find all inputs that are used to compute new values of properties:
//				for (ASALVariable prop : propsAndOutputs) {
//					NextStateFct.Entry e = t.getNextStateFct().get(prop);
//					
//					if (e != null) {
//						referencedInputs.addAll(e.getValue().getReferencedVars());
//					}
//				}
//				
//				referencedInputs.removeAll(propsAndOutputs);
//				
//				//Sanity check that all found variables are indeed inputs:
//				for (ASALVariable referencedInput : referencedInputs) {
//					if (referencedInput instanceof ReprPort) {
//						ReprPort rp = (ReprPort)referencedInput;
//						
//						if (rp.getDir() == Dir.OUT) {
//							throw new Error("Should not happen!");
//						}
//					} else {
//						throw new Error("Should not happen!");
//					}
//				}
//				
//				//Determine possible values per referenced input: 
//				Map<ASALVariable, Set<ASALSymbolicValue>> inputPvs = new HashMap<ASALVariable, Set<ASALSymbolicValue>>();
//				
//				for (ASALVariable referencedInput : referencedInputs) {
//					Set<ASALSymbolicValue> pvs = new HashSet<ASALSymbolicValue>();
//					
//					for (JType pv : scope.getPossibleValues(referencedInput)) {
//						pvs.add(ASALSymbolicValue.from(scope, pv));
//					}
//					
//					inputPvs.put(referencedInput, pvs);
//				}
//				
//				//Compute possible permutations of possible values per input:
//				for (Map<ASALVariable, ASALSymbolicValue> perm : Permutations.getPermutations(inputPvs, false)) {
//					//New guard:
//					ASALSymbolicValue newGuard = t.getGuard().substitute(sourceStateConfig.getValuation());
//					
//					for (Map.Entry<ASALVariable, ASALSymbolicValue> p : perm.entrySet()) {
//						ASALSymbolicValue restriction = ASALSymbolicValue.eq(ASALSymbolicValue.from(p.getKey()), p.getValue());
//						newGuard = ASALSymbolicValue.and(newGuard, restriction);
//					}
//					
//					//New next state function:
//					NextStateFct f = t.getNextStateFct();
//					f = f.substitute(sourceStateConfig.getValuation());
//					f = f.substitute(perm);
//					
//					//New state configuration:
//					Map<ASALVariable, ASALSymbolicValue> newValuePerProp = new HashMap<ASALVariable, ASALSymbolicValue>();
//					newValuePerProp.putAll(sourceStateConfig.getValuation());
//					
//					for (ASALVariable prop : propsAndOutputs) {
//						NextStateFct.Entry e = f.get(prop);
//						
//						if (e != null) {
//							newValuePerProp.put(prop, e.getValue());
//						}
//					}
//					
//					f.removeVariables(propsAndOutputs);
//					
//					//Sanity check that all properties have a closed value:
//					for (Map.Entry<ASALVariable, ASALSymbolicValue> e : newValuePerProp.entrySet()) {
//						if (e.getValue().getReferencedVars().size() > 0) {
//							throw new Error("Should not happen!");
//						}
//					}
//					
//					DecaOneCStateConfig targetStateConfig = new DecaOneCStateConfig(t.getTargetVertex(), newValuePerProp);
//					result.add(new DecaOneCTransition(this, t, newGuard, f, sourceStateConfig, targetStateConfig));
//				}
//			}
//		}
//		
//		return result;
//	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-one-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<DecaOneVertex, String> namePerVertex = new HashMap<DecaOneVertex, String>();
		Map<DecaOneTransition, String> namePerTransition = new HashMap<DecaOneTransition, String>();
		Map<DecaOneTransition, String> colorPerTransition = new HashMap<DecaOneTransition, String>();
		
		for (DecaOneVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaOneTransition t : transitions) {
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
		
		for (DecaOneVertex v : vertices) {
			String s = v.getNames().size() > 0 ? Texts.concat(v.getNames(), "\\n") + "\\n" : "";
			s += Texts.concat(v.getSysmlClzs(), " + ", (c) -> { return c.getSimpleName(); });
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
		}
		
		for (DecaOneTransition t : transitions) {
			String s = "[ " + Texts._break(t.getGuard().toString().replace("\"", "\\\""), "\\n", 70) + " ]";
			
			for (ASALVariable v : t.getNextStateFct().getVariables()) {
				s += "\\n" + Texts._break(TextOptions.GRAPHVIZ_FULL.id(v.getName()) + " := " + t.getNextStateFct().get(v).getValue().toString().replace("\"", "\\\"") + " //" + t.getNextStateFct().get(v).getDebugText(), "\\n", 70);
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, color=\"" + colorPerTransition.get(t) + "\", fontsize=10, fontcolor=\"" + colorPerTransition.get(t) + "\"];");
		}
		
		for (DecaOneTransition t : transitions) {
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








