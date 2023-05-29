package jlx.asal.rewrite;

import java.util.*;

import jlx.asal.controlflow.ASALFlattening;
import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.parsing.api.ASALExpr;
import jlx.asal.parsing.api.ASALLiteral;
import jlx.asal.vars.ASALVariable;
import jlx.blocks.ibd1.PrimitivePort;
import jlx.common.reflection.ModelException;
import jlx.models.UnifyingBlock;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public interface ASALSymbolicValue {
	default public ASALExpr parse(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		ASALCode c = new ASALCode("EXPR", toString());
		ASALSyntaxTree tree = c.toSyntaxTree(scope);
		ASALExpr result = tree.createAPI(null, ASALExpr.class);
		result.validateAndCrossRef(scope, expectedType);
		return result;
	}
	
	default public ASALSymbolicValue negate() {
		return new ASALUnarySv("not", this);
	}
	
	/**
	 * Returns true if the expression is not straightforwardly FALSE.
	 */
	default public boolean couldBeTrue() {
		return !equals(FALSE);
	}
	
	default public boolean toBoolean() {
		if (equals(TRUE)) {
			return true;
		}
		
		if (equals(FALSE)) {
			return false;
		}
		
		throw new Error("Expecting TRUE or FALSE, but " + toString() + " found!");
	}
	
	//@SuppressWarnings("unchecked")
	default public <T extends ASALVariable> Map<T, ASALSymbolicValue> getSolution(Map<T, Set<ASALSymbolicValue>> pvsPerVar) {
		Set<Map<T, ASALSymbolicValue>> sols = getSolutions(pvsPerVar);
		
		if (sols.isEmpty()) {
			return null;
		}
		
		return sols.iterator().next();
		
//		Map<T, Set<ASALSymbolicValue>> temp = new HashMap<T, Set<ASALSymbolicValue>>();
//		
//		for (ASALVariable v : getReferencedVars()) {
//			if (!pvsPerVar.containsKey(v)) {
//				throw new Error("No possible values found for " + v.getName() + "!");
//			}
//			
//			temp.put((T)v, pvsPerVar.get(v));
//		}
//		
//		return Permutations.getFirst(temp, false, (x) -> { return substitute(x).toBoolean(); });
	}
	
	@SuppressWarnings("unchecked")
	default public <T extends ASALVariable> Set<Map<T, ASALSymbolicValue>> getSolutions(Map<T, Set<ASALSymbolicValue>> pvsPerVar) {
		Map<T, Set<ASALSymbolicValue>> temp = new HashMap<T, Set<ASALSymbolicValue>>();
		
		for (ASALVariable v : getReferencedVars()) {
			temp.put((T)v, pvsPerVar.get(v));
		}
		
		Set<Map<T, ASALSymbolicValue>> result = new HashSet<Map<T, ASALSymbolicValue>>();
//		Set<Map<T, ASALSymbolicValue>> substs = Permutations.getPermutations(temp, false);
//		System.out.println("#substs = " + substs.size());
		
		for (Map<T, ASALSymbolicValue> subst : HashMaps.allCombinations(temp)) {
			if (substitute(subst).toBoolean()) {
				result.add(subst);
			}
		}
		
		return result;
	}
	
	public static <T extends ASALVariable> ASALSymbolicValue simplify(ASALSymbolicValue expr, Map<T, Set<ASALSymbolicValue>> pvsPerVar) {
		Set<Map<T, ASALSymbolicValue>> sols = expr.getSolutions(pvsPerVar);
		Set<ASALVariable> vars = expr.getReferencedVars();
		int varRefCount = expr.getVarRefCount();
		
		if (sols.size() * vars.size() < varRefCount) {
			Set<ASALSymbolicValue> disjuncts = new HashSet<ASALSymbolicValue>(); 
			
			for (Map<T, ASALSymbolicValue> sol : sols) {
				disjuncts.add(from(sol));
			}
			
			return or(disjuncts);
		}
		
		return expr;
	}
	
	public static <T extends ASALVariable> ASALSymbolicValue from(Map<T, ASALSymbolicValue> m) {
		ASALSymbolicValue result = ASALSymbolicValue.TRUE;
		
		for (Map.Entry<T, ASALSymbolicValue> e : m.entrySet()) {
			result = ASALSymbolicValue.and(result, ASALSymbolicValue.eq(from(e.getKey()), e.getValue()));
		}
		
		return result;
	}
	
	public static ASALSymbolicValue from(UnifyingBlock unifyingBlock, PrimitivePort<?> port, JType value) {
		try {
			ASALCode code = new ASALCode("EXPR", value);
			
			try {
				ReprPort rp = unifyingBlock.reprPortPerPrimPort.get(port);
				ASALExpr result = code.toSyntaxTree(rp.getReprOwner()).createAPI(null, ASALExpr.class);
				result.validateAndCrossRef(rp.getReprOwner(), rp.getType());
				result.confirmInterpretable(rp.getType());
				return ASALFlattening.getSymbolicValue(rp.getReprOwner(), result, ASALSymbolicValue.from(rp.getType()));
			} catch (ASALException e) {
				throw new ModelException(code.fileLocation, e);
			}
		} catch (ModelException e) {
			throw new Error(e);
		}
	}
	
	public boolean isBooleanType();
	public ASALSymbolicValue substitute(Map<? extends ASALVariable, ASALSymbolicValue> subst);
	public Set<ASALVariable> getReferencedVars();
	public int getVarRefCount();
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(Object obj);
	
	@Override
	String toString();
	
	public final static ASALSymbolicValue NONE = new ASALLitSv("<<NONE>>");
	public final static ASALSymbolicValue ZERO = new ASALLitSv("0");
	public final static ASALSymbolicValue TRUE = new ASALTrueSv();
	public final static ASALSymbolicValue FALSE = new ASALFalseSv();
	public final static ASALSymbolicValue PULSE_OFF = new ASALFalseSv();
	
	public static Set<ASALSymbolicValue> from(Set<? extends JType> values) {
		Set<ASALSymbolicValue> result = new HashSet<ASALSymbolicValue>();
		
		for (JType value : values) {
			result.add(from(value));
		}
		
		return result;
	}
	
	public static Set<ASALSymbolicValue> from(JScope scope, ASALVariable variable) {
		return from(scope.getPossibleValues(variable));
	}
	
	public static ASALSymbolicValue from(Class<? extends JType> clz) {
		return from(JType.createDefaultValue(clz));
	}
	
	public static ASALSymbolicValue from(JType value) {
		if (value.getClass().equals(JBool.TRUE.class) || value.getClass().equals(JPulse.TRUE.class)) {
			return TRUE;
		}
		
		if (value.getClass().equals(JBool.FALSE.class) || value.getClass().equals(JPulse.FALSE.class)) {
			return FALSE;
		}
		
		return new ASALLitSv(value.toStr());
	}
	
	public static ASALSymbolicValue from(JScope scope, ASALLiteral lit) {
		if (lit.getText().equals("TRUE")) {
			return TRUE;
		}
		
		if (lit.getText().equals("FALSE")) {
			return FALSE;
		}
		
		return new ASALLitSv(JTypePrinter.toString(JType.createValue(lit.getResolvedConstructor()), scope, false));
	}
	
	public static ASALSymbolicValue applyRestrMap(ASALSymbolicValue expr, Map<ASALSymbolicValue, ASALSymbolicValue> restrMap) {
		Iterator<Map.Entry<ASALSymbolicValue, ASALSymbolicValue>> q = restrMap.entrySet().iterator();
		ASALSymbolicValue newExpr = q.next().getValue();
		
		while (q.hasNext()) {
			Map.Entry<ASALSymbolicValue, ASALSymbolicValue> restr = q.next();
			newExpr = ite(eq(expr, restr.getKey()), restr.getValue(), newExpr);
		}
		
		return newExpr;
	}
	
//	public static ASALSymbolicValue unassigned(ASALVariable var) {
//		return new ASALUnassignedVarSv(var);
//	}
//	
//	public static ASALSymbolicValue assigned(ASALVariable var) {
//		return eq(new ASALVarSv(var), new ASALUnassignedVarSv(var)).negate();
//	}
	
	public static ASALSymbolicValue from(ASALVariable var) {
		return new ASALVarSv(var);
	}
	
	public static ASALSymbolicValue from(String op, ASALSymbolicValue expr) {
		switch (op) {
			case "+":
			case "-":
				return unaryIntOp(op, expr);
			case "not":
			case "NOT":
				return expr.negate();
			default:
				throw new Error("Should not happen!");
		}
	}
	
	public static ASALSymbolicValue ite(ASALSymbolicValue condition, ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		if (condition instanceof ASALTrueSv) {
			return lhs;
		}
		
		if (condition instanceof ASALFalseSv) {
			return rhs;
		}
		
		if (lhs.equals(rhs)) {
			return lhs;
		}
		
		if (lhs.isBooleanType()) {
			return or(and(condition, lhs), and(condition.negate(), rhs));
		}
		
		return new ASALTernarySv(condition, lhs, rhs);
	}
	
	public static ASALSymbolicValue from(String op, ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		switch (op) {
			case "+":
			case "-":
			case "*":
			case "/":
			case "%":
			case "<":
				return binaryIntOp(op, lhs, rhs);
			case "<=":
				if (lhs.equals(rhs)) {
					return TRUE;
				}
				
				return binaryIntOp(op, lhs, rhs);
			case "and":
				return and(lhs, rhs);
			case "or":
				return or(lhs, rhs);
			case "=":
				return eq(lhs, rhs);
			case "<>":
				return from("=", lhs, rhs).negate();
			case ">=":
				return from("<=", lhs, rhs).negate();
			case ">":
				return from("<", lhs, rhs).negate();
		}
		
		throw new Error("Should not happen!");
	}
	
	public static ASALSymbolicValue and(ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		if (lhs.equals(rhs)) {
			return lhs;
		}
		
		if (lhs instanceof ASALFalseSv || rhs instanceof ASALFalseSv) {
			return FALSE;
		}
		
		if (lhs instanceof ASALTrueSv) {
			return rhs;
		}
		
		if (rhs instanceof ASALTrueSv) {
			return lhs;
		}
		
//		//Use disjunctive normal form:
//		if (lhs instanceof ASALOrSv) {
//			ASALSymbolicValue result = _false();
//			
//			//Distributive law:
//			for (ASALSymbolicValue expr : ((ASALOrSv)lhs).getExprs()) {
//				result = or(result, (and(expr, rhs)));
//			}
//			
//			return result;
//		}
//		
//		//Use disjunctive normal form:
//		if (rhs instanceof ASALOrSv) {
//			ASALSymbolicValue result = _false();
//			
//			//Distributive law:
//			for (ASALSymbolicValue expr : ((ASALOrSv)rhs).getExprs()) {
//				result = or(result, (and(lhs, expr)));
//			}
//			
//			return result;
//		}
		
		if (lhs.equals(rhs.negate())) {
			return FALSE; //Contradiction!
		}
		
		if (lhs instanceof ASALOrSv && rhs instanceof ASALOrSv) {
			if (((ASALOrSv)lhs).getExprs().containsAll(((ASALOrSv)rhs).getExprs())) {
				return lhs;
			}
			
			if (((ASALOrSv)rhs).getExprs().containsAll(((ASALOrSv)lhs).getExprs())) {
				return rhs;
			}
		}
		
		if (lhs instanceof ASALAndSv && rhs instanceof ASALAndSv) {
			Set<ASALSymbolicValue> conjuncts = new HashSet<ASALSymbolicValue>();
			conjuncts.addAll(((ASALAndSv)lhs).getExprs());
			conjuncts.addAll(((ASALAndSv)rhs).getExprs());
			return and(conjuncts);
		}
		
		if (lhs instanceof ASALAndSv) {
			if (((ASALAndSv)lhs).getExprs().contains(rhs.negate())) {
				return FALSE; //Contradiction!
			}
			
			Set<ASALSymbolicValue> conjuncts = new HashSet<ASALSymbolicValue>();
			boolean addRhs = true;
			
			if (rhs instanceof ASALOrSv) {
				for (ASALSymbolicValue e : ((ASALAndSv)lhs).getExprs()) {
					if (e instanceof ASALOrSv) {
						//RHS is more demanding than an existing demand in LHS.
						// (A or B or C) and (A or B) <=> (A or B)
						if (((ASALOrSv)e).getExprs().containsAll(((ASALOrSv)rhs).getExprs())) {
							//Do nothing.
						} else {
							//RHS is less demanding than an existing demand in LHS.
							// (A or B) and (A or B or C) <=> (A or B)
							if (((ASALOrSv)rhs).getExprs().containsAll(((ASALOrSv)e).getExprs())) {
								conjuncts.add(e);
								addRhs = false;
							} else {
								conjuncts.add(e);
							}
						}
					} else {
						conjuncts.add(e);
					}
				}
			} else {
				conjuncts.addAll(((ASALAndSv)lhs).getExprs());
				
				if (rhs instanceof ASALEqSv) {
					for (ASALSymbolicValue e : ((ASALAndSv)lhs).getExprs()) {
						addTransitiveEq((ASALEqSv)rhs, e, conjuncts);
					}
				}
			}
			
			if (addRhs) {
				conjuncts.add(rhs);
			}
			
			return new ASALAndSv(conjuncts);
		}
		
		if (rhs instanceof ASALAndSv) {
			return and(rhs, lhs);
		}
		
		Set<ASALSymbolicValue> conjuncts = new HashSet<ASALSymbolicValue>();
		conjuncts.add(lhs);
		conjuncts.add(rhs);
		return new ASALAndSv(conjuncts);
	}
	
	public static ASALSymbolicValue and(Set<ASALSymbolicValue> exprs) {
		ASALSymbolicValue result = TRUE;
		
		for (ASALSymbolicValue expr : exprs) {
			result = and(result, expr);
		}
		
		return result;
	}
	
	private static void addTransitiveEq(ASALEqSv eq, ASALSymbolicValue expr, Set<ASALSymbolicValue> dest) {
		if (!(expr instanceof ASALEqSv)) {
			return;
		}
		
		ASALEqSv eq2 = (ASALEqSv)expr;
		
		Set<ASALSymbolicValue> subExprs1 = new HashSet<ASALSymbolicValue>();
		subExprs1.add(eq.getLhs());
		subExprs1.add(eq.getRhs());
		
		Set<ASALSymbolicValue> subExprs2 = new HashSet<ASALSymbolicValue>();
		subExprs2.add(eq2.getLhs());
		subExprs2.add(eq2.getRhs());
		
		Set<ASALSymbolicValue> overlap = new HashSet<ASALSymbolicValue>(subExprs1);
		overlap.retainAll(subExprs2);
		
		if (overlap.size() != 1) {
			return;
		}
		
		ASALSymbolicValue v = overlap.iterator().next();
		subExprs1.remove(v);
		subExprs2.remove(v);
		dest.add(eq(subExprs1.iterator().next(), subExprs2.iterator().next()));
	}
	
	private static ASALSymbolicValue unaryIntOp(String op, ASALSymbolicValue expr) {
		if (expr instanceof ASALLitSv) {
			int i = Integer.parseInt(((ASALLitSv)expr).getText());
			
			switch (op) {
				case "+":
					return new ASALLitSv("" + (i));
				case "-":
					return new ASALLitSv("" + (-i));
				default:
					throw new Error("Should not happen!");
			}
		}
		
		return new ASALUnarySv(op, expr);
	}
	
	private static ASALSymbolicValue binaryIntOp(String op, ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		if (lhs instanceof ASALLitSv && rhs instanceof ASALLitSv) {
			int i1 = Integer.parseInt(((ASALLitSv)lhs).getText());
			int i2 = Integer.parseInt(((ASALLitSv)rhs).getText());
			
			switch (op) {
				case "+":
					return new ASALLitSv("" + (i1 + i2));
				case "-":
					return new ASALLitSv("" + (i1 - i2));
				case "*":
					return new ASALLitSv("" + (i1 * i2));
				case "/":
					return new ASALLitSv("" + (i1 / i2));
				case "%":
					return new ASALLitSv("" + (i1 % i2));
				case "<":
					return (i1 < i2) ? TRUE : FALSE;
				case "<=":
					return (i1 <= i2) ? TRUE : FALSE;
				default:
					throw new Error("Should not happen!");
			}
		}
		
		return new ASALBinarySv(op, lhs, rhs);
	}
	
	public static ASALSymbolicValue or(ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		String x = or2(lhs, rhs).toString();
		
		if (x.contains("(D22_PM2_Position = \"PointPos.RIGHT\") or (not (D22_PM2_Position = \"PointPos.RIGHT\"))")) {
			System.out.println("lhs = " + lhs.toString());
			System.out.println("rhs = " + rhs.toString());
			System.out.println("lhs||rhs = " + x);
			CLI.waitForEnter();
		}
		
		return or2(lhs, rhs);
	}
	
	private static ASALSymbolicValue or2(ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		if (lhs.equals(rhs)) {
			return lhs;
		}
		
		if (lhs instanceof ASALTrueSv || rhs instanceof ASALTrueSv) {
			return TRUE;
		}
		
		if (lhs instanceof ASALFalseSv) {
			return rhs;
		}
		
		if (rhs instanceof ASALFalseSv) {
			return lhs;
		}
		
		if (lhs.equals(rhs.negate())) {
			return TRUE; //Tautology!
		}
		
		if (lhs instanceof ASALAndSv && rhs instanceof ASALAndSv) {
			if (((ASALAndSv)lhs).getExprs().containsAll(((ASALAndSv)rhs).getExprs())) {
				return rhs;
			}
			
			if (((ASALAndSv)rhs).getExprs().containsAll(((ASALAndSv)lhs).getExprs())) {
				return lhs;
			}
		}
		
		if (lhs instanceof ASALOrSv && rhs instanceof ASALOrSv) {
			Set<ASALSymbolicValue> disjuncts = new HashSet<ASALSymbolicValue>();
			disjuncts.addAll(((ASALOrSv)lhs).getExprs());
			disjuncts.addAll(((ASALOrSv)rhs).getExprs());
			return or(disjuncts);
		}
		
		if (lhs instanceof ASALOrSv) {
			if (((ASALOrSv)lhs).getExprs().contains(rhs.negate())) {
				return TRUE; //Tautology!
			}
			
			Set<ASALSymbolicValue> disjuncts = new HashSet<ASALSymbolicValue>();
			boolean addRhs = true;
			
			if (rhs instanceof ASALAndSv) {
				for (ASALSymbolicValue e : ((ASALOrSv)lhs).getExprs()) {
					if (e instanceof ASALAndSv) {
						//RHS is less demanding than an existing demand in LHS.
						// (A and B and C) or (A and B) <=> (A and B)
						if (((ASALAndSv)e).getExprs().containsAll(((ASALAndSv)rhs).getExprs())) {
							//Do nothing.
						} else {
							//RHS is more demanding than an existing demand in LHS.
							// (A and B) or (A and B and C) <=> (A and B)
							if (((ASALAndSv)rhs).getExprs().containsAll(((ASALAndSv)e).getExprs())) {
								disjuncts.add(e);
								addRhs = false;
							} else {
								disjuncts.add(e);
							}
						}
					} else {
						disjuncts.add(e);
					}
				}
			} else {
				disjuncts.addAll(((ASALOrSv)lhs).getExprs());
			}
			
			if (addRhs) {
				disjuncts.add(rhs);
			}
			
			return new ASALOrSv(disjuncts);
		}
		
		if (rhs instanceof ASALOrSv) {
			return or(rhs, lhs);
		}
		
		Set<ASALSymbolicValue> disjuncts = new HashSet<ASALSymbolicValue>();
		disjuncts.add(lhs);
		disjuncts.add(rhs);
		return new ASALOrSv(disjuncts);
	}
	
	public static ASALSymbolicValue or(Set<ASALSymbolicValue> exprs) {
		ASALSymbolicValue result = FALSE;
		
		for (ASALSymbolicValue expr : exprs) {
			result = or(result, expr);
		}
		
		return result;
	}
	
	public static ASALSymbolicValue eq(ASALSymbolicValue lhs, ASALSymbolicValue rhs) {
		if (lhs.equals(rhs)) {
			return TRUE;
		}
		
		if (lhs instanceof ASALLitSv && rhs instanceof ASALLitSv) {
			return FALSE;
		}
		
		if (lhs instanceof ASALTrueSv) {
			return rhs;
		}
		
		if (rhs instanceof ASALTrueSv) {
			return lhs;
		}
		
		if (lhs instanceof ASALFalseSv) {
			return rhs.negate();
		}
		
		if (rhs instanceof ASALFalseSv) {
			return lhs.negate();
		}
		
		return new ASALEqSv(lhs, rhs);
	}
	
	public static ASALSymbolicValue litFct(ASALSymbolicValue expr, Map<ASALSymbolicValue, ASALSymbolicValue> fct) {
		if (expr instanceof ASALLitSv) {
			ASALLitSv lit = (ASALLitSv)expr;
			
			if (!fct.containsKey(lit)) {
				throw new Error("No mapping exists for " + expr.toString() + "!");
			}
			
			return fct.get(lit);
		}
		
		if (expr instanceof ASALLitFctSv) {
			ASALLitFctSv litFct = (ASALLitFctSv)expr;
			Map<ASALSymbolicValue, ASALSymbolicValue> newFct = new HashMap<ASALSymbolicValue, ASALSymbolicValue>();
			
			for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> entry : litFct.getFct().entrySet()) {
				newFct.put(entry.getKey(), litFct(entry.getValue(), fct));
			}
			
			return litFct(litFct.getExpr(), newFct);
		}
		
		for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> entry : fct.entrySet()) {
			if (!entry.getValue().equals(entry.getKey())) {
				return new ASALLitFctSv(expr, fct);
			}
		}
		
		return expr;
	}
}


