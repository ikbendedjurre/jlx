package jlx.asal;

import java.util.*;

import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALPort;
import jlx.asal.vars.ASALVariable;

public abstract class ASALBuildSetVisitor<T> extends ASALVisitor<Set<T>> {
	protected Set<T> combine(Set<T> elems1, Set<T> elems2) {
		Set<T> result = new HashSet<T>();
		result.addAll(elems1);
		result.addAll(elems2);
		return result;
	}
	
	protected Set<T> combine(List<Set<T>> elems) {
		Set<T> result = new HashSet<T>();
		
		for (Set<T> elem : elems) {
			result.addAll(elem);
		}
		
		return result;
	}
	
	protected Set<T> combine(Set<T> elems, T elem) {
		Set<T> result = new HashSet<T>();
		result.addAll(elems);
		result.add(elem);
		return result;
	}
	
	@Override
	public Set<T> handle(ASALAssignStatement node, Set<T> var, Set<T> expr) {
		return combine(var, expr);
	}
	
	@Override
	public Set<T> handle(ASALBinaryExpr node, Set<T> lhs, Set<T> rhs) {
		return combine(lhs, rhs);
	}
	
	@Override
	public Set<T> handle(ASALCall node, Set<T> fct) {
		return fct;
	}
	
	@Override
	public Set<T> handle(ASALEmptyStatement node) {
		return Collections.emptySet();
	}
	
	@Override
	public Set<T> handle(ASALFunctionCall node, Set<T> fct, List<Set<T>> params) {
		return combine(fct, combine(params));
	}
	
	@Override
	public Set<T> handle(ASALFunctionCallStatement node, Set<T> fct, List<Set<T>> params) {
		return combine(fct, combine(params));
	}
	
	@Override
	public Set<T> handle(ASALIfStatement node, Set<T> cond, Set<T> thenBranch, Set<T> elseBranch) {
		if (elseBranch != null) {
			return combine(cond, combine(thenBranch, elseBranch));
		}
		
		return combine(cond, thenBranch);
	}
	
	@Override
	public Set<T> handle(ASALLiteral leaf) {
		return Collections.emptySet();
	}
	
	@Override
	public Set<T> handle(ASALOp leaf, Set<T> stat) {
		return stat;
	}
	
	@Override
	public Set<T> handle(ASALReturnStatement node, Set<T> expr) {
		return expr;
	}
	
	@Override
	public Set<T> handle(ASALSeqStatement node, Set<T> first, Set<T> second) {
		return combine(first, second);
	}
	
	@Override
	public Set<T> handle(ASALTimeout node, ASALPort durationPort) {
		return Collections.emptySet();
	}
	
	@Override
	public Set<T> handle(ASALTrigger node, Set<T> expr) {
		return expr;
	}
	
	@Override
	public Set<T> handle(ASALUnaryExpr node, Set<T> expr) {
		return expr;
	}
	
	@Override
	public Set<T> handle(ASALVariable leaf) {
		return Collections.emptySet();
	}
	
	@Override
	public Set<T> handle(ASALVarRef node, Set<T> var) {
		return var;
	}
	
	@Override
	public Set<T> handle(ASALWhileStatement node, Set<T> cond, Set<T> body) {
		return combine(cond, body);
	}
}

