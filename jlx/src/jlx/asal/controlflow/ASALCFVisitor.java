package jlx.asal.controlflow;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALParam;

public class ASALCFVisitor {
	private final JScope scope;
	
	/**
	 * Slow for some reason, and gives longer expressions . . .
	 */
	public final static boolean SHORT_CIRCUIT = false;
	
	public ASALCFVisitor(JScope scope) {
		this.scope = scope;
	}
	
	private ASALCFState visit(ASALCFState state, ASALWhileStatement node) {
		throw new Error("Not supported!");
	}
	
	private ASALCFState visit(ASALCFState state, ASALIfStatement node) {
		ASALCFState stateAfterCondition = visitExpr(state, node.getCondition());
		ASALCFState stateAfterThenBranch = visitStat(stateAfterCondition, node.getThenBranch());
		ASALCFState stateAfterElseBranch;
		
		if (node.getElseBranch() != null) {
			stateAfterElseBranch = visitStat(stateAfterCondition, node.getElseBranch());
		} else {
			stateAfterElseBranch = new ASALCFState(stateAfterCondition);
		}
		
		return ASALCFState.ite(stateAfterCondition.valueSoFar, stateAfterThenBranch, stateAfterElseBranch);
	}
	
	private ASALCFState visit(ASALCFState state, ASALAssignStatement node) {
		ASALCFState stateAfterExpr = visitExpr(state, node.getExpression());
		
		if (state.valuePerParam.containsKey(node.getResolvedVar())) {
			stateAfterExpr.valuePerParam.put(node.getResolvedVar(), stateAfterExpr.valueSoFar);
		} else {
			stateAfterExpr.valuePerVar.put(node.getResolvedVar(), stateAfterExpr.valueSoFar);
			stateAfterExpr.assignedVars.add(node.getResolvedVar());
		}
		
		return stateAfterExpr;
	}
	
	private ASALCFState visit(ASALCFState state, ASALSeqStatement node) {
		ASALCFState stateAfterStatement = visitStat(state, node.getStatement());
		ASALCFState stateAfterSuccessor = visitStat(stateAfterStatement, node.getSuccessor());
		return ASALCFState.ite(stateAfterStatement.hasEncounteredReturn, stateAfterStatement, stateAfterSuccessor);
	}
	
	private ASALCFState visit(ASALCFState state, ASALEmptyStatement node) {
		return state;
	}
	
	private ASALCFState visitParams(ASALCFState state, List<ASALParam> params, List<ASALExpr> paramExprs) {
		List<ASALCFState> afterParamExprStates = new ArrayList<ASALCFState>();
		afterParamExprStates.add(state);
		
		for (ASALExpr param : paramExprs) {
			afterParamExprStates.add(visitExpr(afterParamExprStates.get(afterParamExprStates.size() - 1), param));
		}
		
		ASALCFState result = new ASALCFState(afterParamExprStates.get(afterParamExprStates.size() - 1));
		result.valuePerParam.clear();
		
		for (int index = 0; index < params.size(); index++) {
			result.valuePerParam.put(params.get(index), afterParamExprStates.get(index + 1).valueSoFar);
		}
		
		return result;
	}
	
	private ASALCFState visit(ASALCFState state, ASALFunctionCall node) {
		ASALCFState stateAfterParams = visitParams(state, node.getResolvedOperation().getParams(), node.getParams());
		ASALCFState stateAfterBody = visitStat(stateAfterParams, node.getResolvedOperation().getBody());
		ASALCFState result = new ASALCFState(state);
		result.assignedVars.addAll(stateAfterBody.assignedVars);
		result.valuePerVar.putAll(stateAfterBody.valuePerVar); //Copy variable values only (b/c they are "global").
		result.valueSoFar = stateAfterBody.returnExpr;
		return result;
	}
	
	private ASALCFState visit(ASALCFState state, ASALFunctionCallStatement node) {
		ASALCFState stateAfterParams = visitParams(state, node.getResolvedOperation().getParams(), node.getParams());
		ASALCFState stateAfterBody = visitStat(stateAfterParams, node.getResolvedOperation().getBody());
		ASALCFState result = new ASALCFState(state);
		result.assignedVars.addAll(stateAfterBody.assignedVars);
		result.valuePerVar.putAll(stateAfterBody.valuePerVar); //Copy variable values only (b/c they are "global").
		result.valueSoFar = stateAfterBody.returnExpr; //(Will not be used.)
		return result;
	}
	
	private ASALCFState visit(ASALCFState state, ASALReturnStatement node) {
		ASALCFState stateAfterExpr = visitExpr(state, node.getExpression());
		ASALCFState result = new ASALCFState(stateAfterExpr);
		result.returnExpr = stateAfterExpr.valueSoFar;
		result.hasEncounteredReturn = ASALSymbolicValue.TRUE;
		return result;
	}
	
	private ASALCFState visit(ASALCFState state, ASALTernaryExpr node) {
		ASALCFState stateAfterCondition = visitExpr(state, node.getCondition());
		ASALCFState stateAfterThenBranch = visitExpr(stateAfterCondition, node.getLhs());
		ASALCFState stateAfterElseBranch = visitExpr(stateAfterCondition, node.getRhs());
		return ASALCFState.ite(stateAfterCondition.valueSoFar, stateAfterThenBranch, stateAfterElseBranch);
	}
	
	private ASALCFState visit(ASALCFState state, ASALBinaryExpr node) {
		ASALCFState stateAfterLhs = visitExpr(state, node.getLhs());
		ASALCFState stateAfterRhs = visitExpr(stateAfterLhs, node.getRhs());
		
		if (SHORT_CIRCUIT) {
			switch (node.getOp()) {
				case "and":
					return ASALCFState.ite(stateAfterLhs.valueSoFar, stateAfterRhs, stateAfterLhs);
				case "or":
					return ASALCFState.ite(stateAfterLhs.valueSoFar, stateAfterLhs, stateAfterRhs);
			}
		}
		
		stateAfterRhs.valueSoFar = ASALSymbolicValue.from(node.getOp(), stateAfterLhs.valueSoFar, stateAfterRhs.valueSoFar);
		return stateAfterRhs;
	}
	
	private ASALCFState visit(ASALCFState state, ASALUnaryExpr node) {
		ASALCFState stateAfterExpr = visitExpr(state, node.getExpr());
		stateAfterExpr.valueSoFar = ASALSymbolicValue.from(node.getOp(), stateAfterExpr.valueSoFar);
		return stateAfterExpr;
	}
	
	private ASALCFState visit(ASALCFState state, ASALVarRef node) {
		ASALCFState result = new ASALCFState(state);
		result.valueSoFar = state.valuePerParam.get(node.getResolvedVar());
		
		if (result.valueSoFar == null) {
			result.valueSoFar = state.valuePerVar.get(node.getResolvedVar());
		}
		
		return result;
	}
	
	private ASALCFState visit(ASALCFState state, ASALLiteral node) {
		ASALCFState result = new ASALCFState(state);
		result.valueSoFar = ASALSymbolicValue.from(scope, node);
		return result;
	}
	
	/**
	 * Computes the control flow state that exists after the application of an expression in the context of the given control flow state. 
	 */
	public ASALCFState visitExpr(ASALCFState state, ASALExpr expr) {
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
	
	/**
	 * Computes the control flow state that exists after the application of a statement in the context of the given control flow state. 
	 */
	public ASALCFState visitStat(ASALCFState state, ASALStatement stat) {
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


