package jlx.behave.proto;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.common.FileLocation;
import jlx.utils.*;

public class PentaTransition {
	private Set<PentaVertex> sourceVertices;
	private Set<PentaVertex> targetVertices;
	private Set<List<ConditionalExitBehavior<PentaVertex>>> effectPerms;
	private ASALEvent event;
	private ASALExpr guard;
	private TetraTransition legacy;
	
	public PentaTransition(TetraTransition source, Map<TetraVertex, PentaVertex> newVertexPerOldVertex) {
		sourceVertices = HashSets.map(source.getSourceVertices(), newVertexPerOldVertex);
		targetVertices = HashSets.map(source.getTargetVertices(), newVertexPerOldVertex);
		event = source.getEvent();
		guard = source.getGuard();
		legacy = source;
		effectPerms = new HashSet<List<ConditionalExitBehavior<PentaVertex>>>();
		
		for (List<ConditionalExitBehavior<TetraVertex>> exitPerm : source.getExitBehaviorInOrder()) {
			for (List<ASALStatement> entryPerm : source.getEntryBehaviorInOrder()) {
				List<ConditionalExitBehavior<PentaVertex>> newEffectPerm = new ArrayList<ConditionalExitBehavior<PentaVertex>>();
				
				for (ConditionalExitBehavior<TetraVertex> e : exitPerm) {
					newEffectPerm.add(e.changeKey(newVertexPerOldVertex));
				}
				
				newEffectPerm.add(new ConditionalExitBehavior<PentaVertex>("EFFECT " + getFileLocation().tiny(), Collections.emptySet(), source.getStatement()));
				
				for (ASALStatement e : entryPerm) {
					newEffectPerm.add(new ConditionalExitBehavior<PentaVertex>("ENTRY " + getFileLocation().tiny(), Collections.emptySet(), e));
				}
				
				effectPerms.add(newEffectPerm);
			}
		}
	}
	
	public TetraTransition getLegacy() {
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
	
	public Set<List<ConditionalExitBehavior<PentaVertex>>> getEffectPerms() {
		return effectPerms;
	}
	
	public Set<PentaVertex> getSourceVertices() {
		return sourceVertices;
	}
	
	public Set<PentaVertex> getTargetVertices() {
		return targetVertices;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
}

