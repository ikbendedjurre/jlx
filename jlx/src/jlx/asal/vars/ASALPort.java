package jlx.asal.vars;

import java.lang.reflect.Field;
import java.util.*;

import jlx.asal.j.*;
import jlx.blocks.ibd1.InPort;
import jlx.blocks.ibd1.Type1IBD;
import jlx.blocks.ibd1.VerificationAction;
import jlx.common.FileLocation;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.InstanceReflectionException;
import jlx.common.reflection.ReflectionException;
import jlx.utils.*;

public class ASALPort implements ASALVariable {
	private final Type1IBD owner;
	private final Field field;
	private final jlx.blocks.ibd1.PrimitivePort<?> legacy;
	private final String name;
	private final Class<? extends JType> type;
	private final Dir dir;
	
	private ASALPort pulsePort;
	private List<ASALPort> dataPorts;
	private JType initialValue;
	
	public ASALPort(Type1IBD owner, String name, Dir dir, JType initialValue) {
		this.owner = owner;
		this.name = name;
		this.dir = dir;
		this.legacy = null;
		this.initialValue = initialValue;
		
		field = null;
		type = initialValue.getType();
		
		pulsePort = null;
		dataPorts = Collections.emptyList();
	}
	
	public ASALPort(Type1IBD owner, Field field) throws ReflectionException {
		this.owner = owner;
		this.field = field;
		
		name = field.getName();
		legacy = ReflectionUtils.getFieldValue(owner, field, jlx.blocks.ibd1.PrimitivePort.class);
		type = ReflectionUtils.getTypeParam(field).asSubclass(JType.class);
		dir = jlx.blocks.ibd1.OutPort.class.isAssignableFrom(legacy.getClass()) ? Dir.OUT : Dir.IN;
		
		pulsePort = null;
		dataPorts = Collections.emptyList();
		initialValue = null;
	}
	
//	@Override
//	public String textify(TextOptions lod, String i) {
//		String result = lod.id(name);
//		
//		if (dir == Dir.OUT) {
//			result += type.getSimpleName();
//		} else {
//			result += "~" + type.getSimpleName();
//		}
//		
//		return result;
//	}
	
	public ASALPort getPulsePort() {
		return pulsePort;
	}
	
	public List<? extends ASALPort> getDataPorts() {
		return dataPorts;
	}
	
	public int getPriority() {
		throw new Error("Not supported here!");
	}
	
	public int getExecutionTime() {
		throw new Error("Not supported here!");
	}
	
	public Set<VerificationAction> getVerificationActions() {
		throw new Error("Not supported here!");
	}
	
	@Override
	public Type1IBD getOwner() {
		return owner;
	}
	
	@Override
	public jlx.blocks.ibd1.PrimitivePort<?> getLegacy() {
		return legacy;
	}
	
	@Override
	public FileLocation getFileLocation() {
		if (legacy != null) {
			return legacy.getFileLocation();
		}
		
		return owner.getFileLocation();
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
		return ASALVarOrigin.STM_PORT;
	}
	
	@Override
	public Field getField() {
		return field;
	}
	
	public Dir getDir() {
		return dir;
	}
	
	@Override
	public boolean isWritable() {
		return dir == Dir.OUT;
	}
	
	@Override
	public void setInitialValue(JType newValue) throws InstanceReflectionException {
		if (newValue == null) {
			if (initialValue == null) {
				return;
			}
			
			throw new Error("Cannot clear an initial value!");
		}
		
		if (!ReflectionUtils.isFinal(newValue.getClass())) {
			throw new Error("Cannot assign " + newValue.getClass().getCanonicalName() + ", which is non-final!" + legacy.getFileLocation());
		}
		
		if (!JType.isAssignableTo(type, newValue.getType())) {
			throw new InstanceReflectionException(this, legacy.getFileLocation(), "Cannot assign the value " + newValue.getClass().getCanonicalName() + " to a port of type " + type.getCanonicalName() + "!" + legacy.getFileLocation());
		}
		
		if (getInitialValue() != null) {
			System.err.println("WARNING! Initial value of " + field.getDeclaringClass() + "." + field.getName() + " is overridden!" + legacy.getFileLocation());
		}
		
		if (legacy instanceof InPort) {
			InPort<?> ip = (InPort<?>)legacy;
			
			if (ip.getPossibleValues() != null) {
				if (!newValue.isElemIn(ip.getPossibleValues())) {
					throw new Error("Cannot assign a value of type " + newValue.getClass().getCanonicalName() + " because of restrictions!" + legacy.getFileLocation());
				}
			}
		}
		
		if (getType().equals(JPulse.class)) {
			if (JBool.FALSE.class.equals(newValue.getClass())) {
				initialValue = JPulse.FALSE;
				return;
			}
			
			if (JBool.TRUE.class.equals(newValue.getClass())) {
				initialValue = JPulse.TRUE;
				return;
			}
		}
		
		initialValue = newValue;
	}
	
	/**
	 * Can be null!
	 */
	@Override
	public JType getInitialValue() {
		return initialValue;
		
//		if (initialValue != null) {
//			return initialValue;
//		}
//		
//		if (legacy instanceof InPort) {
//			InPort<?> ip = (InPort<?>)legacy;
//			
//			if (ip.getInitialValue() != null) {
//				initialValue = ip.getInitialValue();
//				
//				if (!type.isAssignableFrom(initialValue.getClass())) {
//					throw new Error(initialValue.getClass().getCanonicalName() + " is not a subclass of " + type.getCanonicalName() + "!" + legacy.getFileLocation());
//				}
//				
//				if (ip.getPossibleValues() != null) {
//					if (!initialValue.isElemIn(ip.getPossibleValues())) {
//						throw new Error(initialValue.getClass().getCanonicalName() + " does not conform to value restrictions of port " + name + "!" + legacy.getFileLocation());
//					}
//				}
//				
//				return initialValue;
//			}
//		}
//		
//		return null;
	}
}
