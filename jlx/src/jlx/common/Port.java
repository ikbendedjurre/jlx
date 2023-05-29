package jlx.common;

import java.util.*;

import jlx.asal.j.*;

public class Port extends JType {
	private Set<Port> connectedPorts;
	
	public Port() {
		connectedPorts = new HashSet<Port>();
	}
	
	public void disconnectAll() {
		connectedPorts.clear();
	}
	
	protected final void addSourcePort(Port sourcePort) {
		if (sourcePort == null) {
			throw new Error("Source port should NOT be null!");
		}
		
		sourcePort.connectedPorts.add(this);
		connectedPorts.add(sourcePort);
	}
	
	protected final void addTargetPort(Port targetPort) {
		if (targetPort == null) {
			throw new Error("Target port should NOT be null!");
		}
		
		targetPort.connectedPorts.add(this);
		connectedPorts.add(targetPort);
	}
	
	public Set<Port> getConnectedPorts() {
		return Collections.unmodifiableSet(connectedPorts);
	}
}
