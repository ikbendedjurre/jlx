package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.PulsePackMap;

public class PalmaX {
	private final Set<PulsePackMap> trimmedInputs;
	private final Set<PalmaNode> tgts;
	private final int hashCode;
	
	public PalmaX(Set<PulsePackMap> trimmedInputs, Set<PalmaNode> tgts) {
		this.trimmedInputs = trimmedInputs;
		this.tgts = tgts;
		
		hashCode = Objects.hash(tgts, trimmedInputs);
	}
	
	public Set<PulsePackMap> getTrimmedInputs() {
		return trimmedInputs;
	}
	
	public Set<PalmaNode> getTgts() {
		return tgts;
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
		PalmaX other = (PalmaX) obj;
		return Objects.equals(tgts, other.tgts) && Objects.equals(trimmedInputs, other.trimmedInputs);
	}
}

