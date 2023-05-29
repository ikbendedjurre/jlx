package jlx.asal;

import java.util.List;

import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALPort;
import jlx.asal.vars.ASALVariable;

public abstract class ASALHandler<T> {
	public T handle(ASALAssignStatement node, T var, T expr) {
		return null;
	}
	
	public T handle(ASALBinaryExpr node, T lhs, T rhs) {
		return null;
	}
	
	public T handle(ASALEmptyStatement node) {
		return null;
	}
	
	public T handle(ASALFunctionCall node, T fct, List<T> params) {
		return null;
	}
	
	public T handle(ASALFunctionCallStatement node, T fct, List<T> params) {
		return null;
	}
	
	public T handle(ASALIfStatement node, T cond, T thenBranch, T elseBranch) {
		return null;
	}
	
	public T handle(ASALWhileStatement node, T cond, T body) {
		return null;
	}
	
	public T handle(ASALReturnStatement node, T expr) {
		return null;
	}
	
	public T handle(ASALSeqStatement node, T first, T second) {
		return null;
	}
	
	public T handle(ASALVarRef node, T var) {
		return null;
	}
	
	public T handle(ASALUnaryExpr node, T expr) {
		return null;
	}
	
	public T handle(ASALTernaryExpr node, T condition, T lhs, T rhs) {
		return null;
	}
	
	public T handle(ASALOp leaf, T stat) {
		return null;
	}
	
	public T handle(ASALVariable leaf) {
		return null;
	}
	
	public T handle(ASALLiteral leaf) {
		return null;
	}
	
	public T handle(ASALTrigger node, T expr) {
		return null;
	}
	
	public T handle(ASALTimeout node, ASALPort durationPort) {
		return null;
	}
	
	public T handle(ASALCall node, T fct) {
		return null;
	}
	
	public T handle(ASALFinalized node) {
		return null;
	}
}
