package jlx.blocks.ibd1;

import java.util.*;

import jlx.asal.j.*;
import jlx.blocks.ibd2.InterfacePort;
import jlx.common.ReflectionUtils;
import jlx.utils.Dir;

public final class InPort<T extends JType> extends PrimitivePort<T> {
	private Set<JType> possibleValues;
	private T initialValue;
	
	public InPort() {
		initialValue = null;
	}
	
	public void connect(OutPort<T> sourcePort) {
		addSourcePort(sourcePort);
	}
	
	public void connect(InterfacePort sourcePort) {
		addSourcePort(sourcePort);
	}
	
	/**
	 * Setting this value has no effect when the port is also initialized by its state machine!
	 */
	public InPort<T> setInitialValue(T initialValue) {
		if (!ReflectionUtils.isFinal(initialValue.getClass())) {
			throw new Error("Should be final!");
		}
		
		this.initialValue = initialValue;
		return this;
	}
	
	public T getInitialValue() {
		return initialValue;
	}
	
//	public void restrict(PortValues<T> possibleValues) {
//		this.possibleValues = possibleValues;
//	}
	
	@SuppressWarnings("unchecked")
	public InPort<T> restrict(T... values) {
		if (possibleValues != null) {
			throw new Error("Can only be set once!");
		}
		
		if (values == null) {
			throw new Error("Should not happen!");
		}
		
		if (values.length == 0) {
			throw new Error("Should not happen!");
		}
		
		possibleValues = new HashSet<JType>();
		
		for (JType value : values) {
			if (!value.isElemIn(possibleValues)) {
				possibleValues.add(value);
			}
		}
		
		if (values.length == 1) {
			setInitialValue(values[0]);
		}
		
		return this;
	}
	
	/**
	 * If the port is restricted, returns the permitted values.
	 * Otherwise, it returns NULL. 
	 */
	public Set<JType> getPossibleValues() {
		return possibleValues != null ? Collections.unmodifiableSet(possibleValues) : null;
	}
	
	@Override
	public final Dir getDir() {
		return Dir.IN;
	}
}
