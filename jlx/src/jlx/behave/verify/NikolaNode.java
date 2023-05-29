package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.*;

public class NikolaNode {
	private final Set<PalmaNode> nodes;
	private final Map<PulsePackMap, Set<NikolaNode>> outgoing;
	private final PulsePackMap output;
	
	public NikolaNode(Set<PalmaNode> nodes, PulsePackMap output) {
		this.nodes = nodes;
		this.output = output;
		
		outgoing = new HashMap<PulsePackMap, Set<NikolaNode>>();
	}
	
	public Set<PalmaNode> getNodes() {
		return nodes;
	}
	
	public Map<PulsePackMap, Set<NikolaNode>> getOutgoing() {
		return outgoing;
	}
	
	public PulsePackMap getOutput() {
		return output;
	}
	
	public void populateOutgoing(NikolaGraph g, Map<PalmaNode, NikolaNode> nodePerNode) {
		Map<PulsePackMap, Set<NikolaNode>> newOutgoing = new HashMap<PulsePackMap, Set<NikolaNode>>();
		
		for (PalmaNode n : nodes) {
			for (Map.Entry<PulsePackMap, Set<PalmaNode>> e : n.getOutgoing().entrySet()) {
				for (PalmaNode n2 : e.getValue()) {
					HashMaps.inject(newOutgoing, e.getKey(), nodePerNode.get(n2));
				}
			}
		}
		
//		Map<Set<PulsePackMap>, Set<NikolaNode>> newOutgoingGrouped = HashMaps.mergeKeysByValues(newOutgoing);
//		Map<Set<PulsePackMap>, Set<NikolaNode>> newOutgoingTrimmed = new HashMap<Set<PulsePackMap>, Set<NikolaNode>>();
//		
//		for (Map.Entry<Set<PulsePackMap>, Set<NikolaNode>> e : newOutgoingGrouped.entrySet()) {
//			Set<PulsePackMap> trimmed = PulsePackMap.trim(e.getKey(), g.getPvsPerInputPort(), Dir.IN);
//			newOutgoingTrimmed.put(trimmed, e.getValue());
//		}
		
		outgoing.clear();
		outgoing.putAll(newOutgoing);
	}
	
//	public void trimOutgoing(Map<ReprPort, Set<PulsePack>> pvsPerInputPort) {
//		Map<NikolaNode, Set<PulsePackMap>> inputsPerSuccs = new HashMap<NikolaNode, Set<PulsePackMap>>();
//		
//		for (Map.Entry<NikolaIO, Set<NikolaNode>> e : outgoing.entrySet()) {
//			for (NikolaNode succ : e.getValue()) {
//				HashMaps.inject(inputsPerSuccs, succ, e.getKey().getInput());
//			}
//		}
//		
//		Map<NikolaIO, Set<NikolaNode>> newOutgoing = new HashMap<NikolaIO, Set<NikolaNode>>();
//		
//		for (Map.Entry<NikolaNode, Set<PulsePackMap>> e : inputsPerSuccs.entrySet()) {
//			for (PulsePackMap m : PulsePackMap.trim(e.getValue(), pvsPerInputPort)) {
//				HashMaps.inject(newOutgoing, new NikolaIO(m, PulsePackMap.EMPTY), e.getKey());
//			}
//		}
//		
//		outgoing.clear();
//		outgoing.putAll(newOutgoing);
//	}
	
	public int getTransitionCount() {
		int result = 0;
		
		for (Map.Entry<PulsePackMap, Set<NikolaNode>> e : outgoing.entrySet()) {
			for (NikolaNode tgt : e.getValue()) {
				if (tgt != this) {
					result++;
				}
			}
		}
		
		return result;
	}
	
//	public Map<PulsePackMap, Set<Nikola>> computeSuccsPerInput(Set<PulsePackMap> inputAlphabet, Map<PalmaNode, Nikola> clzPerCfg) {
//		Map<PulsePackMap, Set<Nikola>> result = new HashMap<PulsePackMap, Set<Nikola>>();
//		
//		for (PulsePackMap input : inputAlphabet) {
//			Set<Nikola> succClzs = new HashSet<Nikola>();
//			
//			for (PalmaNode cfg : cfgs) {
//				for (DecaFourStateConfig succ : cfg.computeReachableViaInputVal(input)) {
//					succClzs.add(clzPerCfg.get(succ));
//				}
//			}
//			
//			result.put(input, succClzs);
//		}
//		
//		return result;
//	}
	
	public Set<NikolaNode> splitByInput(PulsePackMap input, Map<PalmaNode, NikolaNode> clzPerCfg) {
		Map<Set<NikolaNode>, Set<PalmaNode>> cfgsPerSuccClzs = new HashMap<Set<NikolaNode>, Set<PalmaNode>>();
		
		for (PalmaNode cfg : nodes) {
			Set<NikolaNode> succClzs = new HashSet<NikolaNode>();
			
			for (Map.Entry<PulsePackMap, Set<PalmaNode>> e : cfg.getOutgoing().entrySet()) {
				if (input.implies(e.getKey())) {
					for (PalmaNode succ : e.getValue()) {
						succClzs.add(clzPerCfg.get(succ));
					}
				}
			}
			
			HashMaps.inject(cfgsPerSuccClzs, succClzs, cfg);
		}
		
		Set<NikolaNode> result = new HashSet<NikolaNode>();
		
		for (Set<PalmaNode> v : cfgsPerSuccClzs.values()) {
			result.add(new NikolaNode(v, output));
		}
		
		return result;
	}
	
	public static Set<NikolaNode> splitByOutput(Collection<PalmaNode> cfgs) {
		Map<PulsePackMap, Set<PalmaNode>> cfgsPerOutput = new HashMap<PulsePackMap, Set<PalmaNode>>();
		Set<NikolaNode> pulseCfgs = new HashSet<NikolaNode>();
		
		for (PalmaNode cfg : cfgs) {
//			if (cfg.getOutput().containsTruePulse()) {
//				pulseCfgs.add(new NikolaNode(Collections.singleton(cfg), cfg.getOutput()));
//			} else {
				HashMaps.inject(cfgsPerOutput, cfg.getOutput(), cfg);
//			}
		}
		
		Set<NikolaNode> result = new HashSet<NikolaNode>(pulseCfgs);
		
		for (Set<PalmaNode> v : cfgsPerOutput.values()) {
			result.add(new NikolaNode(v, v.iterator().next().getOutput()));
		}
		
		return result;
	}
	
	public Map<String, Set<Class<?>>> getSysmlClzs() {
		Map<String, Set<Class<?>>> result = new HashMap<String, Set<Class<?>>>();
		
		for (PalmaNode node : nodes) {
			HashMaps.merge(result, node.getSysmlClzs());
		}
		
		return result;
	}
}

