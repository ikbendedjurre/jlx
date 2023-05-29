package jlx.asal.parsing;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.behave.proto.DeuteroTransition;

public class ASALRules {
	private List<ASALRule> rules;
	private Set<String> keywords;
	private Set<String> symbols;
	
	public final static ASALRules SINGLETON = new ASALRules();
	
	private ASALRules() {
		initRules();
		initKeywordsAndSymbols();
	}
	
	private void initRules() {
		rules = new ArrayList<ASALRule>();
		
		addRule(DeuteroTransition.class, "TRANSITION -> `event=EVENT [ `guard=EXPR ] / `statement=STATS1");
		addRule(DeuteroTransition.class, "TRANSITION -> `event=EVENT / `statement=STATS1");
		addRule(DeuteroTransition.class, "TRANSITION -> Entry / `statement=STATS1");
		addRule(DeuteroTransition.class, "TRANSITION -> Exit / `statement=STATS1");
		addRule(DeuteroTransition.class, "TRANSITION -> [ `guard=EXPR ] / `statement=STATS1");
		addRule(DeuteroTransition.class, "TRANSITION -> / `statement=STATS1");
		addRule(DeuteroTransition.class, "TRANSITION -> `statement=STATS1");
		addRule(ASALTrigger.class, "EVENT -> when ( `expr=EXPR )");
		//addRule(ASALTimeout.class, "EVENT -> after ( `duration=EXPR )");
		addRule(ASALTimeout.class, "EVENT -> after ( `[durationPort:identifier] )");
		addRule(ASALFinalized.class, "EVENT -> finalized");
		addRule(ASALCall.class, "EVENT -> `[methodName:identifier]");
		
		// Splitting outer sequences of statements from nested sequences of statements
		// produces better parser messages!
		
		// Outer sequences of statements:
		addRule(ASALSeqStatement.class, "STATS1 -> `statement=STAT `successor=STATS2 `[eof:EOF]");
		addRule(null, "STATS1 -> `[self]=STAT `[eof:EOF]");
		addRule(ASALEmptyStatement.class, "STATS1 -> `[eof:EOF]");
		
		// Nested sequences of statements:
		addRule(ASALSeqStatement.class, "STATS2 -> `statement=STAT `successor=STATS2");
		addRule(null, "STATS2 -> `[self]=STAT");
		addRule(ASALEmptyStatement.class, "STATS2 ->");
		
		addRule(null, "STAT -> if `[self]=IFSTAT");
		addRule(ASALIfStatement.class, "IFSTAT -> `condition=EXPR then `thenBranch=STATS2 else `elseBranch=STATS2 end if");
		addRule(ASALIfStatement.class, "IFSTAT -> `condition=EXPR then `thenBranch=STATS2 elseif `elseBranch=IFSTAT");
		addRule(ASALIfStatement.class, "IFSTAT -> `condition=EXPR then `thenBranch=STATS2 end if");
		
		addRule(ASALWhileStatement.class, "STAT -> while `condition=EXPR do `body=STATS2 end while");
		addRule(ASALAssignStatement.class, "STAT -> `[ref:identifier] := `expr=EXPR ;");
		addRule(ASALReturnStatement.class, "STAT -> return `expr=EXPR ;");
		addRule(ASALFunctionCallStatement.class, "STAT -> `[fct:identifier] ( `params=EXPR_LIST ) ;");
		addRule(ASALFunctionCallStatement.class, "STAT -> `[fct:identifier] ( ) ;");
		
		addRule(ASALSeqExpr.class, "EXPR_LIST -> `expr=EXPR , `tail=EXPR_LIST");
		addRule(ASALSeqExpr.class, "EXPR_LIST -> `expr=EXPR");
		
		addRule(null, "EXPR -> `[self]=LOGIC_EXPR_1");
		
		addRule(null, "LOGIC_EXPR_1 -> `[self]=EQ_EXPR_1 `[self]=LOGIC_EXPR_2*");
		addRule(ASALBinaryExpr.class, "LOGIC_EXPR_2 -> `lhs=[last] `op=LOGIC_OP `rhs=EQ_EXPR_1");
		addRule(null, "LOGIC_OP || and or");
		
		addRule(null, "EQ_EXPR_1 -> `[self]=ADD_EXPR_1 `[self]=EQ_EXPR_2*");
		addRule(ASALBinaryExpr.class, "EQ_EXPR_2 -> `lhs=[last] `op=EQ_OP `rhs=ADD_EXPR_1");
		addRule(null, "EQ_OP || = <> <= >= < >");
		
		addRule(null, "ADD_EXPR_1 -> `[self]=MULT_EXPR_1 `[self]=ADD_EXPR_2*");
		addRule(ASALBinaryExpr.class, "ADD_EXPR_2 -> `lhs=[last] `op=ADD_OP `rhs=MULT_EXPR_1");
		addRule(null, "ADD_OP || + -");
		
		addRule(null, "MULT_EXPR_1 -> `[self]=POW_EXPR_1 `[self]=MULT_EXPR_2*");
		addRule(ASALBinaryExpr.class, "MULT_EXPR_2 -> `lhs=[last] `op=MULT_OP `rhs=POW_EXPR_1");
		addRule(null, "MULT_OP || * / %");
		
		addRule(null, "POW_EXPR_1 -> `[self]=OPERAND `[self]=POW_EXPR_2*");
		addRule(ASALBinaryExpr.class, "POW_EXPR_2 -> `lhs=[last] `op=POW_OP `rhs=OPERAND");
		addRule(null, "POW_OP || ^");
		
		addRule(null, "OPERAND -> ( `[self]=EXPR )");
		addRule(ASALUnaryExpr.class, "OPERAND -> - `expr=OPERAND");
		addRule(ASALUnaryExpr.class, "OPERAND -> + `expr=OPERAND");
		addRule(ASALUnaryExpr.class, "OPERAND -> not `expr=OPERAND");
		addRule(ASALUnaryExpr.class, "OPERAND -> NOT `expr=OPERAND");
		addRule(ASALTernaryExpr.class, "OPERAND -> if `condition=OPERAND then `lhs=OPERAND else `rhs=OPERAND end if");
		addRule(ASALLiteral.class, "OPERAND -> `[value:boolean]");
		addRule(ASALLiteral.class, "OPERAND -> `[value:number]");
		addRule(ASALLiteral.class, "OPERAND -> `[value:string]");
		addRule(ASALFunctionCall.class, "OPERAND -> `[fct:identifier] ( `params=EXPR_LIST )");
		addRule(ASALFunctionCall.class, "OPERAND -> `[fct:identifier] ( )");
		addRule(ASALVarRef.class, "OPERAND -> `[ref:identifier]");
	}
	
	private void addRule(Class<? extends ASALSyntaxTreeAPI> clz, String def) {
		rules.add(new ASALRule(def, clz));
	}
	
	private static boolean isKeyword(String s) {
		if (!Character.isJavaIdentifierStart(s.charAt(0))) {
			return false;
		}
		
		for (int idx = 0; idx < s.length(); idx++) {
			if (!Character.isJavaIdentifierPart(s.charAt(idx))) {
				return false;
			}
		}
		
		return true;
	}
	
	private void initKeywordsAndSymbols() {
		keywords = new HashSet<String>();
		symbols = new HashSet<String>();
		
		for (ASALRule rule : rules) {
			String[] split = rule.getDef().split(" ");
			
			for (int index = 2; index < split.length; index++) {
				String s = split[index];
				
				if (s.charAt(0) != '`') {
					if (isKeyword(s)) {
						keywords.add(s);
					} else {
						symbols.add(s);
					}
				}
			}
		}
	}
	
	public Set<String> getKeywords() {
		return keywords;
	}
	
	public Set<String> getSymbols() {
		return symbols;
	}
	
	private static String indentation = null;
	
	public void println(String s) {
		System.out.println(indentation + s);
	}
	
	public ASALRuleApplication applyRule(String ruleName, ASALTokens tokens, ASALRuleOpportunities missedOppsDest, ASALRuleApplication lastApp) {
		ASALRuleOpportunities newMissedOpps = new ASALRuleOpportunities();
		String prefix = ruleName + " ";
		int matchCount = 0;
		
		for (ASALRule rule : rules) {
			if (rule.getDef().startsWith(prefix)) {
				matchCount++;
				ASALRuleApplication app = new ASALRuleApplication(this, tokens, lastApp);
				
				if (indentation != null) {
					println("Rule " + rule);
					println("Location is " + tokens.getFirstStr());
					indentation += "\t";
				}
				
				if (app.applyRule(rule)) {
					if (indentation != null) {
						indentation = indentation.substring(1);
						println("Rule " + rule + " succeeded!");
					}
					
					return app;
				}
				
				if (indentation != null) {
					int oldSize = newMissedOpps.getSize();
					newMissedOpps.addAll(app.getMissedOpportunities());
					int newSize = newMissedOpps.getSize();
					
					indentation = indentation.substring(1);
					println("Rule " + rule + " failed (" + oldSize + " U " + app.getMissedOpportunities().getSize() +  " = " + newSize + ")!");
				} else {
					newMissedOpps.addAll(app.getMissedOpportunities());
				}
			}
		}
		
		if (matchCount == 0) {
			throw new Error("Unknown rule (\"" + ruleName + "\")!");
		}
		
//		if (missedOppsDest.isEmpty()) {
			missedOppsDest.addAll(newMissedOpps);
//		}
		
		return null;
	}
}
