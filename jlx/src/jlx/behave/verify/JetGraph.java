package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;
import models.generic.v3x0x0A.EST_EfeS_STD_2;
import models.generic.v3x0x0A.SCI_EfeS_Sec_STD_1;

public class JetGraph {
	private final TeleGraph legacy;
	private final VerificationModel vm;
	private final Set<JetNode> nodes;
	private final Set<PulsePackMap> alphabet;
	private final Set<JetTransition> transitions;
	private final IdMap<JetNode> idPerNode;
	private final Map<TeleNode, JetNode> nodePerMainNode;
	private final JetNode initialNode;
	
	public JetGraph(TeleGraph legacy) {
		this.legacy = legacy;
		
		vm = legacy.getVm();
		
		nodes = new HashSet<JetNode>();
		transitions = new HashSet<JetTransition>();
		idPerNode = new IdMap<JetNode>();
		nodePerMainNode = extractNodePerMainNode();
		populateStabilizingTransitions();
		
		initialNode = nodePerMainNode.get(legacy.getInitialNode());
		alphabet = extractAlphabet();
		
		idPerNode.getOrAdd(initialNode);
		
		for (JetNode node : getNodes()) {
			idPerNode.getOrAdd(node);
		}
		
		System.out.println("#jet-nodes = " + nodes.size());
		System.out.println("#jet-transitions = " + transitions.size());
		
		Map<JetX, Set<JetNode>> srcsPerX = new HashMap<JetX, Set<JetNode>>();
		
		for (JetTransition t : transitions) {
			JetX x = new JetX(t.getTgt(), t.getMap().get(0));
			HashMaps.inject(srcsPerX, x, t.getSrc());
		}
		
		System.out.println("#jet-xs = " + srcsPerX.size());
		
		for (JetNode mainNode : nodePerMainNode.values()) {
			if (mainNode.getLegacy().getSysmlClzs().get("sec").contains(SCI_EfeS_Sec_STD_1.PDI_CONNECTION_ESTABLISHED.class)) {
				if (mainNode.getLegacy().getSysmlClzs().get("est").contains(EST_EfeS_STD_2.OPERATING_VOLTAGE_SUPPLIED.OPERATIONAL.class)) {
					System.out.println("#jet-id = " + mainNode.getId());
				}
			}
		}
		
		int nonDetOutputNodeCount = 0;
		int nonDetInputNodeCount = 0;
		
		for (JetNode node : nodes) {
			if (node.getDir() == Dir.OUT) {
				if (node.getOutgoing().size() > 1) {
					nonDetOutputNodeCount++;
				}
			} else {
				for (Map.Entry<List<PulsePackMap>, Set<JetTransition>> e : node.getOutgoing().entrySet()) {
					if (e.getValue().size() > 1) {
						nonDetInputNodeCount++;
						break;
					}
				}
			}
		}
		
		System.out.println("#non-det-output-nodes = " + nonDetOutputNodeCount);
		System.out.println("#non-det-input-nodes = " + nonDetInputNodeCount);
	}
	
	private Map<TeleNode, JetNode> extractNodePerMainNode() {
		Map<TeleNode, JetNode> nodePerMainNode = new HashMap<TeleNode, JetNode>();
		
		for (TeleNode mainNode : legacy.getNodes()) {
			JetNode newNode = new JetNode(this, mainNode, Dir.IN);
			nodes.add(newNode);
			
			nodePerMainNode.put(mainNode, newNode);
		}
		
		for (TeleNode mainNode : legacy.getNodes()) {
			JetNode src = nodePerMainNode.get(mainNode);
			
			for (Map.Entry<Set<PulsePackMap>, Map<List<PulsePackMap>, Set<TeleNode>>> e : mainNode.splitOutgoing(legacy).entrySet()) {
				JetNode mid = new JetNode(this, mainNode, Dir.OUT);
				nodes.add(mid);
				
				for (PulsePackMap input : e.getKey()) {
					transitions.add(new JetTransition(src, mid, Collections.singletonList(input)));
				}
				
				for (Map.Entry<List<PulsePackMap>, Set<TeleNode>> e2 : e.getValue().entrySet()) {
					for (TeleNode e3 : e2.getValue()) {
						JetNode tgt = nodePerMainNode.get(e3);
						transitions.add(new JetTransition(mid, tgt, e2.getKey()));
					}
				}
			}
		}
		
		for (JetTransition t : transitions) {
			HashMaps.inject(t.getSrc().getOutgoing(), t.getMap(), t);
		}
		
		return nodePerMainNode;
	}
	
	private void populateStabilizingTransitions() {
		boolean done = false;
		
		while (!done) {
			done = true;
			Set<JetTransition> newTrs = new HashSet<JetTransition>(transitions);
			
			for (JetTransition t : transitions) {
				List<PulsePackMap> deactivated = Collections.singletonList(t.getMap().get(0).deactivate());
				Set<JetTransition> succs = t.getTgt().getOutgoing().get(deactivated);
				
				if (succs != null) {
					for (JetTransition succ : succs) {
						JetTransition newTr = new JetTransition(t.getSrc(), succ.getTgt(), t.getMap());
						
						if (newTrs.add(newTr)) {
							done = false;
						}
					}
				}
			}
			
			transitions.clear();
			transitions.addAll(newTrs);
			
			System.out.println("#jet-transitions = " + transitions.size());
		}
	}
	
	private Set<PulsePackMap> extractAlphabet() {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (JetTransition t : getTransitions()) {
			result.add(t.getMap().get(0));
		}
		
		return result;
	}
	
	public TeleGraph getLegacy() {
		return legacy;
	}
	
	public VerificationModel getVm() {
		return vm;
	}
	
	public IdMap<JetNode> getIdPerNode() {
		return idPerNode;
	}
	
	public Set<JetNode> getNodes() {
		return nodes;
	}
	
	public Set<JetTransition> getTransitions() {
		return transitions;
	}
	
	public Map<TeleNode, JetNode> getNodePerMainNode() {
		return nodePerMainNode;
	}
	
	public JetNode getInitialNode() {
		return initialNode;
	}
	
	public Set<PulsePackMap> getAlphabet() {
		return alphabet;
	}
	
	public Map<ReprPort, Set<PulsePack>> getPvsPerInputPort() {
		return legacy.getPvsPerInputPort();
	}
	
	public PulsePackMap getInitialInputs() {
		return legacy.getInitialInputs();
	}
}

