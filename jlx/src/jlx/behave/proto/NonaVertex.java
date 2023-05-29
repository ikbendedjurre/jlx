package jlx.behave.proto;

import java.util.*;

import jlx.common.FileLocation;

public class NonaVertex {
	private OctoVertex legacy;
	
	public NonaVertex(OctoVertex source) {
		legacy = source;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return legacy.getSysmlClzs();
	}
	
	public Set<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public OctoVertex getLegacy() {
		return legacy;
	}
}
