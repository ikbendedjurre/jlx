package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALTernaryExpr extends ASALExpr {
	private ASALExpr condition;
	private ASALExpr lhs;
	private ASALExpr rhs;
	
	public ASALTernaryExpr(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		condition = createAPI("condition", ASALExpr.class, false);
		lhs = createAPI("lhs", ASALExpr.class, false);
		rhs = createAPI("rhs", ASALExpr.class, false);
	}
	
	public ASALExpr getCondition() {
		return condition;
	}
	
	public ASALExpr getLhs() {
		return lhs;
	}
	
	public ASALExpr getRhs() {
		return rhs;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		condition.validateAndCrossRef(scope, JBool.class);
		lhs.validateAndCrossRef(scope, expectedType);
		rhs.validateAndCrossRef(scope, expectedType);
		
		condition.confirmInterpretable(JBool.class);
		
		if (!JType.isEquatableTo(lhs.getResolvedType(), rhs.getResolvedType())) {
			throw new ASALException(rhs, "Cannot interchange " + lhs.getResolvedType().getSimpleName() + " and " + rhs.getResolvedType().getSimpleName() + "!");
		}
		
		setResolvedType(lhs.getResolvedType());
	}
	
	@Override
	public String toText(TextOptions options) {
		return "(if " + condition.toText(options) + " then " + lhs.toText(options) + " else " + rhs.toText(options) + " end if)";
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(condition.getReferencedVars());
		result.addAll(lhs.getReferencedVars());
		result.addAll(rhs.getReferencedVars());
		return result;
	}
}
