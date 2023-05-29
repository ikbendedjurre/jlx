package jlx.common;

import java.lang.reflect.*;

import jlx.asal.j.JType;
import jlx.common.reflection.*;

public class ReflectionUtils {
	public final static boolean isStatic(Field f) {
		return Modifier.isStatic(f.getModifiers());
	}
	
	public final static boolean isAbstract(Class<?> clz) {
		return Modifier.isAbstract(clz.getModifiers());
	}
	
	public final static boolean isFinal(Class<?> clz) {
		return Modifier.isFinal(clz.getModifiers());
	}
	
	public final static boolean isJType(Field f) {
		return JType.class.isAssignableFrom(f.getType());
	}
	
	public final static boolean isPrimitive(Field f) {
		return f.getType().isPrimitive();
	}
	
	/**
	 * Checks if a class can be accessed+constructed from anywhere in the program.
	 * Construction does not have to occur with the default constructor!
	 */
	public final static String getIndependentConstructionProblem(Class<?> clz) {
		int mod = clz.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			return "Should be public!";
		}
		
		if (Modifier.isAbstract(mod)) {
			return "Should not be abstract!";
		}
		
		if (clz.getTypeParameters().length > 0) {
			return "Should have no type parameters!";
		}
		
		if (Modifier.isInterface(mod)) {
			return "Should not be an interface!";
		}
		
		if (clz.isArray()) {
			return "Should not be an array!";
		}
		
		if (clz.isAnonymousClass()) {
			return "Should not anonymous!";
		}
		
		if (!Modifier.isStatic(mod) && clz.getDeclaringClass() != null) {
			return "Should be static or not nested inside another class!";
		}
		
		//Note that we count the number of PUBLIC constructors!
		if (clz.getConstructors().length == 0) {
			return "Should have public default constructor!";
		}
		
		return null;
	}
	
	public final static boolean hasDefaultConstructor(Class<?> clz) {
		try {
			return Modifier.isPublic(clz.getConstructor().getModifiers());
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}
	
	public final static String getImmutableFieldProblem(Field f) {
		int mod = f.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			return "Should be public!";
		}
		
		if (!Modifier.isFinal(mod)) {
			return "Should be final!";
		}
		
		if (f.getType().isArray()) {
			return "Should not be an array!";
		}
		
		return null;
	}
	
	public static <T> Class<?> getLastSuperClz(Class<?> clz, Class<T> justBeforeClz) throws ClassReflectionException {
		if (clz.getSuperclass() == null) {
			return null;
		}
		
		if (justBeforeClz.isAssignableFrom(ReflectionUtils.getRawSuperclass(clz))) {
			return clz;
		}
		
		return getLastSuperClz(clz.getSuperclass(), justBeforeClz);
	}
	
	public static Class<?> getRawSuperclass(Class<?> clz) throws ClassReflectionException {
		Type fieldGenericType = clz.getGenericSuperclass();
		
		if (!(fieldGenericType instanceof ParameterizedType)) {
			return clz;
		}
		
		ParameterizedType pt = (ParameterizedType)fieldGenericType;
		
		if (!(pt.getRawType() instanceof Class)) {
			throw new ClassReflectionException(clz, "Class supertype should be a class!");
		}
		
		return Class.class.cast(pt.getRawType());
	}
	
	public static Class<?> getSuperclassTypeParam(Class<?> clz) throws ClassReflectionException {
		Type fieldGenericType = clz.getGenericSuperclass();
		
		if (!(fieldGenericType instanceof ParameterizedType)) {
			throw new ClassReflectionException(clz, "Class supertype should be parameterized!");
		}
		
		ParameterizedType pt = (ParameterizedType)fieldGenericType;
		Type[] typeArgs = pt.getActualTypeArguments();
		
		if (typeArgs.length != 1) {
			throw new ClassReflectionException(clz, "Class supertype should have 1 type parameter, not " + typeArgs.length + "!");
		}
		
		if (!(typeArgs[0] instanceof Class)) {
			throw new ClassReflectionException(clz, "Class supertype should have a type parameter that is a class!");
		}
		
		return Class.class.cast(typeArgs[0]);
	}
	
	public static Class<?> getTypeParam(Field f) throws FieldReflectionException {
		Type fieldGenericType = f.getGenericType();
		
		if (!(fieldGenericType instanceof ParameterizedType)) {
			throw new FieldReflectionException(f, "Field type should be parameterized!");
		}
		
		ParameterizedType pt = (ParameterizedType)fieldGenericType;
		Type[] typeArgs = pt.getActualTypeArguments();
		
		if (typeArgs.length != 1) {
			throw new FieldReflectionException(f, "Field type should have 1 type parameter, not " + typeArgs.length + "!");
		}
		
		if (!(typeArgs[0] instanceof Class)) {
			throw new FieldReflectionException(f, "Field type should have a type parameter that is a class!");
		}
		
		return Class.class.cast(typeArgs[0]);
	}
	
	public static boolean isObjectMethod(Method m) {
		return m.getDeclaringClass().equals(Object.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createStdInstance(Class<T> clz) throws ReflectionException {
		int m = clz.getModifiers();
		
		try {
			if (!Modifier.isStatic(m) && clz.getDeclaringClass() != null) {
				Constructor<?> c = clz.getConstructor(clz.getDeclaringClass());
				return (T)c.newInstance(createStdInstance(clz.getDeclaringClass()));
			} else {
				return clz.getConstructor().newInstance();
			}
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ClassReflectionException(clz, e);
		}
	}
	
	public static <T> T getFieldValue(Object instance, Field f, Class<T> targetClz) throws ReflectionException {
		try {
			return targetClz.cast(f.get(instance));
		} catch (ClassCastException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new FieldReflectionException(f, e);
		}
	}
}
