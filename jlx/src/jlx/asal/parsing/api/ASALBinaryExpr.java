package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALBinaryExpr extends ASALExpr {
	private ASALExpr lhs;
	private ASALExpr rhs;
	private String op;
	
	public ASALBinaryExpr(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		lhs = createAPI("lhs", ASALExpr.class, false);
		rhs = createAPI("rhs", ASALExpr.class, false);
		op = tree.get("op").getFirstToken().text;
	}
	
	public ASALBinaryExpr(ASALExpr lhs, ASALExpr rhs, String op) {
		super(lhs.getParent(), lhs.getTree());
		
		//this.lhs = lhs.getTree().createAPI(this, ASALExpr.class);
		//this.lhs = rhs.getTree().createAPI(this, ASALExpr.class); //Old code; I suspect this is a bug!
		//this.rhs = rhs.getTree().createAPI(this, ASALExpr.class);
		this.lhs = lhs;
		this.rhs = rhs;
		this.op = op;
	}
	
	public ASALExpr getLhs() {
		return lhs;
	}
	
	public ASALExpr getRhs() {
		return rhs;
	}
	
	public String getOp() {
		return op;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		switch (op) {
			case "+":
			case "-":
			case "*":
			case "/":
			case "%":
				lhs.validateAndCrossRef(scope, JInt.class);
				rhs.validateAndCrossRef(scope, JInt.class);
				lhs.confirmInterpretable(JInt.class);
				rhs.confirmInterpretable(JInt.class);
				setResolvedType(JInt.class);
				return;
			case "and":
			case "or":
				lhs.validateAndCrossRef(scope, JBool.class);
				rhs.validateAndCrossRef(scope, JBool.class);
				lhs.confirmInterpretable(JBool.class);
				rhs.confirmInterpretable(JBool.class);
				setResolvedType(JBool.class);
				return;
			case "=":
			case "<>":
				lhs.validateAndCrossRef(scope, JType.class);
				rhs.validateAndCrossRef(scope, JType.class);
				
				if (!JType.isEquatableTo(lhs.getResolvedType(), rhs.getResolvedType())) {
					throw new ASALException(rhs, "Cannot equate " + lhs.getResolvedType().getSimpleName() + " and " + rhs.getResolvedType().getSimpleName() + "!");
				}
				
				setResolvedType(JBool.class);
				return;
			case ">=":
			case "<=":
			case ">":
			case "<":
				lhs.validateAndCrossRef(scope, JInt.class);
				rhs.validateAndCrossRef(scope, JInt.class);
				lhs.confirmInterpretable(JInt.class);
				rhs.confirmInterpretable(JInt.class);
				setResolvedType(JBool.class);
				return;
		}
		
		throw new Error("Should not happen, no implementation for operation " + op + "!");
	}
	
	@Override
	public String toText(TextOptions options) {
		return "(" + lhs.toText(options) + " " + op + " " + rhs.toText(options) + ")";
	}
	
	public static ASALExpr fromList(List<ASALExpr> exprs, String op, ASALLiteral fallback) {
		if (exprs.size() > 0) {
			ASALExpr prevExpr = exprs.get(0);
			
			for (int index = 1; index < exprs.size(); index++) {
				prevExpr = new ASALBinaryExpr(prevExpr, exprs.get(index), op);
			}
			
			return prevExpr;
		}
		
		return fallback;
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(lhs.getReferencedVars());
		result.addAll(rhs.getReferencedVars());
		return result;
	}
}
