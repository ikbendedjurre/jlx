package jlx.asal.parsing;

import java.util.*;

@SuppressWarnings("serial")
public class ASALException extends Exception {
	public ASALException(ASALSyntaxTreeAPI treeAPI, String msg) {
		this(treeAPI.getTree(), msg, Collections.emptyList());
	}
	
	public ASALException(ASALSyntaxTreeAPI treeAPI, String msg, List<String> scopeSuggestions) {
		this(treeAPI.getTree(), msg, scopeSuggestions);
	}
	
	public ASALException(ASALCode code, String msg, List<String> scopeSuggestions) {
		this(code, null, msg, scopeSuggestions);
	}
	
	private ASALException(ASALSyntaxTree tree, String msg, List<String> scopeSuggestions) {
		this(tree != null ? tree.getCode() : null, tree != null ? tree.getFirstToken() : null, msg, scopeSuggestions);
	}
	
	private ASALException(ASALCode code, ASALToken token, String msg, List<String> scopeSuggestions) {
		super(createMsg(code, token, msg, scopeSuggestions));
	}
	
	private static String createMsg(ASALCode code, ASALToken token, String msg, List<String> scopeSuggestions) {
		String result = msg;
		
		if (code != null) {
			if (code.fileLocation != null) {
				result += "\n\tCode location:";
				
				for (int i = 0; i < code.fileLocation.getStackTrace().size(); i++) {
					result += "\n\t\t" + code.fileLocation.getStackTrace().get(i);
				}
			} else {
				result += "\n\tCode:";
			}
			
			result += "\n\tCode:";
			
			for (int index = 0; index < code.getCode().size(); index++) {
				result += "\n\t\t" + (index + 1) + ": " + code.getCode().get(index);
				
				if (token != null && token.row == index) {
					result += "\n\t\t" + " ".repeat(("" + (index + 1)).length() + token.column + 2) + "^".repeat(token.text.length());
				}
			}
		}
		
		if (scopeSuggestions != null && scopeSuggestions.size() > 0) {
			result += "\n\tPerhaps you meant one of the following:";
			
			for (String scopeSuggestion : scopeSuggestions) {
				result += "\n\t\t" + scopeSuggestion;
			}
		}
		
		return result;
	}
}

