package jlx.utils;

public abstract class TextOptions {
	public abstract String id(String varName);
	public abstract String escapeChars(String text);
	
	public final static TextOptions MINIMAL = new TextOptions() {
		@Override
		public String id(String varName) {
			return Texts.abbreviate(varName, "_");
		}
		
		@Override
		public String escapeChars(String text) {
			return text.replace("\n", "\\n");
		}
	};
	
	public final static TextOptions FULL = new TextOptions() {
		@Override
		public String id(String varName) {
			return varName;
		}
		
		@Override
		public String escapeChars(String text) {
			return text;
		}
	};
	
	public final static TextOptions GRAPHVIZ_MIN = new TextOptions() {
		@Override
		public String id(String varName) {
			return Texts.abbreviate(varName, "_");
		}
		
		@Override
		public String escapeChars(String text) {
			return text.replace("\"", "").replace("\n", "\\n");
		}
	};
	
	public final static TextOptions GRAPHVIZ_FULL = new TextOptions() {
		@Override
		public String id(String varName) {
			return varName;
		}
		
		@Override
		public String escapeChars(String text) {
			return text.replace("\"", "\\\"").replace("\n", "\\n");
		}
	};
	
	public final static TextOptions ALDEBARAN = new TextOptions() {
		@Override
		public String id(String varName) {
			return varName;
		}
		
		@Override
		public String escapeChars(String text) {
			return text.replace("\"", "").replace("\n", "; ");
		}
	};
	
	public final static TextOptions MCRL2 = new TextOptions() {
		@Override
		public String id(String varName) {
			return varName;
		}
		
		@Override
		public String escapeChars(String text) {
			return text.replace("\"", "").replace("\n", "_");
		}
	};
	
	private static TextOptions current = TextOptions.FULL;
	
	public static TextOptions current() {
		return current;
	}
	
	public static void select(TextOptions options) {
		current = options;
	}
	
//	public static 
	
//	MINIMAL(" ", "\"", false, true),
//	ONE_LINE(" ", "\"", false, false),
//	FORMATTED_MIN("\\n", "\"", true, true),
//	FORMATTED_FULL("\\n", "\"", true, false),
//	DOT_MIN(" ", "\\\"", false, true),
//	DOT_ONE_LINE(" ", "\\\"", false, false),
//	DOT_FULL("\\n", "\\\"", false, false),
//	
//	;
//	
//	private String lineSep;
//	private String quote;
//	private boolean indent;
//	private boolean abbreviateIdentifiers;
//	
//	private LOD(String lineSep, String quote, boolean indent, boolean abbreviateIdentifiers) {
//		this.lineSep = lineSep;
//		this.quote = quote;
//		this.indent = indent;
//		this.abbreviateIdentifiers = abbreviateIdentifiers;
//	}
//	
//	public LOD toOneLine() {
//		switch (this) {
//			case MINIMAL:
//				return MINIMAL;
//			case ONE_LINE:
//				return ONE_LINE;
//			case FORMATTED_MIN:
//				return MINIMAL;
//			case FORMATTED_FULL:
//				return ONE_LINE;
//			case DOT_MIN:
//				return DOT_MIN;
//			case DOT_ONE_LINE:
//				return DOT_ONE_LINE;
//			case DOT_FULL:
//				return DOT_ONE_LINE;
//			default:
//				throw new Error("Should not happen!");
//		}
//	}
//	
//	/**
//	 * i <=> indentation
//	 */
//	public String lineSep(String i) {
//		return lineSep + (indent ? i : "");
//	}
//	
//	public String replaceQuotes(String text) {
//		return text.replace("\"", quote);
//	}
//	
//	public boolean abbreviateIdentifiers() {
//		return abbreviateIdentifiers;
//	}
	
	
}
