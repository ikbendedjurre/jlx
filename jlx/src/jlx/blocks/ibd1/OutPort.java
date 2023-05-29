package jlx.blocks.ibd1;

import jlx.asal.j.JType;
import jlx.blocks.ibd2.InterfacePort;
import jlx.utils.Dir;

public final class OutPort<T extends JType> extends PrimitivePort<T> {
	public OutPort() {
		//Empty.
	}
	
	public void connect(InPort<T> targetPort) {
		addTargetPort(targetPort);
	}
	
	public void connect(InPort<T> targetPort1, InPort<T> targetPort2) {
		addTargetPort(targetPort1);
		addTargetPort(targetPort2);
	}
	
	public void connect(InPort<T> targetPort1, InPort<T> targetPort2, InPort<T> targetPort3) {
		addTargetPort(targetPort1);
		addTargetPort(targetPort2);
		addTargetPort(targetPort3);
	}
	
	public void connect(InterfacePort targetPort) {
		addTargetPort(targetPort);
	}
	
	@Override
	public final Dir getDir() {
		return Dir.OUT;
	}
}
