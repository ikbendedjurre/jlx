package jlx.behave.proto;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.common.FileLocation;

public class OctoTransition {
	private OctoStateConfig sourceStateConfig;
	private OctoStateConfig targetStateConfig;
	private ASALEvent event;
	private ASALExpr guard;
	private List<ASALStatement> effectSeq;
	private List<SeptaTransition> legacy;
	
	public OctoTransition(OctoStateConfig sourceStateConfig, SeptaTransition used) {
		this(sourceStateConfig, used.getGuard(), Collections.singletonList(used), Collections.emptySet());
	}
	
	public OctoTransition(OctoStateConfig sourceStateConfig, ASALExpr guard, List<SeptaTransition> used, Set<SeptaTransition> unused) {
		this.sourceStateConfig = sourceStateConfig;
		this.guard = guard;
		
		Set<SeptaVertex> states = new HashSet<SeptaVertex>(sourceStateConfig.states);
		
		for (SeptaTransition t : used) {
			states.removeAll(t.getSourceVertices());
		}
		
		for (SeptaTransition t : used) {
			states.addAll(t.getTargetVertices());
		}
		
		List<ASALStatement> effectElems = new ArrayList<ASALStatement>();
		
		for (SeptaTransition t : used) {
			for (ConditionalExitBehavior<SeptaVertex> ceb : t.getEffectPerm()) {
				if (ceb.getEffect().containsNonEmpty()) {
					if (sourceStateConfig.states.containsAll(ceb.getExitedVertices())) {
						effectElems.add(ceb.getEffect());
					}
				}
			}
		}
		
		targetStateConfig = new OctoStateConfig(states);
		event = removeFinalizedEvent(used.get(0).getEvent());
		effectSeq = Collections.unmodifiableList(effectElems);
		legacy = used;
	}
	
	private static ASALEvent removeFinalizedEvent(ASALEvent e) {
		return e instanceof ASALFinalized ? null : e;
	}
	
	public List<SeptaTransition> getLegacy() {
		return legacy;
	}
	
	public List<FileLocation> getFileLocations() {
		List<FileLocation> result = new ArrayList<FileLocation>();
		
		for (SeptaTransition t : legacy) {
			result.addAll(t.getFileLocations());
		}
		
		return result;
	}
	
	public OctoStateConfig getSourceStateConfig() {
		return sourceStateConfig;
	}
	
	public OctoStateConfig getTargetStateConfig() {
		return targetStateConfig;
	}
	
	public ASALEvent getEvent() {
		return event;
	}
	
	public ASALExpr getGuard() {
		return guard;
	}
	
	public List<ASALStatement> getEffectSeq() {
		return effectSeq;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		Set<ProtoTransition> result = new HashSet<ProtoTransition>();
		
		for (SeptaTransition t : legacy) {
			result.addAll(t.getProtoTrs());
		}
		
		return result;
	}
}



