package jlx.scenario;

import java.util.*;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public abstract class Scenario {
	public abstract Step[] getSteps();
	
	protected <T extends JType> Step setInput(InPort<T> inputPort, T newValue) {
		return new Step.InputStep(inputPort, newValue);
	}
	
	protected <T extends JType> Step triggerTimeout(InPort<T> durationPort) {
		return new Step.TimeoutStep(durationPort);
	}
	
	protected Step expectStateChange(Type1IBD blockInstance, String state0, String... states) {
		return new Step.StateChange(blockInstance, state0, states);
	}
	
	protected Step expectPulse(OutPort<JPulse> pulsePort) {
		return expectOutput(Output.from(pulsePort, JPulse.FALSE), Output.from(pulsePort, JPulse.TRUE), Output.from(pulsePort, JPulse.FALSE));
	}
	
	protected Step expectOutput(Output initial, Output... evolution) {
		List<Map<OutPort<?>, JType>> outputSeq = new ArrayList<Map<OutPort<?>, JType>>();
		outputSeq.add(initial.getValuePerPort());
		
		for (Output e : evolution) {
			outputSeq.add(e.getValuePerPort());
		}
		
		return new Step.OutputStep(outputSeq);
	}
	
	protected Step stabilize() {
		return new Step.StabilizeStep();
	}
	
	protected Step step() {
		return new Step.SingleStep();
	}
}

