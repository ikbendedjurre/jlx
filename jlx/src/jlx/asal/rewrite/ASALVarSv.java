package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALVarSv implements ASALSymbolicValue {
	private ASALVariable var;
	
	public ASALVarSv(ASALVariable var) {
		this.var = var;
	}
	
	public ASALVariable getVar() {
		return var;
	}
	
	@Override
	public boolean isBooleanType() {
		return JBool.class.isAssignableFrom(var.getType()) || JPulse.class.isAssignableFrom(var.getType());
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return subst.getOrDefault(var, this);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.singleton(var);
	}
	
	@Override
	public int getVarRefCount() {
		return 1;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(var);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALVarSv)) {
			return false;
		}
		ASALVarSv other = (ASALVarSv) obj;
		return Objects.equals(var, other.var);
	}
	
	@Override
	public String toString() {
		return TextOptions.current().id(var.getName());
	}
}

