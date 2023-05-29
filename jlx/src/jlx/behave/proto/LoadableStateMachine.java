package jlx.behave.proto;

import java.io.*;
import java.util.*;

public class LoadableStateMachine {
	public static class Component {
		public final String name;
		
		private Component(String name) {
			this.name = name;
		}
	}
	
	public static class Port {
		public final Component owner;
		public final String portName;
		
		private Port(Component owner, String portName) {
			this.owner = owner;
			this.portName = portName;
		}
	}
	
	public static class Input {
		public final String guardText;
		
		private Input(String guardText) {
			this.guardText = guardText;
		}
	}
	
	public static class Output {
		public final Map<Port, String> valuePerPort;
		
		private Output(Map<Port, String> valuePerPort) {
			this.valuePerPort = Collections.unmodifiableMap(valuePerPort);
		}
	}
	
	public static class State {
		public final Output output;
		public final Set<Transition> outgoing;
		public final Set<Transition> incoming;
		
		private State(Output output) {
			this.output = output;
			
			outgoing = new HashSet<Transition>();
			incoming = new HashSet<Transition>();
		}
		
		public Set<Transition> getOutgoing() {
			return outgoing;
		}
		
		public Set<Transition> getIncoming() {
			return incoming;
		}
	}
	
	public static class Transition {
		public final State src;
		public final Input input;
		public final State tgt;
		
		private Transition(State src, Input input, State tgt) {
			this.src = src;
			this.input = input;
			this.tgt = tgt;
		}
	}
	
	private Collection<Component> components;
	private Collection<Port> ports;
	private Collection<Input> inputs;
	private Collection<Output> outputs;
	private Collection<State> states;
	private Collection<Transition> transitions;
	private State initialState;
	
	public LoadableStateMachine(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fis);
		
		int componentCount = in.readInt();
		int portCount = in.readInt();
		int inputCount = in.readInt();
		int outputCount = in.readInt();
		int stateCount = in.readInt();
		int transitionCount = in.readInt();
		
		// Components:
		Map<Integer, Component> componentPerId = new HashMap<Integer, Component>();
		
		for (int i = 0; i < componentCount; i++) {
			componentPerId.put(i, new Component(in.readUTF()));
		}
		
		// Ports:
		Map<Integer, Port> portPerId = new HashMap<Integer, Port>();
		
		for (int i = 0; i < portCount; i++) {
			String portName = in.readUTF();
			Component owner = componentPerId.get(in.readInt());
			portPerId.put(i, new Port(owner, portName));
		}
		
		// Inputs:
		Map<Integer, Input> inputPerId = new HashMap<Integer, Input>();
		
		for (int i = 0; i < inputCount; i++) {
			inputPerId.put(i, new Input(in.readUTF()));
		}
		
		// Outputs:
		Map<Integer, Output> outputPerId = new HashMap<Integer, Output>();
		
		for (int i = 0; i < outputCount; i++) {
			Map<Port, String> valuePerPort = new HashMap<Port, String>();
			
			int mapSize = in.readInt();
			
			for (int j = 0; j < mapSize; j++) {
				Port port = portPerId.get(in.readInt());
				String portValue = in.readUTF();
				valuePerPort.put(port, portValue);
			}
			
			outputPerId.put(i, new Output(valuePerPort));
		}
		
		// States:
		Map<Integer, State> statePerId = new HashMap<Integer, State>();
		
		for (int i = 0; i < stateCount; i++) {
			Output output = outputPerId.get(in.readInt());
			statePerId.put(i, new State(output));
		}
		
		// Initial state:
		initialState = statePerId.get(in.readInt());
		
		// Transitions:
		Map<Integer, Transition> transitionPerId = new HashMap<Integer, Transition>();
		
		for (int i = 0; i < transitionCount; i++) {
			State src = statePerId.get(in.readInt());
			Input input = inputPerId.get(in.readInt());
			State tgt = statePerId.get(in.readInt());
			
			transitionPerId.put(i, new Transition(src, input, tgt));
		}
		
		for (Transition t : transitionPerId.values()) {
			t.src.outgoing.add(t);
			t.tgt.incoming.add(t);
		}
		
		in.close();
		
		components = Collections.unmodifiableCollection(componentPerId.values());
		ports = Collections.unmodifiableCollection(portPerId.values());
		inputs = Collections.unmodifiableCollection(inputPerId.values());
		outputs = Collections.unmodifiableCollection(outputPerId.values());
		states = Collections.unmodifiableCollection(statePerId.values());
		transitions = Collections.unmodifiableCollection(transitionPerId.values());
	}
	
	public Collection<Component> getComponents() {
		return components;
	}
	
	public Collection<Port> getPorts() {
		return ports;
	}
	
	public Collection<Input> getInputs() {
		return inputs;
	}
	
	public Collection<Output> getOutputs() {
		return outputs;
	}
	
	public Collection<State> getStates() {
		return states;
	}
	
	public Collection<Transition> getTransitions() {
		return transitions;
	}
	
	public State getInitialState() {
		return initialState;
	}
	
	public void printStats() {
		System.out.println(getClass().getSimpleName() + ":");
		System.out.println("  #components   = " + components.size());
		System.out.println("  #ports        = " + ports.size());
		System.out.println("  #inputs       = " + inputs.size());
		System.out.println("  #outputs      = " + outputs.size());
		System.out.println("  #states       = " + states.size());
		System.out.println("  #transitions  = " + transitions.size());
	}
}

