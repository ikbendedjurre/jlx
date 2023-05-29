package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.controlflow.ASALFlattening;
import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;
import jlx.behave.*;
import jlx.common.reflection.ClassReflectionException;
import jlx.common.reflection.ModelException;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * For this state machine, we make the assumption that TRIGGERS CAN BE INTERPRETED AS GUARDS.
 * This assumption is based on the observation that EULYNX engineers use triggers and guards interchangeably, and
 * on the fact that triggers seem to be used exclusively for three purposes:
 *  1. Handling incoming signals (= pulse ports in this version of EULYNX).
 *  2. Synchronization between parallel regions (which we have already handled previously).
 *     It should be considered whether this mechanism should remain in place, at all.  
 *  3. Call events (which we already rewrote to "local pulse ports").
 * 
 * We also flatten ASAL everywhere, so that we can get rid of operations.
 */
public class NonaStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<NonaVertex> vertices;
	public final NonaVertex initialVertex;
	public final Map<ASALVariable, ASALSymbolicValue> initialization;
	public final Set<NonaTransition> transitions;
	public final OctoStateMachine legacy;
	
	public NonaStateMachine(OctoStateMachine source) throws ClassReflectionException, ModelException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		Map<OctoVertex, NonaVertex> newVertexPerOldVertex = new HashMap<OctoVertex, NonaVertex>();
		Map<ASALVariable, ASALSymbolicValue> restrictedInputs = getRestrictedInputs(source);
		
		vertices = new HashSet<NonaVertex>();
		
		for (OctoVertex oldVertex : source.vertices.values()) {
			NonaVertex newVertex = new NonaVertex(oldVertex);
			vertices.add(newVertex);
			newVertexPerOldVertex.put(oldVertex, newVertex);
		}
		
		initialVertex = newVertexPerOldVertex.get(source.initialVertex);
		initialization = ASALFlattening.getNextStateFct2(scope, source.initialization);
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : initialization.entrySet()) {
			if (entry.getValue().getReferencedVars().size() > 0) {
				throw new ClassReflectionException(instance.getClass(), "Initialization expression for " + entry.getKey().getName() + ", namely " + entry.getValue() + ", is not closed!");
			}
		}
		
		for (ASALVariable v : scope.getVariablePerName().values()) {
			if (!initialization.containsKey(v)) {
				initialization.put(v, ASALSymbolicValue.from(v.getInitialValue()));
			}
		}
		
		transitions = new HashSet<NonaTransition>();
		
		for (OctoTransition t : source.transitions) {
			NonaVertex sourceVertex = newVertexPerOldVertex.get(source.vertices.get(t.getSourceStateConfig()));
			NonaVertex targetVertex = newVertexPerOldVertex.get(source.vertices.get(t.getTargetStateConfig()));
			transitions.add(new NonaTransition(t, this, sourceVertex, targetVertex, restrictedInputs));
		}
		
		//getVarsNotReadAfterTransition();
		//addResetAssignments();
		//insertConstants();
	}
	
	private static Map<ASALVariable, ASALSymbolicValue> getRestrictedInputs(OctoStateMachine source) {
		Map<ASALVariable, ASALSymbolicValue> result = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (ASALVariable v : source.scope.getVariablePerName().values()) {
			if (v instanceof ReprPort) {
				ReprPort rp = (ReprPort)v;
				
				if (rp.getDir() == Dir.IN) {
					Set<JType> possibleValues = rp.getPossibleValues();
					
					if (possibleValues != null) {
						if (possibleValues.size() == 1) {
							JType x = possibleValues.iterator().next();
							ASALSymbolicValue y = ASALSymbolicValue.from(x);
							
							//TODO check that pulses are not restricted to { TRUE }
							
							result.put(v, y);
							
							System.out.println("Restricted input: " + v.getName() + " (" + y + ")");
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("nona-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<NonaVertex, String> namePerVertex = new HashMap<NonaVertex, String>();
		Map<NonaTransition, String> namePerTransition = new HashMap<NonaTransition, String>();
		Map<NonaTransition, String> colorPerTransition = new HashMap<NonaTransition, String>();
		
		for (NonaVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (NonaTransition t : transitions) {
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
		
//		Map<NonaTransition, Set<NonaTransition>> succPerTransition = getSuccPerTransition();
		
		for (NonaVertex vtx : vertices) {
			String s = Texts.concat(vtx.getLegacy().getStateConfig().states, " + ", (x) -> { return x.getSysmlClz().getSimpleName(); });
//			List<String> xs = new ArrayList<String>();
//			
//			for (ASALVariable v : scope.getVariablePerName().values()) {
//				List<String> elems = new ArrayList<String>();
//				VarValueEquivDistr d = new VarValueEquivDistr();
//				
//				for (NonaTransition t : getReachableTransitionsUntilWritten(vtx, v, succPerTransition)) {
//					VarValueEquivDistr q = new VarValueEquivDistr(scope, v, t.getEqns(), namePerTransition.get(t));
//					d.add(q);
//					elems.add(namePerTransition.get(t) + "(" + q.getEqClzs().size() + "," + q.outcomeCount + ")");
//				}
//				
//				if (elems.size() > 0) {
//					xs.add(v.getName() + "=>[" + Texts.concat(elems, ",") + "](" + d.getEqClzs().size() + "," + d.outcomeCount + ")");
//				}
//			}
//			
//			String xx = Texts.concat(xs, "\\n");
			
			out.println("\t" + namePerVertex.get(vtx) + " [label=\"" + s + "\", shape=ellipse];");
			//out.println("\t" + namePerVertex.get(vtx) + " [label=\"" + s + "\\n" + xx + "\", shape=ellipse];");
		}
		
		for (NonaTransition t : transitions) {
			String s = "id=" + namePerTransition.get(t);
			
			if (t.getTimeoutEvent() != null) {
				s += "\\n" + Texts._break(t.getTimeoutEvent().toText(TextOptions.GRAPHVIZ_MIN), "\\n", 50);
			} else {
				s += "\\nNO_EVENT";
			}
			
			if (t.getGuard() != null) {
				s += "\\n[ " + Texts._break(t.getGuard().toString().replace("\"", "\\\""), "\\n", 50) + " ]";
			}
			
			for (ASALVariable v : t.getNextStateFct().getVariables()) {
				s += "\\n" + TextOptions.GRAPHVIZ_MIN.id(v.getName()) + " := " + t.getNextStateFct().get(v).getValue().toString().replace("\"", "\\\"") + " //" + t.getNextStateFct().get(v).getDebugText();
			}
			
			//s += "\\n{ " + Texts.concat(t.unread, ", ", (z) -> { return z.getName(); }) + " }";
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, color=\"" + colorPerTransition.get(t) + "\", fontsize=10, fontcolor=\"" + colorPerTransition.get(t) + "\"];");
		}
		
		for (NonaTransition t : transitions) {
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


