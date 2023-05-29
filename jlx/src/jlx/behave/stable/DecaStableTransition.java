package jlx.behave.stable;

import java.util.*;

import jlx.asal.vars.ASALPort;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaStableTransition {
	private DecaStableVertex src;
	private DecaStableVertex tgt;
	private PulsePackMap sicInputs;
	private List<DecaFourStateConfig> seq;
	private boolean isHiddenTimerTrigger;
	
	public boolean tested;
	
	public DecaStableTransition(DecaStableVertex src, DecaStableVertex tgt, PulsePackMap sicInputs, List<DecaFourStateConfig> seq) {
		this.src = src;
		this.tgt = tgt;
		this.sicInputs = sicInputs;
		this.seq = seq;
		
		isHiddenTimerTrigger = false;
	}
	
	public DecaStableTransition(DecaStableVertex src, PulsePackMap sicInputs, boolean isHiddenTimerTrigger) {
		this.src = src;
		this.sicInputs = sicInputs;
		this.isHiddenTimerTrigger = isHiddenTimerTrigger;
		
		tgt = src;
		seq = Collections.singletonList(src.getCfg());
	}
	
	public DecaStableVertex getSrc() {
		return src;
	}
	
	public DecaStableVertex getTgt() {
		return tgt;
	}
	
	public PulsePackMap getSicInputs() {
		return sicInputs;
	}
	
	public List<DecaFourStateConfig> getSeq() {
		return seq;
	}
	
	public boolean isHiddenTimerTrigger() {
		return isHiddenTimerTrigger;
	}
	
	public void normalizeTgt(NormMap<DecaStableVertex> vertices) {
		tgt = vertices.get(tgt);
	}
	
	public Set<Pair<DecaFourVertex>> computeVtxPairs() {
		Set<Pair<DecaFourVertex>> result = new HashSet<Pair<DecaFourVertex>>();
		
		for (int index = 1; index < seq.size(); index++) {
			result.addAll(seq.get(index).computeVtxPairsFrom(seq.get(index - 1)));
		}
		
		return result;
	}
	
	public DecaStableInputChanges getInputChanges(DecaStableStateMachine stableSm) {
		if (isHiddenTimerTrigger) {
			return new DecaStableInputChanges(PulsePackMap.EMPTY_IN, null, null, true);
		}
		
		PulsePackMap changes = getSicInputs().extractEventMap(getSrc().getExternalIncomingInputs());
		
		if (changes.getPackPerPort().isEmpty() && !src.equals(tgt)) {
			throw new Error("At least 1 input value should change!");
		}
		
		for (Map.Entry<ReprPort, PulsePack> e : changes.getPackPerPort().entrySet()) {
			ASALPort durationPort = stableSm.durationPortPerTimeoutPort.get(e.getKey());
			
			if (durationPort != null) {
				if (durationPort instanceof ReprPort) {
					ReprPort rp = (ReprPort)durationPort;
					
					return new DecaStableInputChanges(PulsePackMap.EMPTY_IN, e.getKey(), rp, false);
				} else {
					throw new Error("Should not happen!");
				}
			}
		}
		
		return new DecaStableInputChanges(changes, null, null, false);
	}
	
	public Set<DecaStableOutputEvolution> getOutputEvolutions() {
		return DecaStableOutputEvolution.getOutputEvolutions(seq);
	}
}

