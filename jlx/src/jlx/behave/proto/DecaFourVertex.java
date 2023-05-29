package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.blocks.ibd1.VerificationModel;
import jlx.common.FileLocation;
import jlx.utils.*;

public class DecaFourVertex implements Comparable<DecaFourVertex> {
	private JScope scope;
	private PulsePackMap outputVal;
	private Set<DecaFourTransition> incoming;
	private Map<Set<PulsePackMap>, Set<DecaFourTransition>> outgoing;
	private Map<Set<InputEquivClz>, DecaFourTransition> detOutgoing;
	private Map<DecaFourTransitionReqSet, Set<DecaFourTransition>> outgoingPerReqSet;
	private Map<DecaFourTransitionReqSet, Set<DecaFourTransition>> nondetOutgoingPerReqSet;
	private Map<DecaFourTransitionReqSet, Set<DecaFourTransition>> detOutgoingPerReqSet;
	private Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> inputsPerNonDetTgtsPerReqSet;
	private Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>> inputsPerNonDetTgtsPerReqSet2;
//	private Map<DecaFourTransitionReqSet, Set<ProtoTransition>> protoTrsPerReqSet;
//	private Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<InputEquivClz>>> inputsPerDetTgtPerReqSet;
	//private Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<ExternalInputEquivClz>>> externalInputsPerDetTgtPerReqSet;
	private Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> externalInputsPerNonDetTgtsPerReqSet;
	private Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>> externalInputsPerNonDetTgtsPerReqSet2;
	private Map<DecaFourTransitionReqSet, Map<PulsePackMap, Set<DecaFourTransition>>> protoTrsPerNonDetTgtsPerReqSet;
	private Map<VerificationModel, Map<DecaFourTransitionReqSet, Map<List<PulsePack>, Set<DecaFourVertex>>>> bla;
	
	private DecaTwoBVertex legacy;
	private int globalLevel;
	
	public boolean isReachable;
	
//	public List<InputEquivClz> orderedInputClzs;
	
	public DecaFourVertex(JScope scope, DecaTwoBVertex source) {
		this.scope = scope;
		
		incoming = new HashSet<DecaFourTransition>();
		outgoing = new HashMap<Set<PulsePackMap>, Set<DecaFourTransition>>();
		detOutgoing = new HashMap<Set<InputEquivClz>, DecaFourTransition>();
		outgoingPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<DecaFourTransition>>();
		nondetOutgoingPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<DecaFourTransition>>();
		detOutgoingPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<DecaFourTransition>>();
		inputsPerNonDetTgtsPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>>();
		inputsPerNonDetTgtsPerReqSet2 = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>>();
//		inputsPerDetTgtPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<InputEquivClz>>>();
//		externalInputsPerDetTgtPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<ExternalInputEquivClz>>>();
		externalInputsPerNonDetTgtsPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>>();
		externalInputsPerNonDetTgtsPerReqSet2 = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>>();
		protoTrsPerNonDetTgtsPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<PulsePackMap, Set<DecaFourTransition>>>();
//		protoTrsPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<ProtoTransition>>(); 
		bla = new HashMap<VerificationModel, Map<DecaFourTransitionReqSet, Map<List<PulsePack>, Set<DecaFourVertex>>>>();
		outputVal = PulsePackMap.from(source.getOutputVal(), Dir.OUT);
		
		this.legacy = source;
		
		globalLevel = 0;
	}
	
	public DecaFourVertex(JScope scope, DecaThreeVertex source) {
		this.scope = scope;
		
		incoming = new HashSet<DecaFourTransition>();
		outgoing = new HashMap<Set<PulsePackMap>, Set<DecaFourTransition>>();
		detOutgoing = new HashMap<Set<InputEquivClz>, DecaFourTransition>();
		outgoingPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<DecaFourTransition>>();
		nondetOutgoingPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<DecaFourTransition>>();
		detOutgoingPerReqSet = new HashMap<DecaFourTransitionReqSet, Set<DecaFourTransition>>();
		inputsPerNonDetTgtsPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>>();
		inputsPerNonDetTgtsPerReqSet2 = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>>();
//		inputsPerDetTgtPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<InputEquivClz>>>();
//		externalInputsPerDetTgtPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<ExternalInputEquivClz>>>();
		externalInputsPerNonDetTgtsPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>>();
		externalInputsPerNonDetTgtsPerReqSet2 = new HashMap<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>>();
		protoTrsPerNonDetTgtsPerReqSet = new HashMap<DecaFourTransitionReqSet, Map<PulsePackMap, Set<DecaFourTransition>>>(); 
		bla = new HashMap<VerificationModel, Map<DecaFourTransitionReqSet, Map<List<PulsePack>, Set<DecaFourVertex>>>>();
		outputVal = source.getStateConfig().getOutputVal();
		
		legacy = null;
		
		globalLevel = source.getLocalLevel();
	}
	
	public JScope getScope() {
		return scope;
	}
	
	public int getId() {
		return legacy.getId();
	}
	
	public DecaTwoBVertex getLegacy() {
		return legacy;
	}
	
	public PulsePackMap getOutputVal() {
		return outputVal;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return legacy.getSysmlClzs();
	}
	
	public Set<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public Set<DecaFourTransition> getIncoming() {
		return incoming;
	}
	
	public Map<Set<PulsePackMap>, Set<DecaFourTransition>> getOutgoing() {
		return outgoing;
	}
	
	public Map<Set<InputEquivClz>, DecaFourTransition> getDetOutgoing() {
		return detOutgoing;
	}
	
	public Map<Set<DecaFourVertex>, Set<DecaFourTransition>> getOutgoingPerTgts() {
		Map<Set<DecaFourVertex>, Set<DecaFourTransition>> result = new HashMap<Set<DecaFourVertex>, Set<DecaFourTransition>>();
		
		for (Set<DecaFourTransition> trs : outgoing.values()) {
			for (DecaFourTransition t : trs) {
				HashMaps.inject(result, t.getTgtGrp().getVtxs(), t);
			}
		}
		
		return result;
	}
	
	public Map<DecaFourTransitionReqSet, Set<DecaFourTransition>> getOutgoingPerReqSet() {
		return outgoingPerReqSet;
	}
	
	public Map<DecaFourTransitionReqSet, Set<DecaFourTransition>> getNondetOutgoingPerReqSet() {
		return nondetOutgoingPerReqSet;
	}
	
	public Map<DecaFourTransitionReqSet, Set<DecaFourTransition>> getDetOutgoingPerReqSet() {
		return detOutgoingPerReqSet;
	}
	
	public Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> getInputsPerNonDetTgtsPerReqSet() {
		return inputsPerNonDetTgtsPerReqSet;
	}
	
	public Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>> getInputsPerNonDetTgtsPerReqSet2() {
		return inputsPerNonDetTgtsPerReqSet2;
	}
	
	public Map<DecaFourTransitionReqSet, Map<List<PulsePack>, Set<DecaFourVertex>>> getMapsPerNonDetTgtsPerReqSet(VerificationModel vm) {
		Map<DecaFourTransitionReqSet, Map<List<PulsePack>, Set<DecaFourVertex>>> result = bla.get(vm);
		
//		if (result == null) {
//			result = new HashMap<DecaFourTransitionReqSet, Map<List<PulsePack>, Set<DecaFourVertex>>>();
//			
//			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e : inputsPerNonDetTgtsPerReqSet.entrySet()) {
//				Map<List<PulsePack>, Set<DecaFourVertex>> zs = new HashMap<List<PulsePack>, Set<DecaFourVertex>>();
//				
//				for (Map.Entry<DecaFourTgtGrp, Set<PulsePackMap>> e2 : e.getValue().entrySet()) {
//					for (InputEquivClz y : e2.getValue()) {
//						for (PulsePackMap m : y.getVmMap(vm)) {
//							List<PulsePack> packs = new ArrayList<PulsePack>();
//							packs.addAll(m.getPackPerPort().values());
//							
//							for (List<PulsePack> seq : ArrayLists.allOrderings(packs)) {
//								HashMaps.injectAll(zs, seq, e2.getKey().getVtxs());
//							}
//						}
//					}
//				}
//				
//				result.put(e.getKey(), zs);
//			}
//			
//			bla.put(vm, result);
//		}
		
		return result;
	}
	
//	public Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<InputEquivClz>>> getInputsPerDetTgtPerReqSet() {
//		return inputsPerDetTgtPerReqSet;
//	}
	
//	public Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<ExternalInputEquivClz>>> getExternalInputsPerDetTgtPerReqSet() {
//		return externalInputsPerDetTgtPerReqSet;
//	}
	
	public Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> getExternalInputsPerNonDetTgtsPerReqSet() {
		return externalInputsPerNonDetTgtsPerReqSet;
	}
	
	public Map<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>> getExternalInputsPerNonDetTgtsPerReqSet2() {
		return externalInputsPerNonDetTgtsPerReqSet2;
	}
	
	public Map<DecaFourTransitionReqSet, Map<PulsePackMap, Set<DecaFourTransition>>> getTrsPerNonDetTgtsPerReqSet() {
		return protoTrsPerNonDetTgtsPerReqSet;
	}
	
	public String getName() {
		//return scope.getName() + ":" + legacy.getId() + ":" + getGlobalLevel();
		return legacy.getName();
	}
	
	public int getLocalLevel() {
		return 0;
		//return legacy.getLocalLevel();
	}
	
	public int getGlobalLevel() {
		return globalLevel;
	}
	
	public void setGlobalLevel(int globalLevel) {
		if (globalLevel < this.globalLevel) {
			throw new Error("Should not happen (" + globalLevel + " < " + this.globalLevel + ")!");
		}
		
		this.globalLevel = globalLevel;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public int compareTo(DecaFourVertex other) {
		return getId() - other.getId();
	}
}


