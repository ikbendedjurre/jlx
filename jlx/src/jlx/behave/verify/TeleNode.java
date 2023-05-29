package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.*;

public class TeleNode {
	private final Set<NecronNode> nodes;
	private final Map<PulsePackMapIO, Set<TeleNode>> outgoing;
	private final Map<Set<PulsePackMapIO>, Set<TeleNode>> outgoing2;
	
	public TeleNode(Set<NecronNode> nodes) {
		this.nodes = nodes;
		
		outgoing = new HashMap<PulsePackMapIO, Set<TeleNode>>();
		outgoing2 = new HashMap<Set<PulsePackMapIO>, Set<TeleNode>>();
	}
	
//	public Map<TeleX, Set<PulsePackMap>> getInputsPerX() {
//		Map<TeleX, Set<PulsePackMap>> result = new HashMap<TeleX, Set<PulsePackMap>>();
//		
//		for (Map.Entry<PulsePackMapIO, Set<TeleNode>> e : outgoing.entrySet()) {
//			for (TeleNode node : e.getValue()) {
//				HashMaps.inject(result, new TeleX(e.getKey().getO(), node), e.getKey().getI());
//			}
//		}
//		
//		return result;
//	}
	
	public Set<TeleX> getXs() {
		Map<TeleX, Set<PulsePackMap>> inputsPerX = new HashMap<TeleX, Set<PulsePackMap>>();
		
		for (Map.Entry<PulsePackMapIO, Set<TeleNode>> e : outgoing.entrySet()) {
			for (TeleNode node : e.getValue()) {
				HashMaps.inject(inputsPerX, new TeleX(Collections.emptySet(), e.getKey().getO(), node), e.getKey().getI());
			}
		}
		
		Set<TeleX> result = new HashSet<TeleX>();
		
		for (Map.Entry<TeleX, Set<PulsePackMap>> e : inputsPerX.entrySet()) {
			result.add(new TeleX(e.getValue(), e.getKey().getOutput(), e.getKey().getTgt()));
		}
		
		return result;
	}
	
	public Set<NecronNode> getNodes() {
		return nodes;
	}
	
	public Map<PulsePackMapIO, Set<TeleNode>> getOutgoing() {
		return outgoing;
	}
	
	public Map<Set<PulsePackMapIO>, Set<TeleNode>> getOutgoing2() {
		return outgoing2;
	}
	
	public Map<String, Set<Class<?>>> getSysmlClzs() {
		Map<String, Set<Class<?>>> result = new HashMap<String, Set<Class<?>>>();
		
		for (NecronNode node : getNodes()) {
			HashMaps.merge(result, node.getSysmlClzs());
		}
		
		return result;
	}
	
	public int getTransitionCount() {
		int result = 0;
		
		for (Map.Entry<PulsePackMapIO, Set<TeleNode>> e : outgoing.entrySet()) {
			result += e.getValue().size();
		}
		
		return result;
	}
	
	public void populateOutgoing(TeleGraph g, Map<NecronNode, TeleNode> nodePerNode) {
		Map<PulsePackMapIO, Set<TeleNode>> newOutgoing = new HashMap<PulsePackMapIO, Set<TeleNode>>();
		
		for (NecronNode n : nodes) {
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : n.getOutgoing().entrySet()) {
				for (NecronNode n2 : e.getValue()) {
					HashMaps.inject(newOutgoing, e.getKey(), nodePerNode.get(n2)); //We expect exactly 1 element per PulsePackMap.
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
		
		outgoing2.clear();
		outgoing2.putAll(HashMaps.mergeKeysByValues(outgoing));
	}
	
	public Set<TeleNode> splitByOutgoing(PulsePackMapIO io, Map<NecronNode, TeleNode> clzPerCfg) {
		Map<Set<TeleNode>, Set<NecronNode>> cfgsPerSuccClzs = new HashMap<Set<TeleNode>, Set<NecronNode>>();
		
		for (NecronNode cfg : nodes) {
			Set<TeleNode> succClzs = new HashSet<TeleNode>();
			
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : cfg.getOutgoing().entrySet()) {
				if (e.getKey().equals(io)) {
					for (NecronNode succ : e.getValue()) {
						succClzs.add(clzPerCfg.get(succ));
					}
				}
			}
			
			HashMaps.inject(cfgsPerSuccClzs, succClzs, cfg);
		}
		
		Set<TeleNode> result = new HashSet<TeleNode>();
		
		for (Set<NecronNode> v : cfgsPerSuccClzs.values()) {
			result.add(new TeleNode(v));
		}
		
		return result;
	}
	
	public static Set<TeleNode> splitByOutgoing(Collection<NecronNode> cfgs) {
		Map<Set<PulsePackMapIO>, Set<NecronNode>> cfgsPerOutput = new HashMap<Set<PulsePackMapIO>, Set<NecronNode>>();
		Set<TeleNode> pulseCfgs = new HashSet<TeleNode>();
		
		for (NecronNode cfg : cfgs) {
			HashMaps.inject(cfgsPerOutput, cfg.getOutgoing().keySet(), cfg);
		}
		
		Set<TeleNode> result = new HashSet<TeleNode>(pulseCfgs);
		
		for (Set<NecronNode> v : cfgsPerOutput.values()) {
			result.add(new TeleNode(v));
		}
		
		return result;
	}
	
	public Map<Set<PulsePackMap>, Map<List<PulsePackMap>, Set<TeleNode>>> splitOutgoing(TeleGraph tg) {
		Map<PulsePackMap, Map<List<PulsePackMap>, Set<TeleNode>>> result = new HashMap<PulsePackMap, Map<List<PulsePackMap>, Set<TeleNode>>>();
		
		for (Map.Entry<PulsePackMapIO, Set<TeleNode>> e : outgoing.entrySet()) {
			HashMaps.injectInjectAll(result, e.getKey().getI(), e.getKey().getO(), e.getValue());
		}
		
		Map<Set<PulsePackMap>, Map<List<PulsePackMap>, Set<TeleNode>>> result2 = new HashMap<Set<PulsePackMap>, Map<List<PulsePackMap>, Set<TeleNode>>>();
		
		for (Map.Entry<Set<PulsePackMap>, Map<List<PulsePackMap>, Set<TeleNode>>> e : HashMaps.mergeKeysByValues(result).entrySet()) {
			Set<PulsePackMap> trimmed = PulsePackMap.trim(e.getKey(), tg.getPvsPerInputPort(), Dir.IN);
			HashMaps.injectMerge(result2, trimmed, e.getValue());
		}
		
		return result2;
	}
}
