package jlx.behave.proto;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.HashMaps;

public class DecaThreeTransition {
	private DecaThreeStateConfig sourceStateConfig;
	private Set<DecaThreeStateConfig> targetStateConfigs;
	private DecaThreeVertex sourceVertex;
	private Set<DecaThreeVertex> targetVertices;
	private Set<InputEquivClz> inputs;
	private Set<DecaTwoTransition> enabledLegacy;
	private Set<DecaTwoTransition> disabledLegacy;
	
	public DecaThreeTransition(Set<DecaTwoTransition> enabledTrs, Set<DecaTwoTransition> disabledTrs, Set<InputEquivClz> inputs, DecaThreeStateConfig sourceStateConfig, Set<DecaThreeStateConfig> targetStateConfigs) {
		this.sourceStateConfig = sourceStateConfig;
		this.targetStateConfigs = targetStateConfigs;
		this.inputs = inputs;
		
		sourceVertex = null;
		targetVertices = new HashSet<DecaThreeVertex>();
		
		enabledLegacy = enabledTrs;
		disabledLegacy = disabledTrs;
	}
	
	public Set<DecaTwoTransition> getEnabledLegacy() {
		return enabledLegacy;
	}
	
	public Set<ProtoTransition> getEnabledProtoLegacy() {
		Set<ProtoTransition> result = new HashSet<ProtoTransition>();
		
		for (DecaTwoTransition t : enabledLegacy) {
			if (t.getLegacy().getLegacy().getLegacy().getLegacyTransition() != null) {
				for (SeptaTransition t2 : t.getLegacy().getLegacy().getLegacy().getLegacyTransition().getLegacy().getLegacy()) {
					if (t2.getLegacy().getLegacy().getLegacy().getLegacy().getLegacy().getLegacy() != null) {
						result.add(t2.getLegacy().getLegacy().getLegacy().getLegacy().getLegacy().getLegacy());
					}
				}
			}
		}
		
		return result;
	}
	
	public Set<DecaTwoTransition> getDisabledLegacy() {
		return disabledLegacy;
	}
	
	public DecaThreeStateConfig getSourceStateConfig() {
		return sourceStateConfig;
	}
	
	public Set<DecaThreeStateConfig> getTargetStateConfigs() {
		return targetStateConfigs;
	}
	
	public Set<InputEquivClz> getInputs() {
		return inputs;
	}
	
	public void populateVertices(Map<DecaThreeStateConfig, DecaThreeVertex> vertices) {
		sourceVertex = vertices.get(sourceStateConfig);
		
		for (DecaThreeStateConfig cfg : targetStateConfigs) {
			targetVertices.add(vertices.get(cfg));
		}
	}
	
	public DecaThreeVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public Set<DecaThreeVertex> getTargetVertices() {
		return targetVertices;
	}
	
	public boolean contains(PulsePackMap input) {
		for (InputEquivClz i : getInputs()) {
			if (i.getInputVals().contains(input)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static PulsePackMap computeChangedPorts(DecaThreeVertex firstVtx, DecaThreeVertex secondVtx) {
		return firstVtx.getStateConfig().getOutputVal().extractEventMap(secondVtx.getStateConfig().getOutputVal());
	}
	
	private static Set<DecaThreeOutputRun> computeUntilConvergedOutput(DecaThreeVertex firstVtx, DecaThreeVertex secondVtx, PulsePackMap input) {
		Set<DecaThreeOutputRun> result = new HashSet<DecaThreeOutputRun>();
		
		Set<DecaThreeOutputRun> changedOutputsPerReachedVtx = new HashSet<DecaThreeOutputRun>();
		Set<DecaThreeOutputRun> newChangedOutputsPerReachedVtx = new HashSet<DecaThreeOutputRun>();
		DecaThreeOutputRun run1 = new DecaThreeOutputRun(secondVtx, computeChangedPorts(firstVtx, secondVtx));
		
		for (DecaThreeOutputRun v : run1.computeOutputClosure(input)) {
			changedOutputsPerReachedVtx.add(v);
		}
		
		while (changedOutputsPerReachedVtx.size() > 0) {
			newChangedOutputsPerReachedVtx.clear();
			
			for (DecaThreeOutputRun e : changedOutputsPerReachedVtx) {
				Set<DecaThreeOutputRun> succs = e.computeNewOutputSuccs(input);
//				System.out.println("#changed-outputs = " + e.getChangedOutputs().size());
				
				if (succs.isEmpty()) {
					result.add(new DecaThreeOutputRun(e.getVertex(), firstVtx.getStateConfig().getOutputVal().combine(e.getOutput())));
				} else {
					for (DecaThreeOutputRun succ : succs) {
						for (DecaThreeOutputRun succ3 : succ.computeOutputClosure(input)) {
							newChangedOutputsPerReachedVtx.add(succ3);
						}
					}
				}
			}
			
			changedOutputsPerReachedVtx.clear();
			changedOutputsPerReachedVtx.addAll(newChangedOutputsPerReachedVtx);
			
			System.out.println("[" + LocalTime.now() + "] #current = " + changedOutputsPerReachedVtx.size() + "; #finished = " + result.size());
		}
		
		return result;
	}
	
	public Map<Set<DecaThreeOutputRun>, Set<PulsePackMap>> computeOutputRuns() {
		Map<Set<DecaThreeOutputRun>, Set<PulsePackMap>> inputsPerSuccs = new HashMap<Set<DecaThreeOutputRun>, Set<PulsePackMap>>();
		
		for (InputEquivClz i : getInputs()) {
			for (PulsePackMap val : i.getInputVals()) {
				for (DecaThreeVertex targetVertex : getTargetVertices()) {
					Set<DecaThreeOutputRun> succs = computeUntilConvergedOutput(getSourceVertex(), targetVertex, val);
					HashMaps.inject(inputsPerSuccs, succs, val);
				}
			}
		}
		
		return inputsPerSuccs;
	}
}

