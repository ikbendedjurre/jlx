package jlx.scenario;

import java.util.*;

import jlx.asal.j.JType;
import jlx.blocks.ibd1.*;
import jlx.common.FileLocation;

public abstract class Step {
	private final FileLocation fileLocation;
	
	private Step() {
		fileLocation = new FileLocation();
	}
	
	public FileLocation getFileLocation() {
		return fileLocation;
	}
	
	public static class InputStep extends Step {
		private InPort<?> port;
		private JType newValue;
		
		public <T extends JType, V extends T> InputStep(InPort<T> port, V newValue) {
			this.port = port;
			this.newValue = newValue;
		}
		
		public InPort<?> getPort() {
			return port;
		}
		
		public JType getNewValue() {
			return newValue;
		}
	}
	
	public static class TimeoutStep extends Step {
		private InPort<?> durationPort;
		
		public <T extends JType, V extends T> TimeoutStep(InPort<T> durationPort) {
			this.durationPort = durationPort;
		}
		
		public InPort<?> getDurationPort() {
			return durationPort;
		}
	}
	
	public static class OutputStep extends Step {
		private List<Map<OutPort<?>, JType>> outputSeq;
		
		public OutputStep(List<Map<OutPort<?>, JType>> outputSeq) {
			this.outputSeq = Collections.unmodifiableList(outputSeq);
		}
		
		public List<Map<OutPort<?>, JType>> getOutputSeq() {
			return outputSeq;
		}
	}
	
	public static class StabilizeStep extends Step {
		//Empty.
	}
	
	public static class SingleStep extends Step {
		//Empty.
	}
	
	public static class StateChange extends Step {
		private final Type1IBD blockInstance;
		private final List<String> stateSeq;
		
		public StateChange(Type1IBD blockInstance, String state0, String... states) {
			this.blockInstance = blockInstance;
			
			stateSeq = new ArrayList<String>();
			stateSeq.add(state0);
			
			for (String state : states) {
				stateSeq.add(state);
			}
		}
		
		public Type1IBD getBlockInstance() {
			return blockInstance;
		}
		
		public List<String> getStateSeq() {
			return stateSeq;
		}
	}
}

