package jlx.asal.j;

import java.lang.reflect.*;
import java.util.*;

import jlx.common.ReflectionUtils;
import jlx.common.reflection.ClassReflectionException;
import jlx.utils.HashMaps;

public class JTypeLibrary {
	public static class Type {
		private final Class<? extends JType> legacy;
		private final Map<String, Constructor> constructorsPerPreferredName;
		
		private Type(Class<? extends JType> clz) throws ClassReflectionException {
			legacy = clz;
			constructorsPerPreferredName = extractConstructorsPerName();
		}
		
		private Map<String, Constructor> extractConstructorsPerName() throws ClassReflectionException {
			Map<String, Constructor> result = new HashMap<String, Constructor>();
			
			for (Class<?> nestedClz : legacy.getDeclaredClasses()) {
				if (!JType.class.isAssignableFrom(nestedClz)) {
					throw new ClassReflectionException(nestedClz, "Should be a " + legacy.getCanonicalName() + "!");
				}
				
				Constructor c = new Constructor(this, nestedClz.asSubclass(JType.class));
				
				if (result.containsKey(c.getPreferredName())) {
					throw new ClassReflectionException(nestedClz, "Constructor name is already in use!");
				}
				
				result.put(c.getPreferredName(), c);
			}
			
			return Collections.unmodifiableMap(result);
		}
		
		public Class<? extends JType> getLegacy() {
			return legacy;
		}
		
		public Map<String, Constructor> getConstructorsPerPreferredName() {
			return constructorsPerPreferredName;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(legacy);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Type)) {
				return false;
			}
			Type other = (Type) obj;
			return Objects.equals(legacy, other.legacy);
		}
	}
	
	public static class Constructor {
		private final Class<? extends JType> legacy;
		private final String preferredName;
		private final String canonicalName;
		private final Set<String> names;
		private final Type type;
		private final Map<String, Field> fieldsPerName;
		
		private Constructor(Type type, Class<? extends JType> clz) {
			this.type = type;
			
			JTypeName annotation = clz.getAnnotation(JTypeName.class);
			canonicalName = type.legacy.getSimpleName() + "." + clz.getSimpleName();
			preferredName = annotation != null ? annotation.s() : canonicalName; 
			
			names = new HashSet<String>();
			names.add(canonicalName);
			names.add(preferredName);
			names.add(clz.getSimpleName());
			
			legacy = clz;
			fieldsPerName = extractFieldsPerName();
		}
		
		private Map<String, Field> extractFieldsPerName() {
			Map<String, Field> result = new HashMap<String, Field>();
			
			for (Field f : legacy.getFields()) {
				if (!ReflectionUtils.isStatic(f)) {
					result.put(f.getName(), f);
				}
			}
			
			return Collections.unmodifiableMap(result);
		}
		
		public Class<? extends JType> getLegacy() {
			return legacy;
		}
		
		public String getCanonicalName() {
			return canonicalName;
		}
		
		public String getPreferredName() {
			return preferredName;
		}
		
		public Set<String> getNames() {
			return names;
		}
		
		public Type getType() {
			return type;
		}
		
		public Map<String, Field> getFieldsPerName() {
			return fieldsPerName;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(legacy);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Constructor)) {
				return false;
			}
			Constructor other = (Constructor) obj;
			return Objects.equals(legacy, other.legacy);
		}
	}
	
	private final Map<String, Type> typePerName;
	private final Map<String, Set<Constructor>> constrsPerName;
	private final Map<Class<? extends JType>, Constructor> constrPerDecl;
	
	public JTypeLibrary() {
		typePerName = new HashMap<String, Type>();
		constrsPerName = new HashMap<String, Set<Constructor>>();
		constrPerDecl = new HashMap<Class<? extends JType>, Constructor>();
	}
	
	public static JTypeLibrary createStdLibrary() throws ClassReflectionException {
		JTypeLibrary result = new JTypeLibrary();
		result.add(JVoid.class);
		result.add(JBool.class);
		result.add(JInt.class);
		result.add(JPulse.class);
		return result;
	}
	
	public JTypeLibrary(JTypeLibrary source) {
		this();
		
		typePerName.putAll(source.typePerName);
		
		for (Map.Entry<String, Set<Constructor>> entry : source.constrsPerName.entrySet()) {
			constrsPerName.put(entry.getKey(), new HashSet<Constructor>(entry.getValue()));
		}
		
		constrPerDecl.putAll(source.constrPerDecl);
	}
	
	public JTypeLibrary(JTypeLibrary source1, JTypeLibrary source2) throws ClassReflectionException {
		this();
		
//		for (Map.Entry<String, Set<Constructor>> entry : source2.constructorsPerConstructorName.entrySet()) {
//			constructorsPerConstructorName.put(entry.getKey(), new HashSet<Constructor>(entry.getValue()));
//		}
//		
//		//TODO we could put more effort into checking that type names do not conflict...
//		
//		typePerName.putAll(source2.typePerName);
//		typePerDecl.putAll(source2.typePerDecl);
//		constrPerDecl.putAll(source2.constrPerDecl);
		
		add(source1);
		add(source2);
	}
	
	public void add(JTypeLibrary other) throws ClassReflectionException {
		for (Type type : other.getTypes()) {
			add(type.getLegacy());
		}
	}
	
	public void add(Class<? extends JType> newType) throws ClassReflectionException {
		JTypeValidation.check(newType);
		
		Type existingProcessedType = typePerName.get(newType.getSimpleName());
		
		if (existingProcessedType != null && existingProcessedType.getLegacy() != newType) {
			throw new Error(newType.getCanonicalName() + " cannot use the same name as " + existingProcessedType.getLegacy().getCanonicalName());
		}
		
		if (existingProcessedType == null) {
			Type newProcessedType = new Type(newType);
			
			for (Constructor c : newProcessedType.constructorsPerPreferredName.values()) {
				constrPerDecl.put(c.getLegacy(), c);
				
				for (String name : c.names) {
					HashMaps.inject(constrsPerName, name, c);
				}
			}
			
			typePerName.put(newType.getSimpleName(), newProcessedType);
		}
	}
	
//	private void addConstructor(Constructor c, String name) {
//		
//		Set<Constructor> constructors = constructorsPerName.get(name);
//		constrPerDecl.put(c.legacy, c);
//		
//		if (constructors == null) {
//			constructors = new HashSet<Constructor>();
//			constructorsPerName.put(name, constructors);
//		}
//		
//		constructors.add(c);
//	}
	
//	public Type getType(Class<? extends JType> clz) {
//		return typePerDecl.get(clz);
//	}
	
	public Constructor getConstructor(Class<? extends JType> clz) {
		return constrPerDecl.get(clz);
	}
	
	public Collection<Type> getTypes() {
		return typePerName.values();
	}
	
	public Constructor getConstructor(String tokenText, Class<? extends JType> expectedType) throws ClassReflectionException {
		if (tokenText.startsWith("\"") && tokenText.endsWith("\"")) {
			String noQuotes = tokenText.substring(1, tokenText.length() - 1);
			Set<Constructor> constructors = constrsPerName.get(noQuotes);
			
			if (constructors == null) {
				throw new ClassReflectionException(JType.class, "Unknown constructor name of type " + expectedType.getCanonicalName() + " (\"" + noQuotes + "\")!");
			}
			
			if (constructors.size() == 0) {
				throw new Error("Should not happen!!");
			}
			
			if (constructors.size() == 1) {
				return constructors.iterator().next();
			}
			
			Set<Constructor> newConstructors = new HashSet<Constructor>();
			
			for (Constructor c : constructors) {
				if (JType.isAssignableTo(expectedType, c.getType().getLegacy())) {
					newConstructors.add(c);
				}
			}
			
			if (newConstructors.size() == 0) {
				return constructors.iterator().next();
			}
			
			if (newConstructors.size() == 1) {
				return newConstructors.iterator().next();
			}
			
			String msg = "Ambiguous constructor name! Do you mean:";
			
			for (Constructor c : newConstructors) {
				msg += "\n\t\t\"" + c.getCanonicalName() + "\"";
			}
			
			throw new ClassReflectionException(JType.class, msg);
		}
		
		switch (tokenText.toUpperCase()) {
			case "TRUE":
				return getConstructor(JBool.TRUE.class);
			case "FALSE":
				return getConstructor(JBool.FALSE.class);
		}
		
		try {
			Integer.parseInt(tokenText);
			return getConstructor(JInt.LITERAL.class);
		} catch (NumberFormatException e) {
			throw new ClassReflectionException(JType.class, "Invalid literal value (\"" + tokenText + "\")!");
		}
	}
	
//	public Constructor getConstructor(Class<? extends JType> constrDecl) {
//		Type t = typePerName.get(typeDecl.getSimpleName());
//		
//		if (t == null) {
//			for (Map.Entry<String, Type> e : typePerName.entrySet()) {
//				System.out.println(e.getKey() + " -> " + e.getValue().getLegacy().getCanonicalName());
//			}
//			
//			throw new Error("Should not happen; unknown type declaration " + typeDecl.getCanonicalName() + "!");
//		}
//		
//		Constructor c = t.constructorsPerPreferredName.get(constrDecl.getSimpleName());
//		
//		if (c == null) {
//			throw new Error("Should not happen; unknown constructor declaration " + constrDecl.getCanonicalName() + "!");
//		}
//		
//		return c;
//	}
	
//	public Map<String, Type> getTypePerName() {
//		return Collections.unmodifiableMap(typePerName);
//	}
//	
//	public Map<Class<? extends JType>, Type> getTypePerDecl() {
//		return Collections.unmodifiableMap(typePerDecl);
//	}
}
