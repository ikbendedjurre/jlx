package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;
import jlx.utils.Texts;

public class ASALPulseSv implements ASALSymbolicValue {
	private final static Object HASH_OBJECT = new Object();
	
	private Map<? extends ASALVariable, ASALSymbolicValue> params;
	
	public ASALPulseSv(Map<? extends ASALVariable, ASALSymbolicValue> params) {
		this.params = params;
	}
	
	public Map<? extends ASALVariable, ASALSymbolicValue> getParams() {
		return params;
	}
	
	@Override
	public boolean isBooleanType() {
		return false;
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		Map<ASALVariable, ASALSymbolicValue> newParams = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (Map.Entry<? extends ASALVariable, ASALSymbolicValue> e : params.entrySet()) {
			newParams.put(e.getKey(), e.getValue().substitute(subst));
		}
		
		return new ASALPulseSv(newParams);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		
		for (ASALSymbolicValue param : params.values()) {
			result.addAll(param.getReferencedVars());
		}
		
		return result;
	}
	
	@Override
	public int getVarRefCount() {
		int result = 0;
		
		for (ASALSymbolicValue param : params.values()) {
			result += param.getVarRefCount();
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(HASH_OBJECT, params);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASALPulseSv other = (ASALPulseSv) obj;
		return Objects.equals(params, other.params);
	}
	
	@Override
	public String toString() {
		return params.size() > 0 ? "PULSE_ON [" + Texts.concat(params.values(), ", ") + "]" : "PULSE_ON";
	}
}
