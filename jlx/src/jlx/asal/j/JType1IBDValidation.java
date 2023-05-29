package jlx.asal.j;

import java.lang.reflect.Field;

import jlx.blocks.ibd1.*;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.*;

public class JType1IBDValidation {
	public static void check(Class<? extends Type1IBD> clz) throws ClassReflectionException {
		if (clz.getDeclaringClass() == null) {
			throw new ClassReflectionException(clz, "Should be nested inside another class!");
		}
		
		if (!Type1IBD.class.isAssignableFrom(clz)) {
			throw new ClassReflectionException(clz, "Should inherit from " + Type1IBD.class.getCanonicalName() + "!");
		}
		
		if (!clz.getSuperclass().equals(Type1IBD.class)) {
			check(clz.getSuperclass().asSubclass(Type1IBD.class));
		}
		
		if (!clz.getSimpleName().equals("Block")) {
			throw new ClassReflectionException(clz, "Should be named \"Block\"!");
		}
		
		if (clz.getInterfaces().length > 0) {
			throw new ClassReflectionException(clz, "Should not implement interfaces!");
		}
		
		if (ReflectionUtils.getIndependentConstructionProblem(clz) != null) {
			throw new ClassReflectionException(clz, ReflectionUtils.getIndependentConstructionProblem(clz));
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
				if (PrimitivePort.class.isAssignableFrom(f.getType())) {
					checkPrimitivePortField(f);
					continue;
				}
				
				if (JType.class.isAssignableFrom(f.getType())) {
					checkPropertyField(f);
					continue;
				}
				
				if (Operation.class.isAssignableFrom(f.getType())) {
					checkOperationField(f);
					continue;
				}
				
				throw new FieldReflectionException(f, "Should be a " + JType.class.getCanonicalName() + " or " + PrimitivePort.class.getCanonicalName() + "!");
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
	
	private static void checkPrimitivePortField(Field f) throws FieldReflectionException {
		if (ReflectionUtils.getImmutableFieldProblem(f) != null) {
			throw new FieldReflectionException(f, ReflectionUtils.getImmutableFieldProblem(f));
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
		
		Class<?> typeParam = ReflectionUtils.getTypeParam(f);
		
		if (!JType.class.isAssignableFrom(typeParam)) {
			throw new FieldReflectionException(f, "Should be a " + JType.class.getCanonicalName() + "!");
		}
		
		if (JVoid.class.isAssignableFrom(typeParam)) {
			throw new FieldReflectionException(f, "Should not be a " + JVoid.class.getCanonicalName() + "!");
		}
	}
	
	private static void checkPropertyField(Field f) throws FieldReflectionException {
		if (ReflectionUtils.getImmutableFieldProblem(f) != null) {
			throw new FieldReflectionException(f, ReflectionUtils.getImmutableFieldProblem(f));
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
		
		if (JVoid.class.isAssignableFrom(f.getType())) {
			throw new FieldReflectionException(f, "Should not be a " + JVoid.class.getCanonicalName() + "!");
		}
	}
	
	private static void checkOperationField(Field f) throws FieldReflectionException {
		if (ReflectionUtils.getImmutableFieldProblem(f) != null) {
			throw new FieldReflectionException(f, ReflectionUtils.getImmutableFieldProblem(f));
		}
		
		if (f.getType().isArray()) {
			throw new FieldReflectionException(f, "Should not be an array!");
		}
		
		if (!Initialization.class.isAssignableFrom(f.getType())) {
			Class<?> typeParam = ReflectionUtils.getTypeParam(f);
			
			if (!JType.class.isAssignableFrom(typeParam)) {
				throw new FieldReflectionException(f, "Should be a " + JType.class.getCanonicalName() + "!");
			}
		}
	}
}

