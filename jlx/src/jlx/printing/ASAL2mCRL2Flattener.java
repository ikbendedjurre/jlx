package jlx.printing;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALParam;
import jlx.asal.vars.ASALVariable;

public class ASAL2mCRL2Flattener {
	public final IMCRL2Printer printer;
	
	public ASAL2mCRL2Flattener(IMCRL2Printer printer) {
		this.printer = printer;
	}
	
	private StatFlat visit(AbstractFlat prev, ASALAssignStatement node) {
		ExprFlat expr = applyExpr(prev, node.getExpression(), FlatTarget.fromType(node.getResolvedVar().getType()));
		
		StatFlat result = new StatFlat(expr, "false", null);
		result.put(node.getResolvedVar().getLegacy(), expr.constructedJExpr);
		return result;
	}
	
	private String rawToBoolTarget(String s, FlatTarget target) {
		switch (target) {
			case JBOOL:
				return printer.boolToJBool(s);
			case JPULSE:
				return printer.boolToJPulse(s);
			case JTYPE:
				return printer.boolToJBool(s);
			case RAW_MCRL2:
				return "(" + s + ")";
		}
		
		throw new Error("Should not happen!");
	}
	
	private String rawToIntTarget(String s, FlatTarget target) {
		switch (target) {
			case JBOOL:
				throw new Error("Should not happen!");
			case JPULSE:
				throw new Error("Should not happen!");
			case JTYPE:
				return printer.intToJInt(s);
			case RAW_MCRL2:
				return "(" + s + ")";
		}
		
		throw new Error("Should not happen!");
	}
	
	private ExprFlat twoRawToIntTarget(ExprFlat lhs, ExprFlat rhs, String newOp, FlatTarget target) {
		String v = lhs.constructedJExpr + " " + newOp + " " + rhs.constructedJExpr;
		return new ExprFlat(rhs, rawToIntTarget(v, target));
	}
	
	private ExprFlat twoRawToBoolTarget(ExprFlat lhs, ExprFlat rhs, String newOp, FlatTarget target) {
		String v = lhs.constructedJExpr + " " + newOp + " " + rhs.constructedJExpr;
		return new ExprFlat(rhs, rawToBoolTarget(v, target));
	}
	
	private ExprFlat visitEqn(AbstractFlat prev, ASALBinaryExpr node, FlatTarget target) {
		ExprFlat lhs = applyExpr(prev, node.getLhs(), FlatTarget.JTYPE);
		ExprFlat rhs = applyExpr(lhs, node.getRhs(), FlatTarget.JTYPE);
		
		switch (node.getOp()) {
			case "=":
				return twoRawToBoolTarget(lhs, rhs, "==", target);
			case "<>":
				return twoRawToBoolTarget(lhs, rhs, "!=", target);
		}
		
		throw new Error("Should not happen!");
	}
	
	private ExprFlat visitRawOp(AbstractFlat prev, ASALBinaryExpr node, FlatTarget target) {
		ExprFlat lhs = applyExpr(prev, node.getLhs(), FlatTarget.RAW_MCRL2);
		ExprFlat rhs = applyExpr(lhs, node.getRhs(), FlatTarget.RAW_MCRL2);
		
		switch (node.getOp()) {
			case "+":
				return twoRawToIntTarget(lhs, rhs, "+", target);
			case "-":
				return twoRawToIntTarget(lhs, rhs, "-", target);
			case "*":
				return twoRawToIntTarget(lhs, rhs, "*", target);
			case "/":
				return twoRawToIntTarget(lhs, rhs, "div", target);
			case "%":
				return twoRawToIntTarget(lhs, rhs, "mod", target);
			case "and":
				return twoRawToBoolTarget(lhs, rhs, "&&", target);
			case "or":
				return twoRawToBoolTarget(lhs, rhs, "||", target);
			case ">=":
				return twoRawToBoolTarget(lhs, rhs, ">=", target);
			case "<=":
				return twoRawToBoolTarget(lhs, rhs, "<=", target);
			case ">":
				return twoRawToBoolTarget(lhs, rhs, ">", target);
			case "<":
				return twoRawToBoolTarget(lhs, rhs, "<", target);
		}
		
		throw new Error("Should not happen!");
	}
	
	private ExprFlat visit(AbstractFlat prev, ASALBinaryExpr node, FlatTarget target) {
		switch (node.getOp()) {
			case "=":
			case "<>":
				return visitEqn(prev, node, target);
			default:
				return visitRawOp(prev, node, target);
		}
	}
	
	private ExprFlat visit(AbstractFlat prev, ASALUnaryExpr node, FlatTarget target) {
		ExprFlat expr;
		
		switch (node.getOp()) {
			case "+":
				return applyExpr(prev, node.getExpr(), target);
			case "-":
				expr = applyExpr(prev, node.getExpr(), FlatTarget.RAW_MCRL2);
				return new ExprFlat(expr, rawToIntTarget("-" + expr.constructedJExpr, target));
			case "not":
			case "NOT":
				expr = applyExpr(prev, node.getExpr(), FlatTarget.RAW_MCRL2);
				return new ExprFlat(expr, rawToBoolTarget("!" + expr.constructedJExpr, target));
		}
		
		throw new Error("Could not handle " + node.getOp() + "!");
	}
	
	private String cast(String v, Class<? extends JType> source, FlatTarget target) {
		switch (target) {
			case JBOOL:
				if (source.equals(JBool.class)) {
					return v;
				}
				
				if (source.equals(JPulse.class)) {
					return printer.jpulseToJBool(v);
				}
				
				throw new Error("Should not happen!");
			case JPULSE:
				if (source.equals(JBool.class)) {
					return printer.jboolToPulse(v);
				}
				
				throw new Error("Should not happen!");
			case JTYPE:
				return v;
			case RAW_MCRL2:
				if (source.equals(JBool.class)) {
					return printer.jboolToBool(v);
				}
				
				if (source.equals(JPulse.class)) {
					return printer.jpulseToBool(v);
				}
				
				if (source.equals(JInt.class)) {
					return printer.jintToInt(v);
				}
				
				throw new Error("Should not happen!");
		}
		
		throw new Error("Should not happen!");
	}
	
	private ExprFlat visit(AbstractFlat prev, ASALVarRef node, FlatTarget target) {
		ASALVariable v = node.getResolvedVar();
		return new ExprFlat(prev, cast(prev.get(v.getLegacy()), v.getType(), target));
	}
	
	private StatFlat visit(AbstractFlat prev, ASALEmptyStatement node) {
		return new StatFlat(prev, "false", null);
	}
	
	private ExprFlat visitParams(AbstractFlat prev, List<ASALParam> params, List<ASALExpr> exprs) {
		ExprFlat result = new ExprFlat(prev, null);
		Map<JType, String> exprPerParam = new HashMap<JType, String>();
		
		for (int index = 0; index < params.size(); index++) {
			ExprFlat temp = applyExpr(result, exprs.get(index), FlatTarget.JTYPE); //Visit the parameter expression.
			exprPerParam.put(params.get(index).getLegacy(), temp.constructedJExpr); //Store the result.
			result = new ExprFlat(temp, null); //Clear the constructed expression.
		}
		
		result.putAll(exprPerParam);
		return result;
	}
	
	private ExprFlat visit(AbstractFlat prev, ASALFunctionCall node, FlatTarget target) {
		ExprFlat v = visitParams(prev, node.getResolvedOperation().getParams(), node.getParams());
		StatFlat w = applyStat(v, node.getResolvedOperation().getBody());
		String returnedExpr = cast(w.returnedJExpr, node.getResolvedOperation().getReturnType(), target);
		ExprFlat result = new ExprFlat(prev, returnedExpr);
		
		//Forget the variables:
		for (JType var : prev.getMap().keySet()) {
			result.put(var, w.get(var));
		}
		
		return result;
	}
	
	private StatFlat visit(AbstractFlat prev, ASALFunctionCallStatement node) {
		ExprFlat v = visitParams(prev, node.getResolvedOperation().getParams(), node.getParams());
		StatFlat w = applyStat(v, node.getResolvedOperation().getBody());
		StatFlat result = new StatFlat(prev, "false", null);
		
		//Forget the variables:
		for (JType var : prev.getMap().keySet()) {
			result.put(var, w.get(var));
		}
		
		return result;
	}
	
	private StatFlat visit(AbstractFlat prev, ASALIfStatement node) {
		if (node.getElseBranch() != null) {
			ExprFlat condition = applyExpr(prev, node.getCondition(), FlatTarget.RAW_MCRL2);
			StatFlat thenBranch = applyStat(condition, node.getThenBranch());
			StatFlat elseBranch = applyStat(condition, node.getElseBranch());
			
			String s1 = _if(condition.constructedJExpr, thenBranch.hasReturnedCondition, elseBranch.hasReturnedCondition);
			String s2 = _if(condition.constructedJExpr, thenBranch.returnedJExpr, elseBranch.returnedJExpr);
			StatFlat result = new StatFlat(null, s1, s2);
			
			for (JType var : condition.getVars()) {
				String v1 = thenBranch.get(var);
				String v2 = elseBranch.get(var);
				
				if (v1.equals(v2)) {
					result.put(var, v1);
				} else {
					result.put(var, _if(condition.constructedJExpr, v1, v2));
				}
			}
			
			return result;
		} else {
			ExprFlat condition = applyExpr(prev, node.getCondition(), FlatTarget.RAW_MCRL2);
			StatFlat thenBranch = applyStat(condition, node.getThenBranch());
			
			String s1 = condition.constructedJExpr;
			String s2 = thenBranch.returnedJExpr;
			StatFlat result = new StatFlat(null, s1, s2);
			
			for (JType var : condition.getVars()) {
				String v1 = thenBranch.get(var);
				String v2 = condition.get(var);
				
				if (v1.equals(v2)) {
					result.put(var, v1);
				} else {
					result.put(var, _if(condition.constructedJExpr, v1, v2));
				}
			}
			
			return result;
		}
	}
	
	private String _if(String condition, String thenBranch, String elseBranch) {
		return printer._if(condition, thenBranch, elseBranch);
	}
	
	private StatFlat _if(String condition, StatFlat thenBranch, StatFlat elseBranch) {
		StatFlat result = new StatFlat();
		
		for (JType v : thenBranch.getVars()) {
			result.put(v, _if(condition, thenBranch.get(v), elseBranch.get(v)));
		}
		
		return result;
	}
	
	private StatFlat visit(AbstractFlat prev, ASALWhileStatement node) {
		throw new Error("Not supported!");
	}
	
	private ExprFlat visit(AbstractFlat prev, ASALLiteral leaf, FlatTarget target) {
		return new ExprFlat(prev, printer.literalToStr(leaf, target));
	}
	
	private StatFlat visit(AbstractFlat prev, ASALReturnStatement node) {
		ExprFlat v = applyExpr(prev, node.getExpression(), FlatTarget.JTYPE);
		return new StatFlat(v, "true", v.constructedJExpr); 
	}
	
	private StatFlat visit(AbstractFlat prev, ASALSeqStatement node) {
		StatFlat v = applyStat(prev, node.getStatement());
		StatFlat w = applyStat(v, node.getSuccessor());
		
		if (v.hasReturnedCondition != null) {
			return new StatFlat(_if(v.hasReturnedCondition, v, w), v.hasReturnedCondition, v.returnedJExpr); 
		}
		
		return w;
	}
	
	public ExprFlat applyExpr(AbstractFlat prev, ASALExpr expr, FlatTarget target) {
		if (expr == null) {
			throw new Error("Should not happen!");
		}
		
		String macro = prev.get(expr);
		
		if (macro != null) {
			return new ExprFlat(prev, cast(macro, expr.getResolvedType(), target));
		}
		
		if (expr instanceof ASALBinaryExpr) {
			return visit(prev, (ASALBinaryExpr)expr, target);
		}
		
		if (expr instanceof ASALFunctionCall) {
			return visit(prev, (ASALFunctionCall)expr, target);
		}
		
		if (expr instanceof ASALLiteral) {
			return visit(prev, (ASALLiteral)expr, target);
		}
		
		if (expr instanceof ASALUnaryExpr) {
			return visit(prev, (ASALUnaryExpr)expr, target);
		}
		
		if (expr instanceof ASALVarRef) {
			return visit(prev, (ASALVarRef)expr, target);
		}
		
		throw new Error("Could not visit " + expr.getClass().getCanonicalName() + "!");
	}
	
	public StatFlat applyStat(AbstractFlat prev, ASALStatement stat) {
		if (stat == null) {
			throw new Error("Should not happen!");
		}
		
		if (stat instanceof ASALAssignStatement) {
			return visit(prev, (ASALAssignStatement)stat);
		}
		
		if (stat instanceof ASALEmptyStatement) {
			return visit(prev, (ASALEmptyStatement)stat);
		}
		
		if (stat instanceof ASALFunctionCallStatement) {
			return visit(prev, (ASALFunctionCallStatement)stat);
		}
		
		if (stat instanceof ASALIfStatement) {
			return visit(prev, (ASALIfStatement)stat);
		}
		
		if (stat instanceof ASALWhileStatement) {
			return visit(prev, (ASALWhileStatement)stat);
		}
		
		if (stat instanceof ASALReturnStatement) {
			return visit(prev, (ASALReturnStatement)stat);
		}
		
		if (stat instanceof ASALSeqStatement) {
			return visit(prev, (ASALSeqStatement)stat);
		}
		
		throw new Error("Could not visit " + stat.getClass().getCanonicalName() + "!");
	}
}

