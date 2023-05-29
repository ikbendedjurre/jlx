package jlx.blocks.ibd1;

import java.lang.reflect.*;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.*;
import jlx.asal.vars.*;
import jlx.common.*;
import jlx.common.reflection.*;

public abstract class Type1IBD extends JBuilder implements JScope {
	private Map<JType, ASALVariable> varPerJType;
	private Map<String, ASALVariable> varPerName;
	private Map<String, ASALPort> primitivePortPerName;
	private Map<String, ASALProperty> propertyPerName;
	private Map<String, ASALOp> operationPerName;
	private JTypeLibrary typeLibrary;
	private FileLocation fileLocation;
	
	public Type1IBD() {
		varPerJType = null;
		varPerName = null;
		primitivePortPerName = null;
		propertyPerName = null;
		operationPerName = null;
		typeLibrary = null;
		fileLocation = new FileLocation();
	}
	
	public FileLocation getFileLocation() {
		return fileLocation;
	}
	
	@Override
	public final String getName() {
		if (getClass().getDeclaringClass() != null) {
			return getClass().getDeclaringClass().getSimpleName();
		}
		
		if (getClass().getSuperclass() != null) {
			return getClass().getSimpleName();
		}
		
		throw new Error("Should not happen, could not find a name for " + getClass().getCanonicalName() + "!");
	}
	
	@Override
	public ASALVariable getVarInScope(JType jtype) {
		return getVarPerJType().get(jtype);
	}
	
	public final Map<JType, ASALVariable> getVarPerJType() {
		if (varPerJType == null) {
			varPerJType = extractVarPerJType();
		}
		
		return varPerJType;
	}
	
	private Map<JType, ASALVariable> extractVarPerJType() {
		Map<JType, ASALVariable> fieldPerScopeObject = new HashMap<JType, ASALVariable>();
		
		for (ASALPort p : getPrimitivePortPerName().values()) {
			fieldPerScopeObject.put(p.getLegacy(), p);
		}
		
		for (ASALProperty p : getPropertyPerName().values()) {
			fieldPerScopeObject.put(p.getLegacy(), p);
		}
		
		return Collections.unmodifiableMap(fieldPerScopeObject);
	}
	
	@Override
	public Map<String, ASALVariable> getVariablePerName() {
		if (varPerName == null) {
			varPerName = extractVarPerName();
		}
		
		return varPerName;
	}
	
	public final Map<String, ASALPort> getPrimitivePortPerName() {
		if (primitivePortPerName == null) {
			primitivePortPerName = extractPrimitivePortPerName();
		}
		
		return primitivePortPerName;
	}
	
	private Map<String, ASALPort> extractPrimitivePortPerName() {
		Map<String, ASALPort> result = new HashMap<String, ASALPort>();
		
		for (Field f : getClass().getFields()) {
			if (ReflectionUtils.isStatic(f)) {
				continue;
			}
			
			try {
				if (PrimitivePort.class.isAssignableFrom(f.getType())) {
					result.put(f.getName(), new ASALPort(this, f));
				}
			} catch (ReflectionException e) {
				throw new Error("Should not happen!", e);
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	public final Map<String, ASALProperty> getPropertyPerName() {
		if (propertyPerName == null) {
			propertyPerName = extractPropertyPerName();
		}
		
		return propertyPerName;
	}
	
	private Map<String, ASALProperty> extractPropertyPerName() {
		Map<String, ASALProperty> result = new HashMap<String, ASALProperty>();
		
		for (Field f : getClass().getFields()) {
			if (ReflectionUtils.isStatic(f)) {
				continue;
			}
			
			if (PrimitivePort.class.isAssignableFrom(f.getType())) {
				continue;
			}
			
			try {
				if (JType.class.isAssignableFrom(f.getType())) {
					result.put(f.getName(), new ASALProperty(this, f));
				}
			} catch (ReflectionException e) {
				throw new Error("Should not happen!", e);
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<String, ASALVariable> extractVarPerName() {
		Map<String, ASALVariable> result = new HashMap<String, ASALVariable>();
		
		for (Map.Entry<JType, ASALVariable> entry : getVarPerJType().entrySet()) {
			result.put(entry.getValue().getName(), entry.getValue());
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	@Override
	public Map<String, ASALOp> getOperationPerName() {
		if (operationPerName == null) {
			operationPerName = extractOperationPerName();
		}
		
		return operationPerName;
	}
	
	private Map<String, ASALOp> extractOperationPerName() {
		Map<String, ASALOp> result = new HashMap<String, ASALOp>();
		
		for (Field f : getClass().getFields()) {
			if (ReflectionUtils.isStatic(f)) {
				continue;
			}
			
			try {
				if (Operation.class.isAssignableFrom(f.getType())) {
					result.put(f.getName(), new ASALOperation(this, f));
				}
			} catch (ReflectionException e) {
				throw new Error("Should not happen!", e);
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	@Override
	public JScopeType getType() {
		return JScopeType.BLOCK;
	}
	
	@Override
	public Class<? extends JType> getReturnType() {
		return null;
	}
	
	@Override
	public final ASALVariable getWritableVariable(String name) {
		ASALVariable v = getVariablePerName().get(name);
		
		if (v != null && !(v.getLegacy() instanceof InPort)) {
			return v;
		}
		
		return null;
	}
	
	@Override
	public final ASALVariable getVariable(String name) {
		ASALVariable v = getVariablePerName().get(name);
		
		if (v != null) {
			return v;
		}
		
		return null;
	}
	
	@Override
	public final ASALOp getOperation(String name) {
		return getOperationPerName().get(name);
	}
	
	@Override
	public final ASALPort generateHelperPort(String baseName, ASALPortFields fields) {
		throw new Error("No implementation!");
	}
	
	@Override
	public final ASALProperty generateHelperProperty(String baseName, JType initialValue) {
		throw new Error("No implementation!");
	}
	
	@Override
	public Set<JType> getPossibleValues(ASALVariable v) {
		throw new Error("No implementation! (And not needed?)");
	}
	
	@Override
	public JType getDefaultValue(ASALVariable v) {
		throw new Error("No implementation! (And not needed?)");
	}
	
	@Override
	public final List<String> getScopeSuggestions(boolean readableVars, boolean writableVars, boolean operations, boolean literals) {
		List<String> suggestions = new ArrayList<String>();
		
		if (readableVars || writableVars) {
			for (Map.Entry<String, ASALProperty> entry : getPropertyPerName().entrySet()) {
				suggestions.add("PROPERTY " + entry.getKey() + ": " + entry.getValue().getType().getSimpleName());
			}
		}
		
		for (Map.Entry<String, ASALPort> entry : getPrimitivePortPerName().entrySet()) {
			if ((readableVars && !entry.getValue().isWritable()) || (writableVars && entry.getValue().isWritable())) {
				suggestions.add("PORT " + entry.getKey() + ": " + entry.getValue().getType().getSimpleName());
			}
		}
		
		if (operations) {
			for (Map.Entry<String, ASALOp> entry : getOperationPerName().entrySet()) {
				suggestions.add("OPERATION " + entry.getKey() + ": " + entry.getValue().getReturnType().getSimpleName());
			}
		}
		
		if (literals) {
			try {
				for (JTypeLibrary.Type type : getTypeLib().getTypes()) {
					for (Map.Entry<String, JTypeLibrary.Constructor> entry : type.getConstructorsPerPreferredName().entrySet()) {
						suggestions.add("LITERAL \"" + entry.getValue().getPreferredName() + "\"");
					}
				}
			} catch (ClassReflectionException e) {
				throw new Error("Should not happen!", e);
			}
		}
		
		return suggestions;
	}
	
	@Override
	public final JTypeLibrary getTypeLib() throws ClassReflectionException {
		if (typeLibrary == null) {
			typeLibrary = extractTypeLib();
		}
		
		return typeLibrary;
	}
	
	private JTypeLibrary extractTypeLib() throws ClassReflectionException {
		JTypeLibrary result = JTypeLibrary.createStdLibrary();
		
		for (ASALProperty p : getPropertyPerName().values()) {
			result.add(p.getType());
		}
		
		for (ASALPort p : getPrimitivePortPerName().values()) {
			result.add(p.getType());
		}
		
		for (ASALOp op : getOperationPerName().values()) {
			result.add(op.getReturnType());
			
			for (ASALParam par : op.getParams()) {
				result.add(par.getType());
			}
		}
		
		return result;
	}
}
