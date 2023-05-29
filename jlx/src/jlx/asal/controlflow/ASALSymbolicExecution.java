package jlx.asal.controlflow;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.*;

public class ASALSymbolicExecution implements ASALExecution {
	private JScope scope;
	private Map<ASALVariable, ASALSymbolicValue> valuePerVar;
//	private Map<ASALVariable, ASALSymbolicValue> initialValuePerVar;
	private Set<ASALVariable> updatedVariables;
	private Stack<ASALSymbolicValue> exprStack;
	private ASALSymbolicValue returningCondition;
	private Stack<ASALSymbolicValue> returnValueStack;
	private String debugText;
	
	@SuppressWarnings("unchecked")
	public ASALSymbolicExecution(ASALSymbolicExecution source) {
		scope = source.scope;
		valuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>(source.valuePerVar);
//		initialValuePerVar = source.initialValuePerVar;
		updatedVariables = new HashSet<ASALVariable>(source.updatedVariables);
		exprStack = (Stack<ASALSymbolicValue>)source.exprStack.clone();
		returningCondition = source.returningCondition;
		returnValueStack = (Stack<ASALSymbolicValue>)source.returnValueStack.clone();
		debugText = source.debugText;
	}
	
	public ASALSymbolicExecution(JScope scope, Map<ASALVariable, ASALSymbolicValue> initialValuePerVar, ASALSymbolicValue defaultReturnValue, String debugText) {
		this.scope = scope;
		this.debugText = debugText;
//		this.initialValuePerVar = initialValuePerVar;
		
		updatedVariables = new HashSet<ASALVariable>();
		valuePerVar = new HashMap<ASALVariable, ASALSymbolicValue>(initialValuePerVar);
		exprStack = new Stack<ASALSymbolicValue>();
		returningCondition = ASALSymbolicValue.FALSE;
		returnValueStack = new Stack<ASALSymbolicValue>();
		returnValueStack.push(defaultReturnValue);
	}
	
	public static Map<ASALVariable, ASALSymbolicValue> getDefaultInitialValuePerVar(JScope scope) {
		Map<ASALVariable, ASALSymbolicValue> result = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (Map.Entry<String, ASALVariable> entry : scope.getVariablePerName().entrySet()) {
			result.put(entry.getValue(), ASALSymbolicValue.from(entry.getValue()));
		}
		
		return result;
	}
	
	@Override
	public ASALExecution createCopy() {
		return new ASALSymbolicExecution(this);
	}
	
	public JScope getScope() {
		return scope;
	}
	
	public ASALSymbolicValue getVarValue(ASALVariable var) {
		return valuePerVar.get(var);
	}
	
	public ASALSymbolicValue getCurrentExpr() {
		return exprStack.peek();
	}
	
	public boolean hasCurrentExpr() {
		return exprStack.size() > 0;
	}
	
	public Set<ASALVariable> getUpdatedVariables() {
		return updatedVariables;
	}
	
	public Map<ASALVariable, ASALSymbolicValue> getValuePerVar() {
		return valuePerVar;
	}
	
	public Map<ASALVariable, ASALSymbolicValue> getValuePerUpdatedVar() {
		Map<ASALVariable, ASALSymbolicValue> result = new HashMap<ASALVariable, ASALSymbolicValue>();
		
		for (ASALVariable v : updatedVariables) {
			result.put(v, getVarValue(v));
		}
		
//		for (ASALVariable v : scope.getVariablePerName().values()) {
//			ASALSymbolicValue value = getVarValue(v);
//			
//			if (!value.equals(initialValuePerVar.get(v))) {
//				result.put(v, value);
//			}
//		}
		
		return result;
	}
	
	@Override
	public void applyCurrentExprAsLocationConjunct(ASALExecution elseBranch) {
		applyLocationConjunct(exprStack.peek(), elseBranch);
	}
	
	@Override
	public void popCurrentExpr() {
		exprStack.pop();
	}
	
	@Override
	public void applyReturningConditionAsLocationConjunct(ASALExecution elseBranch) {
		applyLocationConjunct(returningCondition, elseBranch);
	}
	
	private void applyLocationConjunct(ASALSymbolicValue conjunct, ASALExecution elseBranch) {
		final ASALSymbolicExecution symbolicElseBranch = (ASALSymbolicExecution)elseBranch;
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : valuePerVar.entrySet()) {
			entry.setValue(ASALSymbolicValue.ite(conjunct, entry.getValue(), symbolicElseBranch.valuePerVar.get(entry.getKey())));
		}
		
		System.out.println("ite(" + conjunct + ", " + returnValueStack.peek() + ", " + symbolicElseBranch.returnValueStack.peek() + ")");
		returnValueStack.push(ASALSymbolicValue.ite(conjunct, returnValueStack.pop(), symbolicElseBranch.returnValueStack.peek()));
		returningCondition = ASALSymbolicValue.ite(conjunct, returningCondition, symbolicElseBranch.returningCondition);
		updatedVariables.addAll(symbolicElseBranch.updatedVariables);
	}
	
	private void pushToStack(ASALSymbolicValue expr) {
		if (expr == null) {
			throw new Error("Should not happen! " + debugText);
		}
		
		exprStack.push(expr);
	}
	
	@Override
	public void handle(ASALUnaryExpr node) {
		ASALSymbolicValue expr = exprStack.pop();
		pushToStack(ASALSymbolicValue.from(node.getOp(), expr));
	}
	
	@Override
	public void handle(ASALBinaryExpr node) {
		ASALSymbolicValue rhs = exprStack.pop();
		ASALSymbolicValue lhs = exprStack.pop();
		pushToStack(ASALSymbolicValue.from(node.getOp(), lhs, rhs));
	}
	
	@Override
	public void handle(ASALTernaryExpr node) {
		ASALSymbolicValue result = exprStack.pop(); //Whether we chose lhs or rhs, pop it.
		exprStack.pop(); //Also pop the condition.
		pushToStack(result);
	}
	
	@Override
	public void handle(ASALAssignStatement node) {
		valuePerVar.put(node.getResolvedVar(), exprStack.pop());
		updatedVariables.add(node.getResolvedVar());
	}
	
	@Override
	public void handle(ASALReturnStatement node) {
		returnValueStack.pop(); //There already is a (default) return value on the stack!!
		returnValueStack.push(exprStack.pop());
		returningCondition = ASALSymbolicValue.TRUE;
	}
	
	@Override
	public void handleOpBegin(ASALOp node) {
		for (int index = node.getParams().size() - 1; index >= 0; index--) {
			valuePerVar.put(node.getParams().get(index), exprStack.pop());
		}
		
		if (node.getReturnType().equals(JVoid.class)) {
			returnValueStack.push(ASALSymbolicValue.NONE);
		} else {
			returnValueStack.push(ASALSymbolicValue.from(JType.createDefaultValue(node.getReturnType())));
		}
	}
	
	@Override
	public void handleOpEnd(ASALOp node, boolean isStatement) {
		for (ASALParam p : node.getParams()) {
			valuePerVar.remove(p);
		}
		
		if (isStatement) {
			returnValueStack.pop();
		} else {
			pushToStack(returnValueStack.pop());
		}
		
		returningCondition = ASALSymbolicValue.FALSE;
	}
	
	@Override
	public void handle(ASALLiteral node) {
		pushToStack(ASALSymbolicValue.from(scope, node));
	}
	
	@Override
	public void handle(ASALVarRef node) {
		pushToStack(valuePerVar.get(node.getResolvedVar()));
	}
	
	@Override
	public void handle(ASALIfStatement node) {
		exprStack.pop(); //Pop the condition.
	}
	
	@Override
	public void debugPrint() {
		if (exprStack.size() > 0) {
			for (int index = 0; index < exprStack.size(); index++) {
				System.out.println("\tSTACK[" + index + "] -> " + exprStack.get(index));
			}
		} else {
			System.out.println("\tSTACK TOP -> (none)");
		}
		
		System.out.println("\tIS_RETURNING -> " + returningCondition);
		
		for (int index = 0; index < returnValueStack.size(); index++) {
			System.out.println("\tRETURN[" + index + "] -> " + returnValueStack.get(index));
		}
		
		System.out.println("\tSTACK TOP -> (none)");
		
		for (Map.Entry<ASALVariable, ASALSymbolicValue> entry : getValuePerUpdatedVar().entrySet()) {
			System.out.println("\t" + entry.getKey().getName() + " -> " +  entry.getValue());
		}
	}
}

