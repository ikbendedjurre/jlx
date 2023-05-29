package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

public class DecaStableSCCTestGen {
	private DecaStableSCCs sccs;
	
	public DecaStableSCCTestGen(DecaStableStateMachine stableSm) {
		sccs = new DecaStableSCCs(stableSm);
		
//		for (DecaStableVertex v : stableSm.vertices) {
//			v.scc = sccs.getSccPerCfg().get(v);
//			
//			if (v.scc == null) {
//				throw new Error("Should not happen!");
//			}
//		}
//		
//		for (DecaStableSCCs.SCC x : new HashSet<DecaStableSCCs.SCC>(sccs.getSccPerRootCfg().values())) {
//			System.out.println("Found SCC at depth " + x.getRootVtx().getLevel() + " has " + x.getVtxs().size() + " vertices");
//			
//			for (DecaStableVertex v : x.getVtxs()) {
//				if (v.scc != x) {
//					throw new Error("Should not happen!");
//				}
//			}
//		}
//		
//		for (DecaStableVertex v : stableSm.vertices) {
//			DecaStableSCCs.SCC x = sccs.getSccPerCfg().get(v);
//			
//			if (!x.getVtxs().contains(x.getRootVtx())) {
//				throw new Error("Should not happen!");
//			}
//			
//			//canReachAll(v, sccs.getSccPerCfg().get(v).getVtxs());
//		}
		
		Set<DecaStableTest> tests = new HashSet<DecaStableTest>();
		int testStepCount = 0;
		
		List<DecaStableSCCs.SCC> xs = new ArrayList<DecaStableSCCs.SCC>(sccs.getSccPerRootCfg().values());
//		Set<DecaStableSCCs.SCC> ys = new HashSet<DecaStableSCCs.SCC>(sccs.getSccPerCfg().values());
		
		Map<DecaStableSCCs.SCC, DecaStableVertex> minLevelVtxPerScc = new HashMap<DecaStableSCCs.SCC, DecaStableVertex>();
		
		for (DecaStableSCCs.SCC x : xs) {
			DecaStableVertex minLevelVtx = null;
			
			for (DecaStableVertex v : x.getVtxs()) {
				if (minLevelVtx == null || minLevelVtx.getLevel() > v.getLevel()) {
					minLevelVtx = v;
				}
			}
			
			minLevelVtxPerScc.put(x, minLevelVtx);
		}
//		
//		if (xs.size() != ys.size()) {
//			throw new Error("Should not happen!");
//		}
//		
//		Set<DecaStableTransition> allTrs = getReachableTrs(stableSm.initialTransition.getTgt(), false);
//		
//		if (!allTrs.equals(stableSm.transitions)) {
//			throw new Error("Unequal transitionss!!!");
//		}
		
//		for (DecaStableSCCs.SCC x : xs) {
//			Set<Integer> cnts = new HashSet<Integer>();
//			
//			for (DecaStableVertex v : x.getVtxs()) {
//				Set<DecaStableTransition> trs = getReachableTrs(v, true);
//				cnts.add(trs.size());
//				
//				if (!allTrs.containsAll(trs)) {
//					throw new Error("rujvblrkendfvbn;nion;");
//				}
//			}
//			
//			if (cnts.size() != 1) {
//				throw new Error("?!");
//			}
//			
//			System.out.println("#x = " + cnts.iterator().next());
//		}
		
//		Set<DecaStableVertex> vs = new HashSet<DecaStableVertex>();
//		
//		for (DecaStableSCCs.SCC x : xs) {
//			vs.addAll(x.getVtxs());
//		}
//		
//		if (!vs.equals(stableSm.vertices)) {
//			throw new Error("Should not happen!");
//		}
//		
//		if ("scc-paths".equals("off")) {
//			for (int xix = 0; xix < xs.size(); xix++) {
//				DecaStableSCCs.SCC x1 = xs.get(xix);
//				System.out.println("Path to SCC " + (xix + 1) + " / " + xs.size() + " (size " + x1.getVtxs().size() + "):");
//				
//				List<DecaStableTransition> p = computePathFromInitVtx(stableSm, minLevelVtxPerScc.get(x1));
//				int tix = 1;
//				
//				for (DecaStableTransition t : p) {
//					System.out.println("\tStep " + tix + " / " + p.size() + ":");
//					tix++;
//					
//					if (t.getSicInputs() != null) {
//						Set<String> elems = new TreeSet<String>();
//						
//						for (Map.Entry<ReprPort, ASALSymbolicValue> e : t.getSicInputs().getValuePerPort().entrySet()) {
//							if (e.getKey().getPossibleValues().size() > 1) {
//								elems.add(e.getKey().getReprOwner().getName() + "::" + e.getKey().getName() + " = " + e.getValue().toString());
//							}
//						}
//						
//						if (elems.size() > 0) {
//							if (t.getSrc() != null) {
//								System.out.println("\t\t" + t.getSrc().getCfg().getDescription());
//							} else {
//								System.out.println("\t\t" + stableSm.legacy.initCfg.getDescription());
//							}
//							
//							for (String elem : elems) {
//								System.out.println("\t\t" + elem);
//							}
//							
//							System.out.println("\t\t" + t.getTgt().getCfg().getDescription());
//						} else {
//							System.out.println("\t\t(empty)");
//						}
//					} else {
//						System.out.println("\t\t(hidden timeouts)");
//					}
//				}
//				
//				for (Map.Entry<ReprPort, ASALSymbolicValue> e : minLevelVtxPerScc.get(x1).getCfg().getOutputVal().getValuePerPort().entrySet()) {
//					System.out.println("\t\t\t" + e.getKey().getReprOwner().getName() + "::" + e.getKey().getName() + " = " + e.getValue().toString());
//				}
//				
//				CLI.waitForEnter();
//			}
//		}
		
		int trsCount = 0;
		
		for (int xix = 0; xix < xs.size(); xix++) {
			List<DecaStableTransition> p = new ArrayList<DecaStableTransition>();
			Set<DecaStableTransition> tgts = new HashSet<DecaStableTransition>();
			trsCount += computeTestPathThroughScc(stableSm, minLevelVtxPerScc.get(xs.get(xix)), p, tgts);
			
			testStepCount += p.size();
			
			tests.add(new DecaStableTest(String.format("test%05d", tests.size()), stableSm, stableSm.initialInputs, p));
			System.out.println("[" + LocalTime.now() + "] scc1 = " + xix + " / " + xs.size() + "; #test-steps = " + testStepCount + "; #tgts = " + trsCount + "; #tgts2 = " + tgts.size() + " / " + getReachableTrs(xs.get(xix).getRootVtx(), true).size());
		}
		
		for (int i1 = 0; i1 < xs.size(); i1++) {
			DecaStableSCCs.SCC x1 = xs.get(i1);
			
			for (DecaStableVertex v1 : x1.getVtxs()) {
				for (DecaStableTransition t1 : v1.getOutgoing()) {
//					int i2 = xs.indexOf(t1.getTgt().scc);
					
					if (t1.getTgt().scc != v1.scc) {
						List<DecaStableTransition> p = computePathFromInitVtx(stableSm, v1);
						p.add(t1);
						testStepCount += p.size();
						
						tests.add(new DecaStableTest(String.format("test%05d", tests.size()), stableSm, stableSm.initialInputs, p));
						trsCount++;
					}
				}
			}
			
			System.out.println("[" + LocalTime.now() + "] scc2 = " + i1 + " / " + xs.size() + "; #test-steps = " + testStepCount + "; #tgts = " + trsCount);
		}
		
//		for (DecaStableTest test : tests) {
//			if (test.getSeq().get(0) != stableSm.initialTransition) {
//				throw new Error("Should not happen!");
//			}
//			
//			for (int i = 1; i < test.getSeq().size(); i++) {
//				DecaStableTransition t1 = test.getSeq().get(i - 1);
//				DecaStableTransition t2 = test.getSeq().get(i);
//				
//				if (t1.getTgt() != t2.getSrc()) {
//					throw new Error("Should not happen!");
//				}
//			}
//		}
		
		System.out.println("#step-count = " + testStepCount);
		
		for (DecaStableTransition t : stableSm.transitions) {
			t.tested = false;
		}
		
		for (DecaStableTest test : tests) {
			for (DecaStableTransition t : test.getSeq()) {
				t.tested = true;
			}
		}
		
		Set<DecaStableTransition> remaining = new HashSet<DecaStableTransition>(stableSm.transitions);
		
		for (DecaStableTest test : tests) {
			remaining.removeAll(test.getSeq());
		}
		
		System.out.println("#untested-transitions = " + remaining.size());
		
//		boolean diff = false;
//		
//		for (DecaStableTransition t : remaining) {
//			if (t.getSrc() != t.getTgt()) {
//				diff = true;
//				break;
//			}
//		}
//		
//		boolean same = false;
//		
//		for (DecaStableTransition t : remaining) {
//			if (t.getSrc() == t.getTgt()) {
//				same = true;
//				break;
//			}
//		}
//		
//		System.out.println("diff.remains = " + diff);
//		System.out.println("same.remains = " + same);
		
//		Set<DecaStableSCCs.SCC> rs = new HashSet<DecaStableSCCs.SCC>();
//		Map<DecaStableSCCs.SCC, Set<DecaStableSCCs.SCC>> sdfiuuhn = new HashMap<DecaStableSCCs.SCC, Set<DecaStableSCCs.SCC>>();
//		
//		int loop1 = 0;
//		int loop2 = 0;
//		int nonloop = 0;
//		
//		for (DecaStableTransition t : remaining) {
//			DecaStableSCCs.SCC x = (DecaStableSCCs.SCC)(t.getSrc().scc);
//			rs.add(x);
//			
//			Permutations.inject(sdfiuuhn, (DecaStableSCCs.SCC)t.getSrc().scc, (DecaStableSCCs.SCC)t.getTgt().scc);
//			
//			if (t.getSrc() == t.getTgt()) {
//				loop1++;
//			} else {
//				if (t.getSrc().equals(t.getTgt())) {
//					loop2++;
//				} else {
//					nonloop++;
//				}
//			}
//			
//			if (t.tested) {
//				throw new Error("frbkldlbkj");
//			}
//			
//			if (!x.getVtxs().contains(t.getSrc())) {
//				throw new Error("rgjoren");
//			}
//			
//			if (!x.getVtxs().contains(t.getTgt())) {
//				throw new Error("slkjvdlv");
//			}
//		}
//		
//		System.out.println("loop1 = " + loop1);
//		System.out.println("loop2 = " + loop2);
//		System.out.println("nonloop = " + nonloop);
//		
//		for (Map.Entry<DecaStableSCCs.SCC, Set<DecaStableSCCs.SCC>> e : sdfiuuhn.entrySet()) {
//			for (DecaStableSCCs.SCC e2 : e.getValue()) {
//				System.out.println(e.getKey().getVtxs().size() + " ===> " + e2.getVtxs().size());
//			}
//		}
//		
//		System.out.println("#rs = " + rs.size());
//		
//		for (DecaStableSCCs.SCC r : rs) {
//			int testedCount = 0;
//			int totalCount = 0;
//			
//			for (DecaStableVertex v : r.getVtxs()) {
//				for (DecaStableTransition t : v.getOutgoing()) {
//					if (t.getTgt().scc == t.getSrc().scc) {
//						totalCount++;
//						
//						if (t.tested) {
//							testedCount++;
//						}
//					}
//				}
//			}
//			
//			System.out.println("SCC at depth " + r.getRootVtx().getLevel() + " has " + r.getVtxs().size() + " vertices; tested " + testedCount + " of " + totalCount + " transitions");
//			
//			
//			
////			int sCount = 0;
////			List<DecaStableTransition> p = computePathFromInitVtx(stableSm, r.getRootVtx());
////			
////			while (extendPathWithinSCC(p)) {
////				sCount++;
////			}
////			
////			System.out.println("Reached " + sCount + " of " + totalCount + " targets");
//			
//			canReachAll(r.getRootVtx(), r.getVtxs());
//		}
	}
	
	private static Set<DecaStableTransition> getReachableTrs(DecaStableVertex v, boolean sameScc) {
		Set<DecaStableTransition> beenHere = new HashSet<DecaStableTransition>();
		
		for (DecaStableTransition succ : v.getOutgoing()) {
			if (!sameScc || succ.getTgt().scc == succ.getSrc().scc) { //Stay within the same SCC!
				beenHere.add(succ);
			}
		}
		
		Set<DecaStableTransition> fringe = new HashSet<DecaStableTransition>();
		Set<DecaStableTransition> newFringe = new HashSet<DecaStableTransition>();
		fringe.addAll(beenHere);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaStableTransition t : fringe) {
				for (DecaStableTransition succ : t.getTgt().getOutgoing()) {
					if (!sameScc || succ.getTgt().scc == succ.getSrc().scc) { //Stay within the same SCC!
						if (beenHere.add(succ)) {
							newFringe.add(succ);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return beenHere;
	}
	
//	private static void canReachAll(DecaStableVertex v, Collection<DecaStableVertex> vs) {
//		Set<DecaStableVertex> beenHere = new HashSet<DecaStableVertex>();
//		beenHere.add(v);
//		
//		Set<DecaStableVertex> fringe = new HashSet<DecaStableVertex>();
//		Set<DecaStableVertex> newFringe = new HashSet<DecaStableVertex>();
//		fringe.add(v);
//		
//		while (fringe.size() > 0) {
//			newFringe.clear();
//			
//			for (DecaStableVertex f : fringe) {
//				for (DecaStableTransition ffff : f.getOutgoing()) {
//					if (ffff.getTgt().scc == ffff.getSrc().scc) {
//						if (beenHere.add(ffff.getTgt())) {
//							newFringe.add(ffff.getTgt());
//						}
//					}
//				}
////				for (DecaStableVertex succ : f.getSuccs()) {
////					if (beenHere.add(succ)) {
////						newFringe.add(succ);
////					}
////				}
//			}
//			
//			fringe.clear();
//			fringe.addAll(newFringe);
//		}
//		
//		if (!beenHere.containsAll(vs)) {
//			Set<DecaStableVertex> unreached = new HashSet<DecaStableVertex>();
//			unreached.addAll(vs);
//			unreached.removeAll(beenHere);
//			
//			for (DecaStableVertex u : unreached) {
//				System.out.println(v.getCfg().getDescription() + " cannot reach " + u.getCfg().getDescription());
//			}
//			
//			System.out.println("#unreached = " + unreached.size());
//			
//			throw new Error("SCC is not an SCC!!");
//		}
//	}
	
	private static List<DecaStableTransition> computePathFromInitVtx(DecaStableStateMachine stableSm, DecaStableVertex tgt) {
		List<DecaStableTransition> result = new ArrayList<DecaStableTransition>();
		DecaStableVertex curr = tgt;
		
		while (curr.getLevel() > 1) {
			DecaStableTransition incoming = getIncoming(curr);
			result.add(0, incoming);
			curr = incoming.getSrc();
		}
		
		result.add(0, stableSm.initialTransition);
		return result;
	}
	
	private static DecaStableTransition getIncoming(DecaStableVertex curr) {
		Set<DecaStableVertex> minLevelPreds = new HashSet<DecaStableVertex>();
		int minPredLevel = curr.getLevel();
		
		for (DecaStableVertex pred : curr.getPreds()) {
			if (pred.getLevel() < minPredLevel) {
				minPredLevel = pred.getLevel();
				minLevelPreds.clear();
			}
			
			if (pred.getLevel() == minPredLevel) {
				minLevelPreds.add(pred);
			}
		}
		
		if (minLevelPreds.size() > 0) {
			for (DecaStableTransition t : RandomUtils.getElem(minLevelPreds).getOutgoing()) {
				if (t.getTgt() == curr) {
					return t;
				}
			}
			
			throw new Error("Should not happen!");
		}
		
//		for (DecaStableVertex pred : RandomUtils.shuffle(curr.getPreds())) {
//			if (pred.getLevel() <= curr.getLevel()) {
//				for (DecaStableTransition t : pred.getOutgoing()) {
//					if (t.getTgt() == curr) {
//						return t;
//					}
//				}
//				
//				throw new Error("Should not happen!");
//			}
//		}
		
		throw new Error("Should not happen!");
	}
	
	private static int computeTestPathThroughScc(DecaStableStateMachine stableSm, DecaStableVertex initVtx, List<DecaStableTransition> p, Set<DecaStableTransition> tgts) {
		for (DecaStableTransition t : stableSm.transitions) {
			t.tested = false;
		}
		
		int result = 0;
		
		p.clear();
		p.addAll(computePathFromInitVtx(stableSm, initVtx));
		
		for (DecaStableTransition t : p) {
			t.tested = true;
		}
		
		while (extendPathWithinSCC(p, tgts)) {
			result++;
		}
		
//		Map<DecaStableSCCs.SCC, Set<DecaStableSCCs.SCC>> sdfiuuhn = new HashMap<DecaStableSCCs.SCC, Set<DecaStableSCCs.SCC>>();
//		
//		int i = 0;
//		int j = 0;
//		
//		for (DecaStableVertex v : x.getVtxs()) {
//			for (DecaStableTransition t : v.getOutgoing()) {
//				if (t.getTgt().scc == t.getSrc().scc) {
//				
//				if (t.tested) {
//					if (t.getTgt().scc == t.getSrc().scc) {
//						j++;
//					}
//					
//					if (!p.contains(t)) {
//						throw new Error("Should not happen!");
//					}
//				} else {
//					if (t.getTgt().scc == t.getSrc().scc) {
//						throw new Error("Should not happen!");
//					}
//					
//					Permutations.inject(sdfiuuhn, (DecaStableSCCs.SCC)v.scc, (DecaStableSCCs.SCC)t.getTgt().scc);
//				}
//			}
//		}
//		
//		System.out.println("i / j = " + i + " / " + j);
//		
//		for (Map.Entry<DecaStableSCCs.SCC, Set<DecaStableSCCs.SCC>> e : sdfiuuhn.entrySet()) {
//			for (DecaStableSCCs.SCC e2 : e.getValue()) {
//				System.out.println(e.getKey().getVtxs().size() + " => " + e2.getVtxs().size());
//			}
//		}
		
		return result;
	}
	
	private static boolean extendPathWithinSCC(List<DecaStableTransition> path, Set<DecaStableTransition> tgts) {
		List<DecaStableTransition> detSeqDest = new ArrayList<DecaStableTransition>();
		DecaStableTransition tgt = findClosestUntested(path.get(path.size() - 1), detSeqDest);
		
		if (tgt != null) {
			if (tgt.getTgt().scc != tgt.getSrc().scc) {
				throw new Error("e");
			}
			
			if (path.get(path.size() - 1).getTgt().scc != tgt.getSrc().scc) {
				throw new Error("f");
			}
			
			if (!detSeqDest.contains(tgt)) {
				throw new Error("bla");
			}
			
			path.addAll(detSeqDest);
			tgts.add(tgt);
			
			for (DecaStableTransition t : detSeqDest) {
				t.tested = true;
			}
			
			return true;
		}
		
		return false;
	}
	
	private static DecaStableTransition findClosestUntested(DecaStableTransition start, List<DecaStableTransition> detSeqDest) {
		Map<DecaStableTransition, DecaStableTransition> genPerCfg = new HashMap<DecaStableTransition, DecaStableTransition>();
		
		Set<DecaStableTransition> beenHere = new HashSet<DecaStableTransition>();
		beenHere.add(start);
		
		Set<DecaStableTransition> fringe = new HashSet<DecaStableTransition>();
		Set<DecaStableTransition> newFringe = new HashSet<DecaStableTransition>();
		fringe.add(start);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (DecaStableTransition t : RandomUtils.shuffle(fringe)) {
				if (!t.tested) {
					List<DecaStableTransition> prefixSeq = new ArrayList<DecaStableTransition>();
					DecaStableTransition curr = t;
					
					while (curr != start) {
						prefixSeq.add(0, curr);
						curr = genPerCfg.get(curr);
					}
					
					detSeqDest.addAll(prefixSeq);
					return t;
				}
				
				for (DecaStableTransition succ : t.getTgt().getOutgoing()) {
					if (succ.getTgt().scc == succ.getSrc().scc) { //Stay within the same SCC!
						if (beenHere.add(succ)) {
							genPerCfg.put(succ, t);
							newFringe.add(succ);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return null;
	}
}

