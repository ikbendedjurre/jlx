package jlx.asal.controlflow;

import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;

public interface ASALExecution {
	/**
	 * Creates an exact copy of this instance.
	 */
	public ASALExecution createCopy();
	
	public void applyCurrentExprAsLocationConjunct(ASALExecution elseBranch);
	public void popCurrentExpr();
	public void applyReturningConditionAsLocationConjunct(ASALExecution elseBranch);
	
	public void handle(ASALUnaryExpr node);
	public void handle(ASALBinaryExpr node);
	public void handle(ASALTernaryExpr node);
	public void handle(ASALAssignStatement node);
	public void handle(ASALReturnStatement node);
	public void handleOpBegin(ASALOp node);
	public void handleOpEnd(ASALOp node, boolean isStatement);
	public void handle(ASALLiteral node);
	public void handle(ASALVarRef node);
	public void handle(ASALIfStatement node);
	
	public void debugPrint();
}


