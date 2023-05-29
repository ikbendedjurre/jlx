package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALLitSv implements ASALSymbolicValue {
	private String text;
	
	public ASALLitSv(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public ASALSymbolicValue negate() {
		throw new Error("Should not negate \"" + text + "\"!");
	}
	
	@Override
	public boolean isBooleanType() {
		return false;
	}
	
	@Override
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst) {
		return this;
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.emptySet();
	}
	
	@Override
	public int getVarRefCount() {
		return 0;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(text);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ASALLitSv)) {
			return false;
		}
		ASALLitSv other = (ASALLitSv) obj;
		return Objects.equals(text, other.text);
	}
	
	@Override
	public String toString() {
		return TextOptions.current().escapeChars(text);
	}
}

