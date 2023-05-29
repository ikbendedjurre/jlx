package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;

public class NextStateFct {
	public static class Entry {
		private ASALVariable variable;
		private ASALSymbolicValue value;
		private String debugText;
		
		private Entry(ASALVariable variable, ASALSymbolicValue value, String debugText) {
			this.variable = variable;
			this.value = value;
			this.debugText = debugText;
		}
		
		private Entry(Entry source) {
			variable = source.variable;
			value = source.value;
			debugText = source.debugText;
		}
		
		public ASALVariable getVariable() {
			return variable;
		}
		
		public ASALSymbolicValue getValue() {
			return value;
		}
		
		public String getDebugText() {
			return debugText;
		}
		
		public boolean substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
			ASALSymbolicValue newValue = value.substitute(subst);
			
			if (!newValue.equals(value)) {
				value = newValue;
				return true;
			}
			
			return false;
		}
		
		public boolean applyRestr(Map<ASALSymbolicValue, ASALSymbolicValue> fct) {
			ASALSymbolicValue newValue = ASALSymbolicValue.litFct(value, fct);
			
			if (!newValue.equals(value)) {
				value = newValue;
				return true;
			}
			
			return false;
		}
	}
	
	private Map<ASALVariable, Entry> entryPerVar;
	
	public NextStateFct() {
		entryPerVar = new HashMap<ASALVariable, Entry>();
	}
	
	public NextStateFct(NextStateFct source) {
		entryPerVar = new HashMap<ASALVariable, Entry>();
		
		for (Map.Entry<ASALVariable, Entry> entry : source.entryPerVar.entrySet()) {
			entryPerVar.put(entry.getKey(), new Entry(entry.getValue()));
		}
	}
	
	public NextStateFct(Map<ASALVariable, ASALSymbolicValue> source, String debugText) {
		entryPerVar = new HashMap<ASALVariable, Entry>();
		putAll(source, debugText);
	}
	
	public boolean containsKey(ASALVariable variable) {
		return entryPerVar.containsKey(variable);
	}
	
	public Entry get(ASALVariable variable) {
		return entryPerVar.get(variable);
	}
	
	public void put(ASALVariable variable, ASALSymbolicValue value, String debugText) {
		entryPerVar.put(variable, new Entry(variable, value, debugText));
	}
	
	public void putAll(Map<ASALVariable, ASALSymbolicValue> addition, String debugText) {
		for (Map.Entry<ASALVariable, ASALSymbolicValue> a : addition.entrySet()) {
			Entry e = entryPerVar.get(a.getKey());
			
			if (e != null) {
				e.value = a.getValue();
				e.debugText = debugText;
			} else {
				entryPerVar.put(a.getKey(), new Entry(a.getKey(), a.getValue(), debugText));
			}
		}
	}
	
	public Set<ASALVariable> getVariables() {
		return entryPerVar.keySet();
	}
	
	public Collection<Entry> getEntries() {
		return entryPerVar.values();
	}
	
	public boolean substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		boolean result = false;
		
		for (Map.Entry<? extends ASALVariable, Entry> entry : entryPerVar.entrySet()) {
			if (entry.getValue().substitute(subst)) {
				result = true;
			}
		}
		
		return result;
	}
	
	public NextStateFct removeVariables(Collection<ASALVariable> vars) {
		NextStateFct result = new NextStateFct();
		
		for (Map.Entry<ASALVariable, Entry> entry : entryPerVar.entrySet()) {
			if (!vars.contains(entry.getKey())) {
				result.entryPerVar.put(entry.getKey(), new Entry(entry.getValue()));
			}
		}
		
		return result;
	}
	
	public boolean applyRestrPerVar(Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> restrPerVar) {
		boolean result = false;
		
		for (NextStateFct.Entry entry : entryPerVar.values()) {
			if (restrPerVar.containsKey(entry.getVariable())) {
				entry.applyRestr(restrPerVar.get(entry.getVariable()));
			}
		}
		
		return result;
	}
	
	/**
	 * Read AND written variables!
	 */
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(entryPerVar.keySet());
		result.addAll(getReadVariables());
		return result;
	}
	
	public Set<ASALVariable> getReadVariables() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		
		for (NextStateFct.Entry expr : entryPerVar.values()) {
			result.addAll(expr.getValue().getReferencedVars());
		}
		
		return result;
	}
}

