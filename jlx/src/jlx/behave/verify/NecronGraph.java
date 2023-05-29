package jlx.behave.verify;

import java.time.LocalTime;
import java.util.*;

import jlx.behave.proto.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * Adds stabilization paths as transitions.
 */
public class NecronGraph {
	private final NikolaGraph legacy;
	private final VerificationModel vm;
	private final NormMap<NecronNode> nodes;
	private final NecronNode initialNode;
	private final Set<PulsePackMap> inputAlphabet;
	private final Map<ReprPort, Set<PulsePack>> pvsPerInputPort;
	private final PulsePackMap initialInputs;
	
	private static class Worker extends ConcurrentWorker {
		private int tcount1;
		private int tcount2;
		
		public Worker() {
			tcount1 = 0;
			tcount2 = 0;
		}
		
		@Override
		public String getSuffix() {
			return "; #trs1 = " + tcount1 + "; #trs2 = " + tcount2;
		}
	}
	
	public NecronGraph(NikolaGraph legacy, boolean enabled) {
		this.legacy = legacy;
		
		vm = legacy.getVm();
		pvsPerInputPort = legacy.getPvsPerInputPort();
		inputAlphabet = legacy.getInputAlphabet();
		initialInputs = legacy.getInitialInputs();
		initialNode = new NecronNode(legacy.getInitialNode());
		nodes = extractNodes(enabled);
		
		ConcurrentWork<NecronNode, Worker> cw = new ConcurrentWork<NecronNode, Worker>();
		
		for (int index = 1; index <= 4; index++) {
			cw.getWorkers().add(new Worker());
		}
		
		System.out.println("#necron-nodes = " + nodes.size());
		
		int tcount1 = 0;
		int tcount2 = 0;
		
		for (NecronNode node : nodes) {
			tcount1 += node.getTransitionCount1();
			tcount2 += node.getTransitionCount2();
		}
		
		System.out.println("#necron-transitions = " + tcount1 + " (or perhaps " + tcount2 + ")");
	}
	
	private NormMap<NecronNode> extractNodes(boolean enabled) {
		System.out.println("[" + LocalTime.now() + "] #necron-nodes = 0");
		
		NormMap<NecronNode> result = new NormMap<NecronNode>();
		result.add(initialNode);
		
		Set<NecronNode> fringe = new HashSet<NecronNode>();
		Set<NecronNode> newFringe = new HashSet<NecronNode>();
		fringe.add(initialNode);
		
		System.out.println("[" + LocalTime.now() + "] #necron-nodes = 1");
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (NecronNode n : fringe) {
				n.populateOutgoing(this, enabled);
			}
			
			for (NecronNode n : fringe) {
				for (Map.Entry<PulsePackMapIO, Set<NecronNode>> outgoing : n.getOutgoing().entrySet()) {
					Set<NecronNode> newOutgoingValue = new HashSet<NecronNode>();
					
					for (NecronNode succ : outgoing.getValue()) {
						NecronNode normNode = result.get(succ);
						
						if (normNode == succ) {
							newFringe.add(normNode);
						}
						
						newOutgoingValue.add(normNode);
					}
					
					outgoing.setValue(newOutgoingValue);
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #necron-nodes = " + result.size() + " (+" + fringe.size() + ")");
		}
		
		return result;
	}
	
	public NikolaGraph getLegacy() {
		return legacy;
	}
	
	public VerificationModel getVm() {
		return vm;
	}
	
	public NecronNode getInitialNode() {
		return initialNode;
	}
	
	public NormMap<NecronNode> getNodes() {
		return nodes;
	}
	
	public Set<PulsePackMap> getInputAlphabet() {
		return inputAlphabet;
	}
	
	public Map<ReprPort, Set<PulsePack>> getPvsPerInputPort() {
		return pvsPerInputPort;
	}
	
	public PulsePackMap getInitialInputs() {
		return initialInputs;
	}
}
