package jlx.asal.ops;

import java.lang.reflect.Field;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.parsing.*;
import jlx.asal.parsing.api.*;
import jlx.asal.vars.*;
import jlx.blocks.ibd1.Type1IBD;
import jlx.common.*;
import jlx.common.reflection.*;

public class ASALOperation implements ASALOp {
	private final Field field;
	private final Type1IBD owner;
	private final jlx.blocks.ibd1.Operation<?> legacy;
	private final String name;
	private ASALStatement body;
	private final Class<? extends JType> returnType;
	private final List<ASALParam> params;
	private final Map<String, ASALParam> paramPerName;
	private final Map<JType, ASALParam> paramPerObject;
	private final boolean canInitialize;
	
	public ASALOperation(Type1IBD owner, Field field) throws ReflectionException {
		this.owner = owner;
		this.field = field;
		
		name = field.getName();
		legacy = ReflectionUtils.getFieldValue(owner, field, jlx.blocks.ibd1.Operation.class);
		canInitialize = legacy.canInitialize();
		
		if (canInitialize) {
			returnType = JVoid.class;
		} else {
			returnType = ReflectionUtils.getTypeParam(field).asSubclass(JType.class);
		}
		
		if (JPulse.class.isAssignableFrom(returnType)) {
			throw new FieldReflectionException(field, "Cannot use Pulse as return type!");
		}
		
		params = createParams(legacy);
		paramPerObject = extractParamPerType(params);
		paramPerName = createParamPerName(params);
	}
	
	private List<ASALParam> createParams(jlx.blocks.ibd1.Operation<?> operation) throws ReflectionException {
		List<ASALParam> result = new ArrayList<ASALParam>();
		
		for (int index = 0; index < operation.getParamNames().size(); index++) {
			String paramName = operation.getParamNames().get(index);
			JType typeInstance = operation.getParamTypes().get(index);
			result.add(new ASALParam(this, paramName, typeInstance));
		}
		
		return Collections.unmodifiableList(result);
	}
	
	private static Map<JType, ASALParam> extractParamPerType(List<ASALParam> params) {
		Map<JType, ASALParam> result = new HashMap<JType, ASALParam>();
		
		for (ASALParam param : params) {
			result.put(param.getLegacy(), param);
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private static Map<String, ASALParam> createParamPerName(List<ASALParam> params) {
		Map<String, ASALParam> result = new HashMap<String, ASALParam>();
		
		for (ASALParam param : params) {
			result.put(param.getName(), param);
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	@Override
	public JScope createScope(JScope surroundingScope) {
		return new JScope() {
			@Override
			public String getName() {
				return ASALOperation.this.getName();
			}
			
			@Override
			public ASALVariable getVarInScope(JType scopeObject) {
				ASALVariable result = ASALOperation.this.paramPerObject.get(scopeObject);
				
				if (result != null) {
					return result;
				}
				
				return surroundingScope.getVarInScope(scopeObject);
			}
			
			@Override
			public ASALVariable getWritableVariable(String name) {
				ASALParam result = paramPerName.get(name);
				
				if (result != null) {
					return result;
				}
				
				return surroundingScope.getWritableVariable(name);
			}
			
			@Override
			public ASALVariable getVariable(String name) {
				ASALParam result = paramPerName.get(name);
				
				if (result != null) {
					return result;
				}
				
				return surroundingScope.getVariable(name);
			}
			
			@Override
			public JTypeLibrary getTypeLib() throws ClassReflectionException {
				return surroundingScope.getTypeLib();
			}

			@Override
			public Map<String, ASALOp> getOperationPerName() {
				return surroundingScope.getOperationPerName();
			}
			
			@Override
			public Map<String, ASALVariable> getVariablePerName() {
				Map<String, ASALVariable> result = new HashMap<String, ASALVariable>(surroundingScope.getVariablePerName());
				
				for (Map.Entry<String, ASALParam> entry : paramPerName.entrySet()) {
					result.put(entry.getKey(), entry.getValue());
				}
				
				return result;
			}
			
			@Override
			public Set<JType> getPossibleValues(ASALVariable v) {
				if (params.contains(v)) {
					throw new Error("No restrictions on operation parameters!");
				}
				
				return surroundingScope.getPossibleValues(v);
			}
			
			@Override
			public JType getDefaultValue(ASALVariable v) {
				if (params.contains(v)) {
					throw new Error("No default values for operation parameters!");
				}
				
				return surroundingScope.getDefaultValue(v);
			}
			
			@Override
			public ASALPort generateHelperPort(String baseName, ASALPortFields fields) {
				throw new Error("No implementation!");
			}
			
			@Override
			public ASALProperty generateHelperProperty(String baseName, JType initialValue) {
				throw new Error("No implementation!");
			}
			
			@Override
			public ASALOp getOperation(String name) {
				return surroundingScope.getOperation(name);
			}
			
			@Override
			public List<String> getScopeSuggestions(boolean readableVars, boolean writableVars, boolean operations, boolean literals) {
				List<String> suggestions = new ArrayList<String>();
				
				if (readableVars || writableVars) {
					for (Map.Entry<String, ASALParam> entry : paramPerName.entrySet()) {
						suggestions.add("PARAM " + entry.getKey() + ": " + entry.getValue().getType().getSimpleName());
					}
				}
				
				suggestions.addAll(surroundingScope.getScopeSuggestions(readableVars, writableVars, operations, literals));
				return suggestions;
			}
			
			@Override
			public JScopeType getType() {
				return canInitialize ? JScopeType.INIT : JScopeType.OPERATION;
			}
			
			@Override
			public Class<? extends JType> getReturnType() {
				return returnType;
			}
		};
	}
	
	@Override
	public void initBody(JScope surroundingScope) throws ModelException {
		try {
			JScope scope = createScope(surroundingScope);
			ASALSyntaxTree tree = legacy.treeObject.toSyntaxTree(scope);
			body = tree.createAPI(null, ASALStatement.class);
			body.validateAndCrossRef(scope, JVoid.class);
			body.setFileLocation(getFileLocation());
			
			// Note that operations that return VOID cannot use return statements at the moment...
			if (!returnType.equals(JVoid.class)) {
				body.confirmReturnValue();
			}
		} catch (ASALException e) {
			throw new ModelException(getFileLocation(), e);
		}
	}
	
	@Override
	public ASALStatement getBody() {
		if (body == null) {
			throw new Error("Body has not been initialized (" + owner.getName() + "." + name + ")!");
		}
		
		return body;
	}
	
	@Override
	public void attachHelperPulsePort(ASALProperty helperPulsePort) {
		ASALAssignStatement assignment = new ASALAssignStatement(null, null, helperPulsePort, ASALLiteral._true());
		body = ASALSeqStatement.from(assignment, body);
	}
	
	@Override
	public Type1IBD getOwner() {
		return owner;
	}
	
	@Override
	public Field getField() {
		return field;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public List<ASALParam> getParams() {
		return params;
	}
	
	public boolean canInitialize() {
		return canInitialize;
	}
	
	@Override
	public Class<? extends JType> getReturnType() {
		return returnType;
	}
	
//	@Override
//	public String textify(TextOptions lod, String indentation) {
//		return lod.id(getName()) + "::" + returnType.getSimpleName();
//	}
}

