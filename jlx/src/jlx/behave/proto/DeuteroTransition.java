package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.parsing.api.*;
import jlx.common.FileLocation;

public class DeuteroTransition extends ASALSyntaxTreeAPI {
	private DeuteroVertex sourceVertex;
	private DeuteroVertex targetVertex;
	private ASALEvent event;
	private ASALExpr guard;
	private ASALStatement statement;
	private boolean isLocal;
	private ProtoTransition legacy;
	
	public DeuteroTransition(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		event = createAPI("event", ASALEvent.class, true);
		guard = createAPI("guard", ASALExpr.class, true);
		statement = createAPI("statement", ASALStatement.class, false);
	}
	
	public ProtoTransition getLegacy() {
		return legacy;
	}
	
	public void setLegacy(ProtoTransition legacy) {
		this.legacy = legacy;
		
		statement.setFileLocation(legacy.getFileLocation());
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public void setSourceVertex(DeuteroVertex sourceVertex) {
		this.sourceVertex = sourceVertex;
	}
	
	public void setTargetVertex(DeuteroVertex targetVertex) {
		this.targetVertex = targetVertex;
	}
	
	public void setIsLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}
	
	public boolean interruptsCompositeStates() {
		return event instanceof ASALTrigger || event instanceof ASALTimeout;
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
	
	public DeuteroVertex getSourceVertex() {
		if (sourceVertex == null) {
			throw new Error("!!");
		}
		
		return sourceVertex;
	}
	
	public DeuteroVertex getTargetVertex() {
		return targetVertex;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return Collections.singleton(legacy);
	}
	
	public boolean isLocal() {
		return isLocal;
	}
	
	@Override
	public void validateAndCrossRef(JScope context, Class<? extends JType> expectedType) throws ASALException {
		if (event != null) {
			event.validateAndCrossRef(context, JVoid.class);
		}
		
		if (guard != null) {
			guard.validateAndCrossRef(context, JBool.class);
			guard.confirmInterpretable(JBool.class);
		}
		
		statement.validateAndCrossRef(context, JVoid.class);
	}
}
