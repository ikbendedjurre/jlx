package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;

public class TeleX {
	private final TeleNode tgt;
	private final Set<PulsePackMap> inputs;
	private final List<PulsePackMap> output;
	private final int hashCode;
	
	public TeleX(Set<PulsePackMap> inputs, List<PulsePackMap> output, TeleNode tgt) {
		this.inputs = inputs;
		this.output = output;
		this.tgt = tgt;
		
		hashCode = Objects.hash(inputs, output, tgt);
	}
	
	public TeleNode getTgt() {
		return tgt;
	}
	
	public Set<PulsePackMap> getInputs() {
		return inputs;
	}
	
	public List<PulsePackMap> getOutput() {
		return output;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TeleX other = (TeleX) obj;
		return Objects.equals(output, other.output) && Objects.equals(tgt, other.tgt);
	}
}

