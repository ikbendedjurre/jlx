package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.*;
import jlx.behave.*;
import jlx.utils.*;

/**
 * In this state machine, composite states have been replaced by transitions with extra information (see below).
 * The state configuration of a state machine like this can be expressed as a set of vertices, which is much more convenient.
 * (Later, we can identify all possible sets of vertices that can be reached, and interpret those as states.)
 * 
 * Extra transition information:
 *  1. SourceVertices = Transition can fire if at least 1 of these vertices is in the state configuration.
 *  2. ExitBehaviorInOrder  = When the transition fires, exit behavior of the various vertices (if any active) is executed in one of these orders.
 *  3. EntryBehaviorInOrder = When the transition fires, entry behavior of the various vertices (if any) is executed in one of these orders.
 * 
 * "Do"-behavior is no longer an explicit part of the state machine.
 * Instead, it is copied as regular transitions to all (surviving) child vertices of a composite state.
 */
public class TetraStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<TetraVertex> vertices;
	public final Set<TetraVertex> initialVerticesSM;
	public final ASALStatement initialization;
	public final Set<TetraTransition> transitions;
	public final TritoStateMachine legacy;
	
	public TetraStateMachine(TritoStateMachine source) {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		Map<TritoVertex, TetraVertex> newVertexPerOldVertex = new HashMap<TritoVertex, TetraVertex>();
		
		//Only keep vertices without child vertices:
		vertices = new HashSet<TetraVertex>();
		
		for (TritoVertex oldVertex : source.vertices) {
			if (oldVertex.getChildVertices().size() == 0) {
				TetraVertex newVertex = new TetraVertex(oldVertex);
				vertices.add(newVertex);
				newVertexPerOldVertex.put(oldVertex, newVertex);
			}
		}
		
		//Map initial vertices:
		initialVerticesSM = HashSets.map(source.initialVerticesSM, newVertexPerOldVertex);
		initialization = source.initialization;
		
		//Map regular transitions, computing the extra information:
		transitions = new HashSet<TetraTransition>();
		
		for (TritoTransition t : source.transitions) {
			Set<TritoVertex> oldSourceVertices = new HashSet<TritoVertex>();
			
			if (t.getSourceVertex().getChildVertices().isEmpty()) {
				oldSourceVertices.add(t.getSourceVertex());
			} else {
				//Transitions can only originate from States (final/composite/simple):
				oldSourceVertices.addAll(t.getSourceVertex().getChildlessVertices());
				oldSourceVertices.removeIf((e) -> { return !State.class.isAssignableFrom(e.getSysmlClz()); }); //(FinalState inherits from State.)
			}
			
			Set<TritoVertex> oldTargetVertices = new HashSet<TritoVertex>();
			
			if (t.getTargetVertex().getInitialVertices().isEmpty()) {
				oldTargetVertices.add(t.getTargetVertex());
			} else {
				oldTargetVertices.addAll(t.getTargetVertex().getInitialVertices());
			}
			
			Set<TetraVertex> newSourceVertices = HashSets.map(oldSourceVertices, newVertexPerOldVertex);
			Set<TetraVertex> newTargetVertices = HashSets.map(oldTargetVertices, newVertexPerOldVertex);
			Set<List<ConditionalExitBehavior<TetraVertex>>> x = HashSets.map(t.getExitedVerticesInOrder(), (z) -> { return getExitBehavior(z, newVertexPerOldVertex, "EXIT " + t.getFileLocation().tiny()); });
			Set<List<ASALStatement>> y = HashSets.map(t.getEnteredVerticesInOrder(), (z) -> { return getEntryBehavior(z, newVertexPerOldVertex); });
			transitions.add(new TetraTransition(newSourceVertices, newTargetVertices, x, y, t));
		}
		
		//Keep "do"-behavior of composite states, by copying it to all child vertices:
		for (TritoVertex oldVertex : source.vertices) {
			if (oldVertex.getOnDo().size() > 0) {
				for (TritoVertex cv : oldVertex.getVertices()) {
					if (cv.getChildVertices().size() == 0) {
						TetraVertex ncv = newVertexPerOldVertex.get(cv);
						
						for (TritoTransition t : oldVertex.getOnDo()) {
							transitions.add(new TetraTransition(ncv, t));
						}
					}
				}
			}
		}
	}
	
	private static List<ConditionalExitBehavior<TetraVertex>> getExitBehavior(List<TritoVertex> exitedVerticesInOrder, Map<TritoVertex, TetraVertex> newVertexPerOldVertex, String debug) {
		List<ConditionalExitBehavior<TetraVertex>> result = new ArrayList<ConditionalExitBehavior<TetraVertex>>();
		
		for (TritoVertex exitedVertex : exitedVerticesInOrder) {
			if (exitedVertex.getOnExit() != null) {
				if (!(exitedVertex.getOnExit().getStatement() instanceof ASALEmptyStatement)) {
					Set<TetraVertex> exitedVertices = HashSets.map(exitedVertex.getChildlessVertices(), newVertexPerOldVertex);
					result.add(new ConditionalExitBehavior<TetraVertex>(debug, exitedVertices, exitedVertex.getOnExit().getStatement()));
				}
			}
		}
		
		return result;
	}
	
	private static List<ASALStatement> getEntryBehavior(List<TritoVertex> enteredVerticesInOrder, Map<TritoVertex, TetraVertex> newVertexPerOldVertex) {
		List<ASALStatement> result = new ArrayList<ASALStatement>();
		
		for (TritoVertex enteredVertex : enteredVerticesInOrder) {
			if (enteredVertex.getOnEntry() != null) {
				if (!(enteredVertex.getOnEntry().getStatement() instanceof ASALEmptyStatement)) {
					result.add(enteredVertex.getOnEntry().getStatement());
				}
			}
		}
		
		return result;
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("tetra-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<TetraVertex, String> namePerVertex = new HashMap<TetraVertex, String>();
		Map<TetraTransition, String> namePerTransition = new HashMap<TetraTransition, String>();
		
		for (TetraVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (TetraTransition t : transitions) {
			namePerTransition.put(t, "T" + namePerTransition.size());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"(initial)\", shape=circle];");
		
		for (TetraVertex v : vertices) {
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + v.getSysmlClz().getSimpleName() + "\", shape=ellipse];");
		}
		
		for (TetraTransition t : transitions) {
			String s;
			
			if (t.getEvent() != null) {
				s = t.getEvent().toText(TextOptions.GRAPHVIZ_MIN);
			} else {
				s = "NO_EVENT";
			}
			
			if (t.getGuard() != null) {
				s += "\\n[ " + Texts._break(t.getGuard().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100) + " ]";
			}
			
			Set<String> exits = new HashSet<String>();
			
			for (List<ConditionalExitBehavior<TetraVertex>> exitBehavior : t.getExitBehaviorInOrder()) {
				for (ConditionalExitBehavior<TetraVertex> eb : exitBehavior) {
					String exit = "Exit [ ";
					exit += Texts.concat(eb.getExitedVertices(), ", ", (x) -> { return x.getSysmlClz().getSimpleName(); });
					exit += " ] -> " + Texts._break(eb.getEffect().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100);
					exits.add(exit);
				}
			}
			
			Set<String> entries = new HashSet<String>();
			
			for (List<ASALStatement> entryBehavior : t.getEntryBehaviorInOrder()) {
				for (ASALStatement eb : entryBehavior) {
					entries.add("Entry -> " + Texts._break(eb.toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100));
				}
			}
			
			for (String exit : exits) {
				s += "\\n" + exit;
			}
			
			s += "\\n" + Texts._break(t.getStatement().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100);
			
			for (String entry : entries) {
				s += "\\n" + entry;
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle];");
		}
		
		for (TetraTransition t : transitions) {
			String t_name = namePerTransition.get(t);
			
			for (TetraVertex sv : t.getSourceVertices()) {
				String sv_name = namePerVertex.get(sv);
				out.println("\t" + sv_name + " -> " + t_name + ";");
			}
			
			for (TetraVertex tv : t.getTargetVertices()) {
				String tv_name = namePerVertex.get(tv);
				out.println("\t" + t_name + " -> " + tv_name + ";");
			}
		}
		
		for (TetraVertex iv : initialVerticesSM) {
			String iv_name = namePerVertex.get(iv);
			out.println("\tI0 -> " + iv_name + ";");
		}
		
		out.println("}");
	}
}






