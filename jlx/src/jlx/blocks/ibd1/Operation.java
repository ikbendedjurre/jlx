package jlx.blocks.ibd1;

import java.util.*;

import jlx.asal.j.JType;
import jlx.asal.parsing.ASALCode;
import jlx.behave.CodeObject;
import jlx.common.ReflectionUtils;

public class Operation<T extends JType> extends CodeObject {
	private final List<String> paramNames;
	private final List<JType> paramTypes;
	private final boolean canInitialize;
	
	public Operation(String code0, String... code) {
		this(false, code0, code);
	}
	
	protected Operation(boolean canInitialize, String code0, String... code) {
		super(new ASALCode("STATS1", code0, code));
		
		paramNames = new ArrayList<String>();
		paramTypes = new ArrayList<JType>();
		
		this.canInitialize = canInitialize;
	}
	
	public List<String> getParamNames() {
		return Collections.unmodifiableList(paramNames);
	}
	
	public List<JType> getParamTypes() {
		return Collections.unmodifiableList(paramTypes);
	}
	
	public boolean canInitialize() {
		return canInitialize;
	}
	
	public Operation<T> addParam(String paramName, JType paramType) {
		if (paramNames.contains(paramName)) {
			throw new Error("Parameter name \"" + paramName + "\" is already in use!");
		}
		
		if (ReflectionUtils.isFinal(paramType.getClass())) {
			throw new Error("Parameter type should not be final!");
		}
		
		paramNames.add(paramName);
		paramTypes.add(paramType);
		
		return this;
	}
}

