package jlx.asal.parsing.api;

import java.util.Set;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALUnaryExpr extends ASALExpr {
	private ASALExpr expr;
	private String op;
	
	public ASALUnaryExpr(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		expr = createAPI("expr", ASALExpr.class, false);
		op = tree.getFirstToken().text;
	}
	
	public ASALUnaryExpr(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALExpr expr, String op) {
		super(parent, tree);
		
		this.expr = expr; //TODO is old code better? ("expr.getTree().createAPI(this, ASALExpr.class);")
		this.op = op;
	}
	
	public static ASALUnaryExpr _not(ASALExpr expr) {
		return new ASALUnaryExpr(expr.getParent(), expr.getTree(), expr, "not");
	}
	
	public ASALExpr getExpr() {
		return expr;
	}
	
	public String getOp() {
		return op;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		switch (op) {
			case "+":
			case "-":
				expr.validateAndCrossRef(scope, JInt.class);
				expr.confirmInterpretable(JInt.class);
				setResolvedType(JInt.class);
				return;
			case "not":
			case "NOT":
				expr.validateAndCrossRef(scope, JBool.class);
				expr.confirmInterpretable(JBool.class);
				setResolvedType(JBool.class);
				return;
		}
		
		throw new Error("Should not happen, no implementation for operation " + op + "!");
	}
	
	@Override
	public String toText(TextOptions options) {
		return "(" + op + " " + expr.toText(options) + ")";
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return expr.getReferencedVars();
	}
}
