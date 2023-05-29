package jlx.behave.proto;

import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.*;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.*;
import jlx.behave.*;
import jlx.common.reflection.*;
import jlx.utils.*;

/**
 * Extracts initial valuation from initial transitions, and deletes assignments from those transitions.
 * Makes the first stable state(s) the initial state(s) of the state machine;
 * consequently, the original initial state of the state machine becomes unreachable (it is "skipped").
 * The initial state of the state machine must be deterministic; if it is not, an exception is thrown!
 * 
 * We also replace call events with trigger events, which are triggered by a local input pulse port that is set to TRUE.
 * Note that this occurs in the next cycle!
 */
public class TritoStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final TritoVertex rootVertex;
	public final Set<TritoVertex> vertices;
	public final Set<TritoVertex> initialVerticesSM;
	public final ASALStatement initialization;
	public final Set<TritoTransition> transitions;
	public final DeuteroStateMachine legacy;
	
	public TritoStateMachine(DeuteroStateMachine source) throws ReflectionException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		vertices = new HashSet<TritoVertex>();
		Map<ASALOp, ASALProperty> helperPulsePortPerCallOp = new HashMap<ASALOp, ASALProperty>();
		Map<DeuteroVertex, TritoVertex> newVertexPerOldVertex = new HashMap<DeuteroVertex, TritoVertex>();
		
		for (DeuteroVertex v : source.vertices) {
			TritoVertex w = new TritoVertex(v, scope, helperPulsePortPerCallOp);
			newVertexPerOldVertex.put(v, w);
			vertices.add(w);
		}
		
		rootVertex = newVertexPerOldVertex.get(source.rootVertex);
		//initialVerticesSM = Transformations.map(source.initialVertices, newVertexPerOldVertex);
		
		//Resolve references to root/initial/parent/child vertices:
		for (Map.Entry<DeuteroVertex, TritoVertex> entry : newVertexPerOldVertex.entrySet()) {
			for (DeuteroVertex initialVertex : entry.getKey().getInitialVertices()) {
				entry.getValue().getInitialVertices().add(newVertexPerOldVertex.get(initialVertex));
			}
			
			if (entry.getKey().getParentVertex() != null) {
				entry.getValue().setParentVertex(newVertexPerOldVertex.get(entry.getKey().getParentVertex()));
			}
			
			if (entry.getKey().getRegionVertex() != null) {
				entry.getValue().setRegionVertex(newVertexPerOldVertex.get(entry.getKey().getRegionVertex()));
			}
			
			for (DeuteroVertex childVertex : entry.getKey().getChildVertices()) {
				entry.getValue().getChildVertices().add(newVertexPerOldVertex.get(childVertex));
			}
		}
		
		transitions = new HashSet<TritoTransition>();
		
		for (DeuteroTransition t : source.transitions) {
			TritoVertex sourceVertex = newVertexPerOldVertex.get(t.getSourceVertex());
			TritoVertex targetVertex = newVertexPerOldVertex.get(t.getTargetVertex());
			//transitions.add(new TritoTransition(sourceVertex, targetVertex, t.getEvent(), t.getGuard(), t.getStatement(), t.isLocal()));
			transitions.add(new TritoTransition(t, sourceVertex, targetVertex, scope, helperPulsePortPerCallOp));
		}
		
		Set<TritoVertex> firstStableStates = new HashSet<TritoVertex>();
		List<TritoTransition> initTransitions = new ArrayList<TritoTransition>();
		computeInitialBehavior(HashSets.map(source.initialVertices, newVertexPerOldVertex), firstStableStates, initTransitions);
		
		//Start at the first stable states:
		initialVerticesSM = firstStableStates;
		
		//Initialize valuation based on transitions until the first stable states:
		for(TritoTransition t : initTransitions) {
//			if(this.initialVerticesSM.contains(t.getSourceVertex())) {
//				initializeValuation(t.getStatement());
//				t.setStatement(new ASALEmptyStatement(null, null));
//			}
			
			initializeValuation(t, t.getStatement());
		}
		
		List<ASALStatement> initStats = new ArrayList<ASALStatement>();
		
		for(TritoTransition t : initTransitions) {
			initStats.add(t.getStatement());
		}
		
		initialization = ASALSeqStatement.fromList(initStats);
	}
	
	private void computeInitialBehavior(Collection<TritoVertex> initialVertices, Set<TritoVertex> firstStableStates, List<TritoTransition> initTransitions) {
		Set<TritoVertex> beenHere1 = new HashSet<TritoVertex>();
		beenHere1.addAll(initialVertices);
		
		Set<TritoVertex> fringe = new HashSet<TritoVertex>();
		Set<TritoVertex> newFringe = new HashSet<TritoVertex>();
		fringe.addAll(initialVertices);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			Set<TritoVertex> beenHere2 = new HashSet<TritoVertex>();
			
			for (TritoTransition t : transitions) {
				if (fringe.contains(t.getSourceVertex())) {
					//Vertices can only contribute a new vertex once!
					if (!beenHere2.add(t.getSourceVertex())) {
						throw new Error(instance.getClass().getCanonicalName() + " has a non-deterministic initial state!");
					}
					
					initTransitions.add(t);
					
					if (t.getTargetVertex().getOnEntry() != null) {
						initTransitions.add(t.getTargetVertex().getOnEntry());
					}
					
					if (t.getTargetVertex().getInitialVertices().size() > 0) {
						for (TritoVertex iv : t.getTargetVertex().getInitialVertices()) {
							if (!beenHere1.add(iv)) {
								throw new Error(instance.getClass().getCanonicalName() + " has circularity in its initialization!");
							}
							
							//Cannot be stable, so add immediately:
							newFringe.add(iv);
						}
					} else {
						if (State.class.isAssignableFrom(t.getTargetVertex().getSysmlClz())) {
							firstStableStates.add(t.getTargetVertex());
						} else {
							if (!beenHere1.add(t.getTargetVertex())) {
								throw new Error(instance.getClass().getCanonicalName() + " has circularity in its initialization!");
							}
							
							if (JunctionVertex.class.isAssignableFrom(t.getTargetVertex().getSysmlClz())) {
								throw new Error(instance.getClass().getCanonicalName() + " has junctions in its initialization!");
							}
							
							newFringe.add(t.getTargetVertex());
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		
	}
	
	
	
	/* Recursively go through ASALStatement and initialize variables
	 * We only allow assignments where directly a literal is given
	 * Also functions are allowed, where the function definition should only assign variables
	 */
	private void initializeValuation(TritoTransition t, ASALStatement st) throws ReflectionException {
		if(st instanceof ASALAssignStatement) {
			ASALAssignStatement statement = (ASALAssignStatement) st;
			ASALExpr expr = statement.getExpression();
			if(expr instanceof ASALLiteral) {
				ASALLiteral lit = (ASALLiteral) expr;
				ASALVariable v = statement.getResolvedVar();
				JType newValue = JType.createValue(lit.getResolvedConstructor());
				
				if (v.getInitialValue() != null) {
					if (!JType.isEqual(v.getInitialValue(), newValue)) { 
						System.err.println("WARNING! Initial value of " + statement.getResolvedVar().getLegacy().getFileLocation() + " is overridden at" + t.getFileLocation());
						System.err.println("\tState machine initializes to " + lit.getResolvedConstructor().getCanonicalName());
						System.err.println("\tUser overrides (or block presets) to " + v.getInitialValue().getClass().getCanonicalName());
					}
				} else {
//					System.err.println("INIT! " + statement.getResolvedVar().getLegacy().getFileLocation().link + " is set to " + lit.getResolvedConstructor().legacy.getCanonicalName());
					v.setInitialValue(newValue);
				}
			} else {
				throw new ClassReflectionException(instance.getClass(), new ASALException(statement, "Transitions from initial vertices may only contain assignments of literal values"));
			}
		} else if(st instanceof ASALSeqStatement) {
			ASALSeqStatement statement = (ASALSeqStatement) st;
			initializeValuation(t, statement.getStatement());
			initializeValuation(t, statement.getSuccessor());
		} else if(st instanceof ASALFunctionCallStatement) {
			ASALFunctionCallStatement statement = (ASALFunctionCallStatement) st;
			initializeValuation(t, statement.getResolvedOperation().getBody());
		} else if(st instanceof ASALEmptyStatement) {
			//do nothing, but allowed
		} else {
			throw new ClassReflectionException(instance.getClass(), new ASALException(st, "Transitions from initial vertices may only contain direct variable assignments"));
		}
	}
	
	/**
	 * Returns ALL transitions, including entry/exit/do transitions.
	 */
	public Set<TritoTransition> getAllTransitions() {
		Set<TritoTransition> result = new HashSet<TritoTransition>();
		result.addAll(transitions);
		
		for (TritoVertex v : vertices) {
			if (v.getOnEntry() != null) {
				result.add(v.getOnEntry());
			}
			
			if (v.getOnExit() != null) {
				result.add(v.getOnExit());
			}
			
			result.addAll(v.getOnDo());
		}
		
		return result;
	}
	
	public void printDebugText(PrintStream out, String prefix) {
		Set<TritoTransition> allTransitions = new HashSet<TritoTransition>(getAllTransitions());
		Set<TritoTransition> includedTransitions = new HashSet<TritoTransition>(allTransitions);
		
		out.println(prefix + instance.getClass().getCanonicalName() + " {");
		
		for (TritoVertex v : vertices) {
			out.println(prefix + "\t" + v.getSysmlClz().getCanonicalName() + " {");
			
			if (initialVerticesSM.contains(v)) {
				out.println(prefix + "\t\t" + "isInitial;");
			}
			
			for (TritoVertex cv : v.getChildVertices()) {
				out.println(prefix + "\t\t" + "child " + cv.getSysmlClz().getCanonicalName() + ";");
			}
			
			if (v.getRegionVertex() != null) {
				out.println(prefix + "\t\tregion " + v.getRegionVertex().getSysmlClz().getCanonicalName() + ";");
			} else {
				out.println(prefix + "\t\tnot in a region;");
			}
			
			for (TritoTransition t : allTransitions) {
				if (t.getSourceVertex() == v) {
					t.printDebugText(out, prefix + "\t\t");
					includedTransitions.remove(t);
				}
			}
			
			out.println(prefix + "\t" + "}");
		}
		
		for (TritoTransition t : includedTransitions) {
			out.println(prefix + "\t" + "unprinted {");
			t.printDebugText(out, prefix + "\t\t");
			out.println(prefix + "\t}");
		}
		
		out.println(prefix + "}");
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<TritoVertex, String> namePerVertex = new HashMap<TritoVertex, String>();
		Map<TritoTransition, String> namePerTransition = new HashMap<TritoTransition, String>();
		
		for (TritoVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (TritoTransition t : getAllTransitions()) {
			namePerTransition.put(t, "T" + namePerTransition.size());
		}
		
		out.println("digraph G {");
		
		for (TritoVertex v : vertices) {
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + v.getSysmlClz().getCanonicalName() + "\", shape=ellipse];");
		}
		
		for (TritoTransition t : getAllTransitions()) {
			String s;
			
			if (t.getEvent() != null) {
				s = t.getEvent().toText(TextOptions.GRAPHVIZ_MIN);
			} else {
				s = "NO_EVENT";
			}
			
			if (t.getGuard() != null) {
				s += "\\n[ " + Texts._break(t.getGuard().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100) + " ]";
			}
			
			s += "\\n" + Texts._break(t.getStatement().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100);
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle];");
		}
		
		for (TritoTransition t : getAllTransitions()) {
			String v1 = namePerVertex.get(t.getSourceVertex());
			String v2 = namePerTransition.get(t);
			String v3 = namePerVertex.get(t.getTargetVertex());
			
			out.println("\t" + v1 + " -> " + v2 + ";");
			out.println("\t" + v2 + " -> " + v3 + ";");
		}
		
		out.println("}");
	}
}




