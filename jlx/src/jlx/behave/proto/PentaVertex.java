package jlx.behave.proto;

import jlx.common.FileLocation;

public class PentaVertex {
	private TetraVertex legacy;
	
	public PentaVertex(TetraVertex source) {
		legacy = source;
	}
	
	public Class<?> getSysmlClz() {
		return legacy.getSysmlClz();
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public TetraVertex getLegacy() {
		return legacy;
	}
}
