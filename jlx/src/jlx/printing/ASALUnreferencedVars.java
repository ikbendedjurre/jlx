package jlx.printing;

import java.util.*;

import jlx.asal.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.*;
import jlx.behave.proto.*;

public class ASALUnreferencedVars {
	public static Set<ASALVariable> getUnreferencedVars(TritoStateMachine stateMachine) {
		Vis vis = new Vis();
		
		for (TritoTransition t : stateMachine.transitions) {
			vis.visitTransition(t);
		}
		
		for (TritoVertex v : stateMachine.vertices) {
			if (v.getOnEntry() != null) {
				vis.visitTransition(v.getOnEntry());
			}
			
			if (v.getOnExit() != null) {
				vis.visitTransition(v.getOnExit());
			}
			
			for (TritoTransition t : v.getOnDo()) {
				vis.visitTransition(t);
			}
		}
		
		for (ASALOp op : stateMachine.scope.getOperationPerName().values()) {
			vis.visitFct(op);
		}
		
		Set<ASALVariable> result = new HashSet<ASALVariable>();
		result.addAll(vis.written);
		result.removeAll(vis.read);
		return result;
	}
	
	private static class Vis extends ASALVisitor<Void> {
		public final Set<ASALVariable> written;
		public final Set<ASALVariable> read;
		
		public Vis() {
			written = new HashSet<ASALVariable>();
			read = new HashSet<ASALVariable>();
		}
		
		public void visitTransition(TritoTransition t) {
			if (t.getEvent() != null) {
				visitEvent(t.getEvent());
			}
			
			if (t.getGuard() != null) {
				visitExpr(t.getGuard());
			}
			
			if (t.getStatement() != null) {
				visitStat(t.getStatement());
			}
		}
		
		@Override
		public Void visitFctDef(ASALOp fct) {
			return null;
		}
		
		@Override
		public Void handle(ASALAssignStatement node, Void var, Void expr) {
			written.add(node.getResolvedVar());
			return null;
		}
		
		@Override
		public Void handle(ASALVarRef node, Void var) {
			read.add(node.getResolvedVar());
			return null;
		}
	}
}
