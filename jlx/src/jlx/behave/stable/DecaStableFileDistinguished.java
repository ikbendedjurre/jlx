package jlx.behave.stable;

import java.util.*;

import jlx.utils.*;

public class DecaStableFileDistinguished extends DecaStableFileMinimized {
	public DecaStableFileDistinguished() {
		
	}
	
	@Override
	public void init() {
		super.init();
		
		Map<InitState, Set<EquivClz>> ecsPerInitState = new HashMap<InitState, Set<EquivClz>>();
		
		for (EquivClz v : getEquivClzs()) {
			HashMaps.inject(ecsPerInitState, new InitState(v.someVtx()), v);
		}
		
		for (Set<EquivClz> xs : ecsPerInitState.values()) {
//			for (EquivClz x : xs) {
//				
//			}
//			x.icsPerPreserved
		}
	}
	
	public static void main(String[] args) {
		DecaStableFileDistinguished x = new DecaStableFileDistinguished();
		x.loadFromFile("models", "all.reduced.2.stable", true);
		x.init();
		x.saveToFile("models", "all.reduced.3.stable");
	}
}
