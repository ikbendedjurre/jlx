package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.ASALStatement;
import jlx.behave.*;
import jlx.common.FileLocation;
import jlx.common.reflection.*;
import jlx.utils.*;

/**
 * In this state machine, we merge transitions that are connected by pseudo-states.
 * Consequently, transitions will always move from stable state sets to stable state sets.
 * (TritoStateMachine ensures that we start in a stable initial state.)
 * 
 * New transitions may only have up to one event (or an exception is thrown).
 */
public class HexaStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<HexaVertex> vertices;
	public final Set<HexaVertex> initialVerticesSM;
	public final ASALStatement initialization;
	public final Set<HexaTransition> transitions;
	public final PentaStateMachine legacy;
	
	public HexaStateMachine(PentaStateMachine source) throws ClassReflectionException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		Map<PentaVertex, HexaVertex> newVertexPerOldVertex = new HashMap<PentaVertex, HexaVertex>();
		
		vertices = new HashSet<HexaVertex>();
		
		for (PentaVertex oldVertex : source.vertices) {
			if (State.class.isAssignableFrom(oldVertex.getSysmlClz())) {
				HexaVertex newVertex = new HexaVertex(oldVertex);
				vertices.add(newVertex);
				newVertexPerOldVertex.put(oldVertex, newVertex);
			}
		}
		
		initialVerticesSM = HashSets.map(source.initialVerticesSM, newVertexPerOldVertex);
		initialization = source.initialization;
		transitions = new HashSet<HexaTransition>();
		
		try {
			for (PentaTransition t : source.transitions) {
				if (hasStableSource(t)) {
					Map<List<PentaTransition>, Set<PentaVertex>> x = PentaPath.createFromTransition(legacy, t, Collections.emptySet()).getTgtsPerTrSeq();
					
					for (Map.Entry<List<PentaTransition>, Set<PentaVertex>> e : x.entrySet()) {
						checkTransitions(e.getKey());
						transitions.add(new HexaTransition(e.getKey(), e.getValue(), newVertexPerOldVertex));
					}
					
//					System.out.println("|x| = " + x.size());
//					CLI.waitForEnter();
					
//					expandUntilStable(Collections.singletonList(t), newVertexPerOldVertex);
//					stabilize(t, newVertexPerOldVertex);
				}
			}
		} catch (ModelException e) {
			throw new ClassReflectionException(instance.getClass(), e);
		}
	}
	
//	private void stabilize(PentaTransition t, Map<PentaVertex, HexaVertex> newVertexPerOldVertex) throws ModelException {
//		Set<PentaTransitionTree> stabilized = new HashSet<PentaTransitionTree>();
//		
//		Set<PentaTransitionTree> fringe = new HashSet<PentaTransitionTree>();
//		Set<PentaTransitionTree> newFringe = new HashSet<PentaTransitionTree>();
//		fringe.add(new PentaTransitionTree(null, t));
//		
//		while (fringe.size() > 0) {
//			newFringe.clear();
//			
//			for (PentaTransitionTree tree : fringe) {
//				if (hasStableTarget(tree.getLast())) {
////					System.out.println(Texts.concat(tree.getLast().getTargetVertices(), "|", (x) -> { return x.getSysmlClz().getCanonicalName(); }) + " is stable");
////					tree.getRoot().printDebug("-> ");
//					stabilized.add(tree);
//				} else {
//					Set<PentaTransition> outgoing = computeOutgoing(tree.getLast());
//					
//					if (outgoing.isEmpty()) {
//						throw new ModelException(tree.getLast().getFileLocation(), "Unfinished transition path!");
//					}
//					
//					for (PentaTransition xt : outgoing) {
//						if (tree.contains(xt)) {
//							throw new ModelException(xt.getFileLocation(), "Circular paths of transitions are forbidden!");
//						}
//					}
//					
//					if (isParallelTarget(tree.getLast().getTargetVertices())) {
////						System.out.println(Texts.concat(tree.getLast().getTargetVertices(), "|", (x) -> { return x.getSysmlClz().getCanonicalName(); }) + " is parallel");
//						
//						for (PentaTransition xt : outgoing) {
//							PentaTransitionTree newTree = new PentaTransitionTree(tree, xt);
//							newFringe.add(newTree);
//						}
//					} else {
////						System.out.println(Texts.concat(tree.getLast().getTargetVertices(), "|", (x) -> { return x.getSysmlClz().getCanonicalName(); }) + " is default");
//						
//						for (PentaTransition xt : outgoing) {
//							PentaTransitionTree newTree = new PentaTransitionTree(tree);
//							newTree.getPath().add(xt);
//							newFringe.add(newTree);
//						}
//					}
//				}
//			}
//			
//			fringe.clear();
//			fringe.addAll(newFringe);
//		}
//		
//		Set<PentaTransitionTree> roots = new HashSet<PentaTransitionTree>();
//		
//		for (PentaTransitionTree stab : stabilized) {
//			roots.add(stab.getRoot());
//			stab.populate();
//		}
//		
//		for (PentaTransitionTree root : roots) {
//			root.printDebug("");
//			
//			for (PentaTransitionPath p : root.getPaths()) {
//				checkTransitions(p.getTransitions());
//				
//				for (PentaVertex v : p.getTargetVertices()) {
//					if (!State.class.isAssignableFrom(v.getSysmlClz())) {
//						throw new Error("Ended in a " + v.getSysmlClz().getCanonicalName() + "?!");
//					}
//				}
//				
//				transitions.add(new HexaTransition(p, newVertexPerOldVertex));
//				p.printDebug();
//			}
//		}
//	}
//	
//	private Set<PentaTransition> computeOutgoing(PentaTransition t) {
//		Set<PentaTransition> result = new HashSet<PentaTransition>();
//		
//		for (PentaTransition xt : legacy.transitions) {
//			if (!Collections.disjoint(xt.getSourceVertices(), t.getTargetVertices())) {
//				result.add(xt);
//			}
//		}
//		
//		return result;
//	}
	
	/**
	 * New run-to-completions can only start from stable state configurations.
	 */
	private static boolean hasStableSource(PentaTransition t) {
		switch (t.getSourceVertices().size()) {
			case 0:
				throw new Error("Should not happen!");
			case 1:
				return State.class.isAssignableFrom(t.getSourceVertices().iterator().next().getSysmlClz());
			default:
				for (PentaVertex v : t.getSourceVertices()) {
					if (!State.class.isAssignableFrom(v.getSysmlClz())) {
						throw new Error("Should not happen!");
					}
				}
				
				return true;
		}
	}
	
//	private static boolean isStableTarget(Set<PentaVertex> vtxs) {
//		switch (vtxs.size()) {
//			case 0:
//				throw new Error("Should not happen!");
//			case 1:
//				return State.class.isAssignableFrom(vtxs.iterator().next().getSysmlClz());
//			default:
//				for (PentaVertex v : vtxs) {
//					if (State.class.isAssignableFrom(v.getSysmlClz())) {
//						throw new Error("Should not happen!"); //TODO what about multi-region composite states?!
//					}
//				}
//				
//				return false;
//		}
//	}
//	
//	private static boolean isParallelTarget(Set<PentaVertex> vtxs) {
//		switch (vtxs.size()) {
//			case 0:
//				throw new Error("Should not happen!");
//			case 1:
//				return InitialState.class.isAssignableFrom(vtxs.iterator().next().getSysmlClz());
//			default:
//				boolean result = false;
//				
//				for (PentaVertex v : vtxs) {
//					if (InitialState.class.isAssignableFrom(v.getSysmlClz())) {
//						result = true;
//						break;
//					}
//				}
//				
//				if (result) {
//					for (PentaVertex v : vtxs) {
//						if (!InitialState.class.isAssignableFrom(v.getSysmlClz())) {
//							throw new Error("Should not happen!");
//						}
//					}
//				}
//				
//				return false;
//		}
//	}
	
//	private void expandUntilStable(List<PentaTransition> soFar, Map<PentaVertex, HexaVertex> newVertexPerOldVertex) throws ModelException {
//		PentaTransition last = soFar.get(soFar.size() - 1);
//		
//		if (isStableTarget(last.getTargetVertices())) {
//			checkTransitions(soFar);
//			transitions.add(new HexaTransition(soFar, soFar.get(soFar.size() - 1).getTargetVertices(), newVertexPerOldVertex));
//			return;
//		}
//		
//		if (isParallelTarget(last.getTargetVertices())) {
////			throw new Error("No implementation!"); //TODO ??
//		}
//		
//		for (PentaVertex v : last.getTargetVertices()) {
//			boolean found = false;
//			
//			for (PentaTransition t : legacy.transitions) {
//				if (t.getSourceVertices().contains(v)) {
//					if (soFar.contains(t)) {
//						throw new ModelException(t.getFileLocation(), "Circular paths of transitions are forbidden!");
//					}
//					
//					List<PentaTransition> newSoFar = new ArrayList<PentaTransition>(soFar);
//					newSoFar.add(t);
//					expandUntilStable(newSoFar, newVertexPerOldVertex);
//					found = true;
//				}
//			}
//			
//			if (!found) {
//				throw new Error("Unfinished transition path!");
//			}
//		}
//	}
	
	private void checkTransitions(List<PentaTransition> soFar) throws ModelException {
		for (int index = 1; index < soFar.size(); index++) {
			PentaTransition t = soFar.get(index);
			
			if (t.getEvent() != null) {
				FileLocation c = t.getLegacy().getLegacy().getLegacy().getFileLocation();
				throw new ModelException(c, "No event permitted after the first transition!");
			}
		}
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("hexa-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<HexaVertex, String> namePerVertex = new HashMap<HexaVertex, String>();
		Map<HexaTransition, String> namePerTransition = new HashMap<HexaTransition, String>();
		
		for (HexaVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (HexaTransition t : transitions) {
			namePerTransition.put(t, "T" + namePerTransition.size());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"(initial)\", shape=circle];");
		
		for (HexaVertex v : vertices) {
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + v.getSysmlClz().getSimpleName() + "\", shape=ellipse];");
		}
		
		for (HexaTransition t : transitions) {
			String s;
			
			if (t.getEvent() != null) {
				s = t.getEvent().toText(TextOptions.GRAPHVIZ_MIN);
			} else {
				s = "NO_EVENT";
			}
			
			if (t.getGuard() != null) {
				s += "\\n[ " + Texts._break(t.getGuard().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100) + " ]";
			}
			
			int index = 1;
			
			for (List<ConditionalExitBehavior<HexaVertex>> effectPerm : t.getEffectPerms()) {
				s += "\\n(" + index + ")";
				index++;
				
				for (ConditionalExitBehavior<HexaVertex> ceb : effectPerm) {
					String option = ceb.getDebugText();
					option += "\\n==[ ";
					option += Texts.concat(ceb.getExitedVertices(), ", ", (v) -> { return v.getSysmlClz().getSimpleName(); });
					option += " ]==";
					s += "\\n" + option;
					s += "\\n" + Texts._break(ceb.getEffect().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100);
				}
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle];");
		}
		
		for (HexaTransition t : transitions) {
			String t_name = namePerTransition.get(t);
			
			for (HexaVertex sv : t.getSourceVertices()) {
				String sv_name = namePerVertex.get(sv);
				out.println("\t" + sv_name + " -> " + t_name + ";");
			}
			
			for (HexaVertex tv : t.getTargetVertices()) {
				String tv_name = namePerVertex.get(tv);
				out.println("\t" + t_name + " -> " + tv_name + ";");
			}
		}
		
		for (HexaVertex iv : initialVerticesSM) {
			String iv_name = namePerVertex.get(iv);
			out.println("\tI0 -> " + iv_name + ";");
		}
		
		out.println("}");
	}
}

