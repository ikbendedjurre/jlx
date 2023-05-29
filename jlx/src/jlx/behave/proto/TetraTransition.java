package jlx.behave.proto;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.common.FileLocation;

public class TetraTransition {
	private Set<TetraVertex> sourceVertices;
	private Set<TetraVertex> targetVertices;
	private Set<List<ConditionalExitBehavior<TetraVertex>>> exitBehaviorInOrder;
	private Set<List<ASALStatement>> entryBehaviorInOrder;
	
	/**
	 * Timeout/null: occur non-deterministically
	 * Finalized: occur when final states have been reached, i.e. intersection(sourceVertices, stateConfig) may not contain final states
	 * Trigger: occur when trigger is detected
	 * Call: ... (no support yet) ...
	 */
	private ASALEvent event;
	private ASALExpr guard;
	private ASALStatement statement;
	private TritoTransition legacy;
	
	/**
	 * Should only be used for local (= entry/exit/do) transitions!
	 */
	public TetraTransition(TetraVertex localVertex, TritoTransition source) {
		this(Collections.singleton(localVertex), Collections.singleton(localVertex), Collections.singleton(Collections.emptyList()), Collections.singleton(Collections.emptyList()), source);
	}
	
	public TetraTransition(Set<TetraVertex> sourceVertices, Set<TetraVertex> targetVertices, Set<List<ConditionalExitBehavior<TetraVertex>>> exitBehaviorInOrder, Set<List<ASALStatement>> entryBehaviorInOrder, TritoTransition source) {
		this.sourceVertices = sourceVertices;
		this.targetVertices = targetVertices;
		this.exitBehaviorInOrder = exitBehaviorInOrder;
		this.entryBehaviorInOrder = entryBehaviorInOrder;
		
		event = source.getEvent();
		guard = source.getGuard();
		statement = source.getStatement();
		legacy = source;
	}
	
	public TritoTransition getLegacy() {
		return legacy;
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public ASALEvent getEvent() {
		return event;
	}
	
	public ASALExpr getGuard() {
		return guard;
	}
	
	public ASALStatement getStatement() {
		return statement;
	}
	
	public Set<TetraVertex> getSourceVertices() {
		return sourceVertices;
	}
	
	public Set<TetraVertex> getTargetVertices() {
		return targetVertices;
	}
	
	/**
	 * All possible orderings of (conditional) exit behavior.
	 * Never empty, even it it contains only an empty list.
	 */
	public Set<List<ConditionalExitBehavior<TetraVertex>>> getExitBehaviorInOrder() {
		return exitBehaviorInOrder;
	}
	
	/**
	 * All possible orderings of entry behavior.
	 * Never empty, even it it contains only an empty list.
	 */
	public Set<List<ASALStatement>> getEntryBehaviorInOrder() {
		return entryBehaviorInOrder;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
}
