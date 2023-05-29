package jlx.behave.stable.testgen;

import java.time.LocalTime;
import java.util.*;

import jlx.behave.stable.files.DecaStableFileReader;
import jlx.behave.stable.files.DecaStableFile.*;
import jlx.utils.*;

public class DecaStableTestGen5 {
	private static class Response {
		private final InputChanges inputChanges;
		private final Set<OutputEvolution> evos;
		private final int hashCode;
		
		public Response(InputChanges inputChanges, Set<OutputEvolution> evos) {
			this.inputChanges = inputChanges;
			this.evos = evos;
			
			hashCode = Objects.hash(evos, inputChanges);
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
			Response other = (Response) obj;
			return Objects.equals(evos, other.evos) && Objects.equals(inputChanges, other.inputChanges);
		}
	}
	
	private final DecaStableFileReader file;
	private final int randomPathLength;
	private final boolean responseCoverage;
	private final Map<Integer, Set<Transition>> trsPerLevel;
	private final Map<Transition, Set<Transition>> equivTrsPerTr;
	private final List<Integer> orderedLevels;
	private final Map<Integer, Integer> untouchedCountPerLevel;
	private long visitTimestamp;
	
	public DecaStableTestGen5(DecaStableFileReader file, float randomPathLengthMultiplier, boolean responseCoverage) {
		this.file = file;
		this.responseCoverage = responseCoverage;
		
		Map<Response, Set<Transition>> trsPerResponse = new HashMap<Response, Set<Transition>>();
		
		for (Transition t : file.getTransitions()) {
			t.touched = false;
			
			HashMaps.inject(trsPerResponse, new Response(t.getInputChanges(), t.getExternalOutputEvolutions()), t);
		}
		
		System.out.println("#responses = " + trsPerResponse.size());
		
		equivTrsPerTr = new HashMap<Transition, Set<Transition>>();
		
		for (Set<Transition> trs : trsPerResponse.values()) {
			for (Transition tr : trs) {
				equivTrsPerTr.put(tr, trs);
			}
		}
		
		file.getInitialTransition().touched = true;
		
		trsPerLevel = extractTrsPerLevel();
		orderedLevels = extractOrderedLevels();
		untouchedCountPerLevel = extractUntouchedCountPerLevel();
		visitTimestamp = 0L;
		
		//Do this after computing levels:
		randomPathLength = (int)(randomPathLengthMultiplier * (computeMaxLevel() + 1));
	}
	
	private Map<Integer, Integer> extractUntouchedCountPerLevel() {
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		
		for (Map.Entry<Integer, Set<Transition>> e : trsPerLevel.entrySet()) {
			result.put(e.getKey(), e.getValue().size());
			
//			System.out.println("level = " + e.getKey() + "; count = " + e.getValue().size());
		}
		
//		CLI.waitForEnter();
		
		return result;
	}
	
	private List<Integer> extractOrderedLevels() {
		List<Integer> result = new ArrayList<Integer>(trsPerLevel.keySet());
		Collections.sort(result);
		return result;
	}
	
	private Map<Integer, Set<Transition>> extractTrsPerLevel() {
		Map<Integer, Set<Transition>> result = new HashMap<Integer, Set<Transition>>();
		Set<Transition> beenHere = new HashSet<Transition>();
		
		for (Transition t : file.getInitialTransition().getTgt().getOutgoing()) {
			if (beenHere.add(t)) {
				HashMaps.inject(result, 0, t);
				t.predFromInit = file.getInitialTransition();
				t.visitTimestamp = visitTimestamp;
				t.level = 0;
			}
		}
		
		Set<Transition> fringe = new HashSet<Transition>();
		Set<Transition> newFringe = new HashSet<Transition>();
		fringe.addAll(beenHere);
		
		int depth = 1;
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (Transition t : RandomUtils.shuffle(fringe)) {
				for (Transition succ : t.getTgt().getOutgoing()) {
					if (beenHere.add(succ)) {
						HashMaps.inject(result, depth, succ);
						newFringe.add(succ);
						
						succ.predFromInit = t;
						succ.visitTimestamp = visitTimestamp;
						succ.level = depth;
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			
			System.out.println("[" + LocalTime.now() + "] #preds = " + beenHere.size() + " / " + file.getTransitions().size() + " (+" + fringe.size() + ")");
			depth++;
		}
		
		return result;
	}
	
	private int computeMaxLevel() {
		return orderedLevels.get(orderedLevels.size() - 1);
	}
	
	private int computeMaxSearchDepth() {
		for (int level : orderedLevels) {
			if (untouchedCountPerLevel.get(level) > 0) {
				return level;
			}
		}
		
		return -1;
	}
	
	private List<Transition> findPathToClosestUntouched(Transition start) {
		int maxSearchDepth = computeMaxSearchDepth();
		
//		System.out.println("maxSearchDepth = " + maxSearchDepth);
		
		if (maxSearchDepth < 0) {
			return null;
		}
		
		visitTimestamp++;
		
		start.predFromStart = start; //Circular!
		start.visitTimestamp = visitTimestamp;
		
		for (Transition t : RandomUtils.shuffle(start.getTgt().getOutgoing())) {
			if (!t.touched) {
				List<Transition> result = new ArrayList<Transition>();
				result.add(start);
				result.add(t);
				return result;
			}
			
			t.predFromStart = start;
			t.visitTimestamp = visitTimestamp;
		}
		
		Set<Transition> fringe = new HashSet<Transition>();
		Set<Transition> newFringe = new HashSet<Transition>();
		fringe.addAll(start.getTgt().getOutgoing());
		int depth = 1;
		
		while (fringe.size() > 0 && depth <= maxSearchDepth + randomPathLength) {
			newFringe.clear();
			
			for (Transition t : RandomUtils.shuffle(fringe)) {
				for (Transition succ : RandomUtils.shuffle(t.getTgt().getOutgoing())) {
					if (succ.visitTimestamp < visitTimestamp) {
						succ.predFromStart = t; //Needed for constructing a path.
						
						if (!succ.touched) {
							return constructPath(start, succ);
						}
						
						succ.visitTimestamp = visitTimestamp;
						
						if (succ.getTgt() != succ.getSrc()) {
							newFringe.add(succ);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
			depth++;
		}
		
		for (Transition t : RandomUtils.shuffle(trsPerLevel.get(maxSearchDepth))) {
			if (!t.touched) {
				List<Transition> pathToT = new ArrayList<Transition>();
				pathToT.add(t);
				
				while (pathToT.get(0) != file.getInitialTransition()) {
					pathToT.add(0, pathToT.get(0).predFromInit);
				}
				
				return pathToT;
			}
		}
		
		throw new Error("Should not happen, " + untouchedCountPerLevel.get(maxSearchDepth) + " untouched transitions expected at depth " + maxSearchDepth + "!");
	}
	
	private List<Transition> findRandomPath(Transition start) {
		List<Transition> result = new ArrayList<Transition>();
		result.add(start);
		
		for (int i = 1; i <= randomPathLength; i++) {
			result.add(RandomUtils.getElem(RandomUtils.getElem(result.get(result.size() - 1).getTgt().getTransitionsPerTgtId().values())));
		}
		
		return result;
	}
	
	private List<Transition> constructPath(Transition start, Transition end) {
		List<Transition> pathToT = new ArrayList<Transition>();
		pathToT.add(end);
		
		while (pathToT.get(0) != start) {
			pathToT.add(0, pathToT.get(0).predFromStart);
		}
		
		return pathToT;
	}
	
	public void populateTests() {
		file.getTests().clear();
		file.getTests().addAll(computeTestsGreedily());
	}
	
	private List<Trace> computeTestsGreedily() {
		DecaStableTraceSet dest = new DecaStableTraceSet();
		
		int targetCount = 0;
		int maxTargetCount = file.getTransitions().size();
		
		List<Transition> curr = new ArrayList<Transition>();
		curr.add(file.getInitialTransition());
		
		List<Transition> path;
		
		while ((path = findPathToClosestUntouched(curr.get(curr.size() - 1))) != null) {
			targetCount += touchTransitions(path);
			
			if (path.get(0) == file.getInitialTransition()) {
				if (curr.size() > 1) {
					List<Transition> randomPath = findRandomPath(curr.get(curr.size() - 1));
					targetCount += touchTransitions(randomPath);
					
					curr.remove(curr.size() - 1);
					curr.addAll(randomPath);
					
					dest.add(new Trace(dest.getTraces().size(), curr), targetCount, maxTargetCount);
				}
				
				curr = new ArrayList<Transition>();
				curr.addAll(path);
			} else {
				curr.remove(curr.size() - 1);
				curr.addAll(path);
			}
		}
		
		if (curr.size() > 1) {
			dest.add(new Trace(dest.getTraces().size(), curr), targetCount, maxTargetCount);
		}
		
		dest.done(maxTargetCount);
		return dest.getTraces();
	}
	
	private int touchTransitions(Collection<Transition> transitions) {
		int result = 0;
		
		for (Transition t : transitions) {
			if (!t.touched) {
				if (responseCoverage) {
					for (Transition t2 : equivTrsPerTr.get(t)) {
						touchTransition(t2);
						result++;
					}
				} else {
					touchTransition(t);
					result++;
				}
			}
		}
		
		return result;
	}
	
	private void touchTransition(Transition t) {
		untouchedCountPerLevel.put(t.level, untouchedCountPerLevel.get(t.level) - 1);
		t.touched = true;
	}
}

