package jlx.asal.vars;

import java.lang.reflect.Field;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOperation;
import jlx.blocks.ibd1.Type1IBD;
import jlx.common.FileLocation;
import jlx.common.reflection.ReflectionException;

public class ASALParam implements ASALVariable {
	private final ASALOperation operation;
	private final String name;
	private final JType typeInstance;
	private final boolean isWritable;
	
	public ASALParam(ASALOperation operation, String name, JType typeInstance) throws ReflectionException {
		this.operation = operation;
		this.name = name;
		this.typeInstance = typeInstance;
		
		isWritable = true;
	}
	
	@Override
	public FileLocation getFileLocation() {
		return operation.getFileLocation();
	}
	
//	@Override
//	public String textify(TextOptions lod, String i) {
//		return lod.id(name) + "::" + typeInstance.getClass().getSimpleName();
//	}
	
	@Override
	public JType getLegacy() {
		return typeInstance;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Class<? extends JType> getType() {
		return typeInstance.getClass();
	}
	
	@Override
	public ASALVarOrigin getOrigin() {
		return ASALVarOrigin.FCT_PARAM;
	}
	
	@Override
	public Field getField() {
		return operation.getField();
	}
	
	@Override
	public boolean isWritable() {
		return isWritable;
	}
	
	@Override
	public Type1IBD getOwner() {
		return operation.getOwner();
	}
	
	@Override
	public JType getInitialValue() {
		return null; //(unused... more convenient than checking if ASALVariable is an ASALParam...)
	}
	
	@Override
	public void setInitialValue(JType newValue) {
		//(unused... more convenient than checking if ASALVariable is an ASALParam...)
	}
}
