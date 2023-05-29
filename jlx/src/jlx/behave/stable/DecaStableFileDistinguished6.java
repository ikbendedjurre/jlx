package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

public class DecaStableFileDistinguished6 extends DecaStableFileMinimized {
	public DecaStableFileDistinguished6() {
		
	}
	
	private Map<EquivClz, Integer> getDepthPerEquivClz() {
		Map<Vertex, Integer> result = new HashMap<Vertex, Integer>();
		result.put(getInitialTransition().getTgt(), 0);
		
		Set<Vertex> fringe = new HashSet<Vertex>();
		Set<Vertex> newFringe = new HashSet<Vertex>();
		fringe.add(getInitialTransition().getTgt());
		int depth = 0;
		
		while (fringe.size() > 0) {
			newFringe.clear();
			depth++;
			
			for (Vertex v : fringe) {
				for (Transition t : v.getOutgoing()) {
					if (!result.containsKey(t.getTgt())) {
						result.put(t.getTgt(), depth);
						newFringe.add(t.getTgt());
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		Map<EquivClz, Integer> result2 = new HashMap<EquivClz, Integer>();
		
		for (Map.Entry<Vertex, Integer> e : result.entrySet()) {
			result2.put(getEquivClzPerVtx().get(e.getKey()), e.getValue());
		}
		
		return result2;
	}
	
	private Map<EquivClz, Integer> getFreqPerEquivClz() {
		Map<EquivClz, Integer> result = new HashMap<EquivClz, Integer>();
		
		for (Transition t : getTransitions()) {
			HashMaps.increment(result, getEquivClzPerVtx().get(t.getTgt()), 1, 0);
		}
		
		return result;
	}
	
	@Override
	public void init() {
		super.init();
		
		Map<InitState, Set<EquivClz>> ecsPerInitState = new HashMap<InitState, Set<EquivClz>>();
		
		for (EquivClz v : getEquivClzs()) {
			HashMaps.inject(ecsPerInitState, new InitState(v.someVtx()), v);
		}
		
		Map<EquivClz, Integer> depthPerEquivClz = getDepthPerEquivClz();
		Map<EquivClz, Integer> freqPerEquivClz = getFreqPerEquivClz();
		
//		Map<EquivClz, Set<Bla>> filters = new HashMap<EquivClz, Set<Bla>>();
		
		int xix = 0;
		int stepCount = 0;
		
		for (Set<EquivClz> xs : ecsPerInitState.values()) {
			Set<InputChanges> ics = new HashSet<InputChanges>();
			xix++;
			
			for (EquivClz x : xs) {
				ics.addAll(x.distInputChanges);
			}
			
			Map<Response, Set<EquivClz>> bestDfjol = new HashMap<Response, Set<EquivClz>>();
			InputChanges bestDfjolIc = null;
			int bestDfjolValue = 0;
			
			for (InputChanges ic : ics) {
				Map<Response, Set<EquivClz>> dfjol = new HashMap<Response, Set<EquivClz>>();
				
				for (EquivClz x : xs) {
					Transition t = x.someVtx().getOutgoingTransition(ic);
					Response response = new Response(t);
					//Permutations.inject(dfjol, response, getEquivClzPerVtx().get(t.getTgt()));
					HashMaps.inject(dfjol, response, x);
				}
				
				if (bestDfjolValue == 0 || dfjol.size() > bestDfjolValue) {
					bestDfjolValue = dfjol.size();
					bestDfjol = dfjol;
					bestDfjolIc = ic;
				}
			}
			
			Set<List<InputChanges>> icSeqs = new HashSet<List<InputChanges>>();
			icSeqs.add(Collections.singletonList(bestDfjolIc));
			
			int yiy = 0;
			
			for (Map.Entry<Response, Set<EquivClz>> fjlef : bestDfjol.entrySet()) {
				yiy++;
				
				String s = "[" + xix + " / " + ecsPerInitState.size() + "]";
				s += "[" + yiy + " / " + bestDfjol.size() + "]";
				s += "[#xs = " + fjlef.getValue().size() + "]";
				//filters.putAll(bla(new Bla(fjlef.getValue()), s));
				addFilters(new Bla(fjlef.getValue()), s, icSeqs);
			}
			
			System.out.println("#icSeqs = " + icSeqs.size());
			
			Map<Map<List<InputChanges>, List<Response>>, Set<EquivClz>> undist = new HashMap<Map<List<InputChanges>, List<Response>>, Set<EquivClz>>();
			
			int zix = 0;
			
			for (EquivClz x : xs) {
				zix++;
				
				System.out.println("zix = " + zix + " / " + xs.size());
				
				HashMaps.inject(undist, computeResponseMap(x, icSeqs), x);
			}
			
			System.out.println("#icSeqs2 = " + icSeqs.size());
			
			for (Set<EquivClz> u : undist.values()) {
				if (u.size() > 1) {
					throw new Error(":-(");
				}
			}
			
//			for (EquivClz x : xs) {
//				int freq = freqPerEquivClz.get(x);
//				int depth = depthPerEquivClz.get(x);
//				stepCount += freq * depth * icSeqs.size();
//			}
		}
		
//		int filterCount = 0;
//		
//		for (Map.Entry<EquivClz, Set<Bla>> e : filters.entrySet()) {
//			filterCount += e.getValue().size();
//		}
//		
//		System.out.println("#filters = " + filterCount);
		
		
	}
	
	private Map<List<InputChanges>, List<Response>> computeResponseMap(EquivClz x, Set<List<InputChanges>> icSeqs) {
		Map<List<InputChanges>, List<Response>> result = new HashMap<List<InputChanges>, List<Response>>();
		
		for (List<InputChanges> icSeq : icSeqs) {
			result.put(icSeq, computeResponses(x.someVtx(), icSeq));
		}
		
		return result;
	}
	
	private List<Response> computeResponses(Vertex v, List<InputChanges> icSeq) {
		List<Response> result = new ArrayList<Response>();
		Vertex curr = v;
		
		for (InputChanges ic : icSeq) {
			if (curr.getOutgoingInputChanges().contains(ic)) {
				Transition t = curr.getOutgoingTransition(ic);
				Response response = new Response(t);
				result.add(response);
				curr = t.getTgt();
			} else {
				break;
			}
		}
		
		return result;
	}
	
	private void addFilters(Bla start, String prefix, Set<List<InputChanges>> dest) {
		for (Map.Entry<EquivClz, Set<Bla>> e : bla(start, prefix).entrySet()) {
			for (Bla bla : e.getValue()) {
				dest.add(bla.getInputChangesSeq());
			}
		}
	}
	
	private Map<EquivClz, Set<Bla>> computeFilters(Collection<EquivClz> xs, Map<List<EquivClz>, Set<Bla>> distSets) {
		Map<EquivClz, Set<Bla>> result = new HashMap<EquivClz, Set<Bla>>();
		
//		System.out.println("#xs * #distSets = " + xs.size() + " x " + distSets.size());
//		int xix = 0;
		
		for (EquivClz x : xs) {
//			if (xix % 100 == 0) {
//				System.out.println("xix = " + xix + " / " + xs.size());
//			}
//			
//			xix++;
			
			Set<Bla> filter = computeFilter(x, xs, distSets);
			
			if (filter != null) {
				result.put(x, filter);
			}
		}
		
		return result;
	}
	
	private Set<Bla> computeFilter(EquivClz x, Collection<EquivClz> xs, Map<List<EquivClz>, Set<Bla>> distSets) {
		for (int maxFilterSize = 1; maxFilterSize <= 30; maxFilterSize++) {
			Set<EquivClz> remaining = new HashSet<EquivClz>(xs);
			Map<List<EquivClz>, Set<Bla>> filter = new HashMap<List<EquivClz>, Set<Bla>>();
			
			for (Map.Entry<List<EquivClz>, Set<Bla>> distSet : distSets.entrySet()) {
				if (distSet.getKey().size() > maxFilterSize) {
					continue;
				}
				
				if (!distSet.getKey().contains(x)) {
					continue;
				}
				
				if (remaining.retainAll(distSet.getKey())) {
					filter.put(distSet.getKey(), distSet.getValue());
				}
				
				if (remaining.size() == 1) {
					Set<Bla> blas = new HashSet<Bla>();
					
					for (Map.Entry<List<EquivClz>, Set<Bla>> e : randomFilterMin(filter, xs).entrySet()) {
						blas.addAll(e.getValue());
					}
					
					return blas;
				}
			}
		}
		
		return null;
	}
	
	private Map<List<EquivClz>, Set<Bla>> randomFilterMin(Map<List<EquivClz>, Set<Bla>> filter, Collection<EquivClz> xs) {
		List<List<EquivClz>> urfh = new ArrayList<List<EquivClz>>();
		urfh.addAll(filter.keySet());
		Collections.shuffle(urfh);
		
		for (int index = 0; index < urfh.size(); ) {
			List<EquivClz> us = urfh.get(index);
			urfh.remove(index);
			
			if (isFilter(urfh, xs)) {
				//Empty.
			} else {
				urfh.add(index, us);
				index++;
			}
		}
		
		Map<List<EquivClz>, Set<Bla>> result = new HashMap<List<EquivClz>, Set<Bla>>();
		
		for (List<EquivClz> cs : urfh) {
			result.put(cs, filter.get(cs));
		}
		
		return result;
	}
	
	private boolean isFilter(List<List<EquivClz>> candidate, Collection<EquivClz> xs) {
		Set<EquivClz> remaining = new HashSet<EquivClz>(xs);
		
		for (List<EquivClz> cs : candidate) {
			remaining.retainAll(cs);
			
			if (remaining.size() == 1) {
				return true;
			}
		}
		
		return false;
	}
	
	private Map<EquivClz, Set<Bla>> bla(Bla start, String prefix) {
		System.out.println(prefix + "[" + LocalTime.now() + "]");
		
		Map<List<EquivClz>, Set<Bla>> blasPerUndist = new HashMap<List<EquivClz>, Set<Bla>>();
		HashMaps.inject(blasPerUndist, start.getUndistinguished(), start);
		
		Map<EquivClz, Set<Bla>> distinguished = new HashMap<EquivClz, Set<Bla>>();
		Set<EquivClz> undistinguished = new HashSet<EquivClz>(start.getUndistinguished());
		
		Map<Map<EquivClz, Set<EquivClz>>, Bla> beenHere = new HashMap<Map<EquivClz, Set<EquivClz>>, Bla>();
		beenHere.put(start.potsPerInit, start);
		
		Set<Bla> fringe = new HashSet<Bla>();
		Set<Bla> newFringe = new HashSet<Bla>();
		fringe.add(start);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (Bla bla : fringe) {
				HashMaps.inject(blasPerUndist, bla.getUndistinguished(), bla);
				
				if (bla.getPotentials().size() == 1) {
					
				} else {
					newFringe.add(bla);
				}
			}
			
//			System.out.println("[#xs = " + start.getUndistinguished().size() + "][" + LocalTime.now() + "] A");
			
			Map<EquivClz, Set<Bla>> newDistinguished = computeFilters(undistinguished, blasPerUndist);
			undistinguished.removeAll(newDistinguished.keySet());
			distinguished.putAll(newDistinguished);
			
			if (undistinguished.isEmpty()) {
				System.out.println("[" + LocalTime.now() + "] Distinguished all " + start.getUndistinguished().size() + "!");
				return distinguished;
			}
			
//			System.out.println("[#xs = " + start.getUndistinguished().size() + "][" + LocalTime.now() + "] B");
			
			fringe.clear();
			fringe.addAll(newFringe);
			newFringe.clear();
			
			for (Bla bla : fringe) {
				for (BlaTr outgoing : computeOutgoing(bla, undistinguished)) {
					if (!beenHere.containsKey(outgoing.tgt.potsPerInit)) {
						beenHere.put(outgoing.tgt.potsPerInit, bla);
						newFringe.add(outgoing.tgt);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[#xs = " + start.getUndistinguished().size() + "][" + LocalTime.now() + "] #beenHere = " + beenHere.size() + " (+" + fringe.size() + "); #blasPerUndist = " + blasPerUndist.size() + "; #distinguished = " + distinguished.size() + " / " + start.potsPerInit.size());
		}
		
		{
			Map<EquivClz, Set<Bla>> newDistinguished = computeFilters(start.getUndistinguished(), blasPerUndist);
			undistinguished.removeAll(newDistinguished.keySet());
			distinguished.putAll(newDistinguished);
			
			if (undistinguished.isEmpty()) {
				System.out.println("[" + LocalTime.now() + "] Distinguished all " + start.getUndistinguished().size() + "!");
			} else {
				System.out.println("[" + LocalTime.now() + "] FAILED TO DISTINGUISH ALL " + start.getUndistinguished().size());
			}
			
			return distinguished;
		}
	}
	
	private static class Bla {
		public final Map<EquivClz, Set<EquivClz>> potsPerInit;
		public final BlaTr incoming;
		
		public Bla(Set<EquivClz> inits) {
			potsPerInit = new HashMap<EquivClz, Set<EquivClz>>();
			
			for (EquivClz init : inits) {
				potsPerInit.put(init, Collections.singleton(init));
			}
			
			incoming = null;
		}
		
		public Bla(Map<EquivClz, Set<EquivClz>> potsPerInit, BlaTr incoming) {
			this.potsPerInit = potsPerInit;
			this.incoming = incoming;
		}
		
		public Set<InputChanges> getOutgoingInputChanges() {
			Set<InputChanges> result = new HashSet<InputChanges>();
			
			for (Set<EquivClz> xs : potsPerInit.values()) {
				for (EquivClz x : xs) {
					result.addAll(x.distInputChanges);
				}
			}
			
			return result;
		}
		
		public List<EquivClz> getUndistinguished() {
			return new ArrayList<EquivClz>(potsPerInit.keySet());
		}
		
		public Set<Set<EquivClz>> getPotentials() {
			return new HashSet<Set<EquivClz>>(potsPerInit.values());
		}
		
//		public Map<EquivClz, Set<EquivClz>> computeInitsPerPot() {
//			Map<EquivClz, Set<EquivClz>> result = new HashMap<EquivClz, Set<EquivClz>>();
//			
//			for (Map.Entry<EquivClz, Set<EquivClz>> e : potsPerInit.entrySet()) {
//				Permutations.inject(result, e.getValue(), e.getKey());
//			}
//			
//			return result;
//		}
		
		public List<InputChanges> getInputChangesSeq() {
			List<InputChanges> result = new ArrayList<InputChanges>();
			BlaTr curr = incoming;
			
			while (curr != null) {
				result.add(0, curr.ic);
				curr = curr.src.incoming;
			}
			
			return result;
		}
	}
	
	private static class BlaTr {
		public final Bla src;
		public final Bla tgt;
		public final InputChanges ic;
		public final Response response;
		
		public BlaTr(Bla src, Map<EquivClz, Set<EquivClz>> newPotsPerInit, InputChanges ic, Response response) {
			this.src = src;
			this.tgt = new Bla(newPotsPerInit, this);
			this.ic = ic;
			this.response = response;
		}
	}
	
	private Set<BlaTr> computeOutgoing(Bla src, Set<EquivClz> domain) {
		Set<BlaTr> result = new HashSet<BlaTr>();
		
		for (InputChanges ic : src.getOutgoingInputChanges()) {
			Map<Response, Map<EquivClz, Set<EquivClz>>> perResponse = new HashMap<Response, Map<EquivClz, Set<EquivClz>>>();
			
			for (Map.Entry<EquivClz, Set<EquivClz>> e : src.potsPerInit.entrySet()) {
				if (domain.contains(e.getKey())) {
					for (EquivClz x : e.getValue()) {
						Transition t = x.someVtx().getOutgoingTransition(ic);
						Response response = new Response(t);
						HashMaps.injectInject(perResponse, response, e.getKey(), getEquivClzPerVtx().get(t.getTgt()));
					}
				}
			}
			
			for (Map.Entry<Response, Map<EquivClz, Set<EquivClz>>> e : perResponse.entrySet()) {
				result.add(new BlaTr(src, e.getValue(), ic, e.getKey()));
			}
		}
		
		return result;
	}
	
//	private InputChanges computeGreedySplitter(Set<EquivClz> xs) {
//		Set<InputChanges> ics = new HashSet<InputChanges>();
//		
//		for (EquivClz x : xs) {
//			ics.addAll(x.distInputChanges);
//		}
//		
//		Map<Response, Set<EquivClz>> bestPerResponse = null;
//		InputChanges bestIc = null;
//		
//		for (InputChanges ic : ics) {
//			Map<Response, Set<EquivClz>> perResponse = applySplitter(xs, ic);
//			
//			if (bestPerResponse == null || perResponse.size() > bestPerResponse.size()) {
//				bestPerResponse = perResponse;
//				bestIc = ic;
//			}
//		}
//		
//		return bestIc;
//	}
//	
//	private InputChanges computeGreedyOneSplitter(Set<EquivClz> xs) {
//		Set<InputChanges> ics = new HashSet<InputChanges>();
//		
//		for (EquivClz x : xs) {
//			ics.addAll(x.distInputChanges);
//		}
//		
//		Map<Response, Set<EquivClz>> bestPerResponse = null;
//		int bestOneCount = 0;
//		InputChanges bestIc = null;
//		
//		for (InputChanges ic : ics) {
//			Map<Response, Set<EquivClz>> perResponse = applySplitter(xs, ic);
//			int oneCount = getOneCount(perResponse);
//			
//			if (bestPerResponse == null || oneCount > bestOneCount) {
//				bestPerResponse = perResponse;
//				bestOneCount = oneCount;
//				bestIc = ic;
//			}
//		}
//		
//		return bestIc;
//	}
//	
//	private int getOneCount(Map<Response, Set<EquivClz>> x) {
//		int result = 0;
//		
//		for (Map.Entry<Response, Set<EquivClz>> e : x.entrySet()) {
//			if (e.getValue().size() == 1) {
//				result++;
//			}
//		}
//		
//		return result;
//	}
//	
//	private Map<Response, Set<EquivClz>> applySplitter(Set<EquivClz> xs, InputChanges ic) {
//		Map<Response, Set<EquivClz>> result = new HashMap<Response, Set<EquivClz>>();
//		
//		for (EquivClz x : xs) {
//			Transition t = x.someVtx().getOutgoingTransition(ic);
//			Permutations.inject(result, new Response(t), x);
//		}
//		
//		return result;
//	}
	
	
	public static void main(String[] args) {
		DecaStableFileDistinguished6 x = new DecaStableFileDistinguished6();
//		x.loadFromFile("models", "all.reduced.3.stable", true);
		
		x.loadFromFile("models", "all.reduced.2.stable", true);
		x.init();
		x.saveToFile("models", "all.reduced.3.stable");
		
//		System.out.println("#vtxs = " + x.getVertices().size());
//		System.out.println("#reduced = " + x.equivClzs.size());
	}
}
