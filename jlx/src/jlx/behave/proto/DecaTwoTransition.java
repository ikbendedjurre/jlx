package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.common.FileLocation;

public class DecaTwoTransition {
	private DecaTwoStateMachine owner;
	private DecaTwoStateConfig sourceStateConfig;
	private DecaTwoStateConfig targetStateConfig;
	private ASALSymbolicValue guard;
//	private NextStateFct nextStateFct;
	private DecaOneTransition legacy;
	
//	public DecaTwoTransition(DecaTwoStateMachine owner, DecaOneTransition source, ASALSymbolicValue newGuard, NextStateFct newNextStateFct, DecaTwoStateConfig sourceStateConfig, DecaTwoStateConfig targetStateConfig) {
//		this.owner = owner;
//		this.sourceStateConfig = sourceStateConfig;
//		this.targetStateConfig = targetStateConfig;
//		
//		guard = newGuard;
//		nextStateFct = newNextStateFct;
//		legacy = source;
//	}
	
	public DecaTwoTransition(DecaTwoStateMachine owner, DecaOneTransition source, ASALSymbolicValue newGuard, DecaTwoStateConfig sourceStateConfig, DecaTwoStateConfig targetStateConfig) {
		this.owner = owner;
		this.sourceStateConfig = sourceStateConfig;
		this.targetStateConfig = targetStateConfig;
		
		guard = newGuard;
		legacy = source;
	}
	
	/**
	 * Transition from which this transition has been derived, possibly through the instantiation of inputs.
	 * @return
	 */
	public DecaOneTransition getLegacy() {
		return legacy;
	}
	
	public boolean isIdle() {
		return legacy.isIdle();
	}
	
	public DecaTwoStateMachine getOwner() {
		return owner;
	}
	
	public List<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public DecaTwoStateConfig getSourceStateConfig() {
		return sourceStateConfig;
	}
	
	public DecaTwoStateConfig getTargetStateConfig() {
		return targetStateConfig;
	}
	
	public ASALSymbolicValue getGuard() {
		return guard;
	}
	
//	public NextStateFct getNextStateFct() {
//		return nextStateFct;
//	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getLegacy().getProtoTrs();
	}
}
