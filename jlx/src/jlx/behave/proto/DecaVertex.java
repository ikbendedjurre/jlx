package jlx.behave.proto;

import java.util.*;

import jlx.common.FileLocation;

public class DecaVertex {
	private NonaVertex legacy;
	//private Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> entryRewriteFct;
	
	public DecaVertex(NonaVertex source) {
		legacy = source;
		//entryRewriteFct = new HashMap<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>>();
	}
	
	public Set<Class<?>> getSysmlClzs() {
		Set<Class<?>> result = new HashSet<Class<?>>();
		
		for (SeptaVertex v : legacy.getLegacy().getStateConfig().states) {
			result.add(v.getSysmlClz());
		}
		
		return result;
	}
	
	public Set<FileLocation> getFileLocations() {
		return legacy.getFileLocations();
	}
	
	public NonaVertex getLegacy() {
		return legacy;
	}
	
//	public Map<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> getEntryRewriteFct() {
//		return entryRewriteFct;
//	}
//	
//	public void addToEntryRewriteFct(ASALVariable v, Map<ASALSymbolicValue, ASALSymbolicValue> entryRewriteFctPerVar) {
//		entryRewriteFct.put(v, entryRewriteFctPerVar);
//	}
//	
//	public String getEntryRewriteFctStr(JScope scope, LOD lod) {
//		List<String> xs = new ArrayList<String>();
//		
//		for (Map.Entry<ASALVariable, Map<ASALSymbolicValue, ASALSymbolicValue>> xe : entryRewriteFct.entrySet()) {
//			if (new HashSet<ASALSymbolicValue>(xe.getValue().values()).size() == 1) {
//				xs.add(lod.id(xe.getKey().getName()) + "=>[" + xe.getValue().values().iterator().next().toString() + "]");
//			} else {
//				Map<ASALSymbolicValue, Set<ASALSymbolicValue>> inverted = new HashMap<ASALSymbolicValue, Set<ASALSymbolicValue>>();
//				
//				for (Map.Entry<ASALSymbolicValue, ASALSymbolicValue> e : xe.getValue().entrySet()) {
//					Permutations.inject(inverted, e.getValue(), e.getKey());
//				}
//				
//				List<String> elems = new ArrayList<String>();
//				
//				for (Map.Entry<ASALSymbolicValue, Set<ASALSymbolicValue>> e : inverted.entrySet()) {
//					if (e.getValue().size() > 1) {
//						elems.add(e.getValue().iterator().next().toString() + "/...=>" + e.getKey().toString());
//					} else {
//						elems.add(e.getValue().iterator().next().toString() + "=>" + e.getKey().toString());
//					}
//				}
//				
//				if (elems.size() > 0) {
//					xs.add(lod.id(xe.getKey().getName()) + "=>[" + Texts.concat(elems, ",\\n") + "]");
//				}
//			}
//		}
//		
//		return lod.replaceQuotes(Texts.concat(xs, "\\n"));
//	}
}
