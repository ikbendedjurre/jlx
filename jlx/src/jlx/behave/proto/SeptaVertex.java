package jlx.behave.proto;

import jlx.common.FileLocation;

public class SeptaVertex {
	private HexaVertex legacy;
	
	public SeptaVertex(HexaVertex source) {
		legacy = source;
	}
	
	public Class<?> getSysmlClz() {
		return legacy.getSysmlClz();
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public HexaVertex getLegacy() {
		return legacy;
	}
}
