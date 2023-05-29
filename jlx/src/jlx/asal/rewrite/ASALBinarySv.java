package jlx.asal.rewrite;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jlx.asal.vars.ASALVariable;

public class ASALBinarySv implements ASALSymbolicValue {
	private String op;
	private ASALSymbolicValue lhs;
	private ASALSymbolicValue rhs;
	
	public ASALBinarySv(String op, ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		this.op = op.toLowerCase();
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public String getOp() {
		return op;
	}
	
	public ASALSymbolicValue getLhs() {
		return lhs;
	}
	
	public ASALSymbolicValue getRhs() {
		return rhs;
	}
	
	@Override
	public boolean isBooleanType() {
		switch (op) {
			case "+":
			case "-":
			case "*":
			case "/":
			case "%":
				return false;
			case "and":
			case "or":
			case "=":
			case "<>":
			case ">=":
			case "<=":
			case ">":
			case "<":
				return true;
		}
		
		throw new Error("Should not happen!");
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return ASALSymbolicValue.from(op, lhs.substitute(subst), rhs.substitute(subst));
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
		return Objects.hash(lhs, op, rhs);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALBinarySv)) {
			return false;
		}
		ASALBinarySv other = (ASALBinarySv) obj;
		return Objects.equals(lhs, other.lhs) && Objects.equals(op, other.op) && Objects.equals(rhs, other.rhs);
	}
	
	@Override
	public String toString() {
		return "(" + lhs + " " + op + " " + rhs + ")";
	}
}

