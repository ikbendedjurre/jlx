package jlx.behave.proto;

import java.io.PrintStream;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.ASALProperty;
import jlx.common.FileLocation;

public class TritoTransition {
	private TritoVertex sourceVertex;
	private TritoVertex targetVertex;
	private ASALEvent event;
	private ASALExpr guard;
	private ASALStatement statement;
	private boolean isLocal;
	private DeuteroTransition legacy;
	
	public TritoTransition(DeuteroTransition source, TritoVertex sourceVertex, TritoVertex targetVertex, JScope scope, Map<ASALOp, ASALProperty> helperPulsePortPerCallOp) {
		this.sourceVertex = sourceVertex;
		this.targetVertex = targetVertex;
		
		guard = source.getGuard();
		statement = source.getStatement();
		isLocal = source.isLocal();
		legacy = source;
		
		if (source.getEvent() instanceof ASALCall) {
			ASALCall c = (ASALCall)source.getEvent();
			ASALProperty p = helperPulsePortPerCallOp.get(c.getResolvedOperation());
			
			if (p == null) {
				p = scope.generateHelperProperty(c.getMethodName(), JPulse.FALSE);
				helperPulsePortPerCallOp.put(c.getResolvedOperation(), p);
				c.getResolvedOperation().attachHelperPulsePort(p);
			}
			
			event = new ASALTrigger(c.getParent(), c.getTree(), new ASALVarRef(null, null, p));
			statement = ASALSeqStatement.from(statement, new ASALAssignStatement(null, null, p, ASALLiteral._false()));
		} else {
			event = source.getEvent();
		}
	}
	
	public DeuteroTransition getLegacy() {
		return legacy;
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public void setSourceVertex(TritoVertex sourceVertex) {
		this.sourceVertex = sourceVertex;
	}
	
	public void setTargetVertex(TritoVertex targetVertex) {
		this.targetVertex = targetVertex;
	}
	
	public boolean interruptsCompositeStates() {
		return event != null;
	}
	
	public ASALEvent getEvent() {
		return event;
	}
	
	public ASALExpr getGuard() {
		return guard;
	}
	
	public ASALStatement getStatement() {
		return statement;
	}
	
	public TritoVertex getSourceVertex() {
		return sourceVertex;
	}
	
	public TritoVertex getTargetVertex() {
		return targetVertex;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
	
	public boolean isLocal() {
		return isLocal;
	}
	
	protected void setStatement(ASALStatement st) {
		this.statement = st;
	}
	
	/**
	 * Returns the vertices that are exited by this transition, in all possible exit orders.
	 * We include pseudo-states (even though they have no exit behavior).
	 */
	public Set<List<TritoVertex>> getExitedVerticesInOrder() {
		TritoVertex sharedParent = getSharedParent(getSourceVertex(), getTargetVertex());
		List<TritoVertex> lineage = getSourceVertex().getLineage();
		TritoVertex outermostExitedVertex = lineage.get(lineage.indexOf(sharedParent) + 1);
		return outermostExitedVertex.getVerticesInAllExitOrders();
	}
	
	/**
	 * <b>Transitions that enter a composite state by crossing its boundary are not currently supported;</b>
	 * <b>when this changes, this method will become a whole lot more interesting.</b>
	 * 
	 * Returns the vertices that are entered (recursively) by this transition.
	 * We only include vertices with entry behavior.
	 * 
	 * First vertex must be the target vertex of this transition, if it has entry behavior.
	 * Initial vertices of the target vertex are also entered, but they never have entry behavior.
	 */
	public Set<List<TritoVertex>> getEnteredVerticesInOrder() {
		if (getTargetVertex().getOnEntry() != null) {
			return Collections.singleton(Collections.singletonList(getTargetVertex()));
		} else {
			return Collections.singleton(Collections.emptyList());
		}
	}
	
	/**
	 * When v1 and v2 are the same vertex, the composite state / state machine that nests v1/v2 is returned.
	 * When v1 and v2 are different vertices, the innermost composite state / state machine that (indirectly) contains both v1 and v2 is returned.
	 */
	private static TritoVertex getSharedParent(TritoVertex v1, TritoVertex v2) {
		List<TritoVertex> lineage1 = v1.getLineage();
		List<TritoVertex> lineage2 = v2.getLineage();
		
		//The last vertex in the lineage is v1, which can never be the return value;
		//start therefore with the before-last vertex in the lineage:
		for (int index = lineage1.size() - 2; index >= 0; index--) {
			if (lineage2.contains(lineage1.get(index))) {
				return lineage1.get(index);
			}
		}
		
		throw new Error("Should not happen!");
	}
	
	public void printDebugText(PrintStream out, String prefix) {
//		out.println(prefix + "from " + sourceVertex.getClz().getCanonicalName() + " to " + targetVertex.getClz().getCanonicalName() + " {");
//		
//		if (event != null) {
//			out.println(prefix + "\t" + event.textify(LOD.FULL));
//		}
//		
//		if (guard != null) {
//			out.println(prefix + "\t[ " + guard.textify(LOD.FULL) + " ]");
//		}
//		
//		for (String s : statement.textify(LOD.FULL).split("\\n")) {
//			out.println(prefix + "\t" + s);
//		}
//		
//		for (Set<TritoVertex> vs : getExitedVertices().values()) {
//			for (TritoVertex v : vs) {
//				out.println(prefix + "\texit " + v.getClz().getCanonicalName() + ";");
//			}
//		}
//		
//		for (Set<TritoVertex> vs : getEnteredVertices().values()) {
//			for (TritoVertex v : vs) {
//				out.println(prefix + "\tenter " + v.getClz().getCanonicalName() + ";");
//			}
//		}
//		
//		out.println(prefix + "}");
	}
}
