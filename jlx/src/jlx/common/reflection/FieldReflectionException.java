package jlx.common.reflection;

import java.lang.reflect.*;

@SuppressWarnings("serial")
public class FieldReflectionException extends ReflectionException {
	public final Field field;
	
	public FieldReflectionException(Field field, String msg) {
		super(getFieldDescription(field), new Exception(msg));
		
		this.field = field;
	}
	
	public FieldReflectionException(Field field, Throwable cause) {
		super(getFieldDescription(field), cause);
		
		this.field = field;
	}
	
	private static String getFieldDescription(Field f) {
		return "Reflection on field \"" + f.getName() + "\" of class " + f.getDeclaringClass().getCanonicalName() + " failed!";
	}
}
