package jlx.asal.rewrite;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jlx.asal.vars.ASALVariable;

public class ASALEqSv implements ASALSymbolicValue {
	private final static Object HASH_OBJECT = new Object();
	
	private ASALSymbolicValue lhs;
	private ASALSymbolicValue rhs;
	
	public ASALEqSv(ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public ASALSymbolicValue getLhs() {
		return lhs;
	}
	
	public ASALSymbolicValue getRhs() {
		return rhs;
	}
	
	@Override
	public boolean isBooleanType() {
		return true;
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return ASALSymbolicValue.eq(lhs.substitute(subst), rhs.substitute(subst));
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(lhs.getReferencedVars());
		result.addAll(rhs.getReferencedVars());
		return result;
	}
	
	@Override
	public int getVarRefCount() {
		return lhs.getVarRefCount() + rhs.getVarRefCount();
	}
	
	@Override
	public int hashCode() {
		if (lhs.hashCode() < rhs.hashCode()) {
			return Objects.hash(HASH_OBJECT, lhs, rhs);
		}
		
		return Objects.hash(HASH_OBJECT, rhs, lhs);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALEqSv)) {
			return false;
		}
		ASALEqSv other = (ASALEqSv) obj;
		if (Objects.equals(lhs, other.lhs) && Objects.equals(rhs, other.rhs)) {
			return true;
		}
		return Objects.equals(lhs, other.rhs) && Objects.equals(rhs, other.lhs);
	}
	
	@Override
	public String toString() {
		return "(" + lhs + " = " + rhs + ")";
	}
}
