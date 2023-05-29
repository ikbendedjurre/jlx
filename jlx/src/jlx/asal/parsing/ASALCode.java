package jlx.asal.parsing;

import java.util.*;

import jlx.asal.j.*;
import jlx.common.FileLocation;

public class ASALCode {
	private List<String> code;
	
	public final String rootRuleName;
	public final FileLocation fileLocation;
	public final JType jtype;
	
	public ASALCode(String rootRuleName, String code0, String... code) {
		this(rootRuleName, (JType)null, combine(code0, code));
	}
	
	public ASALCode(String rootRuleName, JType jtype) {
		this(rootRuleName, jtype, null);
	}
	
	public ASALCode(String rootRuleName, JType jtype, List<String> code) {
		this.rootRuleName = rootRuleName;
		this.jtype = jtype;
		this.code = code;
		
		fileLocation = new FileLocation();
	}
	
	private static List<String> combine(String code0, String... code) {
		List<String> result = new ArrayList<String>();
		result.add(code0);
		
		for (String c : code) {
			result.add(c);
		}
		
		return Collections.unmodifiableList(result);
	}
	
	public List<String> getCode() {
		return code != null ? code : Collections.emptyList();
	}

	public ASALSyntaxTree toSyntaxTree(JScope scope) throws ASALException {
		if (jtype != null) {
			code = JTypePrinter.toStringList(jtype, scope, true);
		}
		
		try {
			return new ASALTokens(this).applyRule(rootRuleName);
		} catch (ASALTokenException e) {
			throw new ASALException(this, e.getMessage(), Collections.emptyList());
		}
	}
}
