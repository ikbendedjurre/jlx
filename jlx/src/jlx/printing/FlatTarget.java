package jlx.printing;

import jlx.asal.j.*;

public enum FlatTarget {
	RAW_MCRL2,
	JBOOL,
	JPULSE,
	JTYPE,
	
	;
	
	public static FlatTarget fromType(Class<? extends JType> type) {
		if (type.equals(JBool.class)) {
			return JBOOL;
		}
		
		if (type.equals(JPulse.class)) {
			return JPULSE;
		}
		
		return JTYPE;
	}
}
