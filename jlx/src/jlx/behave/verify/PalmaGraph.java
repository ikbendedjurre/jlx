package jlx.behave.verify;

import java.time.LocalTime;
import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

/**
 * Restricts all valuations to a given verification model.
 * Requires full exploration of DecaFourStateMachines.
 */
public class PalmaGraph {
	private final UnifyingBlock legacy;
	private final VerificationModel vm;
	private final Map<ReprPort, Set<PulsePack>> pvsPerInputPort;
	private final Set<PulsePackMap> inputAlphabet;
	private final Map<DecaFourStateConfig, PalmaNode> nodePerCfg;
	private final PulsePackMap initialInputs;
	private final PalmaNode initialNode;
	
	public PalmaGraph(UnifyingBlock legacy, VerificationModel vm) {
		this.legacy = legacy;
		this.vm = vm;
		
		pvsPerInputPort = extractPvsPerInputPort();
		inputAlphabet = extractInputAlphabet();
		System.out.println("#inputs = " + inputAlphabet.size());
		
		nodePerCfg = new HashMap<DecaFourStateConfig, PalmaNode>();
		
		for (DecaFourStateConfig cfg : legacy.sms4.configs) {
			PalmaNode node = new PalmaNode(this, cfg, vm);
			nodePerCfg.put(cfg, node);
		}
		
		initialInputs = legacy.sms4.initialInputs.extractVmMap(vm);
		initialNode = nodePerCfg.get(legacy.sms4.initCfg);
		ConcurrentWork<PalmaNode, Worker> cw = new ConcurrentWork<PalmaNode, Worker>();
		
		for (int index = 1; index <= 4; index++) {
			cw.getWorkers().add(new Worker());
		}
		
		System.out.println("#palma-nodes = " + nodePerCfg.size());
		
		Map<PalmaX, Set<PalmaNode>> srcsPerX = new HashMap<PalmaX, Set<PalmaNode>>();
		
		int nn = 1;
		
		for (PalmaNode n : nodePerCfg.values()) {
			for (PalmaX x : n.computeXs()) {
				HashMaps.inject(srcsPerX, x, n);
			}
			
			System.out.println("[" + LocalTime.now() + "] progress = " + nn + " / " + nodePerCfg.size() + "; #xs = " + srcsPerX.size());
			nn++;
		}
		
		cw.apply(nodePerCfg.values(), (node, w) -> {
			node.populateOutgoing();
			w.tcount += node.getTransitionCount();
		});
		
		int tcount = 0;
		
		for (Worker worker : cw.getWorkers()) {
			tcount += worker.tcount;
		}
		
		System.out.println("#palma-transitions = " + tcount);
		
//		cw.apply(nodePerCfg.values(), (node, w) -> {
//			for (PulsePackMap ppm : getInputAlphabet()) {
//				node.populateTauClosure(ppm);
//			}
//		});
		
		
//		int i = 0;
//		long tcount = 0;
//		
//		for (PalmaNode node : nodePerCfg.values()) {
//			node.populateOutgoing(this);
//			
//			tcount += node.getOutgoing().size();
//			i++;
//			
//			if (i % 1000 == 0) {
//				System.out.println("[" + LocalTime.now() + "] palma-progress = " + i + " / " + nodePerCfg.size() + "; #transitions = " + tcount);
//			}
//		}
	}
	
	private static class Worker extends ConcurrentWorker {
		private int tcount;
		
		public Worker() {
			tcount = 0;
		}
		
		@Override
		public String getSuffix() {
			return "; #palma-transitions = " + tcount;
		}
	}
	
	private Map<ReprPort, Set<PulsePack>> extractPvsPerInputPort() {
		Map<ReprPort, Set<ASALSymbolicValue>> vmInputs = new HashMap<ReprPort, Set<ASALSymbolicValue>>();
		
		for (ReprPort rp : vm.getReprPorts(legacy.reprPortPerNarrowPort.values())) {
			if (rp.getActionPerVm().containsKey(vm)) {
				if (rp.getDir() == Dir.IN) {
					vmInputs.put(rp, ASALSymbolicValue.from(rp.getPossibleValues()));
					
					for (ReprPort drp : rp.getDataPorts()) {
						vmInputs.put(drp, ASALSymbolicValue.from(drp.getPossibleValues()));
					}
				}
			}
		}
		
		Map<ReprPort, Set<PulsePack>> result = new HashMap<ReprPort, Set<PulsePack>>();
		
		for (Map<ReprPort, ASALSymbolicValue> val : HashMaps.allCombinations(vmInputs)) {
			PulsePackMap m = PulsePackMap.from(val, Dir.IN);
			
			for (Map.Entry<ReprPort, PulsePack> e : m.getPackPerPort().entrySet()) {
				HashMaps.inject(result, e.getKey(), e.getValue());
			}
		}
		
		return result;
	}
	
	private Set<PulsePackMap> extractInputAlphabet() {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (Map<ReprPort, PulsePack> val : HashMaps.allCombinations(pvsPerInputPort)) {
			result.add(new PulsePackMap(val, Dir.IN));
		}
		
		return result;
	}
	
	public UnifyingBlock getLegacy() {
		return legacy;
	}
	
	public VerificationModel getVm() {
		return vm;
	}
	
	public Map<ReprPort, Set<PulsePack>> getPvsPerInputPort() {
		return pvsPerInputPort;
	}
	
	public Set<PulsePackMap> getInputAlphabet() {
		return inputAlphabet;
	}
	
	public Map<DecaFourStateConfig, PalmaNode> getNodePerCfg() {
		return nodePerCfg;
	}
	
	public PulsePackMap getInitialInputs() {
		return initialInputs;
	}
	
	public PalmaNode getInitialNode() {
		return initialNode;
	}
}
