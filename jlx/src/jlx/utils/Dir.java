package jlx.utils;

public enum Dir {
	IN("IN"),
	OUT("OUT"),
	
	;
	
	public final String text;
	
	private Dir(String text) {
		this.text = text;
	}
	
	public Dir getOpposite() {
		return this == IN ? OUT : IN;
	}
	
	@Override
	public String toString() {
		return text;
	}
}
