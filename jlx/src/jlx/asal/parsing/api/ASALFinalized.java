package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

/**
 * Explicit event for when all regions of a composite have reached a final state.
 */
public class ASALFinalized extends ASALEvent {
	public ASALFinalized(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
	}
	
	@Override
	public String toText(TextOptions options) {
		return "finalized";
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		//Do nothing.
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.emptySet();
	}
}
