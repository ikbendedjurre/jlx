package jlx.behave;

import jlx.asal.parsing.ASALCode;
import jlx.common.FileLocation;

public abstract class CodeObject {
	public final ASALCode treeObject;
	
	public CodeObject(ASALCode treeObject) {
		this.treeObject = treeObject;
	}
	
	public FileLocation getFileLocation() {
		return treeObject.fileLocation;
	}
}
