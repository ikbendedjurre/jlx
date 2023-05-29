package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public abstract class ASALExpr extends ASALSyntaxTreeAPI {
	private Class<? extends JType> resolvedType;
	
	public ASALExpr(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
	}
	
	protected final void setResolvedType(Class<? extends JType> resolvedType) {
		this.resolvedType = resolvedType;
	}
	
	public final Class<? extends JType> getResolvedType() {
		return resolvedType;
	}
	
	public void confirmInterpretable(Class<? extends JType> supertypeCandidate) throws ASALException {
		if (!JType.isAssignableTo(supertypeCandidate, resolvedType)) {
			throw new ASALException(this, "Cannot interpret " + resolvedType.getName() + " as " + supertypeCandidate.getCanonicalName() + "!");
		}
	}
	
	public abstract Set<ASALVariable> getReferencedVars();
	public abstract String toText(TextOptions options);
}
