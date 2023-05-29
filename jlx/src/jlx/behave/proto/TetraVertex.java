package jlx.behave.proto;

import jlx.common.FileLocation;

public class TetraVertex {
	private TritoVertex legacy;
	
	public TetraVertex(TritoVertex source) {
		legacy = source;
	}
	
	public Class<?> getSysmlClz() {
		return legacy.getSysmlClz();
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public TritoVertex getLegacy() {
		return legacy;
	}
}
