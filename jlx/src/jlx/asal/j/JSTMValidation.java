package jlx.asal.j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import jlx.behave.*;
import jlx.blocks.ibd1.Initialization;
import jlx.blocks.ibd1.Operation;
import jlx.blocks.ibd1.PrimitivePort;
import jlx.blocks.ibd1.Type1IBD;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.ClassReflectionException;
import jlx.common.reflection.FieldReflectionException;

public class JSTMValidation {
	public static void check(Class<? extends StateMachine> clz) throws ClassReflectionException {
		if (ReflectionUtils.getIndependentConstructionProblem(clz) != null) {
			throw new ClassReflectionException(clz, ReflectionUtils.getIndependentConstructionProblem(clz));
		}
		
		if (!ReflectionUtils.hasDefaultConstructor(clz)) {
			throw new ClassReflectionException(clz, "Should have default constructor!");
		}
		
		if (!Type1IBD.class.isAssignableFrom(clz)) {
			throw new ClassReflectionException(clz, "Should inherit from " + Type1IBD.class.getCanonicalName() + "!");
		}
		
		JType1IBDValidation.check(clz.getSuperclass().asSubclass(Type1IBD.class));
		
		checkRegion(clz);
		
		for (Field f : clz.getDeclaredFields()) {
			if (ReflectionUtils.isStatic(f)) {
				throw new ClassReflectionException(clz, new FieldReflectionException(f, "Should not be static!"));
			}
			
			try {
				if (PrimitivePort.class.isAssignableFrom(f.getType())) {
					throw new FieldReflectionException(f, "Cannot be a " + PrimitivePort.class.getCanonicalName() + "!");
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
	
	private static void checkRegion(Class<?> clz) throws ClassReflectionException {
		try {
			for (Class<?> nestedClz : clz.getDeclaredClasses()) {
				checkRegionElement(nestedClz);
			}
		} catch (ClassReflectionException e) {
			throw new ClassReflectionException(clz, e);
		}
	}
	
	private static void checkRegionElement(Class<?> regionElement) throws ClassReflectionException {
		if (!Vertex.class.isAssignableFrom(regionElement)) {
			throw new ClassReflectionException(regionElement, "Should be a " + Vertex.class.getCanonicalName() + "!");
		}
		
		int mod = regionElement.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			throw new ClassReflectionException(regionElement, "Should be public!");
		}
		
		if (Modifier.isStatic(mod)) {
			throw new ClassReflectionException(regionElement, "Should not be static!");
		}
		
		if (Modifier.isAbstract(mod)) {
			throw new ClassReflectionException(regionElement, "Should not be abstract!");
		}
		
		if (CompositeState.class.isAssignableFrom(regionElement)) {
			if (regionElement.getDeclaredClasses().length == 0) {
				throw new ClassReflectionException(regionElement, "Should nest at least 1 " + Vertex.class.getCanonicalName() + " subclass!");
			}
			
			checkRegion(regionElement);
			
			if (!containsVertexOfType(regionElement, InitialState.class)) {
				throw new ClassReflectionException(regionElement, "Should nest at least 1 " + InitialState.class.getCanonicalName() + " subclass!");
			}
			
			if (!containsVertexOfType(regionElement, State.class)) {
				throw new ClassReflectionException(regionElement, "Should nest at least 1 " + State.class.getCanonicalName() + " subclass!");
			}
		} else {
			if (regionElement.getDeclaredClasses().length > 0) {
				throw new ClassReflectionException(regionElement, "Should not nest classes!");
			}
			
			if (ReferenceState.class.equals(ReflectionUtils.getRawSuperclass(regionElement))) {
				Class<? extends StateMachine> smClz = ReflectionUtils.getSuperclassTypeParam(regionElement).asSubclass(StateMachine.class);
				checkRegion(smClz); //Nested state machines do not have to inherit from StateMachine.class!!
			}
		}
	}
	
	private static boolean containsVertexOfType(Class<?> clz, Class<? extends Vertex> vertexClz) {
		for (Class<?> nestedClz : clz.getDeclaredClasses()) {
			if (vertexClz.isAssignableFrom(nestedClz)) {
				return true;
			}
		}
		
		return false;
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


