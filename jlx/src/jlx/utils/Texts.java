package jlx.utils;

import java.util.*;
import java.util.function.Function;

public class Texts {
	public static String substr(String s, int frontRemoveCount, int backRemoveCount) {
		return s.substring(frontRemoveCount, s.length() - backRemoveCount);
	}
	
	public static <T> String concat(Collection<T> elems, String sep) {
		if (elems.isEmpty()) {
			return "";
		}
		
		Iterator<T> q = elems.iterator();
		String result = q.next().toString();
		
		while (q.hasNext()) {
			result += sep + q.next().toString();
		}
		
		return result;
	}
	
	public static <T> String concat(Collection<T> elems, String sep, Function<T, String> f) {
		if (elems.isEmpty()) {
			return "";
		}
		
		Iterator<T> q = elems.iterator();
		String result = f.apply(q.next());
		
		while (q.hasNext()) {
			result += sep + f.apply(q.next());
		}
		
		return result;
	}
	
	public static String _break(String x, String sep, int n) {
		if (x.length() > n) {
			int break_n = Math.max(n, x.lastIndexOf('\\', n - 1) + 2);
			return x.substring(0, break_n) + sep + _break(x.substring(break_n), sep, n);
		}
		
		return x;
	}
	
//	public static String _break(List<String> xs, String sep, int n) {
////		if (x.length() > n) {
////			int break_n = Math.max(n, x.lastIndexOf('\\', n - 1) + 2);
////			return x.substring(0, break_n) + sep + _break(x.substring(break_n), sep, n);
////		}
////		
////		return x;
//	}
	
	public static String toJavaId(String s) {
		if (s.isEmpty()) {
			return "_";
		}
		
		String result;
		
		{
			char c = s.charAt(0);
			
			if (Character.isJavaIdentifierStart(c)) {
				result = "" + c;
			} else {
				result = "_";
			}
		}
		
		for (int index = 1; index < s.length(); index++) {
			char c = s.charAt(index);
			
			if (Character.isJavaIdentifierPart(c)) {
				result += c;
			} else {
				result += "_";
			}
		}
		
		return result;
	}
	
	public static String indent(String s) {
		String result = "";
		String indentation = "";
		
		for (int index = 0; index < s.length(); index++) {
			switch (s.charAt(index)) {
				case '(':
					indentation += "\t";
					result += "(\n" + indentation;
					break;
				case ')':
					indentation = indentation.substring(0, indentation.length() - 1);
					result += "\n" + indentation + ")";
					break;
				default:
					result += s.charAt(index);
					break;
			}
		}
		
		return result;
	}
	
	public static String abbreviate(String varName, String cutOff) {
		int index = varName.indexOf(cutOff);
		
		if (index > 0) {
			return varName.substring(0, index);
		}
		
		return varName;
	}
	
	public static String toValidIdentifier(String s, char escapeChar) {
		String result = "";
		
		for (int index = 0; index < s.length(); index++) {
			char c = s.charAt(index);
			
			if (Character.isLetter(c) || c == '_' || (Character.isDigit(c) && index > 0)) {
				if (s.charAt(index) == escapeChar) {
					result += escapeChar;
					result += escapeChar;
				} else {
					result += c;
				}
			} else {
				result += escapeChar;
				result += String.valueOf((int)c);
			}
		}
		
		return result;
	}
	
	public static List<String> sorted(Collection<String> elems) {
		List<String> sortedElems = new ArrayList<String>(elems);
		Collections.sort(sortedElems);
		return sortedElems;
	}
}



