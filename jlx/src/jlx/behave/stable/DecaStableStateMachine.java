package jlx.behave.stable;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

import jlx.asal.j.JPulse;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALPort;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaStableStateMachine {
	public final DecaFourStateMachines legacy;
	public final NormMap<DecaStableVertex> vertices;
	public final DecaStableTransition initialTransition;
	public final Set<DecaStableTransition> transitions;
	public final Map<ReprPort, ASALSymbolicValue> initialInputs;
	public final Map<ASALPort, ASALPort> durationPortPerTimeoutPort;
	public final Set<DecaFourStateConfig> coveredCfgs;
	public final Set<Pair<DecaFourVertex>> vtxPairs;
	public final Set<DecaFourVertex> vtxs;
	public final Set<Pair<DecaFourStateConfig>> cfgPairs;
	public final Set<ProtoTransition> protoTrs;
//	public final Map<Integer, Integer> cfgCountPerLevel;
	
	public DecaStableStateMachine(DecaFourStateMachines source, boolean explore) {
		legacy = source;
		
		System.out.println("111");
		
		initialInputs = extractInitialInputs();
		durationPortPerTimeoutPort = new HashMap<ASALPort, ASALPort>();
		
		for (Map.Entry<ASALPort, ASALPort> e : source.timeoutPortPerDurationPort.entrySet()) {
			durationPortPerTimeoutPort.put(e.getValue(), e.getKey());
		}
		
		PulsePackMap initialExternalInputs = source.initialInputs.extractExternalMap();
		
		System.out.println("222");
		
		protoTrs = new HashSet<ProtoTransition>();
		Set<DecaFourStateConfig> cfgs = new HashSet<DecaFourStateConfig>();
		List<DecaFourStateConfig> cfgSeq;
		
		if (explore) {
			DecaFourStateConfigPath p = DecaFourStateConfig.followInputs(Collections.singleton(source.initCfg), initialExternalInputs, null);
			cfgSeq = p.getCfgs();
			protoTrs.addAll(p.getProtoTrs());
		} else {
			cfgSeq = Collections.singletonList(source.initCfg);
		}
		
		cfgs.addAll(cfgSeq);
		
		DecaStableVertex v1 = new DecaStableVertex(cfgSeq.get(cfgSeq.size() - 1), initialExternalInputs, 1);
		initialTransition = new DecaStableTransition(null, v1, initialExternalInputs, cfgSeq);
		
//		Set<DecaStableVertex> result = new HashSet<DecaStableVertex>();
		
		vertices = new NormMap<DecaStableVertex>();
		vertices.add(v1);
		
//		Set<DecaStableVertex> beenHere = new HashSet<DecaStableVertex>();
//		beenHere.add(v1);
		
//		Set<DecaFourStateConfig> stableCfgs = new HashSet<DecaFourStateConfig>();
		
//		cfgCountPerLevel = new TreeMap<Integer, Integer>();
		
		Set<DecaStableVertex> fringe = new HashSet<DecaStableVertex>();
		Set<DecaStableVertex> newFringe = new HashSet<DecaStableVertex>();
		fringe.add(v1);
		
		WallClock.reset();
		int trsCount = 0;
//		int level = 0;
		
		System.out.println("333");
		
		while (explore && fringe.size() > 0) {
//			cfgCountPerLevel.put(level, fringe.size());
//			level++;
			
			ConcurrentWork<DecaStableVertex, ConcurrentWorker> cw = new ConcurrentWork<DecaStableVertex, ConcurrentWorker>();
			
			for (int i = 1; i <= 4; i++) {
				ConcurrentWorker w = new ConcurrentWorker("#stable", "#proto", "#non-det");
				w.set("#stable", new NormMap<DecaStableVertex>());
				w.set("#proto", new HashSet<ProtoTransition>());
				w.set("#non-det", 0);
				cw.getWorkers().add(w);
			}
			
			cw.apply(fringe, (v, worker) -> {
				int nd = v.populateOutgoing(source, worker.get("#stable", NormMap.class), worker.get("#proto", Set.class), true);
				worker.set("#non-det", worker.get("#non-det", Integer.class) + nd);
			});
			
//			ConcurrentDoubleSetBuild<DecaStableVertex, DecaStableVertex, ProtoTransition> csb = new ConcurrentDoubleSetBuild<DecaStableVertex, DecaStableVertex, ProtoTransition>();
//			
//			for (int i = 1; i <= 4; i++) {
//				csb.getWorkers().add(new ConcurrentDoubleSetBuilder<DecaStableVertex, ProtoTransition>("stable", "proto"));
//			}
//			
//			//csb.createDefaultWorkers("stable", vertices, 8);
//			
//			WallClock.tick("apply");
//			
//			csb.apply(fringe, (v, worker) -> {
//				v.populateOutgoing(source, worker.getElems1(), worker.getElems2(), true);
//			});
//			
//			WallClock.tock("apply");
			
			for (DecaStableVertex v : fringe) {
				trsCount += v.getOutgoing().size();
			}
			
			newFringe.clear();
//			csb.combineOutputs();
//			
//			for (DecaStableVertex v : csb.getCombinedOutputs1()) {
//				if (vertices.add(v)) {
//					newFringe.add(v);
//				}
//			}
//			
//			protoTrs.addAll(csb.getCombinedOutputs2());
//			
//			csb.apply(fringe, (v, worker) -> {
//				v.normalizeSuccs(vertices);
//			});
			
			for (ConcurrentWorker w : cw.getWorkers()) {
				for (Object v : w.get("#stable", NormMap.class)) {
					if (vertices.add((DecaStableVertex)v)) {
						newFringe.add((DecaStableVertex)v);
					}
				}
				
				for (Object v : w.get("#proto", Set.class)) {
					protoTrs.add((ProtoTransition)v);
				}
			}
			
			cw.apply(fringe, (v, worker) -> {
				v.normalizeSuccs(vertices);
			});
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[stable][" + LocalTime.now() + "] #vtxs = " + vertices.size() + " (+" + fringe.size() + "); #trs = " + trsCount);
		}
		
		System.out.println("444");
		
//		ConcurrentUpdates<DecaStableVertex> cw = new ConcurrentUpdates<DecaStableVertex>();
//		cw.createDefaultWorkers(8);
//		
//		cw.apply(vertices.uniqueElems(), (v, worker) -> {
//			//v.populateOutgoing(source, mode, vertices, Collections.emptyList());
//			v.normalizeSuccs(vertices);
//		});
		
		System.out.println("#vtxs-non-unique = " + vertices.size());
		vertices.retainUnique();
		System.out.println("#vtxs-unique = " + vertices.size());
		
//		while (fringe.size() > 0) {
//			newFringe.clear();
//			
//			ConcurrentUpdates<DecaStableVertex> csb = new ConcurrentUpdates<DecaStableVertex>();
//			csb.createDefaultWorkers(8);
//			
//			csb.apply(fringe, (v, worker) -> {
//				v.populateOutgoing(source, mode, vertices, legacy.timeoutPortPerDurationPort.values());
//			});
//			
//			for (DecaStableVertex v : fringe) {
//				if (mode == 2) {
//					if (stableCfgs.add(v.getCfg())) {
//						for (DecaStableVertex w : v.populateSelfLoops(source, vertices, legacy.timeoutPortPerDurationPort.values())) {
//							//Do nothing!
//							//Self-loop vertices are automatically explored via outgoing transitions.
//							//(I think.)
//						}
//					}
//				}
//				
//				cfgs.add(v.getCfg());
//				
//				for (DecaStableTransition t : v.getOutgoing()) {
//					cfgs.addAll(t.getSeq());
//					cfgs.add(t.getTgt().getCfg());
//				}
//				
//				for (DecaStableVertex succ : v.getSuccs()) {
//					if (beenHere.add(succ)) {
//						newFringe.add(succ);
//					}
//				}
//			}
//			
//			fringe.clear();
//			fringe.addAll(newFringe);
//			
//			System.out.println("[stable][" + LocalTime.now() + "] #beenHere = " + beenHere.size() + " (+" + fringe.size() + "); #cfgs = " + cfgs.size());
//		}
		
		transitions = new HashSet<DecaStableTransition>();
		cfgPairs = new HashSet<Pair<DecaFourStateConfig>>();
		
		for (DecaStableVertex v : vertices) {
			int hid = 0;
			
			for (DecaStableTransition t : v.getOutgoing()) {
				if (t.isHiddenTimerTrigger()) {
					hid++;
				}
				
				if (!vertices.contains(t.getTgt())) {
					throw new Error("Should not happen!");
				}
				
				cfgPairs.add(new Pair<DecaFourStateConfig>(t.getSrc().getCfg(), t.getTgt().getCfg()));
			}
			
			if (hid > 1) {
				throw new Error("Should not happen!");
			}
			
			transitions.addAll(v.getOutgoing());
		}
		
		vtxPairs = new HashSet<Pair<DecaFourVertex>>();
		vtxs = new HashSet<DecaFourVertex>();
		
		for (DecaStableTransition t : transitions) {
			vtxPairs.addAll(t.computeVtxPairs());
		}
		
		for (Pair<DecaFourVertex> vtxPair : vtxPairs) {
			vtxs.add(vtxPair.getElem1());
			vtxs.add(vtxPair.getElem2());
		}
		
		for (DecaStableTransition t : transitions) {
			t.getTgt().getPreds().add(t.getSrc());
		}
		
		coveredCfgs = new HashSet<DecaFourStateConfig>();
		
		for (DecaStableVertex v : vertices) {
			coveredCfgs.add(v.getCfg());
		}
		
		final int stableCfgCount = coveredCfgs.size();
		
		for (DecaStableTransition t : transitions) {
			coveredCfgs.addAll(t.getSeq());
		}
		
		System.out.println("[stable][" + LocalTime.now() + "] #(cfg,input)s = " + vertices.size() + "; #transitions = " + transitions.size() + "; #stable-cfgs = " + stableCfgCount + "; #covered-cfgs = " + coveredCfgs.size());
		
//		//Sanity checking only (I think):
//		for (DecaStableVertex v : fringe) {
//			Set<Map<ReprPort, ASALSymbolicValue>> sicInputs = v.getCfg().getSelfLoopZeroPulseSicInputs(v.getExternalIncomingInputs().getValuePerPort(), legacy.timeoutPortPerDurationPort.values());
//			
//			for (DecaStableTransition t : v.getOutgoing()) {
//				if (t.getTgt().getCfg().equals(v.getCfg())) {
//					if (t.getSicInputs().withoutActivePulsesOrInternal().equals(t.getSicInputs().getValuePerPort())) {
//						if (!sicInputs.contains(t.getSicInputs().getValuePerPort())) {
//							System.out.println("Unexpectedly, self-loop is found generally but specially!");
//							CLI.waitForEnter();
//						}
//					}
//				}
//			}
//		}
		
		if ("input-enabledness-2".equals("off")) {
			Map<PulsePackMap, Set<DecaStableVertex>> cfgsPerOutputVal = new HashMap<PulsePackMap, Set<DecaStableVertex>>();
			Map<PulsePackMap, Set<DecaStableVertex>> cfgsPerExternalOutputVal = new HashMap<PulsePackMap, Set<DecaStableVertex>>();
			
			for (DecaStableVertex v : vertices) {
				HashMaps.inject(cfgsPerOutputVal, v.getCfg().getOutputVal(), v);
				HashMaps.inject(cfgsPerExternalOutputVal, v.getCfg().getOutputVal().extractExternalMap(), v);
			}
			
			int count = 0;
			
			for (Map.Entry<PulsePackMap, Set<DecaStableVertex>> e : cfgsPerExternalOutputVal.entrySet()) {
				Set<DecaStableInputChanges> inputChangesSet = new HashSet<DecaStableInputChanges>();
				
				for (DecaStableVertex v : e.getValue()) {
					for (DecaStableTransition t : v.getOutgoing()) {
						inputChangesSet.add(t.getInputChanges(this));
					}
				}
				
				for (DecaStableVertex v : e.getValue()) {
					Set<DecaStableInputChanges> outgoingInputChanges = v.getOutgoingInputChanges(this); 
					
					for (DecaStableInputChanges inputChanges : inputChangesSet) {
						if (!outgoingInputChanges.contains(inputChanges)) {
							PulsePackMap inputVal = inputChanges.applyToValuation(v.getExternalIncomingInputs());
							v.getOutgoing().add(new DecaStableTransition(v, inputVal, false));
							count++;
						}
					}
				}
			}
			
			System.out.println("Added " + count + " no-input-change self-loops!");
		}
	}
	
	private Map<ReprPort, ASALSymbolicValue> extractInitialInputs() {
		Map<ReprPort, ASALSymbolicValue> result = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : legacy.initialInputs.extractValuation().entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
					if (e.getValue().toBoolean()) {
						throw new Error("No support for initialization to TRUE of pulse port (" + e.getKey().getReprOwner().getName() + "::" + e.getKey().getName() + ")!");
					}
				} else {
					if (e.getKey().getPulsePort() == null) {
						result.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		
		return result;
	}
	
	public void printGraphvizFile() {
		try {
			PrintStream ps = new PrintStream("deca-stable-overview.gv");
			printGraphvizFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out) {
		TextOptions.select(TextOptions.GRAPHVIZ_FULL);
		
		Map<DecaStableVertex, String> namePerVertex = new HashMap<DecaStableVertex, String>();
		Map<DecaStableTransition, String> namePerTransition = new HashMap<DecaStableTransition, String>();
		
		for (DecaStableVertex v : vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaStableTransition t : transitions) {
			if (t.getTgt() != t.getSrc()) {
				namePerTransition.put(t, "T" + namePerTransition.size());
			}
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"initial\", style=none];");
		
		for (DecaStableVertex v : vertices) {
			List<String> elems = new ArrayList<String>();
			
			for (Map.Entry<ReprPort, ASALSymbolicValue> e : v.getOutputVal().extractValuation().entrySet()) {
				elems.add(e.getKey().getName() + " = " + e.getValue());
			}
			
			Collections.sort(elems);
			String s = "";
//			s += v.getCfg().getDescription("\\n");
			s += Texts.concat(elems, "\\n");
			
			out.println("\t" + namePerVertex.get(v) + " [label=\"" + s + "\", shape=circle];");
			
//			for (DecaStableVertex succ : v.getSuccs()) {
//				if (succ != v) {
//					out.println("\t" + namePerVertex.get(v) + " -> " + namePerVertex.get(succ) + ";");
//				}
//			}
		}
		
		for (DecaStableTransition t : transitions) {
			if (t.getSicInputs() != null) {
				if (t.getTgt() != t.getSrc()) {
					List<String> elems = new ArrayList<String>();
					
					for (Map.Entry<ReprPort, ASALSymbolicValue> e : t.getSicInputs().extractValuation().entrySet()) {
						ASALSymbolicValue v = t.getSrc().getExternalIncomingInputs().getPortValue(e.getKey(), false);
						
						if (v == null || !v.equals(e.getValue())) {
							elems.add(e.getKey().getName() + " = " + e.getValue());
						}
					}
					
					out.println("\t" + namePerTransition.get(t) + " [label=\"" + Texts.concat(elems, "\\n") + "\", shape=rect];");
				}
			}
		}
		
		for (DecaStableTransition t : transitions) {
			if (t.getSicInputs() != null) {
				if (t.getTgt() != t.getSrc()) {
					String v1 = namePerVertex.get(t.getSrc());
					String v2 = namePerTransition.get(t);
					String v3 = namePerVertex.get(t.getTgt());
					
					out.println("\t" + v1 + " -> " + v2 + ";");
					out.println("\t" + v2 + " -> " + v3 + ";");
				}
			}
		}
		
		out.println("\tI0 -> " + namePerVertex.get(initialTransition.getTgt()) + ";");
		out.println("}");
	}
	
	public void printGraphvizFile2(PrintStream out) {
		Set<Set<DecaStableVertex>> vtxGrps = new HashSet<Set<DecaStableVertex>>();
		Set<DecaStableVertex> remainingVtxs = new HashSet<DecaStableVertex>(vertices);
		
		while (remainingVtxs.size() > 0) {
			DecaStableVertex v = remainingVtxs.iterator().next();
			Set<DecaStableVertex> newVtxGrp = new HashSet<DecaStableVertex>();
			
			for (DecaStableVertex succ : v.getSuccs()) {
				if (succ.getSuccs().contains(v)) {
					if (remainingVtxs.contains(succ)) {
						newVtxGrp.add(v);
					}
				}
			}
			
			remainingVtxs.removeAll(newVtxGrp);
			vtxGrps.add(newVtxGrp);
		}
		
		Map<DecaStableVertex, Set<DecaStableVertex>> vtxGrpPerVtx = new HashMap<DecaStableVertex, Set<DecaStableVertex>>();
		
		for (Set<DecaStableVertex> vtxGrp : vtxGrps) {
			for (DecaStableVertex v : vtxGrp) {
				vtxGrpPerVtx.put(v, vtxGrp);
			}
		}
		
		Map<Set<DecaStableVertex>, Set<Set<DecaStableVertex>>> succVtxGrpsPerVtxGrp = new HashMap<Set<DecaStableVertex>, Set<Set<DecaStableVertex>>>();
		
		for (Set<DecaStableVertex> vtxGrp : vtxGrps) {
			Set<Set<DecaStableVertex>> succVtxGrps = new HashSet<Set<DecaStableVertex>>();
			
			for (DecaStableVertex v : vtxGrp) {
				for (DecaStableVertex succ : v.getSuccs()) {
					succVtxGrps.add(vtxGrpPerVtx.get(succ));
				}
			}
			
			succVtxGrpsPerVtxGrp.put(vtxGrp, succVtxGrps);
		}
		
		Map<Set<DecaStableVertex>, String> namePerVtxGrp = new HashMap<Set<DecaStableVertex>, String>();
		
		for (Set<DecaStableVertex> v : vtxGrps) {
			namePerVtxGrp.put(v, "V" + namePerVtxGrp.size());
		}
		
		out.println("// " + getClass().getCanonicalName());
		out.println("digraph G {");
		out.println("\tnode [label=\"\", shape=circle, width=0.1, fillcolor=black, style=filled];");
		out.println("\tI0 [style=none];");
		
		for (Set<DecaStableVertex> vtxGrp : vtxGrps) {
			for (Set<DecaStableVertex> succVtxGrp : succVtxGrpsPerVtxGrp.get(vtxGrp)) {
				if (succVtxGrp != vtxGrp) {
					out.println("\t" + namePerVtxGrp.get(vtxGrp) + " -> " + namePerVtxGrp.get(succVtxGrp) + ";");
				}
			}
		}
		
		out.println("\tI0 -> " + namePerVtxGrp.get(vtxGrpPerVtx.get(initialTransition.getTgt())) + ";");
		out.println("}");
	}
}

