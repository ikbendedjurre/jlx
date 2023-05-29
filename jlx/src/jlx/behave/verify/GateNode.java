package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.*;

public class GateNode {
	private final Set<JetNode> nodes;
	private final Map<PulsePackMap, Set<GateNode>> outgoing;
	
	public GateNode(Set<JetNode> nodes) {
		this.nodes = nodes;
		
		outgoing = new HashMap<PulsePackMap, Set<GateNode>>();
	}
	
	public Set<JetNode> getNodes() {
		return nodes;
	}
	
	public Map<PulsePackMap, Set<GateNode>> getOutgoing() {
		return outgoing;
	}
	
	public void populateOutgoing(GateGraph g, Map<JetNode, GateNode> nodePerNode) {
		Map<PulsePackMap, Set<GateNode>> newOutgoing = new HashMap<PulsePackMap, Set<GateNode>>();
		
		for (JetNode n : nodes) {
			for (Map.Entry<List<PulsePackMap>, Set<JetTransition>> e : n.getOutgoing().entrySet()) {
				for (JetTransition n2 : e.getValue()) {
					HashMaps.inject(newOutgoing, e.getKey().get(0), nodePerNode.get(n2.getTgt()));
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
	
	public int getTransitionCount() {
		int result = 0;
		
		for (Map.Entry<PulsePackMap, Set<GateNode>> e : outgoing.entrySet()) {
			result += e.getValue().size();
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
	
	public Set<GateNode> splitByLabel(PulsePackMap label, Map<JetNode, GateNode> clzPerCfg) {
		Map<Set<GateNode>, Set<JetNode>> cfgsPerSuccClzs = new HashMap<Set<GateNode>, Set<JetNode>>();
		
		for (JetNode cfg : nodes) {
			Set<GateNode> succClzs = new HashSet<GateNode>();
			
			for (Map.Entry<List<PulsePackMap>, Set<JetTransition>> e : cfg.getOutgoing().entrySet()) {
				if (e.getKey().get(0).equals(label)) {
					for (JetTransition succ : e.getValue()) {
						succClzs.add(clzPerCfg.get(succ.getTgt()));
					}
				}
			}
			
			HashMaps.inject(cfgsPerSuccClzs, succClzs, cfg);
		}
		
		Set<GateNode> result = new HashSet<GateNode>();
		
		for (Set<JetNode> v : cfgsPerSuccClzs.values()) {
			result.add(new GateNode(v));
		}
		
		return result;
	}
	
	public static Set<GateNode> splitByOutput(Collection<JetNode> cfgs) {
		Map<Set<List<PulsePackMap>>, Set<JetNode>> cfgsPerOutput = new HashMap<Set<List<PulsePackMap>>, Set<JetNode>>();
		Set<GateNode> pulseCfgs = new HashSet<GateNode>();
		
		for (JetNode cfg : cfgs) {
			HashMaps.inject(cfgsPerOutput, cfg.getOutgoing().keySet(), cfg);
		}
		
		Set<GateNode> result = new HashSet<GateNode>(pulseCfgs);
		
		for (Set<JetNode> v : cfgsPerOutput.values()) {
			result.add(new GateNode(v));
		}
		
		return result;
	}
	
	public Map<String, Set<Class<?>>> getSysmlClzs() {
		Map<String, Set<Class<?>>> result = new HashMap<String, Set<Class<?>>>();
		
		for (JetNode node : nodes) {
			HashMaps.merge(result, node.getSysmlClzs());
		}
		
		return result;
	}
}

