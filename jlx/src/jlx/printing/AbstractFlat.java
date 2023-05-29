package jlx.printing;

import java.util.*;

import jlx.asal.j.JType;
import jlx.asal.parsing.api.ASALExpr;

public abstract class AbstractFlat {
	private Map<JType, String> exprPerValue;
	private Map<ASALExpr, String> macroPerExpr;
	
	public AbstractFlat(AbstractFlat source) {
		exprPerValue = new HashMap<JType, String>();
		macroPerExpr = new HashMap<ASALExpr, String>();
		
		if (source != null) {
			exprPerValue.putAll(source.exprPerValue);
			macroPerExpr.putAll(source.macroPerExpr);
		}
	}
	
	public Set<JType> getVars() {
		return Collections.unmodifiableSet(exprPerValue.keySet());
	}
	
	public Map<JType, String> getMap() {
		return Collections.unmodifiableMap(exprPerValue);
	}
	
	public String get(JType var) {
		return exprPerValue.get(var);
	}
	
	public String get(ASALExpr expr) {
		return macroPerExpr.get(expr);
	}
	
	public void put(JType var, String newValue) {
		exprPerValue.put(var, newValue);
	}
	
	public void putAll(Map<JType, String> newValues) {
		exprPerValue.putAll(newValues);
	}
	
	public void put(ASALExpr expr, String newValue) {
		macroPerExpr.put(expr, newValue);
	}
}
