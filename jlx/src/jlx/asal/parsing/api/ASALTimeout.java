package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.*;
import jlx.utils.TextOptions;

public class ASALTimeout extends ASALEvent {
	private ASALPort resolvedDurationPort;
	private String durationPortName;
	
	public ASALTimeout(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
		
		//duration = createAPI("duration", ASALExpr.class, false);
		durationPortName = tree.getPropery("durationPort");
	}
	
//	public ASALTimeout(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALExpr duration) {
//		super(parent, tree);
//		
//		this.duration = duration;
//	}
	
	public ASALPort getResolvedDurationPort() {
		return resolvedDurationPort;
	}
	
	@Override
	public String toText(TextOptions options) {
		return "after(" + options.id(durationPortName) + ")";
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		ASALVariable v = scope.getVariable(durationPortName);
		
		if (v == null) {
			throw new ASALException(this, "Unknown variable!", scope.getScopeSuggestions(true, false, false, false));
		}
		
		if (v instanceof ASALPort) {
			resolvedDurationPort = (ASALPort)v;
			
			if (!JType.isAssignableTo(JInt.class, resolvedDurationPort.getType())) {
				throw new ASALException(this, "Variable should be a port of type " + JInt.class.getCanonicalName() + "!");
			}
		} else {
			throw new ASALException(this, "Variable should be a port!");
		}
		
//		duration.validateAndCrossRef(scope);
//		duration.confirmInterpretable(JInt.class);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.singleton(resolvedDurationPort);
		//return duration.getReferencedVars();
	}
}
