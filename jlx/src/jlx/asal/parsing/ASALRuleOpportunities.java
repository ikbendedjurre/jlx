package jlx.asal.parsing;

import java.util.*;

public class ASALRuleOpportunities {
	private Map<ASALToken, Set<String>> opportunitiesPerToken;
	private List<ASALToken> sortedTokens;
	private int size;
	
	public ASALRuleOpportunities() {
		opportunitiesPerToken = new HashMap<ASALToken, Set<String>>();
		sortedTokens = new ArrayList<ASALToken>();
		size = 0;
	}
	
	public ASALRuleOpportunities(ASALRuleOpportunities source) {
		this();
		
		addAll(source);
	}
	
	public boolean isEmpty() {
		return opportunitiesPerToken.isEmpty();
	}
	
	public int getSize() {
		return size;
	}
	
	private void addSortedToken(ASALToken token) {
		for (int index = 0; index < sortedTokens.size(); index++) {
			if (sortedTokens.get(index).index <= token.index) {
				sortedTokens.add(index, token);
				return;
			}
		}
		
		sortedTokens.add(token);
	}
	
	private Set<String> getTokenOpportunities(ASALToken token) {
		Set<String> v = opportunitiesPerToken.get(token);
		
		if (v == null) {
			v = new HashSet<String>();
			opportunitiesPerToken.put(token, v);
			addSortedToken(token);
		}
		
		return v;
	}
	
	public void add(ASALToken token, String s) {
		if (getTokenOpportunities(token).add(s)) {
			size++;
		}
	}
	
	public void add(ASALToken token, Set<String> s) {
		for (String item : s) {
			add(token, item);
		}
	}
	
	public void addAll(ASALRuleOpportunities other) {
		for (Map.Entry<ASALToken, Set<String>> entry : other.opportunitiesPerToken.entrySet()) {
			add(entry.getKey(), entry.getValue());
		}
	}
	
	public List<String> getSuggestions() {
		List<String> result = new ArrayList<String>();
		
		for (ASALToken token : sortedTokens) {
			for (String s : opportunitiesPerToken.get(token)) {
				result.add("Perhaps you meant " + s + " instead of " + token + "?");
				
				if (result.size() >= 100) {
					result.add(". . .");
					return result;
				}
			}
		}
		
		return result;
	}
}
