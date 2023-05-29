package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;

public class ASALAndSv implements ASALSymbolicValue {
	private final static Object HASH_OBJECT = new Object();
	
	private Set<ASALSymbolicValue> exprs;
	
	public ASALAndSv(Set<ASALSymbolicValue> exprs) {
		this.exprs = exprs;
	}
	
	public Set<ASALSymbolicValue> getExprs() {
		return Collections.unmodifiableSet(exprs);
	}
	
	@Override
	public ASALSymbolicValue negate() {
		Set<ASALSymbolicValue> negExprs = new HashSet<ASALSymbolicValue>();
		
		for (ASALSymbolicValue expr : exprs) {
			negExprs.add(expr.negate());
		}
		
		return ASALSymbolicValue.or(negExprs);
	}
	
	@Override
	public boolean isBooleanType() {
		return true;
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		Set<ASALSymbolicValue> newExprs = new HashSet<ASALSymbolicValue>();
		
		for (ASALSymbolicValue expr : exprs) {
			newExprs.add(expr.substitute(subst));
		}
		
		return ASALSymbolicValue.and(newExprs);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		
		for (ASALSymbolicValue expr : exprs) {
			result.addAll(expr.getReferencedVars());
		}
		
		return result;
	}
	
	@Override
	public int getVarRefCount() {
		int result = 0;
		
		for (ASALSymbolicValue expr : exprs) {
			result += expr.getVarRefCount();
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(HASH_OBJECT, exprs);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALAndSv)) {
			return false;
		}
		ASALAndSv other = (ASALAndSv) obj;
		return Objects.equals(exprs, other.exprs);
	}
	
	@Override
	public String toString() {
		Iterator<ASALSymbolicValue> q = exprs.iterator();
		String result = q.next().toString();
		
		while (q.hasNext()) {
			result = result + " and " + q.next();
		}
		
		return "(" + result + ")";
	}
}
