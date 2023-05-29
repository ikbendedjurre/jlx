package jlx.common.reflection;

import jlx.common.FileLocation;

@SuppressWarnings("serial")
public class InstanceReflectionException extends ReflectionException {
	public InstanceReflectionException(Object instance, String msg) {
		this(instance, null, msg, null);
	}
	
	public InstanceReflectionException(Object instance, Throwable cause) {
		this(instance, null, null, cause);
	}
	
	public InstanceReflectionException(Object instance, FileLocation fileLocation, String msg) {
		this(instance, fileLocation, msg, null);
	}
	
	public InstanceReflectionException(Object instance, FileLocation fileLocation, Throwable cause) {
		this(instance, fileLocation, null, cause);
	}
	
	private InstanceReflectionException(Object instance, FileLocation fileLocation, String msg, Throwable cause) {
		super(getMsg(instance, fileLocation, msg), cause);
	}
	
	private static String getMsg(Object instance, FileLocation fileLocation, String msg) {
		String result;
		Class<?> clz = instance.getClass();
		
		if (msg != null) {
			result = msg;
		} else {
			result = "Reflection on an instance of " + clz.getCanonicalName() + " failed!";
		}
		
		if (fileLocation != null) {
			result += "\nInstance created at " + fileLocation;
		}
		
		return result;
	}
}
