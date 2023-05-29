package jlx.behave.verify;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.behave.proto.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.utils.*;

public class PalmaNode {
	private final PalmaGraph graph;
	private final DecaFourStateConfig cfg;
	private final Map<PulsePackMap, Set<PalmaNode>> outgoing;
	private final PulsePackMap output;
	
	public PalmaNode(PalmaGraph graph, DecaFourStateConfig cfg, VerificationModel vm) {
		this.graph = graph;
		this.cfg = cfg;
		
		outgoing = new HashMap<PulsePackMap, Set<PalmaNode>>();
		output = cfg.getOutputVal().extractVmMap(vm);
	}
	
	public Set<PalmaX> computeXs() {
		Map<Set<PalmaNode>, Set<PulsePackMap>> perTgts = new HashMap<Set<PalmaNode>, Set<PulsePackMap>>();
		
		for (Map.Entry<PulsePackMap, Set<PalmaNode>> e : computeOutgoing().entrySet()) {
			HashMaps.inject(perTgts, e.getValue(), e.getKey());
		}
		
		Set<PalmaX> result = new HashSet<PalmaX>();
		
		for (Map.Entry<Set<PalmaNode>, Set<PulsePackMap>> e : perTgts.entrySet()) {
			Set<PulsePackMap> trimmed = PulsePackMap.trim(e.getValue(), graph.getPvsPerInputPort(), Dir.IN);
			result.add(new PalmaX(trimmed, e.getKey()));
		}
		
		return result;
	}
	
	public void populateOutgoing() {
		outgoing.clear();
		outgoing.putAll(computeOutgoing());
	}
	
	public Map<PulsePackMap, Set<PalmaNode>> computeOutgoing() {
		Map<PalmaNode, Set<PulsePackMap>> inputsPerSuccs = new HashMap<PalmaNode, Set<PulsePackMap>>();
		
		for (PulsePackMap input : graph.getInputAlphabet()) {
			Set<PalmaNode> succs = new HashSet<PalmaNode>();
			
			for (DecaFourStateConfig succ : cfg.computeSuccsViaInputVal(input)) {
				succs.add(graph.getNodePerCfg().get(succ));
			}
			
			for (PalmaNode succ : succs) {
				HashMaps.inject(inputsPerSuccs, succ, input);
			}
		}
		
		Map<PulsePackMap, Set<PalmaNode>> result = new HashMap<PulsePackMap, Set<PalmaNode>>();
		
		for (Map.Entry<PalmaNode, Set<PulsePackMap>> e : inputsPerSuccs.entrySet()) {
			for (PulsePackMap v : PulsePackMap.trim(e.getValue(), graph.getPvsPerInputPort(), Dir.IN)) {
				HashMaps.inject(result, v, e.getKey());
			}
//			for (PulsePackMap v : e.getValue()) {
//				HashMaps.inject(result, v, e.getKey());
//			}
		}
		
		return result;
	}
	
	public int getTransitionCount() {
		int result = 0;
		
		for (Map.Entry<PulsePackMap, Set<PalmaNode>> e : outgoing.entrySet()) {
			result += e.getValue().size();
		}
		
		return result;
	}
	
	public Map<PulsePackMap, Set<PalmaNode>> getOutgoing() {
		return outgoing;
	}
	
	public DecaFourStateConfig getCfg() {
		return cfg;
	}
	
	public PulsePackMap getOutput() {
		return output;
	}
	
	public Map<String, Set<Class<?>>> getSysmlClzs() {
		Map<String, Set<Class<?>>> result = new HashMap<String, Set<Class<?>>>();
		
		for (Map.Entry<JScope, DecaFourVertex> e : cfg.getVtxs().entrySet()) {
			result.put(e.getKey().getName(), e.getValue().getSysmlClzs());
		}
		
		return result;
	}
}
