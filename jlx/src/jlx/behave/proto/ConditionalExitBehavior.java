package jlx.behave.proto;

import java.util.*;

import jlx.asal.parsing.api.ASALStatement;

public class ConditionalExitBehavior<T> {
	private Set<T> exitedVertices;
	private ASALStatement effect;
	private String debugText;
	
	public ConditionalExitBehavior(String debugText, Set<T> exitedVertices, ASALStatement effect) {
		this.exitedVertices = Collections.unmodifiableSet(exitedVertices);
		this.effect = effect;
		this.debugText = debugText;
	}
	
	public Set<T> getExitedVertices() {
		return exitedVertices;
	}
	
	public ASALStatement getEffect() {
		return effect;
	}
	
	public String getDebugText() {
		return debugText;
	}
	
	public <K> ConditionalExitBehavior<K> changeKey(Map<T, K> map) {
		Set<K> newExitedVertices = new HashSet<K>();
		
		for (T v : exitedVertices) {
			newExitedVertices.add(map.get(v));
		}
		
		return new ConditionalExitBehavior<K>(debugText, newExitedVertices, effect);
	}
}
