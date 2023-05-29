package jlx.behave.proto;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.HashMaps;

public class DecaThreeBReducer {
	public static void bla(DecaThreeBStateMachine sm) {
		System.out.println("[" + LocalTime.now() + "] #vtxClzs = 0");
		
		Set<Set<DecaThreeBVertex>> vtxClzs = extractVtxClzs(sm);
		Set<Set<DecaThreeBVertex>> newVtxClzs = new HashSet<Set<DecaThreeBVertex>>();
		
		System.out.println("[" + LocalTime.now() + "] #vtxClzs = " + vtxClzs.size());
		
		boolean done = false;
		
		while (!done) {
			done = true;
			newVtxClzs.clear();
			
			Map<DecaThreeBVertex, Set<DecaThreeBVertex>> clzPerVtx = new HashMap<DecaThreeBVertex, Set<DecaThreeBVertex>>();
			
			for (Set<DecaThreeBVertex> vtxClz : vtxClzs) {
				for (DecaThreeBVertex vtx : vtxClz) {
					clzPerVtx.put(vtx, vtxClz);
				}
			}
			
			for (Set<DecaThreeBVertex> vtxClz : vtxClzs) {
				boolean hasBeenSplit = false;
				
				for (PulsePackMap input : sm.inputs) {
					Set<Set<DecaThreeBVertex>> split = split(vtxClz, clzPerVtx, input);
					
					if (split.size() > 1) {
						newVtxClzs.addAll(split);
						hasBeenSplit = true;
						done = false;
						break;
					}
				}
				
				if (!hasBeenSplit) {
					newVtxClzs.add(vtxClz);
				}
			}
			
			vtxClzs.clear();
			vtxClzs.addAll(newVtxClzs);
			
			System.out.println("[" + LocalTime.now() + "] #vtxClzs = " + vtxClzs.size());
		}
		
		
	}
	
	private static Set<Set<DecaThreeBVertex>> split(Set<DecaThreeBVertex> vtxClz, Map<DecaThreeBVertex, Set<DecaThreeBVertex>> clzPerVtx, PulsePackMap input) {
		Map<Set<Set<DecaThreeBVertex>>, Set<DecaThreeBVertex>> result = new HashMap<Set<Set<DecaThreeBVertex>>, Set<DecaThreeBVertex>>();
		
		for (DecaThreeBVertex vtx : vtxClz) {
			Set<Set<DecaThreeBVertex>> clzs = new HashSet<Set<DecaThreeBVertex>>();
			
			for (DecaThreeBTransition t : vtx.getOutgoing().get(input)) {
				for (DecaThreeBVertex tgt : t.getTgts()) {
					clzs.add(clzPerVtx.get(tgt));
				}
			}
			
			HashMaps.inject(result, clzs, vtx);
		}
		
		return new HashSet<Set<DecaThreeBVertex>>(result.values());
	}
	
	private static Set<Set<DecaThreeBVertex>> extractVtxClzs(DecaThreeBStateMachine sm) {
		Map<PulsePackMap, Set<DecaThreeBVertex>> result = new HashMap<PulsePackMap, Set<DecaThreeBVertex>>();
		
		for (DecaThreeBVertex vtx : sm.vertices.values()) {
			HashMaps.inject(result, vtx.getLegacy().getOutput(), vtx);
		}
		
		return new HashSet<Set<DecaThreeBVertex>>(result.values());
	}
}
