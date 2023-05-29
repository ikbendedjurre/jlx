package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALParam;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALFunctionCall extends ASALExpr {
	private ASALOp resolvedOperation;
	private String fctName;
	private ASALSeqExpr params;
	
	public ASALFunctionCall(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		fctName = tree.getPropery("fct");
		params = createAPI("params", ASALSeqExpr.class, true);
	}
	
	public String getFctName() {
		return fctName;
	}
	
	public List<ASALExpr> getParams() {
		if (params != null) {
			return Collections.unmodifiableList(params.getList());
		}
		
		return Collections.emptyList();
	}
	
	public ASALOp getResolvedOperation() {
		return resolvedOperation;
	}

	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		resolvedOperation = scope.getOperation(fctName);
		
		if (resolvedOperation == null) {
			throw new ASALException(this, "Unknown operation!", scope.getScopeSuggestions(false, false, true, false));
		}
		
		setResolvedType(resolvedOperation.getReturnType());
		
		List<ASALExpr> foundParams = getParams();
		List<ASALParam> expectedParams = resolvedOperation.getParams();
		
		if (foundParams.size() != expectedParams.size()) {
			throw new ASALException(this, "Expected " + expectedParams.size() + " parameters but found " + foundParams.size() + "!");
		}
		
		for (int index = 0; index < foundParams.size(); index++) {
			Class<? extends JType> expectedParamType = expectedParams.get(index).getType();
			foundParams.get(index).validateAndCrossRef(scope, expectedParamType);
			Class<? extends JType> foundParamType = foundParams.get(index).getResolvedType();
			
			if (!JType.isAssignableTo(expectedParamType, foundParamType)) {
				throw new ASALException(this, "Expected " + expectedParamType.getCanonicalName() + " as type of parameter " + (index + 1) + " (\"" + expectedParams.get(index) + "\") but found " + foundParamType.getCanonicalName() + "!");
			}
		}
	}
	
	@Override
	public String toText(TextOptions options) {
		if (params != null) {
			return fctName + "(" + params.toText(options) + ")";
		}
		
		return fctName + "()";
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		if (params != null) {
			return params.getReferencedVars();
		}
		
		return Collections.emptySet();
	}
}
