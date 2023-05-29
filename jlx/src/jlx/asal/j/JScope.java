package jlx.asal.j;

import java.util.*;

import jlx.asal.ops.ASALOp;
import jlx.asal.vars.*;
import jlx.common.reflection.ClassReflectionException;

public interface JScope {
	/**
	 * Gives the name of the scope, typically the name of a block instance or the name of an operation.
	 */
	public String getName();
	
	/**
	 * Gives the variable in this scope that corresponds with the specified JType instance.
	 * Can return NULL; this indicates that the object represents a value or expression (other than a variable reference).
	 */
	public ASALVariable getVarInScope(JType scopeObject);
	
	/**
	 * TYPE of the scope.
	 * Used to determine if assignments to input variables is permitted ("INIT").
	 * May be used for other things in the future.
	 */
	public JScopeType getType();
	
	/**
	 * RETURN TYPE of the scope.
	 * Is null for blocks, meaning that "return" statements are not permitted unless we are inside an operation.
	 */
	public Class<? extends JType> getReturnType();
	
	/**
	 * Finds a variable by name.
	 * The variable must be writable.
	 */
	public ASALVariable getWritableVariable(String name);
	
	/**
	 * Finds a variable by name.
	 * The variable does not have to be writable.
	 */
	public ASALVariable getVariable(String name);
	
	/**
	 * Finds an operation by name.
	 */
	public ASALOp getOperation(String name);
	
	/**
	 * Gives a complete dictionary of variables.
	 */
	public Map<String, ASALVariable> getVariablePerName();
	
	/**
	 * Gives a complete dictionary of operations.
	 */
	public Map<String, ASALOp> getOperationPerName();
	
	/**
	 * Returns the possible values of the specified value.
	 * (We assume that a set of such values can be given for any port/property.) 
	 */
	public Set<JType> getPossibleValues(ASALVariable v);
	
	public JType getDefaultValue(ASALVariable v);
	
	/**
	 * Gives a list of descriptions of objects in this scope.
	 * Bit messy, could use rewriting.
	 */
	public List<String> getScopeSuggestions(boolean readableVars, boolean writableVars, boolean operations, boolean literals);
	
	/**
	 * Returns the object that manages the types that are used within this scope (and possibly outside of this scope).
	 */
	public JTypeLibrary getTypeLib() throws ClassReflectionException;
	
	/**
	 * Adds a port to this scope (for pre-processing purposes).
	 */
	public ASALPort generateHelperPort(String baseName, ASALPortFields fields);
	
	/**
	 * Adds a property to this scope (for pre-processing purposes).
	 */
	public ASALProperty generateHelperProperty(String baseName, JType initialValue);
}

