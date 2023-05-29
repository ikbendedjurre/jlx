package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALCall extends ASALEvent {
	private ASALOp resolvedOperation;
	private String methodName;
	
	public ASALCall(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		methodName = tree.get("methodName").getFirstToken().text;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public ASALOp getResolvedOperation() {
		return resolvedOperation;
	}
	
	@Override
	public String toText(TextOptions options) {
		return methodName;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		resolvedOperation = scope.getOperation(methodName);
		
		if (resolvedOperation == null) {
			throw new ASALException(this, "Unknown operation!", scope.getScopeSuggestions(false, false, true, false));
		}
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.emptySet();
	}
}
