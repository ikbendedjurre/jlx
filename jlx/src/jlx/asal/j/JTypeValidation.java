package jlx.asal.j;

import java.lang.reflect.*;

import jlx.common.ReflectionUtils;
import jlx.common.reflection.*;

public class JTypeValidation {
	public static void check(Class<? extends JType> type) throws ClassReflectionException {
		Class<?> lastSuperClz = ReflectionUtils.getLastSuperClz(type, JUserType.class);
		
		if (lastSuperClz == null) {
			throw new ClassReflectionException(type, "Should inherit from " + JUserType.class.getCanonicalName() + "<" + type.getCanonicalName() + ">!");
		}
		
		if (!ReflectionUtils.getSuperclassTypeParam(lastSuperClz).equals(lastSuperClz)) {
			throw new ClassReflectionException(type, "Should have superclass " + JUserType.class.getCanonicalName() + "<" + lastSuperClz.getCanonicalName() + ">!");
		}
		
		if (ReflectionUtils.getIndependentConstructionProblem(type) != null) {
			throw new ClassReflectionException(type, ReflectionUtils.getIndependentConstructionProblem(type));
		}
		
		if (!ReflectionUtils.hasDefaultConstructor(type)) {
			throw new ClassReflectionException(type, "Should have default constructor!");
		}
		
		if (ReflectionUtils.isFinal(type)) {
			throw new ClassReflectionException(type, "Should not be final!");
		}
		
		for (Field f : type.getFields()) {
			try {
				checkStdValueField(f);
			} catch (FieldReflectionException e) {
				throw new ClassReflectionException(type, e);
			}
		}
		
		for (Class<?> c : type.getDeclaredClasses()) {
			try {
				checkConstructorClz(c);
			} catch (ClassReflectionException e) {
				throw new ClassReflectionException(type, e);
			}
		}
	}
	
	private static void checkStdValueField(Field f) throws FieldReflectionException {
		if (ReflectionUtils.getImmutableFieldProblem(f) != null) {
			throw new FieldReflectionException(f, ReflectionUtils.getImmutableFieldProblem(f));
		}
		
		if (!ReflectionUtils.isStatic(f)) {
			throw new FieldReflectionException(f, "Should be static!");
		}
		
		if (!f.getDeclaringClass().isAssignableFrom(f.getType())) {
			throw new FieldReflectionException(f, "Should inherit from " + f.getDeclaringClass().getCanonicalName() + "!");
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
	}
	
	private static void checkConstructorClz(Class<?> clz) throws ClassReflectionException {
		if (ReflectionUtils.getIndependentConstructionProblem(clz) != null) {
			throw new ClassReflectionException(clz, ReflectionUtils.getIndependentConstructionProblem(clz));
		}
		
		if (!ReflectionUtils.hasDefaultConstructor(clz)) {
			throw new ClassReflectionException(clz, "Should have default constructor!");
		}
		
		if (!ReflectionUtils.isFinal(clz)) {
			throw new ClassReflectionException(clz, "Should be final!");
		}
		
		if (!clz.getSuperclass().equals(clz.getDeclaringClass())) {
			throw new ClassReflectionException(clz, "Should have superclass " + clz.getDeclaringClass().getCanonicalName() + "!");
		}
		
		for (Field f : clz.getFields()) {
			try {
				checkConstructorField(f);
			} catch (FieldReflectionException e) {
				throw new ClassReflectionException(clz, e);
			}
		}
	}
	
	private static void checkConstructorField(Field f) throws FieldReflectionException {
		if (ReflectionUtils.isStatic(f)) {
			return;
		}
		
		int mod = f.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			throw new FieldReflectionException(f, "Should be public!");
		}
		
		if (Modifier.isFinal(mod)) {
			throw new FieldReflectionException(f, "Should not be final!");
		}
		
		if (!ReflectionUtils.isJType(f) && !ReflectionUtils.isPrimitive(f)) {
			throw new FieldReflectionException(f, "Should be primitive or inherit from " + JType.class.getCanonicalName() + "!");
		}
	}
}
