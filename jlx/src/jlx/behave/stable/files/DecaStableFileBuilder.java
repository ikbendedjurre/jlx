package jlx.behave.stable.files;

import java.io.*;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.*;
import jlx.asal.vars.ASALVariable;
import jlx.behave.proto.*;
import jlx.behave.stable.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaStableFileBuilder {
	private final IdMap<JScope> scopes;
	private final IdMap<ReprPort> inputPorts;
	private final IdMap<ReprPort> outputPorts;
	private final IdMap<DecaStableVertex> vertices;
	private final IdMap<DecaStableInputChanges> inputChanges;
	private final IdMap<DecaStableOutputEvolution> outputEvolutions;
	private final List<Integer> initialTransition;
	private final int initialTransitionTarget;
	private final Map<Integer, Map<Integer, Set<List<Integer>>>> transitionsPerTargetPerSource;
	private final Map<ReprPort, Integer> executionTimePerTimeoutPort;
	
	public DecaStableFileBuilder(DecaStableStateMachine stableSm) {
		scopes = new IdMap<JScope>();
		inputPorts = new IdMap<ReprPort>();
		outputPorts = new IdMap<ReprPort>();
		executionTimePerTimeoutPort = new HashMap<ReprPort, Integer>();
		vertices = new IdMap<DecaStableVertex>();
		inputChanges = new IdMap<DecaStableInputChanges>();
		outputEvolutions = new IdMap<DecaStableOutputEvolution>();
		
		for (JScope scope : stableSm.legacy.smPerScope.keySet()) {
			scopes.getOrAdd(scope);
			
			for (Map.Entry<String, ASALVariable> e : scope.getVariablePerName().entrySet()) {
				if (e.getValue() instanceof ReprPort) {
					ReprPort rp = (ReprPort)e.getValue();
					
					if (rp.getDir() == Dir.IN) {
						inputPorts.getOrAdd((ReprPort)e.getValue());
					} else {
						outputPorts.getOrAdd((ReprPort)e.getValue());
					}
				}
			}
		}
		
		for (ReprPort inputPort : inputPorts.getIdPerElem().keySet()) {
			if (stableSm.durationPortPerTimeoutPort.containsKey(inputPort)) {
				executionTimePerTimeoutPort.put(inputPort, inputPort.getExecutionTime());
			}
		}
		
		Map<ReprPort, ASALSymbolicValue> initialInputs = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : stableSm.initialInputs.entrySet()) {
			if (stableSm.durationPortPerTimeoutPort.values().contains(e.getKey())) {
				initialInputs.put(e.getKey(), new ASALLitSv("\"999999999\""));
			} else {
				initialInputs.put(e.getKey(), e.getValue());
			}
		}
		
		initialTransition = new ArrayList<Integer>();
		initialTransition.add(inputChanges.getOrAdd(new DecaStableInputChanges(PulsePackMap.from(initialInputs, Dir.IN), null, null, false)));
		initialTransition.addAll(outputEvolutions.addAll(stableSm.initialTransition.getOutputEvolutions()));
		vertices.addAll(stableSm.vertices);
		initialTransitionTarget = vertices.get(stableSm.initialTransition.getTgt());
		transitionsPerTargetPerSource = new TreeMap<Integer, Map<Integer, Set<List<Integer>>>>();
		
		for (DecaStableTransition t : stableSm.transitions) {
			List<Integer> transition = new ArrayList<Integer>();
			transition.add(inputChanges.getOrAdd(t.getInputChanges(stableSm)));
			transition.addAll(outputEvolutions.addAll(t.getOutputEvolutions()));
			addTransition(vertices.get(t.getSrc()), vertices.get(t.getTgt()), transition);
		}
		
		if ("input-enabledness".equals("off")) {
			//We use the below for our attempts of state verification;
			//i.e. each vertex with the same output must have the same inputs enabled.
			//This is (at least) very convenient.
			Map<PulsePackMap, Set<DecaStableVertex>> vtxsPerOutputVal = new HashMap<PulsePackMap, Set<DecaStableVertex>>();
			
			for (DecaStableVertex vtx : stableSm.vertices) {
				HashMaps.inject(vtxsPerOutputVal, vtx.getCfg().getOutputVal().extractExternalMap(), vtx);
			}
			
			int count = 0;
			
			for (Map.Entry<PulsePackMap, Set<DecaStableVertex>> e : vtxsPerOutputVal.entrySet()) {
				Set<Integer> inputChangesSet = new HashSet<Integer>();
				
				for (DecaStableVertex vtx : e.getValue()) {
					for (DecaStableInputChanges oic : vtx.getOutgoingInputChanges(stableSm)) {
						inputChangesSet.add(inputChanges.getOrAdd(oic));
					}
				}
				
				for (DecaStableVertex vtx : e.getValue()) {
					Set<Integer> outgoingInputChanges = new HashSet<Integer>();
					int vid = vertices.get(vtx);
					
					for (DecaStableInputChanges oic : vtx.getOutgoingInputChanges(stableSm)) {
						outgoingInputChanges.add(inputChanges.getOrAdd(oic));
					}
					
					for (int i : inputChangesSet) {
						if (!outgoingInputChanges.contains(i)) {
							//DecaStableInputChanges ic = inputChanges.getElemPerId().get(i);
							addTransition(vid, -1, Collections.singletonList(i));
							count++;
						}
					}
				}
			}
			
			System.out.println("count = " + count);
		}
	}
	
	private void addTransition(int src, int tgt, List<Integer> transition) {
		Map<Integer, Set<List<Integer>>> transitionsPerTarget = transitionsPerTargetPerSource.get(src);
		
		if (transitionsPerTarget == null) {
			transitionsPerTarget = new TreeMap<Integer, Set<List<Integer>>>();
			transitionsPerTargetPerSource.put(src, transitionsPerTarget);
		}
		
		Set<List<Integer>> transitions = transitionsPerTarget.get(tgt);
		
		if (transitions == null) {
			transitions = new HashSet<List<Integer>>();
			transitionsPerTarget.put(tgt, transitions);
		}
		
		transitions.add(transition);
	}
	
	public IdMap<JScope> getScopes() {
		return scopes;
	}
	
	public IdMap<ReprPort> getInputPorts() {
		return inputPorts;
	}
	
	public IdMap<ReprPort> getOutputPorts() {
		return outputPorts;
	}
	
	public Map<ReprPort, Integer> getExecutionTimePerTimeoutPort() {
		return executionTimePerTimeoutPort;
	}
	
	public IdMap<DecaStableVertex> getVertices() {
		return vertices;
	}
	
	public IdMap<DecaStableInputChanges> getInputChanges() {
		return inputChanges;
	}
	
	public IdMap<DecaStableOutputEvolution> getOutputEvolutions() {
		return outputEvolutions;
	}
	
	public List<Integer> getInitialTransition() {
		return initialTransition;
	}
	
	public int getInitialTransitionTarget() {
		return initialTransitionTarget;
	}
	
	public Map<Integer, Map<Integer, Set<List<Integer>>>> getTransitionsPerTargetPerSource() {
		return transitionsPerTargetPerSource;
	}
	
	public void saveToFile(String dirName, String fileName) {
		try {
			File dir = new File(dirName);
			
			if (dir.mkdir()) {
				System.out.println("Created directory \"" + dir.getAbsolutePath() + "\" for storing " + getClass().getCanonicalName() + "!");
			}
			
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dirName + "/" + fileName)));
			writeToStream(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	private void writeToStream(DataOutputStream out) throws IOException {
		TextOptions.select(TextOptions.FULL);
		
		out.writeInt(scopes.size());
		
		for (Map.Entry<Integer, JScope> e : scopes.getElemPerId().entrySet()) {
			out.writeUTF(e.getValue().getName());
		}
		
		out.writeInt(inputPorts.size());
		
		for (Map.Entry<Integer, ReprPort> e : inputPorts.getElemPerId().entrySet()) {
			out.writeUTF(e.getValue().getName());
			out.writeInt(scopes.get(e.getValue().getReprOwner()));
			out.writeUTF(e.getValue().getType().getCanonicalName());
			
			if (e.getValue().getPulsePort() != null) {
				out.writeInt(inputPorts.get(e.getValue().getPulsePort()));
			} else {
				out.writeInt(-1);
			}
			
			if (e.getValue().getAdapterLabel() != null) {
				out.writeUTF(e.getValue().getAdapterLabel().label);
			} else {
				out.writeUTF("");
			}
			
			out.writeBoolean(e.getValue().isPortToEnvironment());
			out.writeBoolean(executionTimePerTimeoutPort.containsKey(e.getValue()));
			out.writeInt(e.getValue().getExecutionTime());
		}
		
		out.writeInt(outputPorts.size());
		
		for (Map.Entry<Integer, ReprPort> e : outputPorts.getElemPerId().entrySet()) {
			out.writeUTF(e.getValue().getName());
			out.writeInt(scopes.get(e.getValue().getReprOwner()));
			out.writeUTF(e.getValue().getType().getCanonicalName());
			
			if (e.getValue().getPulsePort() != null) {
				out.writeInt(outputPorts.get(e.getValue().getPulsePort()));
			} else {
				out.writeInt(-1);
			}
			
			if (e.getValue().getAdapterLabel() != null) {
				out.writeUTF(e.getValue().getAdapterLabel().label);
			} else {
				out.writeUTF("");
			}
			
			out.writeBoolean(e.getValue().isPortToEnvironment());
			out.writeBoolean(executionTimePerTimeoutPort.containsKey(e.getValue()));
			out.writeInt(e.getValue().getExecutionTime());
		}
		
		out.writeInt(vertices.size());
		
		for (Map.Entry<Integer, DecaStableVertex> e1 : vertices.getElemPerId().entrySet()) {
			Map<Integer, DecaFourVertex> statePerScope = new TreeMap<Integer, DecaFourVertex>();
			
			for (Map.Entry<JScope, DecaFourVertex> e2 : e1.getValue().getCfg().getVtxs().entrySet()) {
				statePerScope.put(scopes.get(e2.getKey()), e2.getValue());
			}
			
			for (Map.Entry<Integer, DecaFourVertex> e2 : statePerScope.entrySet()) {
				out.writeUTF(e2.getValue().getName());
			}
			
			Map<Integer, Set<Class<?>>> clzsPerScope = new TreeMap<Integer, Set<Class<?>>>();
			
			for (Map.Entry<JScope, DecaFourVertex> e2 : e1.getValue().getCfg().getVtxs().entrySet()) {
				clzsPerScope.put(scopes.get(e2.getKey()), e2.getValue().getSysmlClzs());
			}
			
			for (Map.Entry<Integer, Set<Class<?>>> e2 : clzsPerScope.entrySet()) {
				out.writeUTF(Texts.concat(e2.getValue(), "+", (e3) -> { return e3.getCanonicalName(); }));
			}
		}
		
		out.writeInt(inputChanges.size());
		
		for (Map.Entry<Integer, DecaStableInputChanges> e1 : inputChanges.getElemPerId().entrySet()) {
			Map<ReprPort, ASALSymbolicValue> val = e1.getValue().getNewValuePerPort().extractValuation();
			out.writeInt(val.size());
			
			for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : val.entrySet()) {
				out.writeInt(inputPorts.get(e2.getKey()));
				writeValue(out, e2.getValue());
			}
			
			if (e1.getValue().getDurationPort() != null) {
				out.writeBoolean(true);
				out.writeInt(inputPorts.get(e1.getValue().getDurationPort()));
			} else {
				out.writeBoolean(false);
				out.writeBoolean(e1.getValue().isHiddenTimerTrigger());
			}
		}
		
		out.writeInt(outputEvolutions.size());
		
		for (Map.Entry<Integer, DecaStableOutputEvolution> e1 : outputEvolutions.getElemPerId().entrySet()) {
			out.writeInt(e1.getValue().getEvolution().size());
			
			for (PulsePack m : e1.getValue().getEvolution()) {
				out.writeInt(m.getValuePerPort().size());
				
				for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : m.getValuePerPort().entrySet()) {
					out.writeInt(outputPorts.get(e2.getKey()));
					writeValue(out, e2.getValue());
				}
			}
		}
		
		writeTransition(out, initialTransition);
		out.writeInt(initialTransitionTarget);
		out.writeInt(transitionsPerTargetPerSource.size());
		
		for (Map.Entry<Integer, Map<Integer, Set<List<Integer>>>> e1 : transitionsPerTargetPerSource.entrySet()) {
			out.writeInt(e1.getKey());
			out.writeInt(e1.getValue().size());
			
			for (Map.Entry<Integer, Set<List<Integer>>> e2 : e1.getValue().entrySet()) {
				out.writeInt(e2.getKey());
				out.writeInt(e2.getValue().size());
				
				for (List<Integer> transition : e2.getValue()) {
					writeTransition(out, transition);
				}
			}
		}
		
		//No state verification:
		out.writeBoolean(false);
		
		//No test suite:
		out.writeBoolean(false);
	}
	
	private static void writeTransition(DataOutputStream out, List<Integer> elems) throws IOException {
		out.writeInt(elems.size());
		
		for (int i = 0; i < elems.size(); i++) {
			out.writeInt(elems.get(i));
		}
	}
	
	private static void writeValue(DataOutputStream out, ASALSymbolicValue v) throws IOException {
		out.writeUTF(extractValue(v.toString()));
	}
	
	private static String extractValue(String s) {
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
			int separatorIndex = s.indexOf(".");
			
			if (separatorIndex < 0) {
				return s;
			}
			
			return "\"" + s.substring(separatorIndex + 1, s.length()) + "\"";
		}
		
		return "\"" + s + "\"";
	}
}

