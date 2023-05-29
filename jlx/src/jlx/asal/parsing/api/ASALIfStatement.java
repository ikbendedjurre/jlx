package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.utils.*;

public class ASALIfStatement extends ASALStatement {
	private ASALExpr condition;
	private ASALStatement thenBranch;
	private ASALStatement elseBranch;
	
	public ASALIfStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		condition = createAPI("condition", ASALExpr.class, false);
		thenBranch = createAPI("thenBranch", ASALStatement.class, false);
		elseBranch = createAPI("elseBranch", ASALStatement.class, true);
	}
	
	public ASALIfStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALExpr condition, ASALStatement thenBranch, ASALStatement elseBranch) {
		super(parent, tree);
		
		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
	}
	
	public ASALExpr getCondition() {
		return condition;
	}
	
	public ASALStatement getThenBranch() {
		return thenBranch;
	}
	
	public ASALStatement getElseBranch() {
		return elseBranch;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		condition.validateAndCrossRef(scope, JBool.class);
		condition.confirmInterpretable(JBool.class);
		thenBranch.validateAndCrossRef(scope, JVoid.class);
		
		if (elseBranch != null) {
			elseBranch.validateAndCrossRef(scope, JVoid.class);
		}
	}
	
	@Override
	public void confirmReturnValue() throws ASALException {
		thenBranch.confirmReturnValue();
		
		if (elseBranch != null) {
			elseBranch.confirmReturnValue();
		}
	}
	
	@Override
	public boolean containsNonEmpty() {
		return true;
	}
	
	@Override
	public List<String> toText(Indentation indent, TextOptions options) {
		List<String> result = new ArrayList<String>();
		result.add(indent + "if " + condition.toText(options) + " then");
		result.addAll(thenBranch.toText(indent, options));
		
		if (elseBranch != null) {
			result.add(indent + "else");
			result.addAll(elseBranch.toText(indent, options));
		}
		
		result.add(indent + "end if");
		
		return result;
	}
}
