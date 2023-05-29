package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.utils.TextOptions;

public abstract class ASALEvent extends ASALSyntaxTreeAPI {
	public ASALEvent(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
	}
	
	public abstract Set<ASALVariable> getReferencedVars();
	public abstract String toText(TextOptions options);
}
