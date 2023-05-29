package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.*;

public class ASALAssignStatement extends ASALStatement {
	private ASALVariable resolvedVar;
	private String varName;
	private ASALExpr expression;
	
	public ASALAssignStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		varName = tree.getPropery("ref");
		expression = createAPI("expr", ASALExpr.class, false);
	}
	
	public ASALAssignStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALVariable resolvedVar, ASALExpr expr) {
		super(parent, tree);
		
		this.varName = resolvedVar.getName();
		this.resolvedVar = resolvedVar;
		this.expression = expr;
	}
	
	public String getVarName() {
		return varName;
	}
	
	public ASALExpr getExpression() {
		return expression;
	}
	
	public ASALVariable getResolvedVar() {
		return resolvedVar;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		if (scope.getType() == JScopeType.INIT) {
			resolvedVar = scope.getVariable(varName);
		} else {
			resolvedVar = scope.getWritableVariable(varName);
		}
		
		if (resolvedVar == null) {
			resolvedVar = scope.getVariable(varName);
			
			if (resolvedVar != null) {
				throw new ASALException(this, "Cannot assign to variable!");
			} else {
				throw new ASALException(this, "Unknown variable!", scope.getScopeSuggestions(true, false, false, false));
			}
		}
		
		expression.validateAndCrossRef(scope, resolvedVar.getType());
		expression.confirmInterpretable(resolvedVar.getType());
	}
	
	@Override
	public boolean containsNonEmpty() {
		return true;
	}
	
	@Override
	public List<String> toText(Indentation indent, TextOptions options) {
		return Collections.singletonList(indent + options.id(varName) + " := " + expression.toText(options) + ";");
	}
}
