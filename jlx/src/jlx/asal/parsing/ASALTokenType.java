package jlx.asal.parsing;

public enum ASALTokenType {
	UNKNOWN("<<unknown>>", "<<unknown>>"),
	EOF("EOF", "EOF"),
	STRING_LITERAL("string", "a string"),
	NUMBER_LITERAL("number", "a number"),
	BOOLEAN_LITERAL("boolean", "a boolean"),
	KEYWORD("keyword", "a keyword"),
	SYMBOL("symbol", "a symbol"),
	IDENTIFIER("identifier", "an identifier"),
	
	;
	
	public final String name;
	public final String some;
	
	private ASALTokenType(String name, String some) {
		this.name = name;
		this.some = some;
	}
	
	@Override
	public String toString() {
		return name;
	}

	public static ASALTokenType get(String tokenTypeName) {
		for (ASALTokenType type : values()) {
			if (type.name.equals(tokenTypeName)) {
				return type;
			}
		}
		
		throw new Error("Unknown token type (\"" + tokenTypeName + "\")!");
	}
}
