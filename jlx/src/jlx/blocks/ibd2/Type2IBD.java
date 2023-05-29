package jlx.blocks.ibd2;

import java.lang.reflect.*;
import java.util.*;

import jlx.blocks.ibd1.*;
import jlx.common.FileLocation;
import jlx.common.ReflectionUtils;

public abstract class Type2IBD {
	private Map<Field, Type1IBD> type1IBDPerName;
	private Map<Field, Type2IBD> type2IBDPerName;
	private Map<Field, InterfacePort> interfacePortPerName;
	private FileLocation fileLocation;
	
	public Type2IBD() {
		type1IBDPerName = null;
		type2IBDPerName = null;
		interfacePortPerName = null;
		fileLocation = new FileLocation();
	}
	
	public abstract void connectFlows();
	
	public final FileLocation getFileLocation() {
		return fileLocation;
	}
	
	public final String getName() {
		return getClass().getDeclaringClass().getSimpleName();
	}
	
	public final Map<Field, Type1IBD> getType1IBDPerName() {
		if (type1IBDPerName == null) {
			type1IBDPerName = extractFieldValuePerName(Type1IBD.class);
		}
		
		return type1IBDPerName;
	}
	
	public final Map<Field, Type2IBD> getType2IBDPerName() {
		if (type2IBDPerName == null) {
			type2IBDPerName = extractFieldValuePerName(Type2IBD.class);
		}
		
		return type2IBDPerName;
	}
	
	public final Map<Field, InterfacePort> getInterfacePortPerName() {
		if (interfacePortPerName == null) {
			interfacePortPerName = extractFieldValuePerName(InterfacePort.class);
		}
		
		return interfacePortPerName;
	}
	
	private <T> Map<Field, T> extractFieldValuePerName(Class<T> clz) {
		Map<Field, T> result = new HashMap<Field, T>();
		
		for (Field f : getClass().getFields()) {
			if (ReflectionUtils.isStatic(f)) {
				continue;
			}
			
			try {
				if (clz.isAssignableFrom(f.getType())) {
					result.put(f, clz.cast(f.get(this)));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new Error("Should not happen!", e);
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
}
