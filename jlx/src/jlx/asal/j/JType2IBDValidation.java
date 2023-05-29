package jlx.asal.j;

import java.lang.reflect.*;

import jlx.blocks.ibd1.*;
import jlx.blocks.ibd2.*;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.*;

public class JType2IBDValidation {
	public static void check(Class<? extends Type2IBD> clz) throws ClassReflectionException {
		if (clz.getDeclaringClass() == null) {
			throw new ClassReflectionException(clz, "Should be nested inside another class!");
		}
		
		if (!Type2IBD.class.isAssignableFrom(clz)) {
			throw new ClassReflectionException(clz, "Should inherit from " + Type2IBD.class.getCanonicalName() + "!");
		}
		
		if (!clz.getSuperclass().equals(Type2IBD.class)) {
			check(clz.getSuperclass().asSubclass(Type2IBD.class));
		}
		
		if (!clz.getSimpleName().equals("Block")) {
			throw new ClassReflectionException(clz, "Should be named \"Block\"!");
		}
		
		if (ReflectionUtils.getIndependentConstructionProblem(clz) != null) {
			throw new ClassReflectionException(clz, ReflectionUtils.getIndependentConstructionProblem(clz));
		}
		
		if (clz.getInterfaces().length > 0) {
			throw new ClassReflectionException(clz, "Should not implement interfaces!");
		}
		
		if (!ReflectionUtils.hasDefaultConstructor(clz)) {
			throw new ClassReflectionException(clz, "Should have default constructor!");
		}
		
		checkNestingClz(clz.getDeclaringClass());
		
		for (Field f : clz.getFields()) {
			if (ReflectionUtils.isStatic(f)) {
				throw new ClassReflectionException(clz, new FieldReflectionException(f, "Should not be static!"));
			}
			
			try {
				if (Type1IBD.class.isAssignableFrom(f.getType())) {
					checkType1IBDField(f);
					continue;
				}
				
				if (Type2IBD.class.isAssignableFrom(f.getType())) {
					checkType2IBDField(f);
					continue;
				}
				
				if (InterfacePort.class.isAssignableFrom(f.getType())) {
					checkInterfacePortField(f);
					continue;
				}
				
				throw new FieldReflectionException(f, "Should be a " + Type1IBD.class.getCanonicalName() + " or " + InterfacePort.class.getCanonicalName() + "!");
			} catch (FieldReflectionException e) {
				throw new ClassReflectionException(clz, e);
			}
		}
	}
	
	private static void checkNestingClz(Class<?> clz) throws ClassReflectionException {
		if (clz.getInterfaces().length > 0) {
			throw new ClassReflectionException(clz, "Should not implement interfaces!");
		}
		
		if (ReflectionUtils.isAbstract(clz)) {
			throw new ClassReflectionException(clz, "Should not be abstract!");
		}
		
		if (!clz.getSuperclass().equals(Object.class)) {
			throw new ClassReflectionException(clz, "Should have superclass " + Object.class.getCanonicalName() + ")!");
		}
		
		if (clz.getDeclaredClasses().length != 1) {
			throw new ClassReflectionException(clz, "Should contain exactly 1 nested class!");
		}
	}
	
	private static void checkType1IBDField(Field f) throws FieldReflectionException {
		int mod = f.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			throw new FieldReflectionException(f, "Should be public!");
		}
		
		if (!Modifier.isFinal(mod)) {
			throw new FieldReflectionException(f, "Should be final!");
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
		
		try {
			JType1IBDValidation.check(f.getType().asSubclass(Type1IBD.class));
		} catch (ClassReflectionException e) {
			throw new FieldReflectionException(f, e);
		}
	}
	
	private static void checkType2IBDField(Field f) throws FieldReflectionException {
		int mod = f.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			throw new FieldReflectionException(f, "Should be public!");
		}
		
		if (!Modifier.isFinal(mod)) {
			throw new FieldReflectionException(f, "Should be final!");
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
		
		try {
			JType2IBDValidation.check(f.getType().asSubclass(Type2IBD.class));
		} catch (ClassReflectionException e) {
			throw new FieldReflectionException(f, e);
		}
	}
	
	private static void checkInterfacePortField(Field f) throws FieldReflectionException {
		if (ReflectionUtils.getImmutableFieldProblem(f) != null) {
			throw new FieldReflectionException(f, ReflectionUtils.getImmutableFieldProblem(f));
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
	}
}

