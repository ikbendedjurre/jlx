package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.PulsePackMap;

public class JetX {
	private final JetNode tgt;
	private final PulsePackMap label;
	private final int hashCode;
	
	public JetX(JetNode tgt, PulsePackMap label) {
		this.tgt = tgt;
		this.label = label;
		
		hashCode = Objects.hash(label, tgt);
	}
	
	public JetNode getTgt() {
		return tgt;
	}
	
	public PulsePackMap getLabel() {
		return label;
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
		JetX other = (JetX) obj;
		return Objects.equals(label, other.label) && Objects.equals(tgt, other.tgt);
	}
}

