package jlx.behave.verify;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class NecronNode {
	private final NikolaNode node;
	private final Map<PulsePackMapIO, Set<NecronNode>> outgoing;
	private final Map<Set<PulsePackMapIO>, Set<NecronNode>> outgoing2;
	private final Map<PulsePackMapIO, Set<NecronNode>> outgoing3;
	private final int hashCode;
	
	public NecronNode(NikolaNode node) {
		this.node = node;
		
		outgoing = new HashMap<PulsePackMapIO, Set<NecronNode>>();
		outgoing2 = new HashMap<Set<PulsePackMapIO>, Set<NecronNode>>();
		outgoing3 = new HashMap<PulsePackMapIO, Set<NecronNode>>();
		hashCode = Objects.hash(node);
	}
	
	public NikolaNode getNode() {
		return node;
	}
	
	public Map<PulsePackMapIO, Set<NecronNode>> getOutgoing() {
		return outgoing;
	}
	
	public Map<PulsePackMapIO, Set<NecronNode>> getNewOutgoing() {
		return outgoing3;
	}
	
	public Map<Set<PulsePackMapIO>, Set<NecronNode>> getOutgoing2() {
		return outgoing2;
	}
	
	public Map<PulsePackMapIO, Set<NecronNode>> computeOutgoing(NecronGraph graph, boolean enabled) {
		Map<PulsePackMapIO, Set<NecronNode>> result = new HashMap<PulsePackMapIO, Set<NecronNode>>();
		
		for (PulsePackMap input : graph.getInputAlphabet()) {
			for (NecronExploreState s : computeReachableSuccsViaInput(graph, input, enabled)) {
				PulsePackMapIO io = new PulsePackMapIO(input, s.getOutput());
				HashMaps.inject(result, io, new NecronNode(s.getNode()));
			}
		}
		
		return result;
	}
	
	public void populateOutgoing(NecronGraph graph, boolean enabled) {
		outgoing.clear();
		outgoing.putAll(computeOutgoing(graph, enabled));
		
		outgoing2.clear();
		
		for (Map.Entry<Set<PulsePackMapIO>, Set<NecronNode>> e : HashMaps.mergeKeysByValues(outgoing).entrySet()) {
			HashMaps.injectAll(outgoing2, PulsePackMapIO.trim(e.getKey(), graph.getPvsPerInputPort()), e.getValue()); 
		}
	}
	
	public int getTransitionCount1() {
		return outgoing.size();
	}
	
	public int getTransitionCount2() {
		return outgoing2.size();
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NecronNode other = (NecronNode) obj;
		return Objects.equals(node, other.node);
	}
	
	public Map<String, Set<Class<?>>> getSysmlClzs() {
		return node.getSysmlClzs();
	}
	
	public Set<NecronExploreState> computeReachableSuccsViaInput(NecronGraph graph, PulsePackMap input, boolean enabled) {
		PulsePackMap incomingOutput = getNode().getOutput().deactivate();
		
		Map<NecronExploreState, Set<NecronExploreState>> genPerState = new HashMap<NecronExploreState, Set<NecronExploreState>>();
		
//		NecronExploreState prepNode = new NecronExploreState(getNode(), getNode().getOutput().deactivate().discardFalsePulses());
		NecronExploreState prepNode = new NecronExploreState(getNode(), PulsePackMap.EMPTY_OUT);
		
		Set<NecronExploreState> result = new HashSet<NecronExploreState>();
		result.addAll(prepNode.computeSuccsViaInput(graph, input, incomingOutput, prepNode.getOutput()));
		
		for (NecronExploreState x : result) {
			HashMaps.inject(genPerState, x, prepNode);
		}
		
		if (enabled) {
			Set<NecronExploreState> fringe = new HashSet<NecronExploreState>();
			Set<NecronExploreState> newFringe = new HashSet<NecronExploreState>();
			fringe.addAll(result);
			
			int depth = 0;
			PulsePackMap deactivatedInput = input.deactivate();
			
			while (fringe.size() > 0) {
				newFringe.clear();
				
				for (NecronExploreState node : fringe) {
					for (NecronExploreState succ : node.computeSuccsViaInput(graph, deactivatedInput, incomingOutput, node.getOutput())) {
						if (result.add(succ)) {
							HashMaps.inject(genPerState, succ, node);
							
//							String x1 = toString(getNode().getSysmlClzs());
//							String x2 = toString(succ.getNode().getSysmlClzs());
//							
//							if (x1.contains("BOOTING") && x2.contains("OPERATIONAL")) {
//								System.out.println("Found new successor at depth " + depth + "!");
//								System.out.println("\tTHIS:      " + toString(getNode().getSysmlClzs()));
//								System.out.println("\tSUCC:      " + toString(succ.getNode().getSysmlClzs()));
//								System.out.println("\tINPUT:     " + toString(graph, input));
//								System.out.println("\tINPUT-OFF: " + toString(graph, deactivatedInput));
//								System.out.println("\tOUTPUT:    " + toString(graph, succ.getOutput()));
//								
//								NecronExploreState x = succ;
//								int xix = 1;
//								
//								while (x != prepNode) {
//									x = genPerState.get(x).iterator().next();
//									System.out.println("PRED[" + xix + "]: " + toString(x.getNode().getSysmlClzs()));
//									System.out.println("\tout: " + toString(graph, x.getNode().getOutput()));
//									
//									for (Map.Entry<PulsePackMap, Set<NikolaNode>> e : x.getNode().getOutgoing().entrySet()) {
//										for (NikolaNode n : e.getValue()) {
//											System.out.println("\tin: " + toString(graph, e.getKey()) + " -> " + toString(n.getSysmlClzs()));
//										}
//									}
//									
//									xix++;
//								}
//								
//								CLI.waitForEnter();
//							}
							
							newFringe.add(succ);
						}
					}
				}
				
				fringe.clear();
				fringe.addAll(newFringe);
				
				depth++;
			}
		}
		
		return result;
	}
	
	private String toString(NecronGraph graph, PulsePackMap p) {
		List<String> items = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : p.getPackPerPort().entrySet()) {
			for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValue().getValuePerPort().entrySet()) {
				String k = e2.getKey().getActionPerVm().get(graph.getVm()).getId();
				items.add(k + " = " + e2.getValue().toString());
			}
		}
		
		return Texts.concat(items, " | ");
	}
	
	private static String toString(Map<String, Set<Class<?>>> clzs) {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<String, Set<Class<?>>> e : clzs.entrySet()) {
			List<String> items = new ArrayList<String>();
			
			for (Class<?> c : e.getValue()) {
				items.add(c.getSimpleName());
			}
			
			elems.add(e.getKey() + " = " + Texts.concat(items, " / "));
		}
		
		return Texts.concat(elems, " | ");
	}
}

