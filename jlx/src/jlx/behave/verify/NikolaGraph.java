package jlx.behave.verify;

import java.time.LocalTime;
import java.util.*;

import jlx.behave.proto.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * Reduces by strong bisimulation.
 */
public class NikolaGraph {
	private final PalmaGraph legacy;
	private final VerificationModel vm;
	private final Set<NikolaNode> nodes;
	private final Set<PulsePackMap> inputAlphabet;
	private final Map<ReprPort, Set<PulsePack>> pvsPerInputPort;
	private final PulsePackMap initialInputs;
	private final NikolaNode initialNode;
	
	public NikolaGraph(PalmaGraph legacy) {
		this.legacy = legacy;
		
		vm = legacy.getVm();
		inputAlphabet = legacy.getInputAlphabet();
		nodes = extractNodes();
		
		Map<PalmaNode, NikolaNode> nodePerNode = new HashMap<PalmaNode, NikolaNode>();
		
		for (NikolaNode node : nodes) {
			for (PalmaNode n : node.getNodes()) {
				nodePerNode.put(n, node);
			}
		}
		
		pvsPerInputPort = legacy.getPvsPerInputPort();
		initialInputs = legacy.getInitialInputs();
		initialNode = nodePerNode.get(legacy.getInitialNode());
		int transitionCount = 0;
		
		for (NikolaNode node : nodes) {
			node.populateOutgoing(this, nodePerNode);
			transitionCount += node.getTransitionCount();
		}
		
		System.out.println("#nikola-transitions = " + transitionCount);
	}
	
	private static class Worker extends ConcurrentWorker {
		private Set<NikolaNode> newFringe;
		private Set<NikolaNode> finished;
		
		public Worker() {
			newFringe = new HashSet<NikolaNode>();
			finished = new HashSet<NikolaNode>();
		}
		
		@Override
		public String getSuffix() {
			return "; #nikola-nodes = " + newFringe.size();
		}
	}
	
	private Set<NikolaNode> extractNodes() {
		System.out.println("[" + LocalTime.now() + "] #nikola-nodes = 0");
		
		Set<NikolaNode> fringe = NikolaNode.splitByOutput(legacy.getNodePerCfg().values());
		
		boolean done = false;
		
		System.out.println("[" + LocalTime.now() + "] #nikola-nodes = " + fringe.size());
		
		while (!done) {
			done = true;
			
			Map<PalmaNode, NikolaNode> clzPerCfg = new HashMap<PalmaNode, NikolaNode>();
			
			for (NikolaNode e : fringe) {
				for (PalmaNode cfg : e.getNodes()) {
					clzPerCfg.put(cfg, e);
				}
			}
			
			ConcurrentWork<NikolaNode, Worker> cw = new ConcurrentWork<NikolaNode, Worker>();
			
			for (int index = 1; index <= 4; index++) {
				cw.getWorkers().add(new Worker());
			}
			
			cw.apply(fringe, (n, w) -> {
				for (PulsePackMap i : getInputAlphabet()) {
					Set<NikolaNode> clzs = n.splitByInput(i, clzPerCfg);
					
					if (clzs.size() > 1) {
						w.newFringe.addAll(clzs);
						return;
					}
				}
				
				w.finished.add(n);
			});
			
			fringe.clear();
			
			for (Worker w : cw.getWorkers()) {
				fringe.addAll(w.newFringe);
				fringe.addAll(w.finished);
				
				if (w.newFringe.size() > 0) {
					done = false;
				}
			}
			
			System.out.println("[" + LocalTime.now() + "] #nikola-nodes = " + fringe.size());
		}
		
		return fringe;
	}
	
	public PalmaGraph getLegacy() {
		return legacy;
	}
	
	public VerificationModel getVm() {
		return vm;
	}
	
	public Set<PulsePackMap> getInputAlphabet() {
		return inputAlphabet;
	}
	
	public Set<NikolaNode> getNodes() {
		return nodes;
	}
	
	public Map<ReprPort, Set<PulsePack>> getPvsPerInputPort() {
		return pvsPerInputPort;
	}
	
	public PulsePackMap getInitialInputs() {
		return initialInputs;
	}
	
	public NikolaNode getInitialNode() {
		return initialNode;
	}
}

