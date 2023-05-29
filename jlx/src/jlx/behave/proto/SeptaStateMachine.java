package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.ASALStatement;
import jlx.behave.*;
import jlx.common.reflection.*;
import jlx.utils.*;

/**
 * In this state machine, transitions are split by their effect permutations:
 * Conditional source vertices are removed if they are the same as the source vertices of the transition.
 */
public class SeptaStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<SeptaVertex> vertices;
	public final Set<SeptaVertex> initialVerticesSM;
	public final ASALStatement initialization;
	public final Set<SeptaTransition> transitions;
	public final HexaStateMachine legacy;
	
	public SeptaStateMachine(HexaStateMachine source) throws ClassReflectionException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		Map<HexaVertex, SeptaVertex> newVertexPerOldVertex = new HashMap<HexaVertex, SeptaVertex>();
		
		vertices = new HashSet<SeptaVertex>();
		
		for (HexaVertex oldVertex : source.vertices) {
			SeptaVertex newVertex = new SeptaVertex(oldVertex);
			vertices.add(newVertex);
			newVertexPerOldVertex.put(oldVertex, newVertex);
		}
		
		initialVerticesSM = HashSets.map(source.initialVerticesSM, newVertexPerOldVertex);
		initialization = source.initialization;
		transitions = new HashSet<SeptaTransition>();
		
		for (HexaTransition t : source.transitions) {
			for (int permIndex = 0; permIndex < t.getEffectPerms().size(); permIndex++) {
				transitions.add(new SeptaTransition(t, permIndex, t.getEffectPerms().get(permIndex), newVertexPerOldVertex));
			}
		}
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("septa-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<SeptaVertex, String> namePerVertex = new HashMap<SeptaVertex, String>();
		Map<SeptaTransition, String> namePerTransition = new HashMap<SeptaTransition, String>();
		
		for (SeptaVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (SeptaTransition t : transitions) {
			namePerTransition.put(t, "T" + namePerTransition.size());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"(initial)\", shape=circle];");
		
		for (SeptaVertex v : vertices) {
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + v.getSysmlClz().getSimpleName() + "\", shape=ellipse];");
		}
		
		for (SeptaTransition t : transitions) {
			String s;
			
			if (t.getEvent() != null) {
				s = t.getEvent().toText(TextOptions.GRAPHVIZ_MIN);
			} else {
				s = "NO_EVENT";
			}
			
			if (t.getGuard() != null) {
				s += "\\n[ " + Texts._break(t.getGuard().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100) + " ]";
			}
			
			for (ConditionalExitBehavior<SeptaVertex> ceb : t.getEffectPerm()) {
				String option = ceb.getDebugText();
				option += "\\n==[ ";
				option += Texts.concat(ceb.getExitedVertices(), ", ", (v) -> { return v.getSysmlClz().getSimpleName(); });
				option += " ]==";
				s += "\\n" + option;
				s += "\\n" + Texts._break(ceb.getEffect().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 100);
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle];");
		}
		
		for (SeptaTransition t : transitions) {
			String t_name = namePerTransition.get(t);
			
			for (SeptaVertex sv : t.getSourceVertices()) {
				String sv_name = namePerVertex.get(sv);
				out.println("\t" + sv_name + " -> " + t_name + ";");
			}
			
			for (SeptaVertex tv : t.getTargetVertices()) {
				String tv_name = namePerVertex.get(tv);
				out.println("\t" + t_name + " -> " + tv_name + ";");
			}
		}
		
		for (SeptaVertex iv : initialVerticesSM) {
			String iv_name = namePerVertex.get(iv);
			out.println("\tI0 -> " + iv_name + ";");
		}
		
		out.println("}");
	}
}
