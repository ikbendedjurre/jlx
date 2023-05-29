package jlx.behave.proto;

import java.util.*;

public class DecaTwoVertex {
	private DecaTwoStateConfig stateConfig;
	private Set<DecaTwoTransition> outgoing;
	private String name;
	private int id;
	
	public DecaTwoVertex(DecaTwoStateConfig stateConfig, String name, int id) {
		this.stateConfig = stateConfig;
		this.name = name;
		this.id = id;
		
		outgoing = new HashSet<DecaTwoTransition>();
	}
	
	public DecaTwoStateConfig getStateConfig() {
		return stateConfig;
	}
	
	public Set<DecaTwoTransition> getOutgoing() {
		return outgoing;
	}
	
	public String getName() {
		return name;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
