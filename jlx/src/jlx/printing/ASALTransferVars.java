package jlx.printing;

import java.util.*;

import jlx.asal.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.*;
import jlx.behave.proto.*;

public class ASALTransferVars {
	public static Set<ASALVariable> getTransferVarsOnly(TritoStateMachine stateMachine, Collection<? extends ASALVariable> vars) {
		Set<ASALVariable> result = new HashSet<ASALVariable>(vars);
		result.removeAll(getNonTransferVars(stateMachine));
		return result;
//		return Collections.emptySet();
	}
	
	public static Set<ASALVariable> getNonTransferVars(TritoStateMachine stateMachine) {
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		Vis vis = new Vis();
		
		for (TritoTransition t : stateMachine.transitions) {
			result.addAll(vis.visitTransition(t));
		}
		
		for (TritoVertex v : stateMachine.vertices) {
			if (v.getOnEntry() != null) {
				result.addAll(vis.visitTransition(v.getOnEntry()));
			}
			
			if (v.getOnExit() != null) {
				result.addAll(vis.visitTransition(v.getOnExit()));
			}
			
			for (TritoTransition t : v.getOnDo()) {
				result.addAll(vis.visitTransition(t));
			}
		}
		
		for (ASALOp op : stateMachine.scope.getOperationPerName().values()) {
			result.addAll(vis.visitFct(op));
		}
		
		System.out.println("Found " + result.size() + " non-transfer variables!!");
		
		for (ASALVariable v : result) {
			System.out.println("\t" + v.getName());
		}
		
		return result;
	}
	
	private static class Vis extends ASALBuildSetVisitor<ASALVariable> {
		public Set<ASALVariable> visitTransition(TritoTransition t) {
			Set<ASALVariable> result = new HashSet<ASALVariable>();
			
			if (t.getEvent() != null) {
				result.addAll(visitEvent(t.getEvent()));
			}
			
			if (t.getGuard() != null) {
				result.addAll(visitExpr(t.getGuard()));
			}
			
			if (t.getStatement() != null) {
				result.addAll(visitStat(t.getStatement()));
			}
			
			return result;
		}
		
		@Override
		public Set<ASALVariable> visitFctDef(ASALOp fct) {
			return Collections.emptySet();
		}
		
		@Override
		public Set<ASALVariable> handle(ASALVariable leaf) {
			return Collections.singleton(leaf);
		}
		
		@Override
		public Set<ASALVariable> handle(ASALAssignStatement node, Set<ASALVariable> var, Set<ASALVariable> expr) {
			if (node.getExpression() instanceof ASALVarRef) {
				//ASALVarRef varRef = (ASALVarRef)node.getExpression();
				return Collections.emptySet();
			}
			
			return expr;
		}
	}
}

