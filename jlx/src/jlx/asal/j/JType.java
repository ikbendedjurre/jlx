package jlx.asal.j;

import java.lang.reflect.*;
import java.util.*;

import jlx.common.FileLocation;
import jlx.common.ReflectionUtils;

public abstract class JType {
//	private boolean isInited;
	private FileLocation fileLocation;
	
//	private Field accessorField;
//	private Class<? extends JType> castTargetClz;
//	private JType instance;
	
	public JType() {
//		isInited = false;
		fileLocation = new FileLocation();
	}
	
	public final FileLocation getFileLocation() {
		return fileLocation;
	}
	
//	public String getASAL() {
//		if (ReflectionUtils.isFinal(getClass())) {
//			return "\"\"";
//		}
//		
//		throw new Error("Should not happen!");
//	}
	
	private static void confirmType(Class<? extends JType> candidate) {
		if (ReflectionUtils.isFinal(candidate)) {
			throw new Error("JType error! Since " + candidate.getCanonicalName() + " is final, it is a JType constructor instead of a JType!");
		}
	}
	
	private static void confirmConstructor(Class<? extends JType> candidate) {
		if (!ReflectionUtils.isFinal(candidate)) {
			throw new Error("JType error! Since " + candidate.getCanonicalName() + " is not final, it is a JType instead of a JType constructor!");
		}
	}
	
	/**
	 * Searches for and returns the default value constructor of the specified JType.
	 * The default constructor must be a nested subclass of "clz".
	 * It can be marked with the JTypeDefaultValue annotation, or
	 * an arbitrary subclass WITHOUT the JTypeExpr annotation can be picked arbitrarily.
	 */
	public static <T extends JType> Class<? extends T> getDefaultValueConstructor(Class<T> clz) {
		confirmType(clz);
		
		Class<?> annoClz = null;
		Class<?> fallbackClz = null;
		
		for (Class<?> nestedClz : clz.getDeclaredClasses()) {
			if (ReflectionUtils.isFinal(nestedClz)) {
				JTypeDefaultValue anno = nestedClz.getAnnotation(JTypeDefaultValue.class);
				
				if (anno != null) {
					if (annoClz != null) {
						throw new Error("JType error! " + clz.getCanonicalName() + " has multiple default value constructors!");
					}
					
					if (nestedClz.getAnnotation(JTypeExpr.class) != null) {
						throw new Error("JType error! " + nestedClz.getCanonicalName() + " cannot be annotated with both " + JTypeDefaultValue.class.getCanonicalName() + " and " + JTypeExpr.class.getCanonicalName() + "!");
					}
					
					annoClz = nestedClz;
				}
				
				if (fallbackClz == null && nestedClz.getAnnotation(JTypeExpr.class) == null) {
					fallbackClz = nestedClz;
				}
			}
		}
		
		if (annoClz != null) {
			return annoClz.asSubclass(clz);
		}
		
		if (fallbackClz != null) {
			return fallbackClz.asSubclass(clz);
		}
		
		throw new Error("JType error! " + clz.getCanonicalName() + " does not have a default value constructor!");
	}
	
	public static <T extends JType> T createDefaultValue(Class<T> clz) {
		return createValue(getDefaultValueConstructor(clz));
	}
	
	public static <T extends JType> Set<Class<? extends T>> getAllValueConstructors(Class<T> clz) {
		confirmType(clz);
		
		Set<Class<? extends T>> result = new HashSet<Class<? extends T>>();
		
		for (Class<?> nestedClz : clz.getDeclaredClasses()) {
			if (ReflectionUtils.isFinal(nestedClz)) {
				if (nestedClz.getAnnotation(JTypeExpr.class) == null) {
					result.add(nestedClz.asSubclass(clz));
				}
			}
		}
		
		return result;
	}
	
	public static <T extends JType> Set<T> createAllValues(Class<T> clz) {
		Set<T> result = new HashSet<T>();
		
		for (Class<? extends T> vc : getAllValueConstructors(clz)) {
			result.add(createValue(vc));
		}
		
		return result;
	}
	
//	/**
//	 * Checks that all constructors of this type have 0 fields, making the type 'simple'.
//	 */
//	public static boolean isSimpleType(Class<? extends JType> clz, boolean ignoreValuesWithFields) {
//		if (ignoreValuesWithFields) {
//			for (Class<?> nestedClz : clz.getDeclaredClasses()) {
//				if (getJTypeFields(nestedClz.asSubclass(JType.class)).size() == 0) {
//					return true; //At least 1 value required!
//				}
//			}
//			
//			return false;
//		} else {
//			for (Class<?> nestedClz : clz.getDeclaredClasses()) {
//				if (getJTypeFields(nestedClz.asSubclass(JType.class)).size() > 0) {
//					return false; //Values forbidden!
//				}
//			}
//			
//			return true;
//		}
//	}
	
//	public static Set<JType> createAllValues(Class<? extends JType> clz, boolean ignoreValuesWithFields) {
//		if (ReflectionUtils.isFinal(clz)) {
//			throw new Error("Should not happen; since " + clz.getCanonicalName() + " is final, it IS a value, and does not have a default value!");
//		}
//		
//		if (JVoid.class.equals(clz)) {
//			throw new Error("Should not happen!");
//		}
//		
//		Set<JType> result = new HashSet<JType>();
//		
//		for (Class<?> nestedClz : clz.getDeclaredClasses()) {
//			List<Field> fields = new ArrayList<Field>(getJTypeFields(nestedClz.asSubclass(JType.class)));
//			
//			if (fields.size() > 0) {
//				if (!ignoreValuesWithFields) {
//					String msg = "Not supported because " + nestedClz.getCanonicalName() + " has fields (";
//					msg += fields.get(0).getName();
//					
//					for (int index = 1; index < fields.size(); index++) {
//						msg += ", " + fields.get(index).getName();
//					}
//					
//					msg += ")!";
//					throw new Error(msg);
//				}
//			} else {
//				try {
//					result.add(ReflectionUtils.createStdInstance(nestedClz.asSubclass(JType.class)));
//				} catch (ReflectionException e) {
//					throw new Error("Should not happen!", e);
//				}
//			}
//		}
//		
//		return result;
//	}
	
//	public static JType createDefaultValue(Class<? extends JType> clz) {
//		if (ReflectionUtils.isFinal(clz)) {
//			throw new Error("Should not happen; since " + clz.getCanonicalName() + " is final, it IS a value, and does not have a default value!");
//		}
//		
//		if (JBool.class.equals(clz)) {
//			return JBool.FALSE;
//		}
//		
//		if (JPulse.class.equals(clz)) {
//			return JPulse.FALSE;
//		}
//		
//		if (JVoid.class.equals(clz)) {
//			throw new Error("Should not happen!");
//		}
//		
//		Class<? extends JType> bestValueDecl = null;
//		Set<Field> bestFields = Collections.emptySet();
//		
//		for (Class<?> nestedClz : clz.getDeclaredClasses()) {
//			Set<Field> fields = getJTypeFields(nestedClz.asSubclass(JType.class));
//			
//			if (bestValueDecl == null || fields.size() < bestFields.size()) {
//				bestValueDecl = nestedClz.asSubclass(JType.class);
//				bestFields = fields;
//			}
//		}
//		
//		if (bestValueDecl == null) {
//			throw new Error("Should not happen!");
//		}
//		
//		try {
//			JType result = ReflectionUtils.createStdInstance(bestValueDecl);
//			
//			for (Field f : bestFields) {
//				if (ReflectionUtils.isPrimitive(f)) {
//					//Not initializing it AUTOMATICALLY sets the field to the default value of the primitive type!
//				} else { //Must be a JType then:
//					f.set(result, createDefaultValue(f.getType().asSubclass(JType.class)));
//				}
//			}
//			
//			return result;
//		} catch (SecurityException | ReflectionException | IllegalArgumentException | IllegalAccessException e) {
//			throw new Error("Should not happen!", e);
//		}
//	}
//	
//	private static Set<Field> getJTypeFields(Class<? extends JType> clz) {
//		Set<Field> result = new HashSet<Field>();
//		
//		for (Field f : clz.getFields()) {
//			if (!ReflectionUtils.isStatic(f)) {
//				result.add(f);
//			}
//		}
//		
//		return Collections.unmodifiableSet(result);
//	}
	
//	public final boolean isInited() {
//		return isInited;
//	}
//	
//	public final void initAsAccessor(Field accessorField, JType accessedInstance) {
//		if (isInited) {
//			throw new Error("Already initialized!");
//		}
//		
//		this.accessorField = accessorField;
//		this.instance = accessedInstance;
//		this.isInited = true;
//	}
//	
//	public final Field getAccessorField() {
//		return accessorField;
//	}
//	
//	public final JType getAccessedInstance() {
//		return instance;
//	}
//	
//	public final void initAsCast(Class<? extends JType> castTargetClz, JType castInstance) {
//		if (isInited) {
//			throw new Error("Already initialized!");
//		}
//		
//		this.castTargetClz = castTargetClz;
//		this.instance = castInstance;
//		this.isInited = true;
//	}
//	
//	public final Class<? extends JType> getCastTargetClz() {
//		return castTargetClz;
//	}
//	
//	public final JType getCastInstance() {
//		return instance;
//	}
//	
//	public final <T extends JType> T cast(Class<T> targetClz) {
//		if (!targetClz.isAssignableFrom(getClass())) {
//			throw new Error("Cannot cast from " + getClass().getCanonicalName() + " to " + targetClz.getCanonicalName() + "!");
//		}
//		
//		try {
//			T result = targetClz.getConstructor().newInstance();
//			result.initAsCast(targetClz, result);
//			return result;
//		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
//			throw new Error("Should not happen; cannot instantiate " + targetClz.getCanonicalName() + "!", e);
//		}
//	}
	
	/**
	 * Returns the JType of this JType instance.
	 */
	public final Class<? extends JType> getType() {
		return getType(getClass());
	}
	
	/**
	 * Returns the JType of the specified JType constructor class.
	 */
	public final static Class<? extends JType> getType(Class<? extends JType> constructor) {
		confirmConstructor(constructor);
		Class<? extends JType> result = constructor;
		
		while (result.getSuperclass() != JType.class && result.getSuperclass() != JUserType.class) {
			result = result.getSuperclass().asSubclass(JType.class);
		}
		
		return result;
	}
	
	public final static <T extends JType> T createVar(Class<T> varType) {
		confirmType(varType);
		
		try {
			T result = varType.getConstructor().newInstance();
			
			//Typically, a variable will not have non-static fields; we may add this in the future.
			for (Field f : varType.getFields()) {
				if (!ReflectionUtils.isStatic(f)) {
					//Do nothing.
				}
			}
			
			return result;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new Error("Should not happen; cannot instantiate " + varType.getCanonicalName() + "!", e);
		}
	}
	
	public final static <T extends JType> T createValue(Class<T> constructor) {
		confirmConstructor(constructor);
		
		for (Field f : constructor.getFields()) {
			if (!ReflectionUtils.isStatic(f)) {
				throw new Error("JType error! Cannot instantiate " + constructor.getCanonicalName() + " because it has non-static fields!");
			}
		}
		
		try {
			return constructor.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new Error("Should not happen; cannot instantiate " + constructor.getCanonicalName() + "!", e);
		}
	}
	
	/**
	 * Creates and returns a SHALLOW copy of this JType instance.
	 */
	public final JType createShallowCopy() {
		try {
			JType result = getClass().getConstructor().newInstance();
			
			for (Field f : getClass().getFields()) {
				if (!ReflectionUtils.isStatic(f)) {
					f.set(result, f.get(this));
				}
			}
			
			return result;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new Error("Should not happen; cannot copy " + getClass().getCanonicalName() + "!", e);
		}
	}
	
	/**
	 * Creates and returns a DEEP copy of this JType instance.
	 */
	public final JType createDeepCopy() {
		try {
			JType result = getClass().getConstructor().newInstance();
			
			for (Field f : getClass().getFields()) {
				if (!ReflectionUtils.isStatic(f)) {
					if (ReflectionUtils.isPrimitive(f)) {
						f.set(result, f.get(this));
					} else {
						if (JType.class.isAssignableFrom(f.getType())) {
							f.set(result, JType.class.cast(f.get(this)).createDeepCopy());
						}
					}
				}
			}
			
			return result;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new Error("Should not happen; cannot copy " + getClass().getCanonicalName() + "!", e);
		}
	}
	
//	public final boolean isCopy(JType other) {
//		if (!other.getClass().equals(getClass())) {
//			return false;
//		}
//		
//		try {
//			for (Field f : getClass().getFields()) {
//				if (!ReflectionUtils.isStatic(f)) {
//					if (f.get(this) != f.get(other)) {
//						return false;
//					}
//				}
//			}
//			
//			return true;
//		} catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
//			throw new Error("Should not happen; cannot compare two instances of " + getClass().getCanonicalName() + "!", e);
//		}
//	}
	
//	public static Set<JType> intersection(Collection<? extends JType> c1, Collection<? extends JType> c2) {
//		Set<JType> result = new HashSet<JType>();
//		
//		for (JType e : c1) {
//			if (e.isElemIn(c2)) {
//				result.add(e);
//			}
//		}
//		
//		return result;
//	}
	
	/**
	 * Returns TRUE iff this JType instance (or a JType instance with the same contents!) is in the specified collection of JType instance.
	 */
	public final boolean isElemIn(Collection<? extends JType> elements) {
		for (JType element : elements) {
			if (JType.isEqual(element, this)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns TRUE iff two JType instances, i1 and i2, are equal (content-wise).
	 */
	public static boolean isEqual(JType i1, JType i2) {
		Class<? extends JType> c1 = i1.getClass();
		Class<? extends JType> c2 = i2.getClass();
		
		if (!ReflectionUtils.isFinal(c1) || !ReflectionUtils.isFinal(c2)) {
			return false;
		}
		
		//When their classes are equal, the instances must have equal field values:
		if (c1.equals(c2)) {
			for (Field f : c1.getFields()) {
				if (!ReflectionUtils.isStatic(f)) {
					try {
						if (!f.get(i1).equals(f.get(i2))) {
							return false;
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new Error("Should not happen!", e);
					}
				}
			}
			
			return true;
		}
		
		//When their classes are not equal, the instances can still be equal iff
		//their class names are equal and they have no (non-static) fields:
		//TODO WHY?!
		if (!c1.getSimpleName().equals(c2.getSimpleName())) {
			return false;
		}
		
		for (Field f : c1.getFields()) {
			if (!ReflectionUtils.isStatic(f)) {
				return false;
			}
		}
		
		for (Field f : c2.getFields()) {
			if (!ReflectionUtils.isStatic(f)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean isBooleanType(Class<? extends JType> type) {
		return type.equals(JBool.class) || type.equals(JPulse.class);
	}
	
	/**
	 * Returns TRUE iff a value of type "found" can (always) be legally assigned to a variable/... of type "expected".
	 * (Currently the same as "equatable", we don't do subtyping!) 
	 */
	public static boolean isAssignableTo(Class<? extends JType> expected, Class<? extends JType> found) {
		if (isBooleanType(expected) && isBooleanType(found)) {
			return true;
		}
		
		return expected.equals(found);
	}
	
	/**
	 * Returns TRUE iff a value of type "lhs", A, and a value of type "rhs", B, can (always) be legally used in the expressions "A == B" and "A <> B".
	 */
	public static boolean isEquatableTo(Class<? extends JType> lhs, Class<? extends JType> rhs) {
		if (isBooleanType(lhs) && isBooleanType(rhs)) {
			return true;
		}
		
		return lhs.equals(rhs);
	}
	
	public String toStr() {
		return toStr(null, false);
	}
	
	public String toStr(JScope scope) {
		return toStr(scope, false);
	}
	
	public String toStr(boolean useNewLines) {
		return toStr(null, useNewLines);
	}
	
	public String toStr(JScope scope, boolean useNewLines) {
		return JTypePrinter.toString(this, scope, useNewLines);
	}
}

