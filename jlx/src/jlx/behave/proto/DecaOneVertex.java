package jlx.behave.proto;

import java.util.*;

import jlx.common.FileLocation;

public class DecaOneVertex {
	private DecbVertex legacy;
	private Set<String> names;
	
	public DecaOneVertex(DecbVertex source) {
		legacy = source;
		names = new TreeSet<String>();
	}
	
	public Set<String> getNames() {
		return names;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		return legacy.getSysmlClzs();
	}
	
	public Set<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public DecbVertex getLegacy() {
		return legacy;
	}
}
