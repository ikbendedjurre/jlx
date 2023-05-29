package jlx.asal.parsing;

import java.util.*;

public class ASALTokens {
	private ASALCode code;
	private List<ASALToken> tokens;
	
	public ASALTokens(ASALCode code) throws ASALTokenException {
		this.code = code;
		this.tokens = ASALToken.tokenize(code.getCode());
	}
	
	public ASALTokens(ASALTokens source) {
		code = source.code;
		tokens = new ArrayList<ASALToken>(source.tokens);
	}
	
	public ASALSyntaxTree applyRule(String ruleName) throws ASALException {
		ASALRuleOpportunities missedOpportunities = new ASALRuleOpportunities();
		ASALRuleApplication app = ASALRules.SINGLETON.applyRule(ruleName, this, missedOpportunities, null);
		
		if (app != null) {
			tokens.clear();
			tokens.addAll(app.getTokens().tokens);
			return app.getTree();
		}
		
		throw new ASALException(code, "Could not be parsed!", missedOpportunities.getSuggestions());
	}
	
	public ASALCode getCode() {
		return code;
	}
	
	public boolean hasNext() {
		return tokens.get(0).type != ASALTokenType.EOF;
	}
	
	public ASALToken next() {
		if (hasNext()) {
			return tokens.remove(0);
		}
		
		return tokens.get(0);
	}
	
	public String getFirstStr() {
		return tokens.get(0).toString();
	}
	
	public void print() {
		for (ASALToken token : tokens) {
			System.out.println(token.toString());
		}
	}
}
