package jlx.asal.controlflow;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALVariable;

public class ASALFlattening {
//	public static ASALEvent flatten(ASALEvent event, JScope scope) {
//		if (event == null) {
//			return null;
//		}
//		
//		if (event instanceof ASALTrigger) {
//			ASALTrigger trigger = (ASALTrigger)event;
//			ASALExpr newExpr = flatten(trigger.getExpr(), ASALSymbolicValue.TRUE, scope);
//			return new ASALTrigger(trigger.getParent(), trigger.getTree(), newExpr);
//		}
//		
//		if (event instanceof ASALTimeout) {
//			ASALTimeout timeout = (ASALTimeout)event;
//			ASALExpr newDuration = flatten(timeout.getDuration(), ASALSymbolicValue.ZERO, scope);
//			return new ASALTimeout(timeout.getParent(), timeout.getTree(), newDuration);
//		}
//		
//		if (event instanceof ASALFinalized) {
//			return event;
//		}
//		
//		if (event instanceof ASALCall) {
//			return event;
//		}
//		
//		throw new Error("Should not happen!");
//	}
	
//	public static Map<ASALVariable, ASALSymbolicValue> getInitialValuePerVar(JScope scope) {
//		Map<ASALVariable, ASALSymbolicValue> initialValuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>();
//		
//		for (ASALVariable v : scope.getVariablePerName().values()) {
//			if (v instanceof ReprPort) {
//				ReprPort rp = (ReprPort)v;
//				
//				if (rp.getDir() == Dir.OUT) {
//					if (rp.getPulsePort() != null || rp.getDataPorts().size() > 0) {
//						initialValuePerVar.put(v, ASALSymbolicValue.from(scope, JType.createDefaultValue(v.getType())));
//					} else {
//						initialValuePerVar.put(v, ASALSymbolicValue.from(v));
//					}
//				} else {
//					initialValuePerVar.put(v, ASALSymbolicValue.from(v));
//				}
//			} else {
//				initialValuePerVar.put(v, ASALSymbolicValue.from(v));
//			}
//		}
//		
//		return initialValuePerVar;
//	}
	
//	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct2(JScope scope, List<ASALStatement> stats, Map<ASALVariable, ASALStatement> statPerChangedVar) {
//		return getNextStateFct2(scope, ASALSymbolicExecution.getDefaultInitialValuePerVar(scope), stats, statPerChangedVar);
//	}
	
	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct2(JScope scope, Map<ASALVariable, ASALSymbolicValue> initialValuePerVar, List<ASALStatement> stats, Map<ASALVariable, ASALStatement> statPerChangedVar) {
		Map<ASALVariable, ASALStatement> statPerChangedVar2 = ASALCF.getStatPerVarUpdate(scope, stats);
		statPerChangedVar.clear();
		statPerChangedVar.putAll(statPerChangedVar2);
		return ASALCF.getNextStateFct2(scope, stats);
		
		
//		ASALControlFlowVisitor<ASALSymbolicExecution> v = new ASALControlFlowVisitor<ASALSymbolicExecution>();
//		String txt = Texts.concat(stats, " ", (s) -> { return s.textify(LOD.ONE_LINE); });
//		ASALSymbolicExecution exc = new ASALSymbolicExecution(scope, initialValuePerVar, ASALSymbolicValue.NONE, txt);
//		
//		for (int index = 0; index < stats.size(); index++) {
//			Map<ASALVariable, ASALSymbolicValue> prev = new HashMap<ASALVariable, ASALSymbolicValue>(exc.getValuePerVar());
//			exc = v.visitStat(exc, stats.get(index));
//			
//			for (ASALVariable w : exc.getUpdatedVariables()) {
//				if (statPerChangedVar.containsKey(w)) {
//					if (!exc.getVarValue(w).equals(prev.get(w))) {
//						statPerChangedVar.put(w, stats.get(index));
//					}
//				} else {
//					statPerChangedVar.put(w, stats.get(index));
//				}
//			}
//		}
//		
//		return exc.getValuePerUpdatedVar();
	}
	
	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct2(JScope scope, ASALStatement stat) {
		return ASALCF.getNextStateFct2(scope, stat);
//		return getNextStateFct2(scope, ASALSymbolicExecution.getDefaultInitialValuePerVar(scope), stat);
	}
	
	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct2(JScope scope, Map<ASALVariable, ASALSymbolicValue> initialValuePerVar, ASALStatement stat) {
		return ASALCF.getNextStateFct2(scope, stat);
		
//		ASALSymbolicExecution exc = new ASALSymbolicExecution(scope, initialValuePerVar, ASALSymbolicValue.NONE, stat.textify(LOD.ONE_LINE));
//		return new ASALControlFlowVisitor<ASALSymbolicExecution>().visitStat(exc, stat).getValuePerUpdatedVar();
	}
	
//	public static Map<ASALVariable, ASALSymbolicValue> getNextStateFct(JScope scope, ASALStatement stat) {
//		
//	}
//	
//	public static Map<ASALVariable, ASALExpr> getNextStateFct(JScope scope, Map<ASALVariable, ASALSymbolicValue> initialValuePerVar, ASALStatement stat) {
//		ASALControlFlowVisitor<ASALSymbolicExecution> v = new ASALControlFlowVisitor<ASALSymbolicExecution>();
//		ASALSymbolicExecution exc = new ASALSymbolicExecution(scope, ASALSymbolicValue.NONE, stat.textify(LOD.ONE_LINE));
//		Map<ASALVariable, ASALExpr> result = new HashMap<ASALVariable, ASALExpr>();
//		
//		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : v.visitStat(exc, stat).getChangedValuePerVar().entrySet()) {
//			ASALCode code = new ASALCode("EXPR", entry.getValue().toString());
//			
//			try {
//				ASALSyntaxTree tree = code.toSyntaxTree(scope);
//				ASALExpr newExpr = tree.createAPI(null, ASALExpr.class);
//				newExpr.validateAndCrossRef(scope);
//				result.put(entry.getKey(), newExpr);
//			} catch (ASALException e) {
//				throw new Error("Should not happen!", e);
//			}
//		}
//		
//		return result;
//	}
	
//	public static ASALStatement flatten(JScope scope, ASALStatement stat) {
//		ASALControlFlowVisitor<ASALSymbolicExecution> v = new ASALControlFlowVisitor<ASALSymbolicExecution>();
//		ASALSymbolicExecution exc = new ASALSymbolicExecution(scope, ASALSymbolicValue.NONE, stat.textify(LOD.ONE_LINE));
//		List<String> lines = new ArrayList<String>();
//		
//		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : v.visitStat(exc, stat).getChangedValuePerVar().entrySet()) {
//			lines.add(entry.getKey().getName() + " := " + entry.getValue().toString() + ";");
//		}
//		
//		ASALCode code = new ASALCode("STATS1", null, lines);
//		
//		try {
//			ASALSyntaxTree tree = code.toSyntaxTree(scope);
//			ASALStatement result = tree.createAPI(null, ASALStatement.class);
//			result.validateAndCrossRef(scope);
//			return result;
//		} catch (ASALException e) {
//			throw new Error("Should not happen!", e);
//		}
//	}
	
	public static ASALSymbolicValue getSymbolicValue(JScope scope, ASALExpr expr, ASALSymbolicValue fallbackExpr) {
//		String r = ASALCF.getSymbolicValue(scope, expr, fallbackExpr).toString();
//		
//		if (r.contains("D13_PM2_Activation")) {
//			System.out.println("x = " + expr.textify(LOD.ONE_LINE));
//			System.out.println("y = " + r);
//			CLI.waitForEnter();
//		}
		
//		if (expr.textify(LOD.ONE_LINE).contains("cOp5_")) {
//			System.out.println("x = " + expr.textify(LOD.ONE_LINE));
//			System.out.println("y = " + ASALCF.getSymbolicValue(scope, expr, fallbackExpr).toString());
//			CLI.waitForEnter();
//		}
		
		return ASALCF.getSymbolicValue(scope, expr, fallbackExpr);
		
//		ASALControlFlowVisitor<ASALSymbolicExecution> v = new ASALControlFlowVisitor<ASALSymbolicExecution>();
//		ASALSymbolicExecution exc = new ASALSymbolicExecution(scope, ASALSymbolicExecution.getDefaultInitialValuePerVar(scope), fallbackExpr, expr.textify(LOD.ONE_LINE));
//		System.out.println(expr.textify(LOD.ONE_LINE));
//		
//		if (expr.textify(LOD.ONE_LINE).contains("cOp2_")) {
//			ASALSymbolicValue x = v.visitExpr(exc, expr).getCurrentExpr();
//			System.out.println("x = " + x.toString());
//			System.out.println("y = " + ASALCF.getSymbolicValue(scope, expr, fallbackExpr).toString());
//			CLI.waitForEnter();
//		}
//		
////		System.out.println(expr.textify(LOD.ONE_LINE));
////		CLI.waitForEnter();
//		
//		return v.visitExpr(exc, expr).getCurrentExpr();
	}
	
//	public static ASALExpr compileExpr(JScope scope, ASALSymbolicValue value) {
//		ASALCode code = new ASALCode("EXPR", value.toString());
//		
//		try {
//			ASALSyntaxTree tree = code.toSyntaxTree(scope);
//			ASALExpr result = tree.createAPI(null, ASALExpr.class);
//			result.validateAndCrossRef(scope);
//			return result;
//		} catch (ASALException e) {
//			throw new Error("Should not happen!", e);
//		}
//	}
	
//	public static ASALExpr flatten(JScope scope, ASALExpr expr, ASALSymbolicValue fallbackExpr) {
//		return compileExpr(scope, getSymbolicValue(scope, expr, fallbackExpr));
//	}
	
	public static boolean couldBeSat(JScope scope, ASALExpr expr) {
		return ASALCF.couldBeSat(scope, expr);
//		return getSymbolicValue(scope, expr, ASALSymbolicValue.FALSE).couldBeTrue();
	}
}

