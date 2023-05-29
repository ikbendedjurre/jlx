package jlx.utils;

import java.util.*;

public class ConcurrentWorker {
	private final Map<String, Object> valuePerKey;
	private final String[] displayKeys;
	
	public ConcurrentWorker() {
		valuePerKey = new HashMap<String, Object>();
		displayKeys = null;
	}
	
	public ConcurrentWorker(String... displayKeys) {
		valuePerKey = new HashMap<String, Object>();
		
		this.displayKeys = displayKeys;
	}
	
	public <T> T get(String key, Class<T> valueType) {
		Object result = valuePerKey.get(key);
		
		if (result == null) {
			throw new Error("No value for " + key + "!");
		}
		
		if (!valueType.isAssignableFrom(result.getClass())) {
			throw new Error("Inconsistent types for " + key + "!");
		}
		
		return valueType.cast(result);
	}
	
	public ConcurrentWorker set(String key, Object value) {
		valuePerKey.put(key, value);
		return this;
	}
	
	private static String keyToSuffix(String key, Object value) {
		if (value == null) {
			return "";
		}
		
		if (value instanceof Collection) {
			return "; " + key + " = " + ((Collection<?>)value).size();
		}
		
		return "; " + key + " = " + value.toString();
	}
	
	public String getSuffix() {
		String result = "";
		
		if (displayKeys != null) {
			for (String displayKey : displayKeys) {
				result += keyToSuffix(displayKey, valuePerKey.get(displayKey));
			}
		} else {
			for (Map.Entry<String, Object> e : valuePerKey.entrySet()) {
				result += keyToSuffix(e.getKey(), e.getValue());
			}
		}
		
		return result;
	}
}

