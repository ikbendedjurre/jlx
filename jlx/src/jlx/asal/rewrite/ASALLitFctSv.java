package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;
import jlx.utils.*;

public class ASALLitFctSv implements ASALSymbolicValue {
	private ASALSymbolicValue expr;
	private Map<ASALSymbolicValue, ASALSymbolicValue> fct;
	
	public ASALLitFctSv(ASALSymbolicValue expr, Map<ASALSymbolicValue, ASALSymbolicValue> fct) {
		this.expr = expr;
		this.fct = fct;
	}
	
	public ASALSymbolicValue getExpr() {
		return expr;
	}
	
	public Map<ASALSymbolicValue, ASALSymbolicValue> getFct() {
		return fct;
	}
	
	public Map<ASALSymbolicValue, Set<ASALSymbolicValue>> getInverseFct() {
		Map<ASALSymbolicValue, Set<ASALSymbolicValue>> result = new HashMap<ASALSymbolicValue, Set<ASALSymbolicValue>>();
		
		for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> entry : fct.entrySet()) {
			HashMaps.inject(result, entry.getValue(), entry.getKey());
		}
		
		return result;
	}
	
	@Override
	public ASALSymbolicValue negate() {
		return ASALSymbolicValue.litFct(expr.negate(), fct);
	}
	
	@Override
	public boolean isBooleanType() {
		return expr.isBooleanType();
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		Map<ASALSymbolicValue, ASALSymbolicValue> newFct = new HashMap<ASALSymbolicValue, ASALSymbolicValue>();
		
		for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> entry : fct.entrySet()) {
			newFct.put(entry.getKey(), entry.getValue().substitute(subst));
		}
		
		return ASALSymbolicValue.litFct(expr.substitute(subst), newFct);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(expr.getReferencedVars());
		
		for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> entry : fct.entrySet()) {
			result.addAll(entry.getValue().getReferencedVars());
		}
		
		return result;
	}
	
	@Override
	public int getVarRefCount() {
		int result = expr.getVarRefCount();
		
		for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> entry : fct.entrySet()) {
			result += entry.getKey().getVarRefCount();
			result += entry.getValue().getVarRefCount();
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(expr, fct);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALLitFctSv)) {
			return false;
		}
		ASALLitFctSv other = (ASALLitFctSv) obj;
		return Objects.equals(expr, other.expr) && Objects.equals(fct, other.fct);
	}
	
	@Override
	public String toString() {
		List<String> items = new ArrayList<String>();
		
		for (Map.Entry<ASALSymbolicValue, Set<ASALSymbolicValue>> entry : getInverseFct().entrySet()) {
			if (entry.getValue().size() > 1) {
				List<String> xs = new ArrayList<String>();
				
				for (ASALSymbolicValue e : entry.getValue()) {
					xs.add(e.toString());
				}
				
				items.add("[" + Texts.concat(xs, ", ") + "] := " + entry.getKey().toString());
			} else {
				items.add(entry.getValue().iterator().next().toString() +" := " + entry.getKey().toString());
			}
		}
		
		return "(" + expr.toString() + ")[" + Texts.concat(items, ", ") + "]";
	}
}

