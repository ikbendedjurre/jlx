package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.utils.*;

public class ASALEmptyStatement extends ASALStatement {
	public ASALEmptyStatement(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		super(parent, tree);
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) {
		//Do nothing.
	}
	
	@Override
	public boolean containsNonEmpty() {
		return false;
	}
	
	@Override
	public List<String> toText(Indentation indent, TextOptions options) {
		return Collections.singletonList(indent + "--");
	}
}
