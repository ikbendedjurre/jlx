package jlx.behave.stable.files;

import java.io.*;
import java.util.*;

public class DecaStableFileReader extends DecaStableFile {
	private final SortedMap<Integer, Scope> scopes;
	private final SortedMap<Integer, Port> inputPorts;
	private final SortedMap<Integer, Port> outputPorts;
	private final SortedMap<Integer, Vertex> vertices;
	private final SortedMap<Integer, InputChanges> inputChanges;
	private final SortedMap<Integer, OutputEvolution> outputEvolutions;
	private Transition initialTransition;
	private final List<Transition> transitions;
	private final Map<Vertex, CharSet> charSetPerVtx;
	private final List<Trace> tests;
	
	public DecaStableFileReader() {
		scopes = new TreeMap<Integer, Scope>();
		inputPorts = new TreeMap<Integer, Port>();
		outputPorts = new TreeMap<Integer, Port>();
		vertices = new TreeMap<Integer, Vertex>();
		inputChanges = new TreeMap<Integer, InputChanges>();
		outputEvolutions = new TreeMap<Integer, OutputEvolution>();
		initialTransition = null;
		transitions = new ArrayList<Transition>();
		charSetPerVtx = new HashMap<Vertex, CharSet>();
		tests = new ArrayList<Trace>();
	}
	
	public void setInitialTransition(Transition t) {
		initialTransition = t;
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
		scopes.clear();
		
		for (int id = 0; id < scopeCount; id++) {
			scopes.put(id, readScope(id, in));
		}
		
		System.out.println("Loaded " + scopes.size() + " scopes");
		System.out.println("Loading input ports . . .");
		final int inputPortCount = in.readInt();
		int externalInputPortCount = 0;
		inputPorts.clear();
		
		for (int i = 0; i < inputPortCount; i++) {
			Port p = readPort(i, in);
			inputPorts.put(i, p);
			
			if (p.isPortToEnvironment()) {
				externalInputPortCount++;
			}
		}
		
		System.out.println("Loaded " + inputPorts.size() + " input ports (" + externalInputPortCount + " external)");
		System.out.println("Loading output ports . . .");
		final int outputPortCount = in.readInt();
		int externalOutputPortCount = 0;
		outputPorts.clear();
		
		for (int id = 0; id < outputPortCount; id++) {
			Port p = readPort(id, in);
			outputPorts.put(id, p);
			
			if (p.isPortToEnvironment()) {
				externalOutputPortCount++;
			}
		}
		
		System.out.println("Loaded " + outputPorts.size() + " output ports (" + externalOutputPortCount + " external)");
		System.out.println("Loading vertices . . .");
		final int vertexCount = in.readInt();
		vertices.clear();
		
		for (int id = 0; id < vertexCount; id++) {
			vertices.put(id, readVertex(id, in));
		}
		
		System.out.println("Loaded " + vertices.size() + " vertices");
		System.out.println("Loading input changes . . .");
		final int inputChangesCount = in.readInt();
		inputChanges.clear();
		
		for (int id = 0; id < inputChangesCount; id++) {
			inputChanges.put(id, readInputChanges(id, in));
		}
		
		System.out.println("Loaded " + inputChanges.size() + " input changes");
		System.out.println("Loading output evolutions . . .");
		final int outputEvolutionCount = in.readInt();
		outputEvolutions.clear();
		
		for (int id = 0; id < outputEvolutionCount; id++) {
			outputEvolutions.put(id, readOutputEvolution(id, in));
		}
		
		System.out.println("Loaded " + outputEvolutions.size() + " output evolutions");
		
		{
			System.out.println("Loading initial transition . . .");
			Transition t = readTransition(in, -1, null, null);
			Vertex initialTransitionTarget = resolveVertex(in);
			initialTransition = new Transition(t.getId(), null, initialTransitionTarget, t.getInputChanges(), t.getOutputEvolutions());
			System.out.println("Loaded 1 initial transition");
		}
		
		System.out.println("Loading transitions . . .");
		final int sourceVertexCount = in.readInt();
		transitions.clear();
		
		int tCount = 0;
		
		for (int id = 0; id < sourceVertexCount; id++) {
			Vertex src = resolveVertex(in);
			int targetVertexCount = in.readInt();
			
			for (int j = 0; j < targetVertexCount; j++) {
				Vertex tgt = resolveVertex(in);
				final int transitionCount = in.readInt();
				
				for (int k = 0; k < transitionCount; k++) {
					Transition t = readTransition(in, transitions.size(), src, tgt);
					transitions.add(t);
					src.getOutgoing().add(t);
					tgt.getIncoming().add(t);
					tCount++;
					
					if (tCount % 500000 == 0) {
						System.out.println("Loaded " + tCount + " transitions . . .");
					}
				}
			}
		}
		
		System.out.println("Loaded " + tCount + " transitions");
		
		populateVertexValuations(restrictOutputVal);
		
		for (Vertex vtx : vertices.values()) {
			vtx.getOutgoingInputChanges();
		}
		
		if (in.readBoolean()) {
			System.out.println("Loading char-sets . . .");
			int charSetCount = in.readInt();
			charSetPerVtx.clear();
			
			for (int id = 0; id < charSetCount; id++) {
				CharSet charSet = readCharSet(in);
				
				if (charSetPerVtx.containsKey(charSet.getVertex())) {
					System.out.println("WARNING! Duplicate char set for vertex " + charSet.getVertex().getId() + "!");
				}
				
				charSetPerVtx.put(charSet.getVertex(), charSet);
			}
			
			System.out.println("Loaded " + charSetPerVtx.size() + " char-sets");
		}
		
		if (in.readBoolean()) {
			System.out.println("Loading tests . . .");
			int testCount = in.readInt();
			tests.clear();
			
			for (int id = 0; id < testCount; id++) {
				tests.add(readTest(id, in));
			}
			
			System.out.println("Loaded " + tests.size() + " tests");
		}
		
		{
			Set<IO> ios = new HashSet<IO>();
			Set<Set<OutputEvolution>> evos = new HashSet<Set<OutputEvolution>>();
			
			for (Transition t : transitions) {
				ios.add(new IO(t.getInputChanges(), t.getExternalOutputEvolutions()));
				evos.add(t.getExternalOutputEvolutions());
			}
			
			System.out.println("#external-IOs = " + ios.size());
			System.out.println("#external-evos = " + evos.size());
		}
	}
	
	private static class IO {
		private final InputChanges ics;
		private final Set<OutputEvolution> evos;
		private final int hashCode;
		
		public IO(InputChanges ics, Set<OutputEvolution> evos) {
			this.ics = ics;
			this.evos = evos;
			
			hashCode = Objects.hash(evos, ics);
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
			IO other = (IO) obj;
			return Objects.equals(evos, other.evos) && Objects.equals(ics, other.ics);
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
		
		getInitialTransition().getTgt().getValuation().putAll(getInitialValuation(restrictOutputVal));
		
		Set<Vertex> beenHere = new HashSet<Vertex>();
		beenHere.add(getInitialTransition().getTgt());
		
		Set<Vertex> fringe = new HashSet<Vertex>();
		Set<Vertex> newFringe = new HashSet<Vertex>();
		fringe.add(getInitialTransition().getTgt());
		
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
	
	private Scope readScope(int id, DataInputStream in) throws IOException {
		String name = in.readUTF();
		
		return new Scope(id, name);
	}
	
	private Port readPort(int id, DataInputStream in) throws IOException {
		String name = in.readUTF();
		Scope owner = resolveScope(in);
		String typeName = in.readUTF();
		int portIndex = in.readInt();
		String adapterLabel = in.readUTF();
		boolean isPortToEnvironment = in.readBoolean();
		boolean isTimeoutPort = in.readBoolean();
		int executionTime = in.readInt();
		
		return new Port(id, name, owner, typeName, portIndex, adapterLabel, isPortToEnvironment, isTimeoutPort, executionTime);
	}
	
	private Vertex readVertex(int id, DataInputStream in) throws IOException {
		Map<Scope, String> statePerScope = new HashMap<Scope, String>();
		
		for (int j = 0; j < scopes.size(); j++) {
			statePerScope.put(scopes.get(j), in.readUTF());
		}
		
		Map<Scope, String> clzsPerScope = new HashMap<Scope, String>();
		
		for (int j = 0; j < scopes.size(); j++) {
			clzsPerScope.put(scopes.get(j), in.readUTF());
		}
		
		return new Vertex(id, statePerScope, clzsPerScope);
	}
	
	private InputChanges readInputChanges(int id, DataInputStream in) throws IOException {
		Map<Port, String> newValuePerPort = new HashMap<Port, String>();
		Port durationPort;
		boolean isHiddenTimerTrigger;
		int newValuePerPortSize = in.readInt();
		
		for (int j = 0; j < newValuePerPortSize; j++) {
			Port port = resolveInputPort(in);
			String value = resolveValue(in);
			newValuePerPort.put(port, value);
		}
		
		if (in.readBoolean()) {
			durationPort = resolveInputPort(in);
			isHiddenTimerTrigger = false;
		} else {
			durationPort = null;
			isHiddenTimerTrigger = in.readBoolean();
		}
		
		return new InputChanges(id, newValuePerPort, durationPort, isHiddenTimerTrigger);
	}
	
	private OutputEvolution readOutputEvolution(int id, DataInputStream in) throws IOException {
		List<Map<Port, String>> evolution = new ArrayList<Map<Port, String>>();
		final int evolutionLength = in.readInt();
		
		for (int j = 0; j < evolutionLength; j++) {
			Map<Port, String> entries = new HashMap<Port, String>();
			final int entryCount = in.readInt();
			
			for (int k = 0; k < entryCount; k++) {
				Port port = resolveOutputPort(in);
				String value = resolveValue(in);
				entries.put(port, value);
			}
			
			evolution.add(entries);
		}
		
		return new OutputEvolution(id, evolution);
	}
	
//	private Response readResponse(DataInputStream in) throws IOException {
//		Set<OutputEvolution> evos = new HashSet<OutputEvolution>();
//		final int evoCount = in.readInt();
//		
//		for (int index = 0; index < evoCount; index++) {
//			evos.add(resolveOutputEvolution(in.readInt()));
//		}
//		
//		Map<Scope, String> statePerScope = new HashMap<Scope, String>();
//		final int scopeCount = in.readInt();
//		
//		for (int index = 0; index < scopeCount; index++) {
//			Scope scope = resolveScope(in);
//			String state = in.readUTF();
//			statePerScope.put(scope, state);
//		}
//		
//		Map<Port, String> valuation = new HashMap<Port, String>();
//		final int portCount = in.readInt();
//		
//		for (int index = 0; index < portCount; index++) {
//			Port port = resolveOutputPort(in);
//			String value = in.readUTF();
//			valuation.put(port, value);
//		}
//		
//		return new Response(evos, statePerScope, valuation);
//	}
	
	private CharSet readCharSet(DataInputStream in) throws IOException {
		Set<Trace> result = new HashSet<Trace>();
		final int traceCount = in.readInt();
		Vertex vtx = resolveVertex(in);
		
		for (int index = 0; index < traceCount; index++) {
			result.add(readTrace(index, in));
		}
		
		return new CharSet(vtx, result);
	}
	
	private Trace readTrace(int id, DataInputStream in) throws IOException {
		final int transitionCount = in.readInt();
		List<Transition> transitions = new ArrayList<Transition>();
		
		for (int index = 0; index < transitionCount; index++) {
			transitions.add(resolveTransition(in));
		}
		
		return new Trace(id, transitions);
	}
	
	private Trace readTest(int id, DataInputStream in) throws IOException {
		return readTrace(id, in);
	}
	
//	private InputChanges resolveInputChanges(DataInputStream in) throws IOException {
//		InputChanges result = inputChanges.get(in.readInt());
//		
//		if (result == null) {
//			throw new Error("Should not happen!");
//		}
//		
//		return result;
//	}
	
	private Scope resolveScope(DataInputStream in) throws IOException {
		Scope result = scopes.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private Port resolveInputPort(DataInputStream in) throws IOException {
		Port result = inputPorts.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private Port resolveOutputPort(DataInputStream in) throws IOException {
		Port result = outputPorts.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private String resolveValue(DataInputStream in) throws IOException {
		String s = in.readUTF();
		
		if (s.contains("\\")) {
			throw new Error("Should not happen!");
		}
		
		return s;
	}
	
	private Vertex resolveVertex(DataInputStream in) throws IOException {
		Vertex result = vertices.get(in.readInt());
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private Transition resolveTransition(DataInputStream in) throws IOException {
		int id = in.readInt();
		
		if (id == -1) {
			return initialTransition;
		}
		
		Transition result = transitions.get(id);
		
		if (result == null) {
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	private Transition readTransition(DataInputStream in, int id, Vertex src, Vertex tgt) throws IOException {
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
			aes.add(resolveOutputEvolution(data[i]));
		}
		
		return new Transition(id, src, tgt, ic, aes);
	}
	
	private OutputEvolution resolveOutputEvolution(int id) {
		OutputEvolution ae = outputEvolutions.get(id);
		
		if (ae == null) {
			throw new Error("Should not happen; could not find output evolution " + id + "!");
		}
		
		return ae;
	}
	
	public void clear() {
		scopes.clear();
		inputPorts.clear();
		outputPorts.clear();
		vertices.clear();
		inputChanges.clear();
		outputEvolutions.clear();
		charSetPerVtx.clear();
		tests.clear();
	}
	
	public Map<Integer, Scope> getScopes() {
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
	
	public List<Transition> getTransitions() {
		return transitions;
	}
	
	public Map<Vertex, CharSet> getCharSetPerVertex() {
		return charSetPerVtx;
	}
	
	public List<Trace> getTests() {
		return tests;
	}
	
	public Port findInputPort(String ownerName, String nameStart) {
		Port result = null;
		
		for (Port p : inputPorts.values()) {
			if (p.getOwner().getName().equals(ownerName)) {
				if (p.getName().startsWith(nameStart)) {
					if (result != null) {
						throw new Error("Ambiguous input port " + ownerName + "::" + nameStart + "!");
					}
					
					result = p;
				}
			}
		}
		
		if (result == null) {
			throw new Error("Could not find input port " + ownerName + "::" + nameStart + "!");
		}
		
		return result;
	}
	
	public Port findOutputPort(String ownerName, String nameStart) {
		Port result = null;
		
		for (Port p : outputPorts.values()) {
			if (p.getOwner().getName().equals(ownerName)) {
				if (p.getName().startsWith(nameStart)) {
					if (result != null) {
						throw new Error("Ambiguous output port " + ownerName + "::" + nameStart + "!");
					}
					
					result = p;
				}
			}
		}
		
		if (result == null) {
			throw new Error("Could not find output port " + ownerName + "::" + nameStart + "!");
		}
		
		return result;
	}
}
