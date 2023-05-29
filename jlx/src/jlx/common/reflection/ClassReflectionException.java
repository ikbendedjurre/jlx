package jlx.common.reflection;

@SuppressWarnings("serial")
public class ClassReflectionException extends ReflectionException {
	public ClassReflectionException(Class<?> clz, String msg) {
		super(msg);
	}
	
	public ClassReflectionException(Class<?> clz, Throwable cause) {
		super(getMsg(clz), cause);
	}
	
	private static String getMsg(Class<?> clz) {
		return "Reflection on class " + clz.getCanonicalName() + " failed!";
	}
}
