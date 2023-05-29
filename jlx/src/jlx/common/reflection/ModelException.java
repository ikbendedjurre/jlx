package jlx.common.reflection;

import java.util.Collection;

import jlx.common.*;
import jlx.utils.Texts;

@SuppressWarnings("serial")
public class ModelException extends ReflectionException {
	public ModelException(FileLocation fileLocation, String msg) {
		super(msg + fileLocation);
	}
	
	public ModelException(Collection<FileLocation> fileLocations, String msg) {
		super(msg + Texts.concat(fileLocations, ""));
	}
	
	public ModelException(FileLocation fileLocation, Throwable cause) {
		super("Error in model:" + fileLocation, cause);
	}
	
	public ModelException(String msg, FileLocation fileLocation, FileLocations fileLocations) {
		super(msg + fileLocation + fileLocations);
	}
	
	public ModelException(String msg, FileLocations... fileLocations) {
		super(msg + getMsg(fileLocations));
	}
	
	private static String getMsg(FileLocations... fileLocations) {
		String result = "";
		
		for (FileLocations fileLocation : fileLocations) {
			result += fileLocation;
		}
		
		return result;
	}
}
