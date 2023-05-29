package jlx.asal.vars;

import java.lang.reflect.Field;

import jlx.asal.j.JType;
import jlx.blocks.ibd1.Type1IBD;
import jlx.common.FileLocation;
import jlx.common.reflection.InstanceReflectionException;

public interface ASALVariable {
	/**
	 * Block to which this variable belongs.
	 * Reliably non-null.
	 */
	public Type1IBD getOwner();
	
	/**
	 * Variable name.
	 */
	public String getName();
	
	/**
	 * Java object from which this variable was generated.
	 * Can be a final or non-final class (the object could be a type or an initial value).
	 * Reliably non-null.
	 */
	public JType getLegacy();
	
	public FileLocation getFileLocation();
	
	/**
	 * Variable type.
	 */
	public Class<? extends JType> getType();
	
	/**
	 * Location in Java from which this variable was generated.
	 */
	public ASALVarOrigin getOrigin();
	
	/**
	 * Field that contains the Java object from which this variable was generated.
	 * CAN BE NULL!!
	 */
	public Field getField();
	
	/**
	 * Indicates if this variable can be modified with an ASAL assignment. 
	 */
	public boolean isWritable();
	
	/**
	 * Returns the initial value of this variable.
	 * Can be null.
	 */
	public JType getInitialValue();
	
	/**
	 * Sets the initial value of this variable.
	 * The initial value should be compatible with the type of the variable.
	 * If already set, a warning is generated.
	 */
	public void setInitialValue(JType newValue) throws InstanceReflectionException;
}
