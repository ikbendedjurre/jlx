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
 * In this state machine, we merge entry behavior and exit behavior with transition effects. 
 */
public class PentaStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<PentaVertex> vertices;
	public final Set<PentaVertex> initialVerticesSM;
	public final ASALStatement initialization;
	public final Set<PentaTransition> transitions;
	public final TetraStateMachine legacy;
	
	public PentaStateMachine(TetraStateMachine source) throws ClassReflectionException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		Map<TetraVertex, PentaVertex> newVertexPerOldVertex = new HashMap<TetraVertex, PentaVertex>();
		
		vertices = new HashSet<PentaVertex>();
		
		for (TetraVertex oldVertex : source.vertices) {
			PentaVertex newVertex = new PentaVertex(oldVertex);
			vertices.add(newVertex);
			newVertexPerOldVertex.put(oldVertex, newVertex);
		}
		
		initialVerticesSM = HashSets.map(source.initialVerticesSM, newVertexPerOldVertex);
		initialization = source.initialization;
		transitions = new HashSet<PentaTransition>();
		
		for (TetraTransition t : source.transitions) {
			transitions.add(new PentaTransition(t, newVertexPerOldVertex));
		}
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("penta-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<PentaVertex, String> namePerVertex = new HashMap<PentaVertex, String>();
		Map<PentaTransition, String> namePerTransition = new HashMap<PentaTransition, String>();
		
		for (PentaVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (PentaTransition t : transitions) {
			namePerTransition.put(t, "T" + namePerTransition.size());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"(initial)\", shape=circle];");
		
		for (PentaVertex v : vertices) {
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + v.getSysmlClz().getSimpleName() + "\", shape=ellipse];");
		}
		
		for (PentaTransition t : transitions) {
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
			
			for (List<ConditionalExitBehavior<PentaVertex>> effectPerm : t.getEffectPerms()) {
				s += "\\n(" + index + ")";
				index++;
				
				for (ConditionalExitBehavior<PentaVertex> ceb : effectPerm) {
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
		
		for (PentaTransition t : transitions) {
			String t_name = namePerTransition.get(t);
			
			for (PentaVertex sv : t.getSourceVertices()) {
				String sv_name = namePerVertex.get(sv);
				out.println("\t" + sv_name + " -> " + t_name + ";");
			}
			
			for (PentaVertex tv : t.getTargetVertices()) {
				String tv_name = namePerVertex.get(tv);
				out.println("\t" + t_name + " -> " + tv_name + ";");
			}
		}
		
		for (PentaVertex iv : initialVerticesSM) {
			String iv_name = namePerVertex.get(iv);
			out.println("\tI0 -> " + iv_name + ";");
		}
		
		out.println("}");
	}
}



