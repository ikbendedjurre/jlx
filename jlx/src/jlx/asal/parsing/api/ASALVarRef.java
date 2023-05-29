package jlx.asal.parsing.api;

import java.util.Collections;
import java.util.Set;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALVarRef extends ASALExpr {
	private ASALVariable resolvedVar;
	private String varName;
	
	public ASALVarRef(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		varName = tree.getPropery("ref");
	}
	
	public ASALVarRef(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALVariable resolvedVar) {
		super(parent, tree);
		
		this.varName = resolvedVar.getName();
		this.resolvedVar = resolvedVar;
		
		setResolvedType(resolvedVar.getType());
	}
	
	public static ASALVarRef create(ASALVariable resolvedVar) {
		return new ASALVarRef(null, null, resolvedVar);
	}
	
	public String getVarName() {
		return varName;
	}
	
	public ASALVariable getResolvedVar() {
		return resolvedVar;
	}

	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		resolvedVar = scope.getVariable(varName);
		
		if (resolvedVar == null) {
			throw new ASALException(this, "Unknown variable", scope.getScopeSuggestions(true, true, false, true));
		}
		
		setResolvedType(resolvedVar.getType());
	}
	
	@Override
	public String toText(TextOptions options) {
		return options.id(varName);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.singleton(getResolvedVar());
	}
}
