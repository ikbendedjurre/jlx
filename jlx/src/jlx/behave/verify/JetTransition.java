package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;

public class JetTransition {
	private final JetNode src;
	private final JetNode tgt;
	private final List<PulsePackMap> map;
	private final int hashCode;
	
	public JetTransition(JetNode src, JetNode tgt, List<PulsePackMap> map) {
		this.src = src;
		this.tgt = tgt;
		this.map = map;
		
		hashCode = Objects.hash(map, src, tgt);
	}
	
	public JetNode getSrc() {
		return src;
	}
	
	public JetNode getTgt() {
		return tgt;
	}
	
	public List<PulsePackMap> getMap() {
		return map;
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
		JetTransition other = (JetTransition) obj;
		return Objects.equals(map, other.map) && Objects.equals(src, other.src) && Objects.equals(tgt, other.tgt);
	}
}

