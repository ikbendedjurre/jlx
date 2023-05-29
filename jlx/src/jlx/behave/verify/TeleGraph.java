package jlx.behave.verify;

import java.time.LocalTime;
import java.util.*;

import jlx.behave.proto.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class TeleGraph {
	private final NecronGraph legacy;
	private final Set<TeleNode> nodes;
	private final VerificationModel vm;
	private final Set<PulsePackMapIO> alphabet;
	private final Map<ReprPort, Set<PulsePack>> pvsPerInputPort;
	private final PulsePackMap initialInputs;
	private final TeleNode initialNode;
	private final Map<TeleX, Set<TeleNode>> srcsPerX;
	
	public TeleGraph(NecronGraph legacy) {
		this.legacy = legacy;
		
		vm = legacy.getVm();
		alphabet = extractAlphabet();
		
		System.out.println("#tele-alphabet = " + alphabet.size());
		
		nodes = extractNodes();
		
		Map<NecronNode, TeleNode> nodePerNode = new HashMap<NecronNode, TeleNode>();
		
		for (TeleNode node : nodes) {
			for (NecronNode n : node.getNodes()) {
				nodePerNode.put(n, node);
			}
		}
		
		pvsPerInputPort = legacy.getPvsPerInputPort();
		initialInputs = legacy.getInitialInputs();
		initialNode = nodePerNode.get(legacy.getInitialNode());
		int transitionCount = 0;
		
		for (TeleNode node : nodes) {
			node.populateOutgoing(this, nodePerNode);
			transitionCount += node.getTransitionCount();
		}
		
		System.out.println("#tele-transitions = " + transitionCount);
		
		srcsPerX = new HashMap<TeleX, Set<TeleNode>>();
		
		Set<Map<String, Set<Class<?>>>> bla = new HashSet<Map<String, Set<Class<?>>>>();
		
		for (TeleNode node : nodes) {
			for (TeleX x : node.getXs()) {
				HashMaps.inject(srcsPerX, x, node);
			}
			
			bla.add(node.getSysmlClzs());
		}
		
		System.out.println("#tele-xs = " + srcsPerX.size());
		
//		for (Map<String, Set<Class<?>>> b : bla) {
//			System.out.println("tele-cfg");
//			
//			for (Map.Entry<String, Set<Class<?>>> e : b.entrySet()) {
//				List<String> items = new ArrayList<String>();
//				
//				for (Class<?> c : e.getValue()) {
//					items.add(c.getSimpleName());
//				}
//				
//				System.out.println("\t" + e.getKey() + " = " + Texts.concat(items, " / "));
//			}
//		}
	}
	
	private Set<PulsePackMapIO> extractAlphabet() {
		Set<PulsePackMapIO> result = new HashSet<PulsePackMapIO>();
		
		for (NecronNode node : legacy.getNodes()) {
			result.addAll(node.getOutgoing().keySet());
		}
		
		return result;
	}
	
	private Set<TeleNode> extractNodes() {
		System.out.println("[" + LocalTime.now() + "] #tele-nodes = 0");
		
		Set<TeleNode> fringe = TeleNode.splitByOutgoing(legacy.getNodes());
		Set<TeleNode> newFringe = new HashSet<TeleNode>();
		
		boolean done = false;
		
		System.out.println("[" + LocalTime.now() + "] #tele-nodes = " + fringe.size());
		
		while (!done) {
			done = true;
			
			Map<NecronNode, TeleNode> clzPerCfg = new HashMap<NecronNode, TeleNode>();
			
			for (TeleNode e : fringe) {
				for (NecronNode cfg : e.getNodes()) {
					clzPerCfg.put(cfg, e);
				}
			}
			
			newFringe.clear();
			
			for (TeleNode e : fringe) {
				boolean wasSplit = false;
				
				for (PulsePackMapIO i : getAlphabet()) {
					Set<TeleNode> clzs = e.splitByOutgoing(i, clzPerCfg);
					
					if (clzs.size() > 1) {
						newFringe.addAll(clzs);
						wasSplit = true;
						done = false;
						break;
					}
				}
				
				if (!wasSplit) {
					newFringe.add(e);
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #tele-nodes = " + fringe.size());
		}
		
		return newFringe;
	}
	
	public NecronGraph getLegacy() {
		return legacy;
	}
	
	public Set<TeleNode> getNodes() {
		return nodes;
	}
	
	public VerificationModel getVm() {
		return vm;
	}
	
	public Set<PulsePackMapIO> getAlphabet() {
		return alphabet;
	}
	
	public Map<ReprPort, Set<PulsePack>> getPvsPerInputPort() {
		return pvsPerInputPort;
	}
	
	public PulsePackMap getInitialInputs() {
		return initialInputs;
	}
	
	public TeleNode getInitialNode() {
		return initialNode;
	}
	
	public Map<TeleX, Set<TeleNode>> getSrcsPerX() {
		return srcsPerX;
	}
}

