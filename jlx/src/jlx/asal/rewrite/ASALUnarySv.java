package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;

public class ASALUnarySv implements ASALSymbolicValue {
	private String op;
	private ASALSymbolicValue expr;
	
	public ASALUnarySv(String op, ASALSymbolicValue expr) {
		this.op = op.toLowerCase();
		this.expr = expr;
	}
	
	public String getOp() {
		return op;
	}
	
	public ASALSymbolicValue getExpr() {
		return expr;
	}
	
	@Override
	public ASALSymbolicValue negate() {
		switch (op) {
			case "+":
			case "-":
				throw new Error("Should not happen!");
			case "not":
				return expr;
		}
		
		throw new Error("Should not happen!");
	}
	
	@Override
	public boolean isBooleanType() {
		switch (op) {
			case "+":
			case "-":
				return false;
			case "not":
			case "NOT":
				return true;
		}
		
		throw new Error("Should not happen!");
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return ASALSymbolicValue.from(op, expr.substitute(subst));
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return expr.getReferencedVars();
	}
	
	@Override
	public int getVarRefCount() {
		return expr.getVarRefCount();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(expr, op);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALUnarySv)) {
			return false;
		}
		ASALUnarySv other = (ASALUnarySv) obj;
		return Objects.equals(expr, other.expr) && Objects.equals(op, other.op);
	}
	
	@Override
	public String toString() {
		return "(" + op + " " + expr + ")";
	}
}

