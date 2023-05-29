package jlx.blocks.ibd2;

import jlx.blocks.ibd1.*;
import jlx.common.*;

public final class InterfacePort extends Port {
	public final String flowSpec;
	
	public InterfacePort() {
		this(null);
	}
	
	public InterfacePort(String flowSpec) {
		this.flowSpec = flowSpec;
	}
	
	public void connect(InPort<?> targetPort) {
		addTargetPort(targetPort);
	}
	
	public void connect(OutPort<?> sourcePort) {
		addSourcePort(sourcePort);
	}
	
	public void connect(InterfacePort other) {
		addSourcePort(other);
		addTargetPort(other);
	}
}
