package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.controlflow.ASALFlattening;
import jlx.asal.j.JScope;
import jlx.asal.parsing.api.*;
import jlx.behave.*;
import jlx.common.reflection.*;
import jlx.utils.*;

/**
 * In this state machine, we compute which sets of vertices are reachable, and create new states from these.
 * We concatenate transitions that are triggered by the same trigger/call event.
 * We set 'finalized' events to NULL (we require that a state configuration contains the right final states for the corresponding transition to fire).
 */
public class OctoStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Map<OctoStateConfig, OctoVertex> vertices;
	public final OctoVertex initialVertex;
	public final ASALStatement initialization;
	public final Set<OctoTransition> transitions;
	public final SeptaStateMachine legacy;
	
	public OctoStateMachine(SeptaStateMachine source) throws ClassReflectionException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		initialVertex = new OctoVertex(new OctoStateConfig(source.initialVerticesSM));
		initialization = source.initialization;
		vertices = new HashMap<OctoStateConfig, OctoVertex>();
		vertices.put(initialVertex.getStateConfig(), initialVertex);
		transitions = new HashSet<OctoTransition>();
		
		Map<String, Set<SeptaTransition>> transitionsPerTrigger = new HashMap<String, Set<SeptaTransition>>();
		Set<SeptaTransition> independentTransitions = new HashSet<SeptaTransition>();
		
		for (SeptaTransition t : legacy.transitions) {
			if (t.getEvent() instanceof ASALTrigger) {
				HashMaps.inject(transitionsPerTrigger, t.getEvent().toText(TextOptions.FULL), t);
			} else {
				independentTransitions.add(t);
			}
		}
		
		Set<OctoVertex> fringe = new HashSet<OctoVertex>();
		Set<OctoVertex> newFringe = new HashSet<OctoVertex>();
		fringe.add(initialVertex);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (OctoVertex v : fringe) {
				for (OctoTransition t : computeTransitions(v.getStateConfig(), transitionsPerTrigger, independentTransitions)) {
					transitions.add(t);
					
					if (!vertices.containsKey(t.getTargetStateConfig())) {
						OctoVertex newVertex = new OctoVertex(t.getTargetStateConfig());
						vertices.put(t.getTargetStateConfig(), newVertex);
						newFringe.add(newVertex);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
	}
	
	private Set<OctoTransition> computeTransitions(OctoStateConfig sourceStateConfig, Map<String, Set<SeptaTransition>> transitionsPerTrigger, Set<SeptaTransition> independentTransitions) {
		Map<String, Set<SeptaTransition>> enabledTransitionsPerEvent = new HashMap<String, Set<SeptaTransition>>();
		
		for (Map.Entry<String, Set<SeptaTransition>> entry : transitionsPerTrigger.entrySet()) {
			for (SeptaTransition t : entry.getValue()) {
				if (!Collections.disjoint(t.getSourceVertices(), sourceStateConfig.states)) {
					HashMaps.inject(enabledTransitionsPerEvent, entry.getKey(), t);
				}
			}
		}
		
		List<List<SeptaTransition>> permPerIndex = new ArrayList<List<SeptaTransition>>();
		List<Set<SeptaTransition>> nonPermPerIndex = new ArrayList<Set<SeptaTransition>>();
		List<ASALExpr> guardPerIndex = new ArrayList<ASALExpr>();
		
		for (Map.Entry<String, Set<SeptaTransition>> entry : enabledTransitionsPerEvent.entrySet()) {
			for (Set<SeptaTransition> perm : HashSets.getSubsets(entry.getValue())) {
				if (perm.size() > 0) {
					Set<SeptaTransition> nonPerm = new HashSet<SeptaTransition>(entry.getValue());
					nonPerm.removeAll(perm);
					
					if (isCompatiblePerm(perm)) {
						Set<SeptaTransition> newNonPerm = removeBiggerFromNonPerm(perm, nonPerm);
						ASALExpr guard = createGuard(perm, newNonPerm);
						
						if (ASALFlattening.couldBeSat(scope, guard)) {
							permPerIndex.add(new ArrayList<SeptaTransition>(perm));
							nonPermPerIndex.add(removeBiggerFromNonPerm(perm, nonPerm));
							guardPerIndex.add(guard);
						}
					}
				}
			}
		}
		
		Set<OctoTransition> result = new HashSet<OctoTransition>();
		
		//Add transitions with a trigger (using data from above):
		for (int index = 0; index < permPerIndex.size(); index++) {
			for (List<SeptaTransition> seq : ArrayLists.allOrderings(permPerIndex.get(index))) {
				result.add(new OctoTransition(sourceStateConfig, guardPerIndex.get(index), seq, nonPermPerIndex.get(index)));
			}
		}
		
		//Add independent transitions:
		for (SeptaTransition t : independentTransitions) {
			if (!Collections.disjoint(t.getSourceVertices(), sourceStateConfig.states)) {
				if (t.getEvent() instanceof ASALFinalized) {
					Set<SeptaVertex> nonFinalStates = new HashSet<SeptaVertex>(t.getSourceVertices());
					nonFinalStates.removeIf((x) -> { return !FinalState.class.isAssignableFrom(x.getSysmlClz()); });
					
					//Arguably, we should check that ALL final states should be in the state configuration, as well . . .
					if (Collections.disjoint(nonFinalStates, sourceStateConfig.states)) {
						result.add(new OctoTransition(sourceStateConfig, t));
					}
				} else {
					result.add(new OctoTransition(sourceStateConfig, t));
				}
			}
		}
		
		return result;
	}
	
	private static boolean isCompatiblePerm(Set<SeptaTransition> perm) {
		for (SeptaTransition t1 : perm) {
			for (SeptaTransition t2 : perm) {
				if (t2 != t1) {
					//TODO source vertices overlapping with source state configuration, yes?
					//     no, 
					if (!Collections.disjoint(t2.getSourceVertices(), t1.getSourceVertices())) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Transitions that are enabled (= guard holds)
	 * block transitions that start from super-states, EVEN IF those transitions are also enabled (= guard holds).
	 * Therefore, do not check the guards of these transitions.
	 * 
	 * The same logic applies to transitions that start from the same state.
	 */
	private static Set<SeptaTransition> removeBiggerFromNonPerm(Set<SeptaTransition> perm, Set<SeptaTransition> nonPerm) {
		Set<SeptaTransition> result = new HashSet<SeptaTransition>();
		
		for (SeptaTransition t1 : nonPerm) {
			boolean add = true;
			
			for (SeptaTransition t2 : perm) {
				if (t1.getSourceVertices().containsAll(t2.getSourceVertices())) {
					add = false;
					break;
				}
			}
			
			if (add) {
				result.add(t1);
			}
		}
		
		return result;
	}
	
	private static ASALExpr createGuard(Set<SeptaTransition> perm, Set<SeptaTransition> nonPerm) {
		List<ASALExpr> guardElems = new ArrayList<ASALExpr>();
		
		for (SeptaTransition t : perm) {
			guardElems.add(t.getGuard());
		}
		
		for (SeptaTransition t : nonPerm) {
			guardElems.add(ASALUnaryExpr._not(t.getGuard()));
		}
		
		return ASALBinaryExpr.fromList(guardElems, "and", ASALLiteral._true());
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("octo-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<OctoVertex, String> namePerVertex = new HashMap<OctoVertex, String>();
		Map<OctoTransition, String> namePerTransition = new HashMap<OctoTransition, String>();
		
		for (OctoVertex v : vertices.values()) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (OctoTransition t : transitions) {
			namePerTransition.put(t, "T" + namePerTransition.size());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"(initial)\", shape=circle];");
		
		for (OctoVertex v : vertices.values()) {
			String s = Texts.concat(v.getStateConfig().states, " + ", (x) -> { return x.getSysmlClz().getSimpleName(); });
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
		}
		
		for (OctoTransition t : transitions) {
			String s;
			
			if (t.getEvent() != null) {
				s = t.getEvent().toText(TextOptions.GRAPHVIZ_MIN);
			} else {
				s = "NO_EVENT";
			}
			
			if (t.getGuard() != null) {
				s += "\\n[ " + Texts._break(t.getGuard().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 50) + " ]";
			}
			
			for (int index = 0; index < t.getEffectSeq().size(); index++) {
				s += "\\n" + Texts._break(t.getEffectSeq().get(index).toText(TextOptions.GRAPHVIZ_MIN), "\\n", 50);
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, fontsize=10];");
		}
		
		for (OctoTransition t : transitions) {
			String randomColor = Dot.getRandomColor();
			
			String t_name = namePerTransition.get(t);
			String sv_name = namePerVertex.get(vertices.get(t.getSourceStateConfig()));
			String tv_name = namePerVertex.get(vertices.get(t.getTargetStateConfig()));
			out.println("\t" + sv_name + " -> " + t_name + " [color=\"" + randomColor + "\"];");
			out.println("\t" + t_name + " -> " + tv_name + " [color=\"" + randomColor + "\"];");
		}
		
		out.println("\tI0 -> " + namePerVertex.get(initialVertex) + ";");
		out.println("}");
	}
}



