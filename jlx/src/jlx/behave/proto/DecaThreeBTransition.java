package jlx.behave.proto;

import java.util.*;

public class DecaThreeBTransition {
	private final DecaThreeBVertex src;
	private final Set<DecaThreeBVertex> tgts;
	private final Set<PulsePackMap> inputs;
	
	public DecaThreeBTransition(DecaThreeBVertex src, Set<DecaThreeBVertex> tgts, Set<PulsePackMap> inputs) {
		this.src = src;
		this.tgts = tgts;
		this.inputs = inputs;
	}
	
	public DecaThreeBVertex getSrc() {
		return src;
	}
	
	public Set<DecaThreeBVertex> getTgts() {
		return tgts;
	}
	
	public Set<PulsePackMap> getInputs() {
		return inputs;
	}
}

