package jlx.common.reflection;

import java.lang.reflect.Constructor;

@SuppressWarnings("serial")
public class ConstructorReflectionException extends ReflectionException {
	public ConstructorReflectionException(Constructor<?> c, String msg) {
		super(getMsg(c), new Exception(msg));
	}
	
	public ConstructorReflectionException(Constructor<?> c, Throwable cause) {
		super(getMsg(c), cause);
	}
	
	private static String getMsg(Constructor<?> c) {
		return "Reflection on constructor " + c.toGenericString() + " failed!";
	}
}
