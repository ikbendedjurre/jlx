package jlx.printing;

import jlx.asal.parsing.api.*;

public interface IMCRL2Printer {
	public String _if(String condition, String thenBranch, String elseBranch);
	public String jintToInt(String s);
	public String intToJInt(String s);
	public String boolToJBool(String s);
	public String boolToJPulse(String s);
	public String jboolToPulse(String s);
	public String jboolToBool(String s);
	public String jpulseToJBool(String s);
	public String jpulseToBool(String s);
	public String literalToStr(ASALLiteral leaf, FlatTarget target);
}
