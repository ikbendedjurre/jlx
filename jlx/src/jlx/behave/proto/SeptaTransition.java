package jlx.behave.proto;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.common.FileLocation;
import jlx.utils.*;

public class SeptaTransition {
	private Set<SeptaVertex> sourceVertices;
	private Set<SeptaVertex> targetVertices;
	private List<ConditionalExitBehavior<SeptaVertex>> effectPerm;
	private int legacyPermIndex;
	private ASALEvent event;
	private ASALExpr guard;
	private HexaTransition legacy;
	
	public SeptaTransition(HexaTransition source, int permIndex, List<ConditionalExitBehavior<HexaVertex>> pickedEffectPerm, Map<HexaVertex, SeptaVertex> newVertexPerOldVertex) {
		sourceVertices = HashSets.map(source.getSourceVertices(), newVertexPerOldVertex);
		targetVertices = HashSets.map(source.getTargetVertices(), newVertexPerOldVertex);
		effectPerm = translate(pickedEffectPerm, source.getSourceVertices(), newVertexPerOldVertex);
		event = source.getEvent();
		guard = source.getGuard();
		legacy = source;
		legacyPermIndex = permIndex;
	}
	
	private static List<ConditionalExitBehavior<SeptaVertex>> translate(List<ConditionalExitBehavior<HexaVertex>> xs, Set<HexaVertex> sourceVertices, Map<HexaVertex, SeptaVertex> newVertexPerOldVertex) {
		List<ConditionalExitBehavior<SeptaVertex>> result = new ArrayList<ConditionalExitBehavior<SeptaVertex>>();
		
		for (ConditionalExitBehavior<HexaVertex> x : xs) {
			if (x.getEffect().containsNonEmpty()) {
				if (x.getExitedVertices().equals(sourceVertices)) {
					result.add(new ConditionalExitBehavior<SeptaVertex>(x.getDebugText(), Collections.emptySet(), x.getEffect()));
				} else {
					result.add(x.changeKey(newVertexPerOldVertex));
				}
			}
		}
		
		return result;
	}
	
	public HexaTransition getLegacy() {
		return legacy;
	}
	
	public List<FileLocation> getFileLocations() {
		return legacy.getFileLocations(legacyPermIndex);
	}
	
	public ASALEvent getEvent() {
		return event;
	}
	
	public ASALExpr getGuard() {
		return guard;
	}
	
	public List<ConditionalExitBehavior<SeptaVertex>> getEffectPerm() {
		return effectPerm;
	}
	
	public Set<SeptaVertex> getSourceVertices() {
		return sourceVertices;
	}
	
	public Set<SeptaVertex> getTargetVertices() {
		return targetVertices;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
}
