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
public class GateGraph {
	private final JetGraph legacy;
	private final VerificationModel vm;
	private final Set<GateNode> nodes;
	private final Set<PulsePackMap> alphabet;
	private final Map<ReprPort, Set<PulsePack>> pvsPerInputPort;
	private final PulsePackMap initialInputs;
	private final GateNode initialNode;
	
	public GateGraph(JetGraph legacy) {
		this.legacy = legacy;
		
		vm = legacy.getVm();
		alphabet = legacy.getAlphabet();
		nodes = extractNodes();
		
		Map<JetNode, GateNode> nodePerNode = new HashMap<JetNode, GateNode>();
		
		for (GateNode node : nodes) {
			for (JetNode n : node.getNodes()) {
				nodePerNode.put(n, node);
			}
		}
		
		pvsPerInputPort = legacy.getPvsPerInputPort();
		initialInputs = legacy.getInitialInputs();
		initialNode = nodePerNode.get(legacy.getInitialNode());
		int transitionCount = 0;
		
		for (GateNode node : nodes) {
			node.populateOutgoing(this, nodePerNode);
			transitionCount += node.getTransitionCount();
		}
		
		System.out.println("#gate-transitions = " + transitionCount);
	}
	
	private static class Worker extends ConcurrentWorker {
		private Set<GateNode> newFringe;
		private Set<GateNode> finished;
		
		public Worker() {
			newFringe = new HashSet<GateNode>();
			finished = new HashSet<GateNode>();
		}
		
		@Override
		public String getSuffix() {
			return "; #gate-nodes = " + newFringe.size();
		}
	}
	
	private Set<GateNode> extractNodes() {
		System.out.println("[" + LocalTime.now() + "] #gate-nodes = 0");
		
		Set<GateNode> fringe = GateNode.splitByOutput(legacy.getNodes());
		
		boolean done = false;
		
		System.out.println("[" + LocalTime.now() + "] #gate-nodes = " + fringe.size());
		
		while (!done) {
			done = true;
			
			Map<JetNode, GateNode> clzPerCfg = new HashMap<JetNode, GateNode>();
			
			for (GateNode e : fringe) {
				for (JetNode cfg : e.getNodes()) {
					clzPerCfg.put(cfg, e);
				}
			}
			
			ConcurrentWork<GateNode, Worker> cw = new ConcurrentWork<GateNode, Worker>();
			
			for (int index = 1; index <= 4; index++) {
				cw.getWorkers().add(new Worker());
			}
			
			cw.apply(fringe, (n, w) -> {
				for (PulsePackMap i : getAlphabet()) {
					Set<GateNode> clzs = n.splitByLabel(i, clzPerCfg);
					
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
			
			System.out.println("[" + LocalTime.now() + "] #gate-nodes = " + fringe.size());
		}
		
		return fringe;
	}
	
	public JetGraph getLegacy() {
		return legacy;
	}
	
	public VerificationModel getVm() {
		return vm;
	}
	
	public Set<PulsePackMap> getAlphabet() {
		return alphabet;
	}
	
	public Set<GateNode> getNodes() {
		return nodes;
	}
	
	public Map<ReprPort, Set<PulsePack>> getPvsPerInputPort() {
		return pvsPerInputPort;
	}
	
	public PulsePackMap getInitialInputs() {
		return initialInputs;
	}
	
	public GateNode getInitialNode() {
		return initialNode;
	}
}

