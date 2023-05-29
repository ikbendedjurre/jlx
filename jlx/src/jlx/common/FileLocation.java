package jlx.common;

import java.lang.reflect.*;
import java.util.*;

public class FileLocation {
	private List<StackTraceElement> stackTrace;
	private String tinyLink;
	private String link;
	
	private FileLocation(String link) {
		this.link = "\n\t\t" + link;
	}
	
	public final static FileLocation UNKNOWN = new FileLocation("<<Unknown File Location>>");
	public final static FileLocation ERRONEOUS = new FileLocation("<<Erroneous File Location>>");
	
	public FileLocation() {
		stackTrace = Collections.unmodifiableList(getRelevantElements(Thread.currentThread().getStackTrace()));
		tinyLink = stackTrace.get(0).getFileName() + ":" + stackTrace.get(0).getLineNumber();
		link = "";
		
		for (StackTraceElement e : stackTrace) {
			link += "\n\t\t[ at " + e + " ]";
		}
	}
	
	private static List<StackTraceElement> getRelevantElements(StackTraceElement[] elems) {
		List<StackTraceElement> result = new ArrayList<StackTraceElement>();
		
//		for (StackTraceElement elem : elems) {
//			result.add(elem);
//		}
		
		// Ignore the first element (it is from java.lang.Thread).
		// Ignore the second element (it is from the constructor above).
		for (int index = 2; index < elems.length; index++) {
			if (!elems[index].getClassName().startsWith(PACKAGE_PREFIX)) {
				final String PREFIX = getPrefix(elems[index].getClassName());
				
				for (int idx = index; idx < elems.length; idx++) {
					if (!elems[idx].getClassName().startsWith(PREFIX)) {
						return result;
					}
					
					result.add(elems[idx]);
				}
				
				return result;
			}
		}
		
		return result;
	}
	
	public List<StackTraceElement> getStackTrace() {
		return stackTrace;
	}
	
	public String tiny() {
		return tinyLink;
	}
	
	@Override
	public String toString() {
		return link;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(link, stackTrace, tinyLink);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FileLocation)) {
			return false;
		}
		FileLocation other = (FileLocation) obj;
		return Objects.equals(link, other.link) && Objects.equals(stackTrace, other.stackTrace) && Objects.equals(tinyLink, other.tinyLink);
	}
	
	private final static String PACKAGE_PREFIX = getPrefix(FileLocation.class.getCanonicalName());
	
	private static String getPrefix(String x) {
		return x.substring(0, x.indexOf(".")) + ".";
	}
	
	public static FileLocation find(Object x) {
		try {
			Method m = x.getClass().getMethod("getFileLocation");
			Object r = m.invoke(x);
			
			if (r == null) {
				return new FileLocation("<<" + m.getDeclaringClass().getCanonicalName() + ".getFileLocation() Returns Null>>");
			}
			
			return FileLocation.class.cast(r);
		} catch (NoSuchMethodException e) {
			return UNKNOWN;
		} catch (ClassCastException e) {
			return ERRONEOUS;
		} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
			throw new Error(e);
		}
	}
}
