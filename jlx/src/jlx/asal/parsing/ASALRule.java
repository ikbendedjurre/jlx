package jlx.asal.parsing;

public class ASALRule {
	private String rule;
	private Class<? extends ASALSyntaxTreeAPI> clz;
	
	public ASALRule(String rule, Class<? extends ASALSyntaxTreeAPI> clz) {
		this.rule = rule;
		this.clz = clz;
	}
	
	public String getDef() {
		return rule;
	}
	
	public Class<? extends ASALSyntaxTreeAPI> getClz() {
		return clz;
	}
	
	@Override
	public String toString() {
		return rule;
	}
}
