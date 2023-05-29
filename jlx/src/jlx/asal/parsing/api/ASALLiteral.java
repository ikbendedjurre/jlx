package jlx.asal.parsing.api;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.vars.ASALVariable;
import jlx.common.reflection.ClassReflectionException;
import jlx.utils.TextOptions;

public class ASALLiteral extends ASALExpr {
	private String text;
	private Class<? extends JType> resolvedConstructor;
	
	public ASALLiteral(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree) {
		this(parent, tree, tree.getFirstToken());
	}
	
	public ASALLiteral(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, ASALToken token) {
		this(parent, tree, token.text);
	}
	
	public ASALLiteral(ASALSyntaxTreeAPI parent, ASALSyntaxTree tree, String tokenText) {
		super(parent, tree);
		
		text = tokenText;
	}
	
	public ASALLiteral(int i) {
		this("" + i, JInt.LITERAL.class);
	}
	
	public ASALLiteral(boolean b) {
		this(b ? "TRUE" : "FALSE", b ? JBool.TRUE.class : JBool.FALSE.class);
	}
	
	public static ASALLiteral _false() {
		return new ASALLiteral(false);
	}
	
	public static ASALLiteral _true() {
		return new ASALLiteral(true);
	}
	
	public static List<ASALLiteral> boolList(boolean... xs) {
		List<ASALLiteral> result = new ArrayList<ASALLiteral>();
		
		for (boolean x : xs) {
			result.add(new ASALLiteral(x));
		}
		
		return Collections.unmodifiableList(result);
	}
	
	public static ASALLiteral num(int i) {
		return new ASALLiteral(i);
	}
	
	public static List<ASALLiteral> numList(int... xs) {
		List<ASALLiteral> result = new ArrayList<ASALLiteral>();
		
		for (int x : xs) {
			result.add(num(x));
		}
		
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Creates a literal from the default value of a type.
	 */
	public ASALLiteral(JScope scope, Class<? extends JType> type) {
		super(null, null);
		
		JType defaultValue = JType.createDefaultValue(type);
		resolvedConstructor = defaultValue.getClass();
		text = JTypePrinter.toString(defaultValue, scope, false);
		
		setResolvedType(type);
	}
	
	private ASALLiteral(String text, Class<? extends JType> resolvedConstructor) {
		super(null, null);
		
		this.resolvedConstructor = resolvedConstructor;
		this.text = text;
		
		setResolvedType(JType.getType(resolvedConstructor));
	}
	
	public String getText() {
		return text;
	}
	
	public Class<? extends JType> getResolvedConstructor() {
		return resolvedConstructor;
	}
	
	@Override
	public void validateAndCrossRef(JScope scope, Class<? extends JType> expectedType) throws ASALException {
		try {
			resolvedConstructor = scope.getTypeLib().getConstructor(text, expectedType).getLegacy();
			setResolvedType(JType.getType(resolvedConstructor));
		} catch (ClassReflectionException e) {
			throw new ASALException(this, e.getMessage(), scope.getScopeSuggestions(false, false, false, true));
		}
	}
	
	@Override
	public String toText(TextOptions options) {
		return options.escapeChars(text);
	}
	
	@Override
	public Set<ASALVariable> getReferencedVars() {
		return Collections.emptySet();
	}
}
