package jlx.asal.controlflow;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.utils.*;

public class ASALControlFlowVisitor<T extends ASALExecution> {
	private void log(T state, ASALExpr x) {
		System.out.println("visited " + x.getClass().getSimpleName() + ":   " + x.toText(TextOptions.MINIMAL));
		state.debugPrint();
	}
	
	private void log(T state, ASALStatement x) {
		System.out.println("visited " + x.getClass().getSimpleName() + ":   " + x.toText(TextOptions.MINIMAL));
		state.debugPrint();
	}
	
	private T visit(T state, ASALAssignStatement node) {
		T stateAfterExpr = visitExpr(state, node.getExpression());
		stateAfterExpr.handle(node);
		log(stateAfterExpr, node);
		return stateAfterExpr;
	}
	
	@SuppressWarnings("unchecked")
	private T visitShortCircuitAnd(T state, ASALBinaryExpr node) {
		T stateAfterLhs = visitExpr(state, node.getLhs());
		T stateBeforeNoRhs = stateAfterLhs;
		T stateBeforeRhs = (T)stateAfterLhs.createCopy();
		T stateAfterRhs = visitExpr(stateBeforeRhs, node.getRhs());
		stateAfterRhs.handle(node);
		
		stateAfterRhs.applyCurrentExprAsLocationConjunct(stateBeforeNoRhs);
		log(stateAfterRhs, node);
		return stateAfterRhs;
	}
	
	@SuppressWarnings("unchecked")
	private T visitShortCircuitOr(T state, ASALBinaryExpr node) {
		T stateAfterLhs = visitExpr(state, node.getLhs());
		T stateBeforeNoRhs = stateAfterLhs;
		T stateBeforeRhs = (T)stateAfterLhs.createCopy();
		T stateAfterRhs = visitExpr(stateBeforeRhs, node.getRhs());
		stateAfterRhs.handle(node);
		
		stateBeforeNoRhs.applyCurrentExprAsLocationConjunct(stateAfterRhs);
		log(stateBeforeNoRhs, node);
		return stateBeforeNoRhs;
	}
	
	private T visit(T state, ASALBinaryExpr node) {
		switch (node.getOp()) {
			case "and":
				return visitShortCircuitAnd(state, node);
			case "or":
				return visitShortCircuitOr(state, node);
			default:
				T stateAfterLhs = visitExpr(state, node.getLhs());
				T stateAfterRhs = visitExpr(stateAfterLhs, node.getRhs());
				stateAfterRhs.handle(node);
				log(stateAfterRhs, node);
				return stateAfterRhs;
		}
	}
	
	@SuppressWarnings("unchecked")
	private T visit(T state, ASALTernaryExpr node) {
		T stateAfterCondition = visitExpr(state, node.getCondition());
		T stateBeforeThenBranch = stateAfterCondition;
		T stateBeforeElseBranch = (T)stateAfterCondition.createCopy();
		
		T stateAfterThenBranch = visitExpr(stateBeforeThenBranch, node.getLhs());
		T stateAfterElseBranch = visitExpr(stateBeforeElseBranch, node.getRhs());
		
		stateAfterThenBranch.applyCurrentExprAsLocationConjunct(stateAfterElseBranch);
		stateAfterThenBranch.popCurrentExpr();
		log(stateAfterThenBranch, node);
		return stateAfterThenBranch;
	}
	
	private T visit(T state, ASALEmptyStatement node) {
		log(state, node);
		return state;
	}
	
	private T visitParams(T state, List<ASALExpr> params) {
		T result = state;
		
		for (ASALExpr param : params) {
			result = visitExpr(result, param);
		}
		
		return result;
	}
	
	private T visit(T state, ASALFunctionCall node) {
		T stateAfterPrep = visitParams(state, node.getParams());
		stateAfterPrep.handleOpBegin(node.getResolvedOperation());
		
		T stateAfterFct = visitStat(stateAfterPrep, node.getResolvedOperation().getBody());
		stateAfterFct.handleOpEnd(node.getResolvedOperation(), false); //Clear parameters
		log(stateAfterFct, node);
		return stateAfterFct;
	}
	
	private T visit(T state, ASALFunctionCallStatement node) {
		T stateAfterPrep = visitParams(state, node.getParams());
		stateAfterPrep.handleOpBegin(node.getResolvedOperation());
		
		T stateAfterFct = visitStat(stateAfterPrep, node.getResolvedOperation().getBody());
		stateAfterFct.handleOpEnd(node.getResolvedOperation(), true); //Clear parameters
		log(stateAfterFct, node);
		return stateAfterFct;
	}
	
	@SuppressWarnings("unchecked")
	private T visit(T state, ASALIfStatement node) {
		T stateAfterCondition = visitExpr(state, node.getCondition());
		T stateBeforeThenBranch = stateAfterCondition;
		T stateBeforeElseBranch = (T)stateAfterCondition.createCopy();
		
		T stateAfterThenBranch = visitStat(stateBeforeThenBranch, node.getThenBranch());
		T stateAfterElseBranch;
		
		if (node.getElseBranch() != null) {
			stateAfterElseBranch = visitStat(stateBeforeElseBranch, node.getElseBranch());
		} else {
			stateAfterElseBranch = stateBeforeElseBranch;
		}
		
//		System.out.println("then-branch:");
//		stateAfterThenBranch.debugPrint();
//		System.out.println("else-branch:");
//		stateAfterElseBranch.debugPrint();
		
		stateAfterThenBranch.applyCurrentExprAsLocationConjunct(stateAfterElseBranch);
		stateAfterThenBranch.popCurrentExpr();
		log(stateAfterThenBranch, node);
		return stateAfterThenBranch;
	}
	
	private T visit(T state, ASALWhileStatement node) {
		throw new Error("Not supported!");
	}
	
	private T visit(T state, ASALLiteral node) {
		state.handle(node);
		log(state, node);
		return state;
	}
	
	private T visit(T state, ASALReturnStatement node) {
		T stateAfterExpr = visitExpr(state, node.getExpression());
		stateAfterExpr.handle(node);
		log(stateAfterExpr, node);
		return stateAfterExpr;
	}
	
	@SuppressWarnings("unchecked")
	private T visit(T state, ASALSeqStatement node) {
		T stateAfterStatement = visitStat(state, node.getStatement());
		T stateWithoutSuccessor = (T)stateAfterStatement.createCopy();
		T stateAfterSuccessor = visitStat(stateAfterStatement, node.getSuccessor());
		stateWithoutSuccessor.applyReturningConditionAsLocationConjunct(stateAfterSuccessor);
		log(stateWithoutSuccessor, node);
		return stateWithoutSuccessor;
	}
	
	private T visit(T state, ASALUnaryExpr node) {
		T stateAfterExpr = visitExpr(state, node.getExpr());
		stateAfterExpr.handle(node);
		log(stateAfterExpr, node);
		return stateAfterExpr;
	}
	
	private T visit(T state, ASALVarRef node) {
		state.handle(node);
		log(state, node);
		return state;
	}
	
	public T visitExpr(T state, ASALExpr expr) {
		if (expr == null) {
			throw new Error("Should not happen!");
		}
		
		if (expr instanceof ASALBinaryExpr) {
			return visit(state, (ASALBinaryExpr)expr);
		}
		
		if (expr instanceof ASALFunctionCall) {
			return visit(state, (ASALFunctionCall)expr);
		}
		
		if (expr instanceof ASALLiteral) {
			return visit(state, (ASALLiteral)expr);
		}
		
		if (expr instanceof ASALUnaryExpr) {
			return visit(state, (ASALUnaryExpr)expr);
		}
		
		if (expr instanceof ASALTernaryExpr) {
			return visit(state, (ASALTernaryExpr)expr);
		}
		
		if (expr instanceof ASALVarRef) {
			return visit(state, (ASALVarRef)expr);
		}
		
		throw new Error("Could not visit " + expr.getClass().getCanonicalName() + "!");
	}
	
	public T visitStat(T state, ASALStatement stat) {
		if (stat == null) {
			throw new Error("Should not happen!");
		}
		
		if (stat instanceof ASALAssignStatement) {
			return visit(state, (ASALAssignStatement)stat);
		}
		
		if (stat instanceof ASALEmptyStatement) {
			return visit(state, (ASALEmptyStatement)stat);
		}
		
		if (stat instanceof ASALFunctionCallStatement) {
			return visit(state, (ASALFunctionCallStatement)stat);
		}
		
		if (stat instanceof ASALIfStatement) {
			return visit(state, (ASALIfStatement)stat);
		}
		
		if (stat instanceof ASALWhileStatement) {
			return visit(state, (ASALWhileStatement)stat);
		}
		
		if (stat instanceof ASALReturnStatement) {
			return visit(state, (ASALReturnStatement)stat);
		}
		
		if (stat instanceof ASALSeqStatement) {
			return visit(state, (ASALSeqStatement)stat);
		}
		
		throw new Error("Could not visit " + stat.getClass().getCanonicalName() + "!");
	}
}


