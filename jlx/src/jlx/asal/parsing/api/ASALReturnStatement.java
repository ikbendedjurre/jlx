package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.utils.*;

public class ASALReturnStatement extends ASALStatement {
	private ASALExpr expression;
	
	public ASALReturnStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		expression = createAPI("expr", ASALExpr.class, false);
	}
	
	public ASALExpr getExpression() {
		return expression;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		expression.validateAndCrossRef(scope, JType.class);
		
		if (scope.getReturnType() != null) {
			expression.confirmInterpretable(scope.getReturnType());
		} else {
			throw new ASALException(this, "Cannot return outside of an operation!");
		}
	}
	
	@Override
	public boolean containsNonEmpty() {
		return true;
	}
	
	@Override
	public void confirmReturnValue() {
		//Do nothing.
	}
	
	@Override
	public List<String> toText(Indentation indent, TextOptions options) {
		return Collections.singletonList(indent + "return " + expression.toText(options) + ";");
	}
}
