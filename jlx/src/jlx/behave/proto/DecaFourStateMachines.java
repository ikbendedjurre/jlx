package jlx.behave.proto;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALPort;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaFourStateMachines {
	public final String name;
	public final Map<JScope, DecaFourStateMachine> smPerScope;
	public final Map<JScope, DecaFourVertex> initialVertices;
	public final PulsePackMap initialInputs;
	public final Set<DecaFourVertex> vertices;
	public final Set<DecaFourTransition> transitions;
	public final LDDMapFactory<JScope, DecaFourVertex> cfgFactory;
	public final Set<InputEquivClz> inputEquivClzs;
	public final DecaFourStateConfig initCfg;
	public final NormMap<DecaFourStateConfig> configs;
	public final NormMap<Pair<DecaFourVertex>> vtxPairs;
	public final NormMap<ProtoTransition> protoTrs;
	public final NormMap<DecaFourVertex> vtxs;
	public final Set<DecaFourStateConfig> detConfigs;
	public final NormMap<List<DecaFourTgtGrp>> tgtGrps;
	public final Map<ASALPort, ASALPort> timeoutPortPerDurationPort;
	public final List<JScope> orderedScopes;
	public final Map<DecaFourStateConfig, Integer> levelPerCfg;
	
	public int SM_COUNT = 6;
	public boolean USE_RANDOM_SM = false;
	
	private double totalTrsCount;
	
	private static class Worker extends ConcurrentWorker {
		private final NormMap<DecaFourStateConfig> elems1;
		private final NormMap<Pair<DecaFourVertex>> elems2;
		private final NormMap<ProtoTransition> elems3;
		private double trsCount;
		
		public Worker() {
			elems1 = new NormMap<DecaFourStateConfig>();
			elems2 = new NormMap<Pair<DecaFourVertex>>();
			elems3 = new NormMap<ProtoTransition>();
			trsCount = 0;
		}
		
		@Override
		public String getSuffix() {
			return "; #cfgs = " + elems1.size() + "; #pairs = " + elems2.size() + "; #proto = " + elems3.size() + "; #trs = " + trsCount;
		}
		
		@Override
		public String toString() {
			return getSuffix();
		}
	}
	
	public void initCfgs() {
		System.out.println("[cfgs][" + LocalTime.now() + "] #cfgs = 0");
		
		configs.clear();
		configs.add(initCfg);
		vtxPairs.clear();
		protoTrs.clear();
		
		totalTrsCount = 0;
		levelPerCfg.clear();
		int level = 0;
		
		Set<DecaFourStateConfig> fringe = new HashSet<DecaFourStateConfig>();
		fringe.add(initCfg);
		
		while (fringe.size() > 0) {
			for (DecaFourStateConfig cfg : fringe) {
				levelPerCfg.put(cfg, level);
			}
			
			level++;
			
			ConcurrentWork<DecaFourStateConfig, Worker> cw = new ConcurrentWork<DecaFourStateConfig, Worker>();
			
			for (int index = 1; index <= 4; index++) {
				cw.getWorkers().add(new Worker());
			}
			
			cw.apply(fringe, (cfg, worker) -> {
				worker.trsCount += cfg.addAllSuccs(worker.elems1, worker.elems2, worker.elems3);
			});
			
			fringe.clear();
			
			for (Worker worker : cw.getWorkers()) {
				for (DecaFourStateConfig cfg : worker.elems1) {
					if (configs.add(cfg)) {
						fringe.add(cfg);
					}
				}
				
				vtxPairs.addAll(worker.elems2);
				protoTrs.addAll(worker.elems3);
				totalTrsCount += worker.trsCount;
			}
			
			System.out.println("[cfgs][" + LocalTime.now() + "] #cfgs = " + configs.size() + " (+" + fringe.size() + "); #pairs = " + vtxPairs.size() + "; #proto = " + protoTrs.size() + "; #trs = " + totalTrsCount);
		}
	}
	
	public DecaFourStateMachines(String name, Collection<DecaTwoBStateMachine> sources, boolean explore) {
		this.name = name;
		
		Map<JScope, DecaTwoBStateMachine> oldSmPerScope = new HashMap<JScope, DecaTwoBStateMachine>();
		Map<DecaTwoBVertex, DecaFourVertex> newVertexPerOldVertex = new HashMap<DecaTwoBVertex, DecaFourVertex>();
		
		for (DecaTwoBStateMachine s : sources) {
			oldSmPerScope.put(s.scope, s);
			
			for (DecaTwoBVertex oldVertex : s.vertices.values()) {
				newVertexPerOldVertex.put(oldVertex, new DecaFourVertex(s.scope, oldVertex));
			}
		}
		
		smPerScope = new HashMap<JScope, DecaFourStateMachine>();
		initialVertices = new HashMap<JScope, DecaFourVertex>();
		Map<ReprPort, PulsePack> initialValuePerPort = new HashMap<ReprPort, PulsePack>();
		vertices = new HashSet<DecaFourVertex>();
		transitions = new HashSet<DecaFourTransition>();
		inputEquivClzs = new HashSet<InputEquivClz>();
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>();
		cfgFactory = new LDDMapFactory<JScope, DecaFourVertex>(oldSmPerScope.keySet());
		levelPerCfg = new HashMap<DecaFourStateConfig, Integer>();
		
		int reqSetCount = 0;
		
		for (DecaTwoBStateMachine s : sources) {
			System.out.println("[" + LocalTime.now() + "] Building state machine for " + s.scope.getName());
			DecaFourStateMachine newSm = new DecaFourStateMachine(s, oldSmPerScope, newVertexPerOldVertex, cfgFactory);
			smPerScope.put(s.scope, newSm);
			initialVertices.put(s.scope, newSm.initialVertex);
			initialValuePerPort.putAll(newSm.initialInputs.getPackPerPort());
			vertices.addAll(newSm.vertices);
			transitions.addAll(newSm.transitions);
			inputEquivClzs.addAll(newSm.inputEquivClzs);
			timeoutPortPerDurationPort.putAll(s.timeoutPortPerDurationPort);
			
			for (DecaFourVertex v : newSm.vertices) {
				reqSetCount += v.getNondetOutgoingPerReqSet().size();
			}
		}
		
		System.out.println("#reqSets = " + reqSetCount);
		
		initialInputs = new PulsePackMap(initialValuePerPort, Dir.IN);
		
//		System.out.println("#initialInputs = " + initialInputs.extractValuation().size());
//		System.out.println("#initialInputs-external = " + initialInputs.extractExternalMap().extractValuation().size());
//		CLI.waitForEnter();
		
		//DecaFourStateConfigEncoder encoder = new DecaFourStateConfigEncoder(this);
		
		Map<JScope, List<DecaFourVertex>> vtxListPerScope = new HashMap<JScope, List<DecaFourVertex>>();
		
		for (DecaFourStateMachine sm : smPerScope.values()) {
			vtxListPerScope.put(sm.scope, new ArrayList<DecaFourVertex>(sm.vertices));
		}
		
		orderedScopes = new ArrayList<JScope>(smPerScope.keySet());
		initCfg = new DecaFourStateConfig(initialVertices);
		configs = new NormMap<DecaFourStateConfig>();
		vtxPairs = new NormMap<Pair<DecaFourVertex>>();
		vtxs = new NormMap<DecaFourVertex>();
		protoTrs = new NormMap<ProtoTransition>();
		
		if (name != null && explore) {
			initCfgs();
//			tgtGrps = extractTgtGrps(); //(Includes detTgtGrps.)
//			detConfigs = extractDetReachableCfgs(); //Order matters.
			tgtGrps = new NormMap<List<DecaFourTgtGrp>>();
			detConfigs = new HashSet<DecaFourStateConfig>();
		} else {
			configs.add(initCfg);
			tgtGrps = new NormMap<List<DecaFourTgtGrp>>();
			detConfigs = new HashSet<DecaFourStateConfig>();
		}
		
		for (Pair<DecaFourVertex> vtxPair : vtxPairs) {
			vtxs.add(vtxPair.getElem1());
			vtxs.add(vtxPair.getElem2());
		}
		
		System.out.println("DONE");
	}
	
	public DecaFourStateMachines(String name, Collection<DecaThreeStateMachine> sources, boolean explore, boolean HIDE_TOKEN) {
		this.name = name;
		
		Map<JScope, DecaThreeStateMachine> oldSmPerScope = new HashMap<JScope, DecaThreeStateMachine>();
		Map<DecaThreeVertex, DecaFourVertex> newVertexPerOldVertex = new HashMap<DecaThreeVertex, DecaFourVertex>();
		
		for (DecaThreeStateMachine s : sources) {
			oldSmPerScope.put(s.scope, s);
			
			for (DecaThreeVertex oldVertex : s.vertices.values()) {
				newVertexPerOldVertex.put(oldVertex, new DecaFourVertex(s.scope, oldVertex));
			}
		}
		
		smPerScope = new HashMap<JScope, DecaFourStateMachine>();
		initialVertices = new HashMap<JScope, DecaFourVertex>();
		Map<ReprPort, PulsePack> initialValuePerPort = new HashMap<ReprPort, PulsePack>();
		vertices = new HashSet<DecaFourVertex>();
		transitions = new HashSet<DecaFourTransition>();
		inputEquivClzs = new HashSet<InputEquivClz>();
		timeoutPortPerDurationPort = new HashMap<ASALPort, ASALPort>();
		cfgFactory = new LDDMapFactory<JScope, DecaFourVertex>(oldSmPerScope.keySet());
		levelPerCfg = new HashMap<DecaFourStateConfig, Integer>();
		
		int reqSetCount = 0;
		
		for (DecaThreeStateMachine s : sources) {
			System.out.println("[" + LocalTime.now() + "] Building state machine for " + s.scope.getName());
			DecaFourStateMachine newSm = new DecaFourStateMachine(s, oldSmPerScope, newVertexPerOldVertex, cfgFactory);
			smPerScope.put(s.scope, newSm);
			initialVertices.put(s.scope, newSm.initialVertex);
			initialValuePerPort.putAll(newSm.initialInputs.getPackPerPort());
			vertices.addAll(newSm.vertices);
			transitions.addAll(newSm.transitions);
			inputEquivClzs.addAll(newSm.inputEquivClzs);
			timeoutPortPerDurationPort.putAll(s.timeoutPortPerDurationPort);
			
			for (DecaFourVertex v : newSm.vertices) {
				reqSetCount += v.getNondetOutgoingPerReqSet().size();
			}
		}
		
		System.out.println("#reqSets = " + reqSetCount);
		
		initialInputs = new PulsePackMap(initialValuePerPort, Dir.IN);
		
//		System.out.println("#initialInputs = " + initialInputs.extractValuation().size());
//		System.out.println("#initialInputs-external = " + initialInputs.extractExternalMap().extractValuation().size());
//		CLI.waitForEnter();
		
		//DecaFourStateConfigEncoder encoder = new DecaFourStateConfigEncoder(this);
		
		Map<JScope, List<DecaFourVertex>> vtxListPerScope = new HashMap<JScope, List<DecaFourVertex>>();
		
		for (DecaFourStateMachine sm : smPerScope.values()) {
			vtxListPerScope.put(sm.scope, new ArrayList<DecaFourVertex>(sm.vertices));
		}
		
		orderedScopes = new ArrayList<JScope>(smPerScope.keySet());
		initCfg = new DecaFourStateConfig(initialVertices);
		configs = new NormMap<DecaFourStateConfig>();
		vtxPairs = new NormMap<Pair<DecaFourVertex>>();
		vtxs = new NormMap<DecaFourVertex>();
		protoTrs = new NormMap<ProtoTransition>();
		
		if (name != null && explore) {
			initCfgs();
//			tgtGrps = extractTgtGrps(); //(Includes detTgtGrps.)
//			detConfigs = extractDetReachableCfgs(); //Order matters.
			tgtGrps = new NormMap<List<DecaFourTgtGrp>>();
			detConfigs = new HashSet<DecaFourStateConfig>();
		} else {
			configs.add(initCfg);
			tgtGrps = new NormMap<List<DecaFourTgtGrp>>();
			detConfigs = new HashSet<DecaFourStateConfig>();
		}
		
		for (Pair<DecaFourVertex> vtxPair : vtxPairs) {
			vtxs.add(vtxPair.getElem1());
			vtxs.add(vtxPair.getElem2());
		}
		
		System.out.println("DONE");
	}
	
	public void printGraphvizFiles() {
		for (Map.Entry<JScope, DecaFourStateMachine> e : smPerScope.entrySet()) {
			e.getValue().printGraphvizFile();
		}
	}
	
	public double getTotalTrsCount() {
		return totalTrsCount;
	}
	
	public Map<JScope, DecaFourStateMachine> getSmPerScope() {
		return smPerScope;
	}
	
	public void printGraphvizFile(DecaFourStateConfig i, Collection<DecaFourStateConfig> vs) {
		try {
			PrintStream ps = new PrintStream("deca-four-deterministic.gv");
			printGraphvizFile(ps, i, vs);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printGraphvizFile(PrintStream out, DecaFourStateConfig i, Collection<DecaFourStateConfig> vs) {
		Map<DecaFourStateConfig, String> namePerVertex = new HashMap<DecaFourStateConfig, String>();
		Map<DecaFourStateConfig, String> namePerNondetTarget = new HashMap<DecaFourStateConfig, String>();
//		Map<DecaFourStateConfig, String> namePerPeerTarget = new HashMap<DecaFourStateConfig, String>();
		
		for (DecaFourStateConfig v : vs) {
			namePerVertex.put(v, "VTX" + namePerVertex.size());
			namePerNondetTarget.put(v, "X" + namePerNondetTarget.size());
			
//			Set<DecaFourStateConfig> peerTargets = v.getPeerTargets();
//			
//			if (peerTargets.size() > 0) {
//				for (DecaFourStateConfig ptc : peerTargets) {
//					namePerPeerTarget.put(ptc, "PEER" + namePerPeerTarget.size());
//				}
//			}
		}
		
		String initialVertexName = namePerVertex.get(i);
		
		out.println("// " + getClass().getCanonicalName());
		out.println("digraph G {");
		out.println("\tI0 [label=\"(initial)\", shape=circle, fontsize=8];");
		
		for (Map.Entry<DecaFourStateConfig, String> e : namePerVertex.entrySet()) {
			String s = "<" + e.getValue() + " (level " + e.getKey().level + ")>";
			
			for (DecaFourVertex v : e.getKey().getVtxs().values()) {
//				if (e.getKey().sourcePerm != null) {
//					DecaFourTransition t = e.getKey().sourcePerm.get(v.getScope());
//					s += "\\nBEFORE " + t.getSourceVertex().getName();
//					//s += "\\n" + Texts._break(t.getInput().createCombinedGuardStr().replace("\"", "\\\""), "\\n", 100000);
//				}
				
				s += "\\nNOW " + v.getName();
				s += "\\n" + Texts.concat(v.getSysmlClzs(), " + ", (c) -> { return c.getSimpleName(); });
				
				for (Map.Entry<ReprPort, ASALSymbolicValue> entry : v.getOutputVal().extractValuation().entrySet()) {
					if (!entry.getKey().getType().equals(JPulse.class) || entry.getValue().equals(ASALSymbolicValue.TRUE)) {
						s += "\\n" + Texts._break(entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\""), "\\n", 80);
					}
				}
			}
			
			out.println("\t" + e.getValue() + " [label=\"" + s + "\", shape=rectangle, fontsize=8];");
		}
		
//		for (Map.Entry<DecaFourStateConfig, String> e : namePerPeerTarget.entrySet()) {
//			String s = "<" + e.getValue() + " (level " + e.getKey().level + ")>";
//			
//			for (DecaFourVertex v : e.getKey().getVtxs().values()) {
//				if (e.getKey().sourcePerm != null) {
//					DecaFourTransition t = e.getKey().sourcePerm.get(v.getScope());
//					s += "\\nBEFORE " + t.getSourceVertex().getName();
//					s += "\\n" + Texts._break(t.getInput().createCombinedGuardStr().replace("\"", "\\\""), "\\n", 100000);
//				}
//				
//				s += "\\nNOW " + v.getName();
//				s += "\\n" + Texts.concat(v.getClzs(), " + ", (c) -> { return c.getSimpleName(); });
//				
//				for (Map.Entry<ReprPort, ASALSymbolicValue> entry : v.getOutputVal().entrySet()) {
//					if (!entry.getKey().getType().equals(JPulse.class) || entry.getValue().equals(ASALSymbolicValue.TRUE)) {
//						s += "\\n" + Texts._break(entry.getKey().getName() + " := " + entry.getValue().toString().replace("\"", "\\\""), "\\n", 80);
//					}
//				}
//			}
//			
//			out.println("\t" + e.getValue() + " [label=\"" + s + "\", shape=rectangle, fontsize=8, style=dashed];");
//		}
		
//		for (DecaFourStateConfig cfg : vs) {
//			Set<DecaFourVertex> tgts = cfg.getNondetTargets();
//			
//			if (tgts.size() > 0) {
//				String s = "<nondet-targets>";
//				
//				for (DecaFourVertex v : tgts) {
//					s += "\\n" + v.getName();
//				}
//				
//				out.println("\t" + namePerNondetTarget.get(cfg) + " [label=\"" + s + "\", shape=rectangle, fontsize=8];");
//				out.println("\t" + namePerVertex.get(cfg) + " -> " + namePerNondetTarget.get(cfg) + " [];");
//			}
//		}
//		
//		int counter = 0;
//		
//		for (DecaFourStateConfig cfg : vs) {
//			
//		}
		
		out.println("\tI0 -> " + initialVertexName + ";");
		out.println("}");
	}
}

