package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.vars.ASALVariable;

public class ASALPulseFieldSv implements ASALSymbolicValue {
	private final static Object HASH_OBJECT = new Object();
	
	private ASALVariable fieldVar;
	private ASALSymbolicValue expr;
	
	public ASALPulseFieldSv(ASALVariable fieldVar, ASALSymbolicValue expr) {
		this.fieldVar = fieldVar;
		this.expr = expr;
	}
	
	public ASALVariable getFieldVar() {
		return fieldVar;
	}
	
	public ASALSymbolicValue getExpr() {
		return expr;
	}
	
	@Override
	public boolean isBooleanType() {
		return JBool.class.isAssignableFrom(fieldVar.getType()) || JPulse.class.isAssignableFrom(fieldVar.getType());
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return new ASALPulseFieldSv(fieldVar, expr.substitute(subst));
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
		return Objects.hash(HASH_OBJECT, fieldVar, expr);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASALPulseFieldSv other = (ASALPulseFieldSv) obj;
		return Objects.equals(fieldVar, other.fieldVar) && Objects.equals(expr, other.expr);
	}
	
	@Override
	public String toString() {
		return expr.toString() + "." + fieldVar.getName();
	}
}
