package jlx.common.reflection;

@SuppressWarnings("serial")
public abstract class ReflectionException extends Exception {
	public ReflectionException(String msg) {
		super(msg);
	}
	
	public ReflectionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
