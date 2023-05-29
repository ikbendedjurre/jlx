package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;

public class ASALTernarySv implements ASALSymbolicValue {
	private ASALSymbolicValue condition;
	private ASALSymbolicValue lhs;
	private ASALSymbolicValue rhs;
	
	public ASALTernarySv(ASALSymbolicValue condition, ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		this.condition = condition;
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public ASALSymbolicValue getCondition() {
		return condition;
	}
	
	public ASALSymbolicValue getLhs() {
		return lhs;
	}
	
	public ASALSymbolicValue getRhs() {
		return rhs;
	}
	
	@Override
	public ASALSymbolicValue negate() {
		return new ASALTernarySv(condition, rhs, lhs);
	}
	
	@Override
	public boolean isBooleanType() {
		return lhs.isBooleanType();
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return ASALSymbolicValue.ite(condition.substitute(subst), lhs.substitute(subst), rhs.substitute(subst));
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(condition.getReferencedVars());
		result.addAll(lhs.getReferencedVars());
		result.addAll(rhs.getReferencedVars());
		return result;
	}
	
	@Override
	public int getVarRefCount() {
		return condition.getVarRefCount() + lhs.getVarRefCount() + rhs.getVarRefCount();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(condition, lhs, rhs);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALTernarySv)) {
			return false;
		}
		ASALTernarySv other = (ASALTernarySv) obj;
		return Objects.equals(condition, other.condition) && Objects.equals(lhs, other.lhs) && Objects.equals(rhs, other.rhs);
	}
	
	@Override
	public String toString() {
		return "(if " + condition + " then " + lhs + " else " + rhs + " end if)";
	}
}

