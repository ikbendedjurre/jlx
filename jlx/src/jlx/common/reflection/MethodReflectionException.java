package jlx.common.reflection;

import java.lang.reflect.*;

@SuppressWarnings("serial")
public class MethodReflectionException extends ReflectionException {
	public MethodReflectionException(Method method, String msg) {
		super(getMsg(method), new Exception(msg));
	}
	
	public MethodReflectionException(Method method, Throwable cause) {
		super(getMsg(method), cause);
	}
	
	private static String getMsg(Method m) {
		return "Reflection on method \"" + m.getName() + "\" failed!";
	}
}
