package jlx.scenario;

import java.util.HashMap;
import java.util.Map;

import jlx.asal.j.*;
import jlx.blocks.ibd1.OutPort;

public class Output {
	private Map<OutPort<?>, JType> valuePerPort;
	
	private Output(OutPort<?>[] ports, JType[] expectedValues) {
		valuePerPort = new HashMap<OutPort<?>, JType>();
		
		for (int index = 0; index < ports.length; index++) {
			valuePerPort.put(ports[index], expectedValues[index]);
		}
	}
	
	public Map<OutPort<?>, JType> getValuePerPort() {
		return valuePerPort;
	}
	
	public static <T extends JType> Output from(OutPort<T> port, T expectedValue) {
		return new Output(new OutPort<?>[] { port }, new JType[] { expectedValue });
	}
	
	public static <T1 extends JType, T2 extends JType> Output from(OutPort<T1> port1, T1 expectedValue1, OutPort<T2> port2, T2 expectedValue2) {
		return new Output(new OutPort<?>[] { port1, port2 }, new JType[] { expectedValue1, expectedValue2 });
	}
	
	public static <T1 extends JType, T2 extends JType, T3 extends JType> Output from(OutPort<T1> port1, T1 expectedValue1, OutPort<T2> port2, T2 expectedValue2, OutPort<T3> port3, T3 expectedValue3) {
		return new Output(new OutPort<?>[] { port1, port2, port3 }, new JType[] { expectedValue1, expectedValue2, expectedValue3 });
	}
}

