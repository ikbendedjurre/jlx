package jlx.asal.parsing.api;

import java.util.Set;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALTrigger extends ASALEvent {
	private ASALExpr expr;
	
	public ASALTrigger(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		expr = createAPI("expr", ASALExpr.class, false);
	}
	
	public ASALTrigger(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALExpr expr) {
		super(parent, tree);
		
		this.expr = expr;
	}
	
	public ASALExpr getExpr() {
		return expr;
	}
	
	@Override
	public String toText(TextOptions options) {
		return "when(" + expr.toText(options) + ")";
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		expr.validateAndCrossRef(scope, JPulse.class);
		expr.confirmInterpretable(JPulse.class);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return expr.getReferencedVars();
	}
}
