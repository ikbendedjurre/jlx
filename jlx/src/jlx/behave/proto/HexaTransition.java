package jlx.behave.proto;

import java.util.*;

import jlx.asal.parsing.api.*;
import jlx.common.FileLocation;
import jlx.utils.*;

public class HexaTransition {
	private Set<HexaVertex> sourceVertices;
	private Set<HexaVertex> targetVertices;
	private List<List<ConditionalExitBehavior<HexaVertex>>> effectPerms; //We use a list so that SeptaTransitions have a way to reference their permutation!
	private ASALEvent event;
	private ASALExpr guard;
	private PentaTransition legacy;
	
	public HexaTransition(List<PentaTransition> source, Set<PentaVertex> tgtVtxs, Map<PentaVertex, HexaVertex> newVertexPerOldVertex) {
		PentaTransition first = source.get(0);
		//PentaTransition last = source.get(source.size() - 1);
		
		sourceVertices = HashSets.map(first.getSourceVertices(), newVertexPerOldVertex);
		targetVertices = HashSets.map(tgtVtxs, newVertexPerOldVertex);
		event = first.getEvent();
		legacy = first;
		
		List<Set<List<ConditionalExitBehavior<PentaVertex>>>> effectPermsPerIndex = new ArrayList<Set<List<ConditionalExitBehavior<PentaVertex>>>>();
		List<ASALExpr> guards = new ArrayList<ASALExpr>();
		
		for (PentaTransition t : source) {
			if (t.getGuard() != null) {
				guards.add(t.getGuard());
			}
			
			effectPermsPerIndex.add(t.getEffectPerms());
		}
		
		guard = ASALBinaryExpr.fromList(guards, "and", ASALLiteral._true());
		effectPerms = new ArrayList<List<ConditionalExitBehavior<HexaVertex>>>(); 
		
		for (List<List<ConditionalExitBehavior<PentaVertex>>> perm : ArrayLists.allCombinations(effectPermsPerIndex)) {
			List<ConditionalExitBehavior<PentaVertex>> flattenedPerm = new ArrayList<ConditionalExitBehavior<PentaVertex>>();
			
			for (List<ConditionalExitBehavior<PentaVertex>> p : perm) {
				flattenedPerm.addAll(p);
			}
			
			List<ConditionalExitBehavior<HexaVertex>> translatedPerm = translate(flattenedPerm, newVertexPerOldVertex);
			
//			for (ConditionalExitBehavior<HexaVertex> h : translatedPerm) {
//				if (!h.getEffect().containsNonEmpty()) {
//					System.out.println("NON EMPTY:");
//					System.out.println("\t" + h.getEffect().textify(LOD.ONE_LINE));
//				}
//			}
			
			//translatedPerm.removeIf((x) -> { return !x.getEffect().containsNonEmpty(); }); //Why is this removing non-empty statements?!
			effectPerms.add(translatedPerm);
		}
	}
	
	private static List<ConditionalExitBehavior<HexaVertex>> translate(List<ConditionalExitBehavior<PentaVertex>> xs, Map<PentaVertex, HexaVertex> newVertexPerOldVertex) {
		List<ConditionalExitBehavior<HexaVertex>> result = new ArrayList<ConditionalExitBehavior<HexaVertex>>();
		
		for (ConditionalExitBehavior<PentaVertex> x : xs) {
			result.add(x.changeKey(newVertexPerOldVertex));
		}
		
		return result;
	}
	
	public PentaTransition getLegacy() {
		return legacy;
	}
	
	public List<FileLocation> getFileLocations(int permIndex) {
		List<FileLocation> result = new ArrayList<FileLocation>();
		
		for (ConditionalExitBehavior<HexaVertex> ceb : effectPerms.get(permIndex)) {
			result.add(ceb.getEffect().getFileLocation());
		}
		
		return result;
	}
	
	public ASALEvent getEvent() {
		return event;
	}
	
	public ASALExpr getGuard() {
		return guard;
	}
	
	public List<List<ConditionalExitBehavior<HexaVertex>>> getEffectPerms() {
		return effectPerms;
	}
	
	public Set<HexaVertex> getSourceVertices() {
		return sourceVertices;
	}
	
	public Set<HexaVertex> getTargetVertices() {
		return targetVertices;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return legacy.getProtoTrs();
	}
}

