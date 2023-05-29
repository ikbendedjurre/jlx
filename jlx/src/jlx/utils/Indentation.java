package jlx.utils;

public class Indentation {
	private final String text;
	private final String sep;
	
	public Indentation(String sep) {
		text = "";
		
		this.sep = sep;
	}
	
	private Indentation(String text, String sep) {
		this.text = text;
		this.sep = sep;
	}
	
	public Indentation indent() {
		return new Indentation(text + sep, sep);
	}
	
	@Override
	public String toString() {
		return text;
	}
}

