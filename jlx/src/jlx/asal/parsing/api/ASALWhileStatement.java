package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.utils.*;

public class ASALWhileStatement extends ASALStatement {
	private ASALExpr condition;
	private ASALStatement body;
	
	public ASALWhileStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		condition = createAPI("condition", ASALExpr.class, false);
		body = createAPI("body", ASALStatement.class, false);
	}
	
	public ASALExpr getCondition() {
		return condition;
	}
	
	public ASALStatement getBody() {
		return body;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		condition.validateAndCrossRef(scope, JBool.class);
		condition.confirmInterpretable(JBool.class);
		body.validateAndCrossRef(scope, JVoid.class);
	}
	
	@Override
	public boolean containsNonEmpty() {
		return false;
	}
	
	@Override
	public List<String> toText(Indentation indent, TextOptions options) {
		List<String> result = new ArrayList<String>();
		result.add(indent + "while " + condition.toText(options) + " do");
		result.addAll(body.toText(indent.indent(), options));
		result.add(indent + "end while");
		return result;
	}
}
