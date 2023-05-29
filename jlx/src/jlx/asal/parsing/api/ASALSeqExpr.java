package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public class ASALSeqExpr extends ASALExpr {
	private ASALExpr expr;
	private ASALSeqExpr tail;
	
	public ASALSeqExpr(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		expr = createAPI("expr", ASALExpr.class, false);
		tail = createAPI("tail", ASALSeqExpr.class, true);
	}
	
	public ASALExpr getExpr() {
		return expr;
	}
	
	public ASALSeqExpr getTail() {
		return tail;
	}
	
	public List<ASALExpr> getList() {
		List<ASALExpr> result = new ArrayList<ASALExpr>();
		result.add(expr);
		ASALSeqExpr current = tail;
		
		while (current != null) {
			result.add(current.expr);
			current = current.tail;
		}
		
		return Collections.unmodifiableList(result);
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		expr.validateAndCrossRef(scope, JVoid.class);
		
		if (tail != null) {
			tail.validateAndCrossRef(scope, JVoid.class);
		}
	}
	
	@Override
	public String toText(TextOptions options) {
		if (tail != null) {
			return expr.toText(options) + ", " + tail.toText(options);
		}
		
		return expr.toText(options);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(expr.getReferencedVars());
		
		if (tail != null) {
			result.addAll(tail.getReferencedVars());
		}
		
		return result;
	}
}
