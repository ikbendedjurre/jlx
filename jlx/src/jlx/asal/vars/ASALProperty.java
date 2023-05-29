package jlx.asal.vars;

import java.lang.reflect.Field;

import jlx.asal.j.*;
import jlx.blocks.ibd1.Type1IBD;
import jlx.common.FileLocation;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.*;

public class ASALProperty implements ASALVariable {
	private final Field field;
	private final Type1IBD owner;
	private final JType legacy;
	private final String name;
	private final Class<? extends JType> type;
	
	private JType initialValue;
	
	public ASALProperty(Type1IBD owner, String name, JType legacy, JType initialValue) {
		this.owner = owner;
		this.field = null;
		this.name = name;
		this.legacy = legacy;
		this.type = legacy.getClass();
		this.initialValue = initialValue;
	}
	
	public ASALProperty(Type1IBD owner, Field field) throws ReflectionException {
		this.owner = owner;
		this.field = field;
		
		name = field.getName();
		legacy = ReflectionUtils.getFieldValue(owner, field, JType.class);
		type = field.getType().asSubclass(JType.class);
		
		if (ReflectionUtils.isFinal(legacy.getClass())) {
			initialValue = legacy;
		} else {
			initialValue = null;
		}
	}
	
//	@Override
//	public String textify(TextOptions lod, String i) {
//		return lod.id(name) + "::" + type.getSimpleName();
//	}
	
	@Override
	public Type1IBD getOwner() {
		return owner;
	}
	
	@Override
	public JType getLegacy() {
		return legacy;
	}
	
	@Override
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Class<? extends JType> getType() {
		return type;
	}
	
	@Override
	public ASALVarOrigin getOrigin() {
		return ASALVarOrigin.STM_VAR;
	}
	
	@Override
	public Field getField() {
		return field;
	}
	
	@Override
	public boolean isWritable() {
		return true;
	}
	
	@Override
	public void setInitialValue(JType newValue) throws InstanceReflectionException {
		if (newValue == null) {
			if (initialValue == null) {
				return;
			}
			
			throw new Error("Cannot clear an initial value!");
		}
		
		if (!JType.isAssignableTo(type, newValue.getType())) {
			throw new InstanceReflectionException(this, legacy.getFileLocation(), "Cannot assign a value of type " + newValue.getClass().getCanonicalName() + "!");
		}
		
		if (!ReflectionUtils.isFinal(newValue.getClass())) {
			throw new Error("Cannot assign a value of type " + newValue.getClass().getCanonicalName() + "!");
		}
		
		if (initialValue != null && ReflectionUtils.isFinal(initialValue.getClass())) {
			System.err.println("WARNING! Initial value of " + field.getDeclaringClass() + "." + field.getName() + " is overridden!");
		}
		
		initialValue = newValue;
	}
	
	/**
	 * Can be null!
	 */
	@Override
	public JType getInitialValue() {
		return initialValue;
	}
}
