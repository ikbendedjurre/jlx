package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;

public class ASALFalseSv implements ASALSymbolicValue {
	private final static int HASH = new Object().hashCode();
	
	@Override
	public ASALSymbolicValue negate() {
		return new ASALTrueSv();
	}
	
	@Override
	public boolean isBooleanType() {
		return true;
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return this;
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.emptySet();
	}
	
	@Override
	public int getVarRefCount() {
		return 0;
	}
	
	@Override
	public int hashCode() {
		return HASH;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALFalseSv)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "FALSE";
	}
}
