package jlx.behave.proto;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.behave.CodeObject;
import jlx.common.FileLocation;
import jlx.common.reflection.ModelException;

public class ProtoTransition {
	public final ProtoStateMachine stateMachine;
	public final ProtoVertex sourceState;
	public final ProtoVertex targetState;
	public final CodeObject legacy;
	public final boolean isLocal;
	public final boolean isInitCode;
	
	public ProtoTransition(ProtoStateMachine stateMachine, ProtoVertex sourceState, ProtoVertex targetState, CodeObject codeObject, boolean isLocal) {
		this.stateMachine = stateMachine;
		this.sourceState = sourceState;
		this.targetState = targetState;
		this.isLocal = isLocal;
		
		if (sourceState != null) {
			isInitCode = stateMachine.initialVertices.contains(sourceState);
		} else {
			isInitCode = false;
		}
		
		legacy = codeObject;
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public DeuteroTransition parse(JScope scope) throws ModelException {
		try {
			ASALSyntaxTree tree = legacy.treeObject.toSyntaxTree(scope);
			DeuteroTransition result = tree.createAPI(null, DeuteroTransition.class);
			result.validateAndCrossRef(scope, JVoid.class);
			result.setLegacy(this);
			return result;
		} catch (ASALException e) {
			throw new ModelException(legacy.getFileLocation(), e);
		}
	}
	
	@Override
	public String toString() {
		String result = "";
		
		if (sourceState != null) {
			result += "[" + sourceState.sysmlClz.getCanonicalName() + "]";
		} else {
			result += "[??]";
		}
		
		result += "---->";
		
		if (targetState != null) {
			result += "[" + targetState.sysmlClz.getCanonicalName() + "]";
		} else {
			result += "[??]";
		}
		
		return result;
	}
}
