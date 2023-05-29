package jlx.behave.stable.files;

import java.io.*;
import java.util.*;

import jlx.utils.Texts;

public class DecaStableStateMachineLoader {
	private SortedMap<Integer, String> scopes;
	private SortedMap<String, Integer> indexPerScopeName;
	private SortedMap<Integer, Port> inputPorts;
	private SortedMap<Integer, Port> outputPorts;
	private SortedMap<Integer, Vertex> vertices;
	private SortedMap<Integer, InputChanges> inputChanges;
	private SortedMap<Integer, OutputEvolution> outputEvolutions;
	private Transition initialTransition;
	private Vertex initialTransitionTarget;
	private Map<Vertex, Map<Vertex, Set<Transition>>> transitionsPerTargetPerSource;
	
	public static class Port {
		private final String name;
		private final String ownerName;
		private final int ownerIndex;
		private final String typeName;
		private final boolean hasPulsePort;
		private final String adapterLabel;
		private final boolean isPortToEnvironment;
		
		private Port(String name, String ownerName, int ownerIndex, String typeName, boolean hasPulsePort, String adapterLabel, boolean isPortToEnvironment) {
			this.name = name;
			this.ownerName = ownerName;
			this.ownerIndex = ownerIndex;
			this.typeName = typeName;
			this.hasPulsePort = hasPulsePort;
			this.adapterLabel = adapterLabel;
			this.isPortToEnvironment = isPortToEnvironment;
		}
		
		public String getName() {
			return name;
		}
		
		public String getOwnerName() {
			return ownerName;
		}
		
		public int getOwnerIndex() {
			return ownerIndex;
		}
		
		public String getTypeName() {
			return typeName;
		}
		
		public boolean hasPulsePort() {
			return hasPulsePort;
		}
		
		public String getAdapterLabel() {
			return adapterLabel;
		}
		
		public boolean isPortToEnvironment() {
			return isPortToEnvironment;
		}
		
		@Override
		public String toString() {
			return ownerName + "::" + name;
		}
	}
	
	public static class Vertex {
		private final Map<String, String> statePerScope;
		private final Map<String, String> clzsPerScope;
		private final Map<Port, String> valuation;
		private final Set<Transition> outgoing;
		
		private Vertex(Map<String, String> statePerScope, Map<String, String> clzsPerScope) {
			this.statePerScope = statePerScope;
			this.clzsPerScope = clzsPerScope;
			
			valuation = new HashMap<Port, String>();
			outgoing = new HashSet<Transition>();
		}
		
		public Map<String, String> getStatePerScope() {
			return statePerScope;
		}
		
		public Map<String, String> getClzsPerScope() {
			return clzsPerScope;
		}
		
		public Map<Port, String> getValuation() {
			return valuation;
		}
		
		public Set<Transition> getOutgoing() {
			return outgoing;
		}
		
		public Set<InputChanges> getOutgoingInputChanges() {
			Set<InputChanges> result = new HashSet<InputChanges>();
			
			for (Transition t : outgoing) {
				if (!result.add(t.getInputChanges())) {
//					if (!t.getInputChanges().isHiddenTimerTrigger()) {
						System.err.println(t.getInputChanges().toString());
						throw new Error("Duplicate transition labels!!");
//					}
				}
			}
			
			return result;
		}
		
		public Transition getOutgoingTransition(InputChanges inputChanges) {
			Transition result = null;
			
			for (Transition t : outgoing) {
				if (t.inputChanges == inputChanges) {
					if (result != null) {
						throw new Error("Should not happen!");
					}
					
					result = t;
				}
			}
			
			if (result == null) {
				System.out.println("ic = " + inputChanges);
				throw new Error("Should not happen!");
			}
			
			return result;
		}
	}
	
	public static class InputChanges {
		private final Map<Port, String> newValuePerPort;
		private final Port durationPort;
		private final boolean isHiddenTimerTrigger;
		
		private InputChanges(Map<Port, String> newValuePerPort, Port durationPort, boolean isHiddenTimerTrigger) {
			this.newValuePerPort = newValuePerPort;
			this.durationPort = durationPort;
			this.isHiddenTimerTrigger = isHiddenTimerTrigger;
		}
		
		public Map<Port, String> getNewValuePerPort() {
			return newValuePerPort;
		}
		
		public Port getDurationPort() {
			return durationPort;
		}
		
		public boolean isHiddenTimerTrigger() {
			return isHiddenTimerTrigger;
		}
		
		@Override
		public String toString() {
			String result = "";
			
			if (isHiddenTimerTrigger) {
				result += "<<hidden-timer-trigger>>";
			}
			
			if (durationPort != null) {
				result += "<<after(" + durationPort.toString() + ")>>";
			}
			
			Set<String> elems = new TreeSet<String>();
			
			for (Map.Entry<Port, String> e : newValuePerPort.entrySet()) {
				elems.add(e.getKey().toString() + "=" + e.getValue());
			}
			
			result += "<<" + Texts.concat(elems, ";") + ">>";
			return result;
		}
	}
	
	public static class OutputEvolution {
		private final List<Map<Port, String>> evolution;
		private final boolean isExternal;
		
		private OutputEvolution(List<Map<Port, String>> evolution) {
			this.evolution = evolution;
			
			isExternal = extractIsExternal();
		}
		
		private boolean extractIsExternal() {
			for (Map<Port, String> e1 : evolution) {
				for (Port e2 : e1.keySet()) {
					if (e2.isPortToEnvironment) {
						return true;
					}
				}
			}
			
			return false;
		}
		
		public List<Map<Port, String>> getEvolution() {
			return evolution;
		}
		
		public Map<Port, String> getLast() {
			return evolution.get(evolution.size() - 1);
		}
		
		public boolean isExternal() {
			return isExternal;
		}
	}
	
	public static class Transition {
		private final Vertex src;
		private final Vertex tgt;
		private final InputChanges inputChanges;
		private final Set<OutputEvolution> outputEvolutions;
		
		private Transition(Vertex src, Vertex tgt, InputChanges inputChanges, Set<OutputEvolution> outputEvolutions) {
			this.src = src;
			this.tgt = tgt;
			this.inputChanges = inputChanges;
			this.outputEvolutions = outputEvolutions;
		}
		
		public Vertex getSrc() {
			return src;
		}
		
		public Vertex getTgt() {
			return tgt;
		}
		
		public InputChanges getInputChanges() {
			return inputChanges;
		}
		
		public Set<OutputEvolution> getOutputEvolutions() {
			return outputEvolutions;
		}
	}
	
	public DecaStableStateMachineLoader() {
		//Empty.
	}
	
	public void loadFromFile(String dirName, String fileName, boolean restrictOutputVal) {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(dirName + "/" + fileName)));
			readFromStream(in, restrictOutputVal);
			in.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	private void readFromStream(DataInputStream in, boolean restrictOutputVal) throws IOException {
		System.out.println("Loading scopes . . .");
		final int scopeCount = in.readInt();
		scopes = new TreeMap<Integer, String>();
		indexPerScopeName = new TreeMap<String, Integer>();
		
		for (int i = 0; i < scopeCount; i++) {
			String s = in.readUTF();
			scopes.put(i, s);
			indexPerScopeName.put(s, i);
		}
		
		System.out.println("Loaded " + scopes.size() + " scopes");
		System.out.println("Loading input ports . . .");
		final int inputPortCount = in.readInt();
		inputPorts = new TreeMap<Integer, Port>();
		
		for (int i = 0; i < inputPortCount; i++) {
			String name = in.readUTF();
			int ownerIndex = in.readInt();
			String ownerName = scopes.get(ownerIndex);
			String typeName = in.readUTF();
			boolean hasPulsePort = in.readInt() != -1; 
			String adapterLabel = in.readUTF();
			boolean isPortToEnvironment = in.readBoolean();
			
			inputPorts.put(i, new Port(name, ownerName, ownerIndex, typeName, hasPulsePort, adapterLabel, isPortToEnvironment));
		}
		
		System.out.println("Loaded " + inputPorts.size() + " input ports");
		System.out.println("Loading output ports . . .");
		final int outputPortCount = in.readInt();
		outputPorts = new TreeMap<Integer, Port>();
		
		for (int i = 0; i < outputPortCount; i++) {
			String name = in.readUTF();
			int ownerIndex = in.readInt();
			String ownerName = scopes.get(ownerIndex);
			String typeName = in.readUTF();
			boolean hasPulsePort = in.readInt() != -1;
			String adapterLabel = in.readUTF();
			boolean isPortToEnvironment = in.readBoolean();
			
			outputPorts.put(i, new Port(name, ownerName, ownerIndex, typeName, hasPulsePort, adapterLabel, isPortToEnvironment));
		}
		
		System.out.println("Loaded " + outputPorts.size() + " output ports");
		System.out.println("Loading vertices . . .");
		final int vertexCount = in.readInt();
		// System.out.println("#expected = " + vertexCount);
		vertices = new TreeMap<Integer, Vertex>();
		
		for (int i = 0; i < vertexCount; i++) {
			Map<String, String> statePerScope = new HashMap<String, String>();
			
			for (int j = 0; j < scopeCount; j++) {
				statePerScope.put(scopes.get(j), in.readUTF());
			}
			
			Map<String, String> clzsPerScope = new HashMap<String, String>();
			
			for (int j = 0; j < scopeCount; j++) {
				clzsPerScope.put(scopes.get(j), in.readUTF());
			}
			
			vertices.put(i, new Vertex(statePerScope, clzsPerScope));
		}
		
		System.out.println("Loaded " + vertices.size() + " vertices");
		System.out.println("Loading input changes . . .");
		final int inputChangesCount = in.readInt();
		inputChanges = new TreeMap<Integer, InputChanges>();
		
		for (int i = 0; i < inputChangesCount; i++) {
			Map<Port, String> newValuePerPort = new HashMap<Port, String>();
			Port durationPort;
			boolean isHiddenTimerTrigger;
			
			int newValuePerPortSize = in.readInt();
			
			for (int j = 0; j < newValuePerPortSize; j++) {
				Port port = readInputPort(in);
				String value = readValue(in);
				newValuePerPort.put(port, value);
			}
			
			if (in.readBoolean()) {
				durationPort = readInputPort(in);
				isHiddenTimerTrigger = false;
			} else {
				durationPort = null;
				isHiddenTimerTrigger = in.readBoolean();
			}
			
			inputChanges.put(i, new InputChanges(newValuePerPort, durationPort, isHiddenTimerTrigger));
		}
		
		System.out.println("Loaded " + inputChanges.size() + " input changes");
		System.out.println("Loading output evolutions . . .");
		final int outputEvolutionCount = in.readInt();
		outputEvolutions = new TreeMap<Integer, OutputEvolution>();
		
		for (int i = 0; i < outputEvolutionCount; i++) {
			List<Map<Port, String>> evolution = new ArrayList<Map<Port, String>>();
			final int evolutionLength = in.readInt();
			
			for (int j = 0; j < evolutionLength; j++) {
				Map<Port, String> entries = new HashMap<Port, String>();
				final int entryCount = in.readInt();
				
				for (int k = 0; k < entryCount; k++) {
					Port port = readOutputPort(in);
					String value = readValue(in);
					entries.put(port, value);
				}
				
				evolution.add(entries);
			}
			
			outputEvolutions.put(i, new OutputEvolution(evolution));
		}
		
		System.out.println("Loaded " + outputEvolutions.size() + " output evolutions");
		System.out.println("Loading initial transition . . .");
		initialTransition = readTransition(in, null, null);
		initialTransitionTarget = readVertex(in);
		initialTransition = new Transition(null, initialTransitionTarget, initialTransition.getInputChanges(), initialTransition.getOutputEvolutions());
		
		System.out.println("Loading transitions . . .");
		final int sourceVertexCount = in.readInt();
		transitionsPerTargetPerSource = new HashMap<Vertex, Map<Vertex, Set<Transition>>>();
		int tCount = 0;
		
		for (int i = 0; i < sourceVertexCount; i++) {
			Vertex src = readVertex(in);
			int targetVertexCount = in.readInt();
			Map<Vertex, Set<Transition>> transitionsPerTarget = new HashMap<Vertex, Set<Transition>>();
			
			for (int j = 0; j < targetVertexCount; j++) {
				Vertex tgt = readVertex(in);
				final int transitionCount = in.readInt();
				Set<Transition> transitions = new HashSet<Transition>();
				
				for (int k = 0; k < transitionCount; k++) {
					Transition t = readTransition(in, src, tgt);
					transitions.add(t);
					src.getOutgoing().add(t);
					tCount++;
				}
				
				transitionsPerTarget.put(tgt, transitions);
			}
			
			transitionsPerTargetPerSource.put(src, transitionsPerTarget);
		}
		
		System.out.println("Loaded " + tCount + " transitions");
		
		populateVertexValuations(restrictOutputVal);
		
		for (Vertex vtx : vertices.values()) {
			vtx.getOutgoingInputChanges();
		}
	}
	
	public Map<Port, String> getInitialValuation(boolean restrictOutputVal) {
		Map<Port, String> result = new HashMap<Port, String>();
		
		for (OutputEvolution evo : getInitialTransition().getOutputEvolutions()) {
			if (!restrictOutputVal || evo.isExternal()) {
				result.putAll(evo.getLast());
			}
		}
		
		return result;
	}
	
	private void populateVertexValuations(boolean restrictOutputVal) {
		for (Vertex vtx : getVertices().values()) {
			vtx.getValuation().clear();
		}
		
		getInitialTransitionTarget().getValuation().putAll(getInitialValuation(restrictOutputVal));
		
		Set<Vertex> beenHere = new HashSet<Vertex>();
		beenHere.add(getInitialTransitionTarget());
		
		Set<Vertex> fringe = new HashSet<Vertex>();
		Set<Vertex> newFringe = new HashSet<Vertex>();
		fringe.add(getInitialTransitionTarget());
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (Vertex vtx : fringe) {
				for (Transition t : vtx.getOutgoing()) {
					if (beenHere.add(t.getTgt())) {
						Map<Port, String> val = new HashMap<Port, String>();
						val.putAll(vtx.getValuation());
						
						for (OutputEvolution evo : t.getOutputEvolutions()) {
							if (!restrictOutputVal || evo.isExternal()) {
								val.putAll(evo.getLast());
							}
						}
						
						t.getTgt().getValuation().putAll(val);
						newFringe.add(t.getTgt());
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
	}
	
	private Port readInputPort(DataInputStream in) throws IOException {
		Port result = inputPorts.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private Port readOutputPort(DataInputStream in) throws IOException {
		Port result = outputPorts.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private String readValue(DataInputStream in) throws IOException {
		return in.readUTF();
	}
	
	private Vertex readVertex(DataInputStream in) throws IOException {
		Vertex result = vertices.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private Transition readTransition(DataInputStream in, Vertex src, Vertex tgt) throws IOException {
		int dataSize = in.readInt();
		int[] data = new int[dataSize];
		
		for (int i = 0; i < dataSize; i++) {
			data[i] = in.readInt();
		}
		
		InputChanges ic = inputChanges.get(data[0]);
		
		if (ic == null) {
			throw new Error("Should not happen; could not find input change " + data[0] + "!");
		}
		
		Set<OutputEvolution> aes = new HashSet<OutputEvolution>();
		
		for (int i = 1; i < dataSize; i++) {
			OutputEvolution ae = outputEvolutions.get(data[i]);
			
			if (ae == null) {
				throw new Error("Should not happen; could not find output evolution " + data[i] + "!");
			}
			
			aes.add(ae);
		}
		
		return new Transition(src, tgt, ic, aes);
	}
	
	public Map<Integer, String> getScopes() {
		return scopes;
	}
	
	public Map<Integer, Port> getInputPorts() {
		return inputPorts;
	}
	
	public Map<Integer, Port> getOutputPorts() {
		return outputPorts;
	}
	
	public Map<Integer, Vertex> getVertices() {
		return vertices;
	}
	
	public Map<Integer, InputChanges> getInputChanges() {
		return inputChanges;
	}
	
	public Map<Integer, OutputEvolution> getOutputEvolutions() {
		return outputEvolutions;
	}
	
	public Transition getInitialTransition() {
		return initialTransition;
	}
	
	public Vertex getInitialTransitionTarget() {
		return initialTransitionTarget;
	}
	
	public Map<Vertex, Map<Vertex, Set<Transition>>> getTransitionsPerTargetPerSource() {
		return transitionsPerTargetPerSource;
	}
	
	public static void main(String[] args) {
		DecaStableStateMachineLoader loader = new DecaStableStateMachineLoader();
		loader.loadFromFile("models", "all.stable", false);
	}
}
