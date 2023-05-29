package jlx.asal;

import java.util.*;

import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALVariable;

public abstract class ASALVisitor<T> extends ASALHandler<T> {
	public abstract T visitFctDef(ASALOp fct);
	
	protected final T visitVar(ASALVariable node) {
		return handle(node);
	}
	
	protected final T visit(ASALAssignStatement node) {
		return handle(node, handle(node.getResolvedVar()), visitExpr(node.getExpression()));
	}
	
	protected final T visit(ASALBinaryExpr node) {
		return handle(node, visitExpr(node.getLhs()), visitExpr(node.getRhs()));
	}
	
	protected final T visit(ASALEmptyStatement node) {
		return handle(node);
	}
	
	private List<T> visitParams(List<ASALExpr> params) {
		List<T> result = new ArrayList<T>();
		
		for (ASALExpr param : params) {
			result.add(visitExpr(param));
		}
		
		return result;
	}
	
	protected final T visit(ASALFunctionCall node) {
		T visitedFct = visitFctDef(node.getResolvedOperation());
		List<T> visitedParams = visitParams(node.getParams());
		return handle(node, visitedFct, visitedParams);
	}
	
	protected final T visit(ASALFunctionCallStatement node) {
		T visitedFct = visitFctDef(node.getResolvedOperation());
		List<T> visitedParams = visitParams(node.getParams());
		return handle(node, visitedFct, visitedParams);
	}
	
	protected final T visit(ASALIfStatement node) {
		T condition = visitExpr(node.getCondition());
		T thenBranch = visitStat(node.getThenBranch());
		T elseBranch = null;
		
		if (node.getElseBranch() != null) {
			elseBranch = visitStat(node.getElseBranch());
		}
		
		return handle(node, condition, thenBranch, elseBranch);
	}
	
	protected final T visit(ASALWhileStatement node) {
		T condition = visitExpr(node.getCondition());
		T body = visitStat(node.getBody());
		return handle(node, condition, body);
	}
	
	protected final T visit(ASALLiteral node) {
		return handle(node);
	}
	
	protected final T visit(ASALReturnStatement node) {
		return handle(node, visitExpr(node.getExpression()));
	}
	
	protected final T visit(ASALSeqStatement node) {
		return handle(node, visitStat(node.getStatement()), visitStat(node.getSuccessor()));
	}
	
	protected final T visit(ASALUnaryExpr node) {
		return handle(node, visitExpr(node.getExpr()));
	}
	
	protected final T visit(ASALTernaryExpr node) {
		return handle(node, visitExpr(node.getCondition()), visitExpr(node.getLhs()), visitExpr(node.getRhs()));
	}
	
	protected final T visit(ASALVarRef node) {
		return handle(node, visitVar(node.getResolvedVar()));
	}
	
	protected final T visit(ASALTrigger trigger) {
		return handle(trigger, visitExpr(trigger.getExpr()));
	}
	
	protected final T visit(ASALTimeout timeout) {
		return handle(timeout, timeout.getResolvedDurationPort());
	}
	
	protected final T visit(ASALCall call) {
		T visitedFct = visitFctDef(call.getResolvedOperation());
		return handle(call, visitedFct);
	}
	
	protected final T visit(ASALFinalized finalized) {
		return handle(finalized);
	}
	
	public final T visitEvent(ASALEvent evt) {
		if (evt instanceof ASALTrigger) {
			return visit((ASALTrigger)evt);
		}
		
		if (evt instanceof ASALTimeout) {
			return visit((ASALTimeout)evt);
		}
		
		if (evt instanceof ASALCall) {
			return visit((ASALCall)evt);
		}
		
		if (evt instanceof ASALFinalized) {
			return visit((ASALFinalized)evt);
		}
		
		throw new Error("Should not happen!");
	}
	
	public final T visitExpr(ASALExpr expr) {
		if (expr == null) {
			throw new Error("Should not happen!");
		}
		
		if (expr instanceof ASALBinaryExpr) {
			return visit((ASALBinaryExpr)expr);
		}
		
		if (expr instanceof ASALFunctionCall) {
			return visit((ASALFunctionCall)expr);
		}
		
		if (expr instanceof ASALLiteral) {
			return handle((ASALLiteral)expr);
		}
		
		if (expr instanceof ASALUnaryExpr) {
			return visit((ASALUnaryExpr)expr);
		}
		
		if (expr instanceof ASALTernaryExpr) {
			return visit((ASALTernaryExpr)expr);
		}
		
		if (expr instanceof ASALVarRef) {
			return visit((ASALVarRef)expr);
		}
		
		throw new Error("Could not visit " + expr.getClass().getCanonicalName() + "!");
	}
	
	public final T visitStat(ASALStatement stat) {
		if (stat == null) {
			throw new Error("Should not happen!");
		}
		
		if (stat instanceof ASALAssignStatement) {
			return visit((ASALAssignStatement)stat);
		}
		
		if (stat instanceof ASALEmptyStatement) {
			return visit((ASALEmptyStatement)stat);
		}
		
		if (stat instanceof ASALFunctionCallStatement) {
			return visit((ASALFunctionCallStatement)stat);
		}
		
		if (stat instanceof ASALIfStatement) {
			return visit((ASALIfStatement)stat);
		}
		
		if (stat instanceof ASALWhileStatement) {
			return visit((ASALWhileStatement)stat);
		}
		
		if (stat instanceof ASALReturnStatement) {
			return visit((ASALReturnStatement)stat);
		}
		
		if (stat instanceof ASALSeqStatement) {
			return visit((ASALSeqStatement)stat);
		}
		
		throw new Error("Could not visit " + stat.getClass().getCanonicalName() + "!");
	}
	
	public final T visitFct(ASALOp fct) {
		return handle(fct, visitStat(fct.getBody()));
	}
	
	public final List<T> visitExprs(List<? extends ASALExpr> exprs) {
		List<T> result = new ArrayList<T>();
		
		for (ASALExpr expr : exprs) {
			result.add(visitExpr(expr));
		}
		
		return result;
	}
}
