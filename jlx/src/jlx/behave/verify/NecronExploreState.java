package jlx.behave.verify;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Texts;

public class NecronExploreState {
	private final NikolaNode node;
	private final PulsePackMap output;
	private final int hashCode;
	
	public NecronExploreState(NikolaNode node, PulsePackMap output) {
		this.node = node;
		this.output = output;
		
		hashCode = Objects.hash(node, output);
	}
	
	public NikolaNode getNode() {
		return node;
	}
	
	public PulsePackMap getOutput() {
		return output;
	}
	
	public Set<NecronExploreState> computeSuccsViaInput(NecronGraph graph, PulsePackMap input, PulsePackMap incomingOutput, PulsePackMap changedOutputSoFar) {
		if (!getNode().getOutgoing().containsKey(input)) {
			System.out.println("State: " + toString(getNode().getSysmlClzs()));
			System.out.println("Missing input: " + toString(graph, input));
			
			for (Map.Entry<PulsePackMap, Set<NikolaNode>> e : getNode().getOutgoing().entrySet()) {
				for (NikolaNode n : e.getValue()) {
					System.out.println("Available input: " + toString(graph, e.getKey()) + " -> " + toString(n.getSysmlClzs()));
				}
			}
			
			throw new Error("Should not happen!");
		}
		
		Set<NecronExploreState> result = new HashSet<NecronExploreState>();
		
		for (NikolaNode succ : getNode().getOutgoing().get(input)) {
//			List<PulsePackMap> outputSeq = new ArrayList<PulsePackMap>(outputSoFar);
//			
//			if (!outputSeq.get(outputSeq.size() - 1).equals(succ.getOutput())) {
//				outputSeq.add(succ.getOutput());
//			}
			
			//Add changes relative to the original output:
			PulsePackMap newOutput1 = changedOutputSoFar.combine(succ.getOutput().extractEventMap(incomingOutput));
			
			//Add changes relative to the previous output (in case a D-port is changed back):
			PulsePackMap newOutput2 = newOutput1.combine(succ.getOutput().extractEventMap(changedOutputSoFar));
			
//			PulsePackMap z = outputSoFar.get(0).combine(succ.getOutput().getChanges(outputSoFar.get(0)).discardFalsePulses());
			
			//List<PulsePackMap> outputSeq = Collections.singletonList(outputSoFar.get(0).keepLastChange(succ.getOutput()));
//			List<PulsePackMap> outputSeq = Collections.singletonList(z);
			result.add(new NecronExploreState(succ, newOutput2));
		}
		
		return result;
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
		NecronExploreState other = (NecronExploreState) obj;
		return Objects.equals(node, other.node) && Objects.equals(output, other.output);
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

