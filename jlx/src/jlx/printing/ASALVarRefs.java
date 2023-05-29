package jlx.printing;

import java.util.*;

import jlx.asal.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.*;

public class ASALVarRefs {
	public static Set<ASALVariable> getVarRefs(ASALExpr expr) {
		Vis v = new Vis();
		v.visitExpr(expr);
		return Collections.unmodifiableSet(v.varRefs);
	}
	
	public static Set<ASALVariable> getVarRefs(ASALStatement stat) {
		Vis v = new Vis();
		v.visitStat(stat);
		return Collections.unmodifiableSet(v.varRefs);
	}
	
	private static class Vis extends ASALVisitor<Void> {
		private Set<ASALVariable> varRefs;
		
		private Vis() {
			varRefs = new HashSet<ASALVariable>();
		}
		
		@Override
		public Void visitFctDef(ASALOp fct) {
			visitStat(fct.getBody());
			return null;
		}
		
		@Override
		public Void handle(ASALVariable leaf) {
			varRefs.add(leaf);
			return null;
		}
	}
}
