package jlx.asal.parsing;

public class ASALRuleApplication {
	private boolean applied;
	private ASALRuleOpportunities missedOpportunities;
	private ASALRuleApplication lastApp;
	private ASALSyntaxTree tree;
	private ASALTokens tokens;
	
	public ASALRuleApplication(ASALRules rules, ASALTokens tokens, ASALRuleApplication lastApp) {
		missedOpportunities = new ASALRuleOpportunities();
		tree = new ASALSyntaxTree(tokens.getCode());
		
		this.tokens = new ASALTokens(tokens);
		this.lastApp = lastApp;
	}
	
	private ASALRuleApplication(ASALRuleApplication source) {
		copyFrom(source);
	}
	
	private void copyFrom(ASALRuleApplication source) {
		missedOpportunities = new ASALRuleOpportunities(source.missedOpportunities);
		tree = new ASALSyntaxTree(source.tree, null);
		tokens = new ASALTokens(source.tokens);
	}
	
	public ASALRuleOpportunities getMissedOpportunities() {
		return missedOpportunities;
	}
	
	public ASALSyntaxTree getTree() {
		return tree;
	}
	
	public ASALTokens getTokens() {
		return tokens;
	}
	
	private boolean handleFragment(String fragment) {
		//By default, a fragment demands a literal token.
		//This can be escaped with the character `: 
		if (fragment.charAt(0) != '`') {
			ASALToken token = tokens.next();
			
			if (token.text.equals(fragment)) {
				tree.setFirstToken(token);
				return true;
			}
			
			missedOpportunities.add(token, "\"" + fragment + "\"");
			return false;
		}
		
		fragment = fragment.substring(1);
		
		//Fragments can demand token TYPES (as opposed to token texts),
		//and store the text of a consumed token in a field.
		//Syntax for this is `[<field>:<token_type>]
		if (fragment.startsWith("[") && fragment.endsWith("]")) {
			fragment = fragment.substring(1, fragment.length() - 1);
			
			if (fragment.contains(":") && !fragment.contains("=")) {
				String[] split = fragment.split(":");
				
				if (split.length != 2) {
					throw new Error("Invalid compiler command (\"" + fragment + "\")!");
				}
				
				ASALToken token = tokens.next();
				ASALTokenType tokenType = ASALTokenType.get(split[1]);
				
				if (token.type == tokenType) {
					tree.setProperty(split[0], token.text);
					tree.setFirstToken(token);
					return true;
				}
				
				missedOpportunities.add(token, tokenType.some);
				return false;
			}
			
			//Similarly, fields can be set manually; for example, `[<field>=<value>].
			if (!fragment.contains(":") && fragment.contains("=")) {
				String[] split = fragment.split("=");
				
				if (split.length != 2) {
					throw new Error("Invalid compiler command (\"" + fragment + "\")!");
				}
				
				tree.setProperty(split[0], split[1]);
				return true;
			}
			
			if (fragment.contains(":") && fragment.contains("=")) {
				throw new Error("Invalid compiler command (\"" + fragment + "\")!");
			}
			
			ASALToken token = tokens.next();
			
			if (token.type != ASALTokenType.EOF) {
				tree.setProperty(fragment, token.text);
				tree.setFirstToken(token);
				return true;
			}
			
			missedOpportunities.add(token, "some token");
			return false;
		}
		
		//Otherwise, it must be a subrule call.
		//Subrule calls have a left-hand side and a right-hand side, e.g. `<lhs>=<rhs>
		String[] split = fragment.split("=");
		
		if (split.length != 2) {
			throw new Error("Invalid compiler command (\"" + fragment + "\")!");
		}
		
		String key = split[0];
		String value = split[1];
		
		ASALRuleApplication app;
		
		//If the value has the form [<value>], then it is a predefined value:
		if (value.startsWith("[") && value.endsWith("]")) {
			value = value.substring(1, value.length() - 1);
			
			switch (value) {
				case "last":
					if (lastApp == null) {
						throw new Error("No previous AST node found (\"" + fragment + "\")!");
					}
					
					app = lastApp;
					missedOpportunities = new ASALRuleOpportunities(lastApp.missedOpportunities);
					break;
				default:
					throw new Error("Invalid compiler command (\"" + fragment + "\")!");
			}
		} else {
			if (value.endsWith("*")) {
				String newFragment = "`" + key + "=" + value.substring(0, value.length() - 1);
				ASALRuleApplication backup = new ASALRuleApplication(this);
				
				while (handleFragment(newFragment)) {
					backup = new ASALRuleApplication(this);
				}
				
				ASALRuleOpportunities opps = new ASALRuleOpportunities(missedOpportunities);
				copyFrom(backup);
				missedOpportunities.addAll(opps);
				return true;
			}
			
			app = ASALRules.SINGLETON.applyRule(value, tokens, missedOpportunities, lastApp);
			
			if (app == null) {
				return false;
			}
		}
		
		//If the key has the form [<key>], then it is a predefined key:
		if (key.startsWith("[") && key.endsWith("]")) {
			key = key.substring(1, key.length() - 1);
			
			switch (key) {
				case "self":
					tree = app.tree;
					break;
				default:
					throw new Error("Invalid compiler command (\"" + fragment + "\")!");
			}
		} else {
			app.tree.setParent(tree);
			tree.put(key, app.tree);
			tree.setFirstToken(app.tree.getFirstToken());
		}
		
		tokens = app.getTokens();
		missedOpportunities = app.getMissedOpportunities();
		lastApp = app;
		return true;
	}
	
	public boolean applyRule(ASALRule rule) {
		if (applied) {
			throw new Error("Cannot repeat rule application (\"" + rule + "\")!");
		}
		
		applied = true;
		
		String[] split = rule.getDef().split(" ");
		tree.setRule(rule);
		
		if (split.length < 2) {
			throw new Error("Invalid compiler rule (\"" + rule + "\")!");
		}
		
		tree.setProperty("type", split[0]);
		
		switch (split[1]) {
			case "->":
				for (int index = 2; index < split.length; index++) {
					if (!handleFragment(split[index])) {
						return false;
					}
				}
				return true;
			case "||":
				ASALRuleApplication backup = new ASALRuleApplication(this);
				ASALRuleOpportunities allOpps = new ASALRuleOpportunities();
				
				for (int index = 2; index < split.length; index++) {
					if (handleFragment(split[index])) {
						return true;
					}
					
					allOpps.addAll(missedOpportunities);
					copyFrom(backup);
				}
				
				missedOpportunities.addAll(allOpps);
				return false;
			default:
				throw new Error("Invalid compiler rule (\"" + rule + "\")!");
		}
	}
}
