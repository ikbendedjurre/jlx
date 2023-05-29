package jlx.behave.proto;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.common.reflection.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * This state machines computes input valuations for guards, and
 * groups input valuations (locally, i.e. per vertex) so that
 * all input valuations in a group lead to the same target vertex/vertices.
 * 
 * (Disabled:)
 * Also applies (input port) priorities.
 */
public class DecaTwoBStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final Map<DecaTwoVertex, DecaTwoBVertex> vertices;
	public final DecaTwoBVertex initialVertex;
	public final PulsePackMap initialInputs;
	public final Set<PulsePackMap> allInputs;
	public final Set<DecaTwoBTransition> transitions;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final DecaTwoStateMachine legacy;
	
	public static boolean USE_PRIORITIES = false;
	
	public DecaTwoBStateMachine(DecaTwoStateMachine source) throws ClassReflectionException, ModelException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(source.timeoutPortPerDurationPort);
		vertices = new HashMap<DecaTwoVertex, DecaTwoBVertex>();
		
		for (DecaTwoVertex v : source.vertices.values()) {
			DecaTwoBVertex w = new DecaTwoBVertex(v);
			vertices.put(v, w);
		}
		
		initialVertex = vertices.get(source.initialVertex);
		initialInputs = PulsePackMap.from(source.initialInputs, Dir.IN);
		
		allInputs = new HashSet<PulsePackMap>();
		
		for (Map<ReprPort, ASALSymbolicValue> inputVal : HashMaps.allCombinations(source.pvsPerInput)) {
			allInputs.add(PulsePackMap.from(inputVal, Dir.IN));
		}
		
		Set<ASALSymbolicValue> guards = new HashSet<ASALSymbolicValue>();
		
		for (DecaTwoTransition t : source.transitions) {
			guards.add(t.getGuard());
		}
		
		Map<ASALSymbolicValue, Set<PulsePackMap>> inputValsPerGuard = new HashMap<ASALSymbolicValue, Set<PulsePackMap>>();
		
		for (Map<ReprPort, ASALSymbolicValue> inputVal : HashMaps.allCombinations(source.pvsPerInput)) {
			PulsePackMap ppm = PulsePackMap.from(inputVal, Dir.IN);
			
			for (ASALSymbolicValue guard : guards) {
				if (guard.substitute(inputVal).toBoolean()) {
					HashMaps.inject(inputValsPerGuard, guard, ppm);
				}
			}
		}
		
		
		
//		Map<Set<ASALSymbolicValue>, Set<PulsePackMap>> inputValsPerGuardSet = new HashMap<Set<ASALSymbolicValue>, Set<PulsePackMap>>();
//		
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : HashMaps.allCombinations(source.pvsPerInput)) {
//			Set<ASALSymbolicValue> guardSet = new HashSet<ASALSymbolicValue>();
//			
//			for (ASALSymbolicValue guard : guards) {
//				if (guard.substitute(inputVal).toBoolean()) {
//					guardSet.add(guard);
//				}
//			}
//			
//			HashMaps.inject(inputValsPerGuardSet, guardSet, PulsePackMap.from(inputVal, Dir.IN));
//		}
		
		
		
		
		
		transitions = new HashSet<DecaTwoBTransition>();
		
		for (DecaTwoBVertex v : vertices.values()) {
			Map<PulsePackMap, Set<DecaTwoBVertex>> tgtsPerInputVal = new HashMap<PulsePackMap, Set<DecaTwoBVertex>>();
			
			for (DecaTwoTransition t : v.getLegacy().getOutgoing()) {
				for (PulsePackMap i : inputValsPerGuard.get(t.getGuard())) {
					DecaTwoBVertex tgt = vertices.get(legacy.vertices.get(t.getTargetStateConfig()));
					HashMaps.inject(tgtsPerInputVal, i, tgt);
				}
			}
			
//			if (USE_PRIORITIES) {
//				for (Map.Entry<PulsePackMap, Set<DecaTwoBVertex>> e : tgtsPerInputVal.entrySet()) {
//					Set<DecaTwoBVertex> updatedTgts = tgtsPerInputVal.get(e.getKey().prioritize());
//					
//					if (updatedTgts == null) {
//						throw new Error("Should not happen!");
//					}
//					
//					e.setValue(updatedTgts);
//				}
//			}
			
			Map<Set<PulsePackMap>, Set<DecaTwoBVertex>> tgtsPerInputVals = HashMaps.mergeKeysByValues(tgtsPerInputVal);
			
			for (Map.Entry<Set<PulsePackMap>, Set<DecaTwoBVertex>> e : tgtsPerInputVals.entrySet()) {
				Set<DecaTwoTransition> legacyTrs = new HashSet<DecaTwoTransition>();
				
				for (DecaTwoTransition t : v.getLegacy().getOutgoing()) {
					DecaTwoBVertex tgt = vertices.get(legacy.vertices.get(t.getTargetStateConfig()));
					
					if (e.getValue().contains(tgt)) {
						if (!Collections.disjoint(e.getKey(), inputValsPerGuard.get(t.getGuard()))) {
							legacyTrs.add(t);
						}
					}
				}
				
				transitions.add(new DecaTwoBTransition(legacyTrs, v, e.getValue(), e.getKey()));
			}
			
//			Map<PulsePackMap, Set<DecaTwoTransition>> targetsPerInputVal = new HashMap<PulsePackMap, Set<DecaTwoTransition>>();
//			
//			for (DecaTwoTransition t : v.getLegacy().getOutgoing()) {
//				for (PulsePackMap i : inputValsPerGuard.get(t.getGuard())) {
//					HashMaps.inject(targetsPerInputVal, i, t);
//				}
//			}
//			
//			Map<PulsePackMap, Set<DecaTwoTransition>> prioritizedTargetsPerInputVal = new HashMap<PulsePackMap, Set<DecaTwoTransition>>();
//			prioritizedTargetsPerInputVal.putAll(targetsPerInputVal);
//			
////			for (Map.Entry<PulsePackMap, Set<DecaTwoTransition>> e : targetsPerInputVal.entrySet()) {
////				if (e.getValue().size() == 1) {
////					prioritizedTargetsPerInputVal.put(e.getKey(), e.getValue()); //TODO why?
////				} else {
////					Set<DecaTwoTransition> temp = targetsPerInputVal.get(e.getKey().prioritize());
////					
////					if (temp == null) {
////						throw new Error("Should not happen!");
////					}
////					
////					prioritizedTargetsPerInputVal.put(e.getKey(), temp);
////				}
////			}
//			
//			Map<Set<DecaTwoTransition>, Set<PulsePackMap>> inputValPerTrSet = new HashMap<Set<DecaTwoTransition>, Set<PulsePackMap>>();
//			
//			for (Map.Entry<PulsePackMap, Set<DecaTwoTransition>> e : prioritizedTargetsPerInputVal.entrySet()) {
//				HashMaps.inject(inputValPerTrSet, e.getValue(), e.getKey());
//			}
//			
//			for (Map.Entry<Set<DecaTwoTransition>, Set<PulsePackMap>> e : inputValPerTrSet.entrySet()) {
//				Set<DecaTwoBVertex> tgts = new HashSet<DecaTwoBVertex>();
//				
//				for (DecaTwoTransition t : e.getKey()) {
//					tgts.add(vertices.get(legacy.vertices.get(t.getTargetStateConfig())));
//				}
//				
//				transitions.add(new DecaTwoBTransition(e.getKey(), v, tgts, e.getValue()));
//			}
		}
		
		for (DecaTwoBTransition t : transitions) {
			t.getSrc().getOutgoing().add(t);
		}
		
//		if (scope.getName().equals("p3")) {
//			List<String> elems = new ArrayList<String>();
//			
//			for (Map.Entry<ReprPort, Set<ASALSymbolicValue>> e : source.pvsPerInput.entrySet()) {
//				elems.add(e.getKey().getName() + " (x" + e.getValue().size() + ")");
//			}
//			
//			Collections.sort(elems);
//			
//			for (String elem : elems) {
//				System.out.println(elem);
//			}
//			
//			System.out.println("#vertices = " + vertices.size());
//			System.out.println("#transitions = " + transitions.size());
//			System.out.println("#allInputs = " + allInputs.size());
//			
//			System.out.println("#inputValsPerGuard = " + inputValsPerGuard.size());
//			System.out.println("pvsPerInput.#elems = " + source.pvsPerInput.size());
//			System.out.println("pvsPerInput.#perms = " + HashMaps.allCombinations(source.pvsPerInput).size());
//			
//			
//			System.exit(0);
//		}
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-two-b-" + scope.getName() + ".gv");
//			printGraphvizFile(ps, extractStoppedTransitions());
			printGraphvizFile(ps, transitions);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	private Set<DecaTwoBTransition> extractStoppedTransitions() {
		Set<DecaTwoBTransition> result = new HashSet<DecaTwoBTransition>();
		
		for (DecaTwoBTransition t : transitions) {
			if (contains(t.getSrc(), "STOPPED", "ALL_LEFT")) {
				if (contains(t.getTgts(), "MOVING_RIGHT", "ALL_LEFT")) {
					if (t.getTgts().size() == 1) {
//					if (contains(t.getTgts(), "STOPPED", "ALL_LEFT")) {
						result.add(t);
					}
				}
			}
		}
		
		return result;
	}
	
	private static boolean contains(Set<DecaTwoBVertex> v, String... elems) {
		for (DecaTwoBVertex w : v) {
			if (contains(w, elems)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean contains(DecaTwoBVertex v, String... elems) {
		String s = v.getName();
		
		for (String elem : elems) {
			if (!s.contains(elem)) {
				return false;
			}
		}
		
		return true;
	}
	
	public void printGraphvizFile(PrintStream out, Set<DecaTwoBTransition> trs) {
		Map<DecaTwoBVertex, String> namePerVertex = new HashMap<DecaTwoBVertex, String>();
		Map<DecaTwoBTransition, String> namePerTransition = new HashMap<DecaTwoBTransition, String>();
		Map<DecaTwoBTransition, String> colorPerTransition = new HashMap<DecaTwoBTransition, String>();
		Set<DecaTwoBVertex> vtxs = new HashSet<DecaTwoBVertex>();
		
		for (DecaTwoBTransition t : trs) {
			vtxs.add(t.getSrc());
			vtxs.addAll(t.getTgts());
		}
		
		for (DecaTwoBVertex v : vtxs) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaTwoBTransition t : trs) {
			namePerTransition.put(t, "T" + namePerTransition.size());
			colorPerTransition.put(t, Dot.getRandomColor());
		}
		
		TextOptions.select(TextOptions.GRAPHVIZ_MIN);
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		
		{
			String s = "(initial)";
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : initialVertex.getLegacy().getStateConfig().getValuation().entrySet()) {
				s += "\\n" + entry.getKey().getName() + " := " + entry.getValue().toString();
			}
			
			out.println("\tI0 [label=\"" + s + "\", shape=circle, fontsize=10];");
		}
		
		for (DecaTwoBVertex v : vtxs) {
			String s = v.getName();
			
			for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : v.getLegacy().getStateConfig().getValuation().entrySet()) {
				if (!JPulse.class.isAssignableFrom(entry.getKey().getType()) || entry.getValue().toBoolean()) {
					s += "\\n" + TextOptions.current().id(entry.getKey().getName()) + " := " + entry.getValue().toString();
				}
			}
			
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse];");
		}
		
		for (DecaTwoBTransition t : trs) {
			Set<PulsePackMap> localInputs = new HashSet<PulsePackMap>();
			
			for (DecaTwoBTransition x : t.getSrc().getOutgoing()) {
				localInputs.addAll(x.getInputVals());
			}
			
			Set<PulsePackMap> zz = new HashSet<PulsePackMap>();
			
			for (PulsePackMap i : t.getInputVals()) {
				zz.add(i.extractMultiplePvsMap());
			}
			
			String s = "#vals = " + t.getInputVals().size() + " / " + localInputs.size() + " / " + allInputs.size();
			
			if (zz.size() <= 12) {
				for (PulsePackMap i : zz) {
					s += "\\n" + i.toString();
				}
			}
			
			out.println("\t" + namePerTransition.get(t) + " [label=\"" + s + "\", shape=rectangle, color=\"" + colorPerTransition.get(t) + "\", fontsize=10, fontcolor=\"" + colorPerTransition.get(t) + "\"];");
		}
		
		for (DecaTwoBTransition t : trs) {
			String t_name = namePerTransition.get(t);
			String sv_name = namePerVertex.get(t.getSrc());
			
			out.println("\t" + sv_name + " -> " + t_name + " [color=\"" + colorPerTransition.get(t) + "\"];");
			
			for (DecaTwoBVertex tgt : t.getTgts()) {
				String tv_name = namePerVertex.get(tgt);
				out.println("\t" + t_name + " -> " + tv_name + " [color=\"" + colorPerTransition.get(t) + "\"];");
			}
		}
		
		if (vtxs.contains(initialVertex)) {
			out.println("\tI0 -> " + namePerVertex.get(initialVertex) + ";");
		}
		
		out.println("}");
	}
}

