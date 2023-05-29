package jlx.behave.proto;

import java.util.Set;

import jlx.common.FileLocation;

public class DecbVertex {
	private DecaVertex legacy;
	
	public DecbVertex(DecaVertex source) {
		legacy = source;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return legacy.getSysmlClzs();
	}
	
	public Set<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public DecaVertex getLegacy() {
		return legacy;
	}
}
