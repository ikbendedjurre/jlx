package jlx.behave.proto;

import java.util.*;

import jlx.common.FileLocation;

public class OctoVertex {
	private OctoStateConfig stateConfig;
	
	public OctoVertex(OctoStateConfig stateConfig) {
		this.stateConfig = stateConfig;
	}
	
	public Set<Class<?>> getSysmlClzs() {
		Set<Class<?>> result = new HashSet<Class<?>>();
		
		for (SeptaVertex v : stateConfig.states) {
			result.add(v.getSysmlClz());
		}
		
		return result;
	}
	
	public Set<FileLocation> getFileLocations() {
		Set<FileLocation> result = new HashSet<FileLocation>();
		
		for (SeptaVertex v : stateConfig.states) {
			result.add(v.getFileLocation());
		}
		
		return result;
	}
	
	public OctoStateConfig getStateConfig() {
		return stateConfig;
	}
}
