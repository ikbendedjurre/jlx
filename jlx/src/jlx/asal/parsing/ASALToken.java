package jlx.asal.parsing;

import java.util.*;

public class ASALToken implements Comparable<ASALToken> {
	public final ASALTokenType type;
	public final String text;
	public final int row;
	public final int column;
	
	/**
	 * First token that is encountered has index 0, second token has index 1, and so on.
	 */
	public final int index;
	
	private ASALToken(ASALTokenType type, String text, int row, int column, int index) {
		this.type = getActualType(type, text);
		this.text = text;
		this.row = row;
		this.column = column;
		this.index = index;
	}
	
	@Override
	public String toString() {
		switch (type) {
			case EOF:
				return String.format("code end at %d:1", row + 1);
			case STRING_LITERAL:
				return String.format("%s %s at %d:%d", type.name, text, row + 1, column + 1);
			default:
				return String.format("%s \"%s\" at %d:%d", type.name, text, row + 1, column + 1);
		}
	}
	
	private static ASALTokenType getActualType(ASALTokenType type, String text) {
		switch (type) {
			case IDENTIFIER:
				switch (text) {
					case "TRUE":
					case "FALSE":
						return ASALTokenType.BOOLEAN_LITERAL;
					default:
						if (ASALRules.SINGLETON.getKeywords().contains(text)) {
							return ASALTokenType.KEYWORD;
						} else {
							return ASALTokenType.IDENTIFIER;
						}
				}
			default:
				return type;
		}
	}
	
	private static boolean isSymbolStart(String s) {
		for (String symbol : ASALRules.SINGLETON.getSymbols()) {
			if (symbol.startsWith(s)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void tokenize(String code, int codeRow, List<ASALToken> destination) throws ASALTokenException {
		int currentTextStart = 0;
		String currentText = "";
		int state = 0;
		int pos = 0;
		
		while (pos < code.length()) {
			char c = code.charAt(pos);
			
			switch (state) {
				case 0:
					if (c == '_' || Character.isAlphabetic(c)) {
						currentTextStart = pos;
						currentText = "";
						state = 1000;
					} else {
						if (Character.isDigit(c)) {
							currentTextStart = pos;
							currentText = "";
							state = 2000;
						} else {
							if (c == '"') {
								currentTextStart = pos;
								currentText = "" + c;
								state = 3000;
								pos++;
							} else {
								if (Character.isWhitespace(c)) {
									pos++;
								} else {
									if (isSymbolStart("" + c)) {
										currentTextStart = pos;
										currentText = "" + c;
										state = 4000;
										pos++;
									} else {
										throw new ASALTokenException("Invalid character at " + (codeRow + 1) + ":" + (pos + 1) + " (" + c + ")!");
									}
								}
							}
						}
					}
					break;
				case 1000:
					if (c == '_' || Character.isAlphabetic(c) || Character.isDigit(c)) {
						currentText += c;
						pos++;
					} else {
						destination.add(new ASALToken(ASALTokenType.IDENTIFIER, currentText, codeRow, currentTextStart, destination.size()));
						currentTextStart = pos;
						currentText = "";
						state = 0;
					}
					break;
				case 2000:
					if (Character.isDigit(c)) {
						currentText += c;
						pos++;
					} else {
						destination.add(new ASALToken(ASALTokenType.NUMBER_LITERAL, currentText, codeRow, currentTextStart, destination.size()));
						currentTextStart = pos;
						currentText = "";
						state = 0;
					}
					break;
				case 3000:
					if (c == '"') {
						destination.add(new ASALToken(ASALTokenType.STRING_LITERAL, currentText + c, codeRow, currentTextStart, destination.size()));
						currentTextStart = pos;
						currentText = "";
						state = 0;
						pos++;
					} else {
						if (c == '\\') {
							state = 3100;
							pos++;
						} else {
							currentText += c;
							pos++;
						}
					}
					break;
				case 3100:
					switch (c) {
						case 't':
							currentText += "\t";
							state = 3000;
							pos++;
							break;
						case 'n':
							currentText += "\n";
							state = 3000;
							pos++;
							break;
						case '"':
							currentText += "\"";
							state = 3000;
							pos++;
							break;
						case '\\':
							currentText += "\\";
							state = 3000;
							pos++;
							break;
						default:
							throw new ASALTokenException("Invalid character at " + (codeRow + 1) + ":" + (pos + 1) + " (" + c + ")!");
					}
					break;
				case 4000:
					if (isSymbolStart(currentText + c)) {
						currentText += c;
						pos++;
					} else {
						destination.add(new ASALToken(ASALTokenType.SYMBOL, currentText, codeRow, currentTextStart, destination.size()));
						currentTextStart = pos;
						currentText = "";
						state = 0;
					}
					break;
				default:
					throw new Error("Invalid tokenizer state (" + state + ")!");
			}
		}
		
		switch (state) {
			case 1000:
				destination.add(new ASALToken(ASALTokenType.IDENTIFIER, currentText, codeRow, currentTextStart, destination.size()));
				break;
			case 2000:
				destination.add(new ASALToken(ASALTokenType.NUMBER_LITERAL, currentText, codeRow, currentTextStart, destination.size()));
				break;
			case 3000:
				throw new ASALTokenException("Incomplete string literal (" + currentText + ")!");
			case 4000:
				destination.add(new ASALToken(ASALTokenType.SYMBOL, currentText, codeRow, currentTextStart, destination.size()));
				break;
			default:
				break;
		}
	}
	
	public static List<ASALToken> tokenize(String... code) throws ASALTokenException {
		List<ASALToken> result = new ArrayList<ASALToken>();
		
		for (int index = 0; index < code.length; index++) {
			tokenize(code[index], index, result);
		}
		
		result.add(new ASALToken(ASALTokenType.EOF, "", code.length, 0, result.size()));
		return result;
	}
	
	public static List<ASALToken> tokenize(List<String> code) throws ASALTokenException {
		List<ASALToken> result = new ArrayList<ASALToken>();
		
		for (int index = 0; index < code.size(); index++) {
			tokenize(code.get(index), index, result);
		}
		
		result.add(new ASALToken(ASALTokenType.EOF, "", code.size(), 0, result.size()));
		return result;
	}
	
	@Override
	public int compareTo(ASALToken o) {
		return index - o.index;
	}
}

