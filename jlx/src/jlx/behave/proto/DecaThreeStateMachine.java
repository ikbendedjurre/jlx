package jlx.behave.proto;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.common.reflection.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * This state machine has computed valuations for inputs.
 * Inputs are grouped together 
 * Each vertex has one transition per guard equivalence class,
 * although each transition can target more than one vertex.
 */
public class DecaThreeStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	//public final Map<ReprPort, Set<ASALSymbolicValue>> pvsPerInput;
	public final Set<InputEquivClz> inputEquivClzs;
	public final Map<ASALSymbolicValue, Set<DecaTwoTransition>> legacyTrsPerGuard;
	//public final Map<Map<ReprPort, ASALSymbolicValue>, Map<ReprPort, ASALSymbolicValue>> inputValuations;
	public final Map<DecaThreeStateConfig, DecaThreeVertex> vertices;
	public final DecaThreeVertex initialVertex;
	public final PulsePackMap initialInputs;
	public final Set<ASALVariable> propsAndOutputs;
	public final Set<DecaThreeTransition> transitions;
//	public final Map<ReprPort, ASALSymbolicValue> timePerTimeoutPort;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final DecaTwoStateMachine legacy;
	
	private Set<DecaThreeTransition> computeTransitions(DecaThreeStateConfig source, Map<DecaTwoVertex, Map<ASALVariable, ASALSymbolicValue>> outputValPerVtx) {
		Map<Set<DecaThreeStateConfig>, Set<InputEquivClz>> inputsPerTgtSet = new HashMap<Set<DecaThreeStateConfig>, Set<InputEquivClz>>();
		Map<Set<DecaThreeStateConfig>, Set<DecaTwoTransition>> enabledTrsPerTgtSet = new HashMap<Set<DecaThreeStateConfig>, Set<DecaTwoTransition>>();
		
		for (InputEquivClz i : inputEquivClzs) {
			Set<DecaTwoVertex> tgts = new HashSet<DecaTwoVertex>();
			Set<DecaTwoTransition> enabledTrs = new HashSet<DecaTwoTransition>();
			Set<DecaTwoTransition> disabledTrs = new HashSet<DecaTwoTransition>();
			
			for (DecaTwoTransition t : legacy.transitions) {
				DecaTwoVertex srcVertex = legacy.vertices.get(t.getSourceStateConfig());
				DecaTwoVertex tgtVertex = legacy.vertices.get(t.getTargetStateConfig());
				
				if (source.getVertex().equals(srcVertex)) {
					if (i.getEnabledGuards().contains(t.getGuard())) {
						enabledTrs.add(t);
						tgts.add(tgtVertex);
					} else {
						if (i.getDisabledGuards().contains(t.getGuard())) {
							disabledTrs.add(t);
						}
					}
				}
			}
			
			if (tgts.size() > 0) {
				Set<DecaThreeStateConfig> targetStateConfigs = new HashSet<DecaThreeStateConfig>();
				
				for (DecaTwoVertex tgt : tgts) {
					targetStateConfigs.add(new DecaThreeStateConfig(tgt, outputValPerVtx.get(tgt)));
				}
				
				HashMaps.inject(inputsPerTgtSet, targetStateConfigs, i);
				HashMaps.injectAll(enabledTrsPerTgtSet, targetStateConfigs, enabledTrs);
				
				//new DecaThreeTransition(enabledTrs, disabledTrs, i, source, targetStateConfigs)
			} else {
//				if (MUTEX_INPUT_CLASSES) {
//					throw new Error("This should not happen, states should be input-enabled here!!");
//				}
			}
		}
		
		Set<DecaThreeTransition> result = new HashSet<DecaThreeTransition>();
		
		for (Map.Entry<Set<DecaThreeStateConfig>, Set<InputEquivClz>> e : inputsPerTgtSet.entrySet()) {
			Set<DecaTwoTransition> enabledTrs = enabledTrsPerTgtSet.get(e.getKey());
			result.add(new DecaThreeTransition(enabledTrs, Collections.emptySet(), e.getValue(), source, e.getKey()));
		}
		
//		for (DecaTwoTransition t : legacy.transitions) {
//			DecaTwoVertex srcVertex = legacy.vertices.get(t.getSourceStateConfig());
//			
//			if (source.getVertices().contains(srcVertex)) {
//				t.get
//				
//				Permutations.inject(trsPerGuard, t.getGuard(), t);
//			}
//		}
		
		return result;
	}
	
	public DecaThreeStateMachine(String name, DecaTwoStateMachine source) throws ClassReflectionException, ModelException {
		instance = source.instance;
		scope = source.scope;
		legacy = source;
		
		Map<DecaTwoVertex, Map<ASALVariable, ASALSymbolicValue>> outputValPerVtx = new HashMap<DecaTwoVertex, Map<ASALVariable, ASALSymbolicValue>>();
		
		for (DecaTwoVertex v : legacy.vertices.values()) {
			outputValPerVtx.put(v, v.getStateConfig().getValuation());
		}
		
//		timePerTimeoutPort = new HashMap<ReprPort, ASALSymbolicValue>(source.timePerTimeoutPort);
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>(source.timeoutPortPerDurationPort);
		
		//Collect all guards, and all transitions that make use of them:
		legacyTrsPerGuard = new HashMap<ASALSymbolicValue, Set<DecaTwoTransition>>();
		
		for (DecaTwoTransition t : source.transitions) {
			HashMaps.inject(legacyTrsPerGuard, t.getGuard(), t);
		}
		
		Map<ASALSymbolicValue, Set<PulsePackMap>> inputValsPerGuard = new HashMap<ASALSymbolicValue, Set<PulsePackMap>>();
		
		for (Map<ReprPort, ASALSymbolicValue> inputVal : HashMaps.allCombinations(source.pvsPerInput)) {
			PulsePackMap ppm = PulsePackMap.from(inputVal, Dir.IN);
			
			for (ASALSymbolicValue guard : legacyTrsPerGuard.keySet()) {
				if (guard.substitute(inputVal).toBoolean()) {
					HashMaps.inject(inputValsPerGuard, guard, ppm);
				}
			}
		}
		
		inputEquivClzs = new HashSet<InputEquivClz>();
		
		for (Map.Entry<ASALSymbolicValue, Set<PulsePackMap>> e : inputValsPerGuard.entrySet()) {
			inputEquivClzs.add(new InputEquivClz(e.getValue(), Collections.singleton(e.getKey()), Collections.emptySet()));
		}
		
		System.out.println("#inputEquivClzs(" + scope.getName() + ") = " + inputEquivClzs.size());
		
		initialInputs = PulsePackMap.from(source.initialInputs, Dir.IN);
//		InputEquivClz.printVal(scope.getName() + ".initialInputs", source.initialInputs);
		propsAndOutputs = new HashSet<ASALVariable>(source.propsAndOutputs);
		
		initialVertex = new DecaThreeVertex(new DecaThreeStateConfig(source.initialVertex, outputValPerVtx.get(legacy.initialVertex)), 0, 0);
		vertices = new HashMap<DecaThreeStateConfig, DecaThreeVertex>();
		vertices.put(initialVertex.getStateConfig(), initialVertex);
		transitions = new HashSet<DecaThreeTransition>();
		
		//Map<Map<ReprPort, ASALSymbolicValue>, Set<Set<ASALSymbolicValue>>> bla = Permutations.splitValuesByKeys(valsPerGuardSet);
		
		Set<DecaThreeVertex> fringe = new HashSet<DecaThreeVertex>();
		Set<DecaThreeVertex> newFringe = new HashSet<DecaThreeVertex>();
		fringe.add(initialVertex);
		
		int level = 1;
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaThreeVertex v : fringe) {
				for (DecaThreeTransition t : computeTransitions(v.getStateConfig(), outputValPerVtx)) {
					transitions.add(t);
					
					for (DecaThreeStateConfig targetStateConfig : t.getTargetStateConfigs()) {
						if (!vertices.containsKey(targetStateConfig)) {
							DecaThreeVertex targetVertex = new DecaThreeVertex(targetStateConfig, level, vertices.size());
							vertices.put(targetStateConfig, targetVertex);
							newFringe.add(targetVertex);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #beenHere = " + vertices.size() + " (+" + fringe.size() + "); depth = " + level);
			
			level++;
		}
		
		for (DecaThreeTransition t : transitions) {
			t.populateVertices(vertices);
			t.getSourceVertex().getOutgoing().add(t);
		}
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-three-" + scope.getName() + ".gv");
			printGraphvizFile2(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile2(PrintStream out) {
		Map<DecaThreeVertex, String> namePerVertex = new HashMap<DecaThreeVertex, String>();
		Map<DecaThreeVertex, String> colorPerVertex = new HashMap<DecaThreeVertex, String>();
		
		for (DecaThreeVertex v : vertices.values()) {
			namePerVertex.put(v, "V" + namePerVertex.size());
			colorPerVertex.put(v, Dot.getRandomColor());
		}
		
		Map<DecaThreeVertex, Set<DecaThreeVertex>> detTgtsPerSrc = new HashMap<DecaThreeVertex, Set<DecaThreeVertex>>();
		Map<DecaThreeVertex, Set<DecaThreeVertex>> nonDetTgtsPerSrc = new HashMap<DecaThreeVertex, Set<DecaThreeVertex>>();
		
		for (DecaThreeTransition t : transitions) {
			if (t.getTargetStateConfigs().size() == 1) {
				for (DecaThreeStateConfig tgt : t.getTargetStateConfigs()) {
					HashMaps.inject(detTgtsPerSrc, vertices.get(t.getSourceStateConfig()), vertices.get(tgt));
				}
			} else {
				for (DecaThreeStateConfig tgt : t.getTargetStateConfigs()) {
					HashMaps.inject(nonDetTgtsPerSrc, vertices.get(t.getSourceStateConfig()), vertices.get(tgt));
				}
			}
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("// " + scope.getName());
		out.println("digraph G {");
		
		{
			String s = "(initial)";
			
			for (Map.Entry<ReprPort, PulsePack> entry : initialVertex.getStateConfig().getOutputVal().getPackPerPort().entrySet()) {
				s += "\\n" + entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\"");
			}
			
			out.println("\tI0 [label=\"" + s + "\", shape=circle, fontsize=10];");
		}
		
		for (DecaThreeVertex v : vertices.values()) {
			String s = v.getName();
			
			for (Map.Entry<ReprPort, PulsePack> entry : v.getStateConfig().getOutputVal().getPackPerPort().entrySet()) {
				s += "\\n" + Texts._break(entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\""), "\\n", 70);
			}
			
			String colors = "color=\"" + colorPerVertex.get(v) + "\", fontcolor=\"" + colorPerVertex.get(v) + "\"";
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=ellipse, fontsize=10, " + colors + "];");
		}
		
		for (Map.Entry<DecaThreeVertex, Set<DecaThreeVertex>> e : detTgtsPerSrc.entrySet()) {
			String color = "color=\"" + colorPerVertex.get(e.getKey()) + "\"";
			String sv_name = namePerVertex.get(e.getKey());
			
			for (DecaThreeVertex tgt : e.getValue()) {
				String tv_name = namePerVertex.get(tgt);
				out.println("\t" + sv_name + " -> " + tv_name + " [" + color + ", style=solid];");
			}
			
			Map<InputEquivClz, Set<DecaThreeTransition>> trsPerInput = new HashMap<InputEquivClz, Set<DecaThreeTransition>>();
			
			for (DecaThreeTransition t : transitions) {
				if (t.getSourceStateConfig().equals(e.getKey().getStateConfig())) {
					for (InputEquivClz i : t.getInputs()) {
						HashMaps.inject(trsPerInput, i, t);
					}
				}
			}
			
			for (Map.Entry<InputEquivClz, Set<DecaThreeTransition>> e2 : trsPerInput.entrySet()) {
				if (e2.getValue().size() > 1) {
					throw new Error("luiggrnl;nnnnnnnnnnnnnnnnnnnnn");
				}
			}
		}
		
		for (Map.Entry<DecaThreeVertex, Set<DecaThreeVertex>> e : nonDetTgtsPerSrc.entrySet()) {
			String color = "color=\"" + colorPerVertex.get(e.getKey()) + "\"";
			String sv_name = namePerVertex.get(e.getKey());
			
			for (DecaThreeVertex tgt : e.getValue()) {
				String tv_name = namePerVertex.get(tgt);
				out.println("\t" + sv_name + " -> " + tv_name + " [" + color + ", style=dashed];");
			}
		}
		
		out.println("\tI0 -> " + namePerVertex.get(initialVertex) + ";");
		out.println("}");
	}
}








