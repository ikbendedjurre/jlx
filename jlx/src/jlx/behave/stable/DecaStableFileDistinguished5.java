package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

public class DecaStableFileDistinguished5 extends DecaStableFileMinimized {
	public DecaStableFileDistinguished5() {
		
	}
	
	@Override
	public void init() {
		super.init();
		
		Map<InitState, Set<EquivClz>> vtxsPerInitState2 = new HashMap<InitState, Set<EquivClz>>();
		
		for (EquivClz v : getEquivClzs()) {
			HashMaps.inject(vtxsPerInitState2, new InitState(v.someVtx()), v);
		}
		
		int successCount = 0;
		int totalCount = 0;
		
		for (Map.Entry<InitState, Set<EquivClz>> e : vtxsPerInitState2.entrySet()) {
			int localTotalCount = 0;
			
			for (EquivClz x : e.getValue()) {
				if (distinguish(x, e.getValue(), 10000)) {
					successCount++;
				}
				
				totalCount++;
				localTotalCount++;
				
				System.out.println("[" + LocalTime.now() + "] progress = " + totalCount + " / " + getEquivClzs().size() + " -> " + localTotalCount + " / " + e.getValue().size() + "; #succs = " + successCount + " / " + totalCount);
			}
		}
	}
	
	private boolean distinguish(EquivClz x2, Set<EquivClz> xs, int maxDepth) {
		Set<EquivClz> remainingInits = new HashSet<EquivClz>();
		Set<EquivClz> newRemainingInits = new HashSet<EquivClz>();
		remainingInits.addAll(xs);
		
		Set<List<InputChanges>> unfinishedSeqs = new HashSet<List<InputChanges>>();
		Set<List<InputChanges>> newUnfinishedSeqs = new HashSet<List<InputChanges>>();
		unfinishedSeqs.add(Collections.emptyList());
		
		while (remainingInits.size() > 0) {
			newRemainingInits.clear();
			newRemainingInits.add(x2);
			
			ResponseSeqSet rss = computeResponseSeqSet(x2, unfinishedSeqs);
			
			for (EquivClz x : remainingInits) {
//				System.out.println("1");
				
				if (x != xs) {
					if (matchesResponseSeqSet(x, rss, unfinishedSeqs)) {
						newRemainingInits.add(x);
					}
				}
			}
			
			remainingInits.clear();
			remainingInits.addAll(newRemainingInits);
			newRemainingInits.clear();
			
			if (remainingInits.size() > 1) {
				for (List<InputChanges> icSeq : unfinishedSeqs) {
					Map<EquivClz, EquivClz> potPerInit = new HashMap<EquivClz, EquivClz>();
//					System.out.println("2");
					
					for (EquivClz init : remainingInits) {
						potPerInit.put(init, init);
					}
					
					Set<Bla> beenHere = new HashSet<Bla>();
					
					for (InputChanges ic : icSeq) {
						Bla bla = computeNextBla(potPerInit, ic);
						beenHere.add(bla);
						potPerInit = bla.potPerInit;
					}
					
					Set<EquivClz> pots = new HashSet<EquivClz>(potPerInit.values());
					Set<InputChanges> dics = getDistinguishingInputChanges(pots);
					
					if (dics.size() > 0) {
						newRemainingInits.addAll(remainingInits);
						
						for (InputChanges dic : dics) {
							Bla bla = computeNextBla(potPerInit, dic);
							
							if (!beenHere.contains(bla)) {
								List<InputChanges> newIcSeq = new ArrayList<InputChanges>(icSeq);
								newIcSeq.add(dic);
								newUnfinishedSeqs.add(newIcSeq);
							}
						}
					} else {
						newUnfinishedSeqs.add(icSeq);
					}
				}
			} else {
				randomTrim(x2, xs, newUnfinishedSeqs);
				System.out.println("[" + LocalTime.now() + "] #finishedSeqs = " + unfinishedSeqs.size());
				return true;
			}
			
			remainingInits.clear();
			remainingInits.addAll(newRemainingInits);
			unfinishedSeqs.clear();
			unfinishedSeqs.addAll(newUnfinishedSeqs);
			
			System.out.println("[" + LocalTime.now() + "] #remainingInits = " + remainingInits.size() + " / "+ xs.size() +"; #unfinishedSeqs = " + newUnfinishedSeqs.size() + " -> " + unfinishedSeqs.size());
		}
		
		return false;
	}
	
	private Set<List<InputChanges>> randomTrim(EquivClz x2, Set<EquivClz> xs, Set<List<InputChanges>> icSeqs) {
		ResponseSeqSet rss = computeResponseSeqSet(x2, icSeqs);
		List<List<InputChanges>> icSeqs2 = new ArrayList<List<InputChanges>>(icSeqs);
		Collections.shuffle(icSeqs2);
		
		for (int index = 0; index < icSeqs2.size(); ) {
			List<InputChanges> ic = icSeqs2.get(index);
			icSeqs2.remove(index);
			
			if (isUniqueResponseSeqSet(x2, xs, rss, icSeqs2)) {
				//Do nothing.
			} else {
				icSeqs2.add(index, ic);
				index++;
			}
			
//			System.out.println(index + " / " + icSeqs2.size() + " / " + icSeqs.size());
		}
		
		return new HashSet<List<InputChanges>>(icSeqs2);
	}
	
	private boolean isUniqueResponseSeqSet(EquivClz x, Set<EquivClz> xs, ResponseSeqSet rss, Collection<List<InputChanges>> icSeqs) {
		if (icSeqs.isEmpty()) {
			return false;
		}
		
		for (EquivClz y : xs) {
			if (y != x && matchesResponseSeqSet(y, rss, icSeqs)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean matchesResponseSeqSet(EquivClz x, ResponseSeqSet rss, Collection<List<InputChanges>> icSeqs) {
		for (List<InputChanges> icSeq : icSeqs) {
			List<Response> responseSeq = rss.perInputChangesSeq.get(icSeq);
			EquivClz curr = x;
			
			for (int index = 0; index < icSeq.size(); index++) {
				Transition t = curr.someVtx().getOutgoingTransition(icSeq.get(index));
				
				if (!new Response(t).equals(responseSeq.get(index))) {
					return false;
				}
				
				curr = getEquivClzPerVtx().get(t.getTgt());
			}
		}
		
		return true;
	}
	
	private ResponseSeqSet computeResponseSeqSet(EquivClz x, Collection<List<InputChanges>> icSeqs) {
		Map<List<InputChanges>, List<Response>> perInputChangesSeq = new HashMap<List<InputChanges>, List<Response>>();
		
		for (List<InputChanges> icSeq : icSeqs) {
			List<Response> responseSeq = new ArrayList<Response>();
			EquivClz curr = x;
			
			for (InputChanges ic : icSeq) {
				Transition t = curr.someVtx().getOutgoingTransition(ic);
				responseSeq.add(new Response(t));
				curr = getEquivClzPerVtx().get(t.getTgt());
			}
			
			perInputChangesSeq.put(icSeq, responseSeq);
		}
		
		return new ResponseSeqSet(perInputChangesSeq);
	}
	
	private static class ResponseSeqSet {
		public final Map<List<InputChanges>, List<Response>> perInputChangesSeq;
		public final int hashCode;
		
		public ResponseSeqSet(Map<List<InputChanges>, List<Response>> perInputChangesSeq) {
			this.perInputChangesSeq = perInputChangesSeq;
			
			hashCode = perInputChangesSeq.hashCode();
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ResponseSeqSet other = (ResponseSeqSet) obj;
			return Objects.equals(perInputChangesSeq, other.perInputChangesSeq);
		}
	}
	
	private Bla computeNextBla(Map<EquivClz, EquivClz> potPerInit, InputChanges inputChanges) {
		Map<EquivClz, EquivClz> newPotPerInit = new HashMap<EquivClz, EquivClz>();
		Map<EquivClz, Response> responsePerInit = new HashMap<EquivClz, Response>();
		
		for (Map.Entry<EquivClz, EquivClz> e3 : potPerInit.entrySet()) {
			Transition t = e3.getValue().someVtx().getOutgoingTransition(inputChanges);
			newPotPerInit.put(e3.getKey(), getEquivClzPerVtx().get(t.getTgt()));
			responsePerInit.put(e3.getKey(), new Response(t));
		}
		
		return new Bla(newPotPerInit, responsePerInit);
	}
	
	private static class Bla {
		public final Map<EquivClz, EquivClz> potPerInit;
		public final Map<EquivClz, Response> responsePerInit;
		public final int hashCode;
		
		public Bla(Map<EquivClz, EquivClz> potPerInit, Map<EquivClz, Response> responsePerInit) {
			this.potPerInit = potPerInit;
			this.responsePerInit = responsePerInit;
			
			hashCode = Objects.hash(potPerInit, responsePerInit);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bla other = (Bla) obj;
			return Objects.equals(potPerInit, other.potPerInit) && Objects.equals(responsePerInit, other.responsePerInit);
		}
	}
	
	private Set<InputChanges> getDistinguishingInputChanges(Set<EquivClz> xs) {
		Set<InputChanges> ics = new HashSet<InputChanges>();
		
		if (xs.size() > 1) {
			for (EquivClz x : xs) {
//				for (Map.Entry<Pair<Vertex>, Set<InputChanges>> e : x.reasonsPerPair.entrySet()) {
//					EquivClz ec1 = getEquivClzPerVtx().get(e.getKey().getElem1());
//					EquivClz ec2 = getEquivClzPerVtx().get(e.getKey().getElem2());
//					
//					if (xs.contains(ec1) && xs.contains(ec2)) {
//						ics.addAll(e.getValue());
//					}
//				}
				
				ics.addAll(x.distInputChanges);
			}
		}
		
		return ics;
	}
	
	public static void main(String[] args) {
		DecaStableFileDistinguished5 x = new DecaStableFileDistinguished5();
//		x.loadFromFile("models", "all.reduced.3.stable", true);
		
		x.loadFromFile("models", "all.reduced.2.stable", true);
		x.init();
		x.saveToFile("models", "all.reduced.3.stable");
		
//		System.out.println("#vtxs = " + x.getVertices().size());
//		System.out.println("#reduced = " + x.equivClzs.size());
	}
}
