package jlx.behave;

import jlx.asal.j.JEvt;
import jlx.asal.parsing.ASALCode;

public class Outgoing extends CodeObject {
	private Class<? extends Vertex> targetState;
	
	public Outgoing(Class<? extends Vertex> targetState, String code0, String... code) {
		super(new ASALCode("TRANSITION", code0, code));
		
		this.targetState = targetState;
	}
	
	public Outgoing(Class<? extends Vertex> targetState, JEvt event) {
		super(new ASALCode("TRANSITION", event));
		
		this.targetState = targetState;
	}
	
	public Class<? extends Vertex> getTargetState() {
		return targetState;
	}
}
