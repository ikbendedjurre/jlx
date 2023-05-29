package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;
import jlx.common.FileLocation;
import jlx.utils.Texts;

public class DecaTwoStateConfig {
	private DecaOneVertex vertex;
	private Map<ASALVariable, ASALSymbolicValue> valuation;
	
	public DecaTwoStateConfig(DecaOneVertex vertex, Map<ASALVariable, ASALSymbolicValue> valuation) {
		this.vertex = vertex;
		this.valuation = valuation;
	}
	
	public DecaOneVertex getVertex() {
		return vertex;
	}
	
	public Set<FileLocation> getFileLocations() {
		return vertex.getFileLocations();
	}
	
	public Map<ASALVariable, ASALSymbolicValue> getValuation() {
		return valuation;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return vertex.getSysmlClzs();
	}
	
	public String getClzsStr() {
		return Texts.concat(getSysmlClzs(), "+", (c) -> { return c.getSimpleName(); });
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(valuation, vertex);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DecaTwoStateConfig)) {
			return false;
		}
		DecaTwoStateConfig other = (DecaTwoStateConfig) obj;
		return Objects.equals(valuation, other.valuation) && Objects.equals(vertex, other.vertex);
	}
}

