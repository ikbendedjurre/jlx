package jlx.asal.vars;

public class ASALNameParts {
	public final String full;
	
	/**
	 * When the full name is T8_Port, the abbreviation is T8.
	 * When the full name is Port, the abbreviation is Port.
	 */
	public final String abbreviation;
	
	/** When the full name is T8_Port, the prefix is T.*/
	public final String prefix;
	
	/** When the full name is T8_Port, the index is 8.*/
	public final int index;
	
	/** When the full name is T8_Port, the suffix is Port.*/
	public final String suffix;
	
	private ASALNameParts(String full, String prefix, int index, String suffix) {
		this.full = full;
		this.prefix = prefix;
		this.index = index;
		this.suffix = suffix;
		
		abbreviation = extractAbbreviation();
	}
	
	private String extractAbbreviation() {
		return prefix != null ? prefix + index : full;
	}
	
	public boolean hasSameSuffix(ASALNameParts other) {
		if (suffix == null && other.suffix == null) {
			return full.equals(other.full);
		}
		
		if (suffix != null && other.suffix != null) {
			return suffix.equals(other.suffix);
		}
		
		return false;
	}
	
	public static ASALNameParts get(String portName) {
		return get(portName, new ASALNameParts(portName, null, -1, null));
	}
	
	private static ASALNameParts get(String portName, ASALNameParts fallback) {
		int index = portName.indexOf("_");
		
		if (index <= 0) {
			return fallback;
		}
		
		String prefix;
		
		if (portName.startsWith("T")) {
			prefix = "T";
		} else if (portName.startsWith("DT")) {
			prefix = "DT";
		} else if (portName.startsWith("D")) {
			prefix = "D";
		} else {
			return fallback;
		}
		
		String indexStr = portName.substring(prefix.length(), index);
		int indexValue;
		
		try {
			indexValue = Integer.parseInt(indexStr);
		} catch (NumberFormatException e) {
			return fallback;
		}
		
		String suffix = portName.substring(index + 1);
		
		if (suffix.isBlank()) {
			return fallback;
		}
		
		return new ASALNameParts(portName, prefix, indexValue, suffix);
	}
	
	@Override
	public String toString() {
		return full;
	}
}
