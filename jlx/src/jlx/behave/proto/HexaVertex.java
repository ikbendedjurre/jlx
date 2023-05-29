package jlx.behave.proto;

import jlx.common.FileLocation;

public class HexaVertex {
	private PentaVertex legacy;
	
	public HexaVertex(PentaVertex source) {
		legacy = source;
	}
	
	public Class<?> getSysmlClz() {
		return legacy.getSysmlClz();
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public PentaVertex getLegacy() {
		return legacy;
	}
}
