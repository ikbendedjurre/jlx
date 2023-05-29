package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

import jlx.utils.*;

public class DecaStableTestGen {
	private DecaStableStateMachine sm;
	private Set<DecaStableTransition> remainingTargets;
	private Map<Integer, Set<DecaStableTransition>> remainingTargetsPerLevel;
	private int minLevel;
	
	public DecaStableTestGen(DecaStableStateMachine sm) {
		this.sm = sm;
		
		remainingTargets = new HashSet<DecaStableTransition>();
		remainingTargetsPerLevel = new HashMap<Integer, Set<DecaStableTransition>>();
		minLevel = Integer.MAX_VALUE;
		
		for (DecaStableTransition t : sm.transitions) {
			if (t.getSrc() != t.getTgt()) {
				remainingTargets.add(t);
				HashMaps.inject(remainingTargetsPerLevel, t.getSrc().getLevel(), t);
				minLevel = Math.min(minLevel, t.getSrc().getLevel());
				t.tested = false;
			} else {
				t.tested = true;
			}
		}
		
		//removeRemainingTargets(Collections.singleton(sm.initialTransition));
		sm.initialTransition.tested = true;
		
//		int index = 0;
//		
//		for (DecaStableTransition tgt : remainingTargets) {
//			if (index % 1000 == 0) {
//				System.out.println("Confirmed that " + index + " / " + remainingTargets.size() + " targets are reachable.");
//			}
//			
//			index++;
//			
//			List<DecaStableTransition> detSeqDest = new ArrayList<DecaStableTransition>();
//			DecaStableTransition r = findClosest(sm.initialTransition, Collections.singleton(tgt), tgt.getSrc().getLevel(), detSeqDest);
//			
//			if (r == null) {
//				throw new Error("Cannot find path to transition at depth " + tgt.getSrc().getLevel() + "!");
//			}
//		}
//		
//		System.out.println("Confirmed that " + remainingTargets.size() + " / " + remainingTargets.size() + " targets are reachable.");
	}
	
	public List<DecaStableTest> generateTests(int maxTestCount) {
		List<DecaStableTest> result = new ArrayList<DecaStableTest>();
		List<DecaStableTransition> test;
		int stepCount = 0;
		
		long prevTime = System.currentTimeMillis();
		
		System.out.println("[" + LocalTime.now() + "] #targets = " + remainingTargets.size() + " / " + sm.transitions.size() + "; #tests = " + result.size() + "; #steps = " + stepCount);
		
		while ((test = generateTest()) != null && result.size() < maxTestCount) {
			result.add(new DecaStableTest(String.format("Test%05d", result.size()), sm, sm.initialInputs, test));
			stepCount += test.size();
			
			long currTime = System.currentTimeMillis();
			
			if (currTime - prevTime > 10000L) {
				System.out.println("[" + LocalTime.now() + "] #targets = " + remainingTargets.size() + " / " + sm.transitions.size() + "; #tests = " + result.size() + "; #steps = " + stepCount);
				prevTime = currTime;
			}
		}
		
		System.out.println("[" + LocalTime.now() + "] #targets = " + remainingTargets.size() + " / " + sm.transitions.size() + "; #tests = " + result.size() + "; #steps = " + stepCount);
		return result;
	}
	
	public List<DecaStableTransition> generateTest() {
		if (remainingTargets.isEmpty()) {
			return null;
		}
		
		List<DecaStableTransition> result = new ArrayList<DecaStableTransition>();
		result.add(sm.initialTransition);
		
		if (extendTest(result)) {
			while (extendTest(result)) {
				//Empty.
			}
			
			return result;
		}
		
		throw new Error("Should not happen!");
	}
	
	private boolean extendTest(List<DecaStableTransition> test) {
		if (remainingTargets.isEmpty()) {
			return false;
		}
		
		int maxDepth = minLevel;
		
//		int maxDepth = Integer.MAX_VALUE;
//		
//		for (DecaStableTransition tgt : remainingTargets) {
//			maxDepth = Math.min(maxDepth, tgt.getSrc().getLevel());
//		}
		
		List<DecaStableTransition> detSeq = new ArrayList<DecaStableTransition>();
		DecaStableTransition target = findClosest(test.get(test.size() - 1), remainingTargets, maxDepth, detSeq);
		
		//There is no way to reach any of the remaining targets,
		//OR it is faster to get to a target by starting a new test:
		if (target == null) {
//			System.out.println("no target");
			return false;
		}
		
//		System.out.println("test extended by " + detSeq.size());
		
		//Extend test:
		test.addAll(detSeq);
		
		//Remove targets:
		removeRemainingTargets(detSeq);
		
		return true;
	}
	
	private void removeRemainingTargets(Collection<DecaStableTransition> targets) {
		for (DecaStableTransition target : targets) {
			if (!target.tested) {
				target.tested = true;
				remainingTargets.remove(target);
				
				int key = target.getSrc().getLevel();
				Set<DecaStableTransition> value = remainingTargetsPerLevel.get(key);
				value.remove(target);
				
				if (value.isEmpty()) {
					remainingTargetsPerLevel.remove(key);
					
					if (key == minLevel && remainingTargetsPerLevel.size() > 0) {
						while (!remainingTargetsPerLevel.containsKey(minLevel)) {
							minLevel++;
							
//							System.out.println("minLevel = " + minLevel);
						}
					}
				}
			}
		}
	}
	
	private static DecaStableTransition findClosest(DecaStableTransition start, Collection<DecaStableTransition> targets, int maxDepth, List<DecaStableTransition> detSeqDest) {
		Map<DecaStableTransition, DecaStableTransition> genPerCfg = new HashMap<DecaStableTransition, DecaStableTransition>();
		
		Set<DecaStableTransition> beenHere = new HashSet<DecaStableTransition>();
		beenHere.add(start);
		
		Set<DecaStableTransition> fringe = new HashSet<DecaStableTransition>();
		Set<DecaStableTransition> newFringe = new HashSet<DecaStableTransition>();
		fringe.add(start);
		
		int depth = 0;
		
		while (fringe.size() > 0 && depth <= maxDepth) {
//			System.out.println("depth = " + depth + " / " + maxDepth + "; #fringe = " + fringe.size());
			
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
				
				for (DecaStableTransition succ : t.getTgt().getNonLoopOutgoing()) { //Ignore self-loops, of which there are many
					if (beenHere.add(succ)) {
						genPerCfg.put(succ, t);
						newFringe.add(succ);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
//			System.out.println("depth = " + depth);
			depth++;
		}
		
		return null;
	}
}

