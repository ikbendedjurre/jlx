package jlx.behave.proto;

import java.io.*;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.models.UnifyingBlock.*;
import jlx.utils.*;

/**
 * (Currently disabled!)
 * In this state machine, helper properties are created for each D-input.
 * 
 * Let H1 be the helper property that is created for a D-input D1.
 * Helper property H1 is updated with the new value of D1 in the effect of each transition.
 * Furthermore, each guard G becomes a disjunction e.g. G || G[H1/D1] || G[H2/D2] || G[H1/D1, H2/D2]
 */
public class DecbStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Set<DecbVertex> vertices;
	public final DecbVertex initialVertex;
	public final Map<ASALVariable, ASALSymbolicValue> initialization;
	public final Set<DecbTransition> transitions;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final Map<ASALPort, ASALProperty> helperPropPerDInput;
	public final DecaStateMachine legacy;
	
	public static boolean ENABLED = false;
	
	public DecbStateMachine(DecaStateMachine source) {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(source.timeoutPortPerDurationPort);
		helperPropPerDInput = new HashMap<ASALPort, ASALProperty>();
		
		if (ENABLED) {
			for (ASALVariable v : scope.getVariablePerName().values()) {
				if (v instanceof ReprPort) {
					ReprPort rp = (ReprPort)v;
					
					if (rp.getDir() == Dir.IN) {
						if (rp.getPulsePort() == null && !rp.getType().equals(JPulse.class)) {
							if (rp.getInitialValue() == null) {
								throw new Error("Damn I was hoping not to have to implement this . . .");
							}
							
							helperPropPerDInput.put(rp, scope.generateHelperProperty("H_" + rp.getName(), rp.getInitialValue()));
						}
					}
				}
			}
		}
		
		Map<DecaVertex, DecbVertex> newVertexPerOldVertex = new HashMap<DecaVertex, DecbVertex>();
		vertices = new HashSet<DecbVertex>();
		
		for (DecaVertex oldVertex : source.vertices) {
			DecbVertex newVertex = new DecbVertex(oldVertex);
			vertices.add(newVertex);
			newVertexPerOldVertex.put(oldVertex, newVertex);
		}
		
		initialVertex = newVertexPerOldVertex.get(source.initialVertex);
		initialization = new HashMap<ASALVariable, ASALSymbolicValue>(source.initialization);
		
		if (ENABLED) {
			for (Map.Entry<ASALPort, ASALProperty> entry : helperPropPerDInput.entrySet()) {
				if (initialization.get(entry.getKey()) == null) {
					throw new Error("!!");
				}
				
				initialization.put(entry.getValue(), initialization.get(entry.getKey()));
			}
		}
		
		transitions = new HashSet<DecbTransition>();
		
		if (ENABLED) {
			for (DecaTransition t : source.transitions) {
				Set<ASALVariable> referencedDInputs = new HashSet<ASALVariable>();
				referencedDInputs.addAll(t.getGuard().getReferencedVars());
				referencedDInputs.retainAll(helperPropPerDInput.keySet());
				
				Set<Map<ASALVariable, ASALSymbolicValue>> substs = new HashSet<Map<ASALVariable, ASALSymbolicValue>>();
				
				for (Set<ASALVariable> vs : HashSets.getSubsets(referencedDInputs)) {
					Map<ASALVariable, ASALSymbolicValue> subst = new HashMap<ASALVariable, ASALSymbolicValue>();
					
					for (ASALVariable v : vs) {
						subst.put(v, ASALSymbolicValue.from(helperPropPerDInput.get(v)));
					}
					
					substs.add(subst);
				}
				
				Iterator<Map<ASALVariable, ASALSymbolicValue>> q = substs.iterator();
				ASALSymbolicValue newGuard = t.getGuard().substitute(q.next());
				
				while (q.hasNext()) {
					newGuard = ASALSymbolicValue.or(newGuard, t.getGuard().substitute(q.next()));
				}
				
				for (ASALVariable v : t.getGuard().getReferencedVars()) {
					helperPropPerDInput.containsKey(v);
				}
				
				NextStateFct f = new NextStateFct(t.getNextStateFct());
				
				for (Map.Entry<ASALPort, ASALProperty> entry : helperPropPerDInput.entrySet()) {
					f.put(entry.getValue(), ASALSymbolicValue.from(entry.getKey()), "D_INPUT");
				}
				
				transitions.add(new DecbTransition(t, newGuard, f, newVertexPerOldVertex));
			}
		} else {
			for (DecaTransition t : source.transitions) {
				transitions.add(new DecbTransition(t, t.getGuard(), new NextStateFct(t.getNextStateFct()), newVertexPerOldVertex));
			}
		}
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("decb-" + scope.getName() + ".gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		Map<DecbVertex, String> namePerVertex = new HashMap<DecbVertex, String>();
		Map<DecbTransition, String> namePerTransition = new HashMap<DecbTransition, String>();
		Map<DecbTransition, String> colorPerTransition = new HashMap<DecbTransition, String>();
		
		for (DecbVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecbTransition t : transitions) {
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
		
		for (DecbVertex v : vertices) {
			String s = Texts.concat(v.getSysmlClzs(), " + ", (c) -> { return c.getSimpleName(); });
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
		}
		
		for (DecbTransition t : transitions) {
			String s = "[ " + Texts._break(t.getGuard().toString().replace("\"", "\\\""), "\\n", 70) + " ]";
			
			for (ASALVariable v : t.getNextStateFct().getVariables()) {
				s += "\\n" + TextOptions.GRAPHVIZ_FULL.id(v.getName()) + " := " + t.getNextStateFct().get(v).getValue().toString().replace("\"", "\\\"") + " //" + t.getNextStateFct().get(v).getDebugText();
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, color=\"" + colorPerTransition.get(t) + "\", fontsize=10, fontcolor=\"" + colorPerTransition.get(t) + "\"];");
		}
		
		for (DecbTransition t : transitions) {
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



