package jlx.behave;

import jlx.asal.j.JEvt;
import jlx.asal.parsing.ASALCode;

public class Incoming extends CodeObject {
	private Class<? extends Vertex> sourceState;
	
	public Incoming(Class<? extends Vertex> sourceState, String code0, String... code) {
		super(new ASALCode("TRANSITION", code0, code));
		
		this.sourceState = sourceState;
	}
	
	public Incoming(Class<? extends Vertex> sourceState, JEvt event) {
		super(new ASALCode("TRANSITION", event));
		
		this.sourceState = sourceState;
	}
	
	public Class<? extends Vertex> getSourceState() {
		return sourceState;
	}
}
