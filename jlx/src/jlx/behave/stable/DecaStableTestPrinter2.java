package jlx.behave.stable;

import java.io.*;
import java.util.*;

import jlx.utils.*;

public class DecaStableTestPrinter2 {
	public static class Partition {
		public final Set<DecaStableTransition> stepsThatCanOverlap;
		public final Set<DecaStableTransition> stepsThatCannotOverlap;
		public final List<DecaStableTransition> steps;
		public final Set<DecaStableVertex> vtxs;
		
		public final Map<DecaStableTest, String> colorPerTest;
		public final Map<DecaStableTransition, DecaStableTest> testPerStep;
		public final Map<DecaStableTransition, Set<DecaStableTest>> testsPerStep;
		public final Map<DecaStableVertex, DecaStableTest> testPerVtx;
		
		public Partition(DecaStableTest test) {
			stepsThatCanOverlap = new HashSet<DecaStableTransition>();
			stepsThatCanOverlap.addAll(test.getSeq());
			stepsThatCannotOverlap = new HashSet<DecaStableTransition>();
			steps = new ArrayList<DecaStableTransition>();
			colorPerTest = new HashMap<DecaStableTest, String>();
			testsPerStep = new HashMap<DecaStableTransition, Set<DecaStableTest>>();
			testPerStep = new HashMap<DecaStableTransition, DecaStableTest>();
			testPerVtx = new HashMap<DecaStableVertex, DecaStableTest>();
			vtxs = new HashSet<DecaStableVertex>();
			addTest(test, "#000000");
			stepsThatCannotOverlap.clear();
		}
		
		public boolean canAdd(DecaStableTest test) {
			if (Collections.disjoint(stepsThatCannotOverlap, test.getSeq())) {
				return true;
			}
			
			return false;
		}
		
		public void addTest(DecaStableTest test, String c) {
			stepsThatCannotOverlap.addAll(test.getSeq());
			stepsThatCannotOverlap.removeAll(stepsThatCanOverlap);
			colorPerTest.put(test, "\"" + c + "\"");
			
			for (int index = 1; index < test.getSeq().size(); index++) {
				DecaStableTransition step = test.getSeq().get(index);
				HashMaps.inject(testsPerStep, step, test);
				
				steps.add(step);
				vtxs.add(step.getSrc());
				vtxs.addAll(step.getSrc().getSuccs());
				
				testPerVtx.putIfAbsent(step.getSrc(), test);
				testPerStep.putIfAbsent(step, test);
			}
		}
		
		public void print(String dirName, String baseName) {
			try {
				File dir = new File(dirName);
				
				if (dir.mkdir()) {
					System.out.println("Created directory \"" + dir.getAbsolutePath() + "\" for storing test visualizations!");
				}
				
				PrintStream out = new PrintStream(dirName + "/" + baseName + ".gv");
				print(out, dirName + "/" + baseName + ".gv");
				out.flush();
				out.close();
			} catch (IOException e) {
				throw new Error(e);
			}
		}
		
		public void print(PrintStream out, String filename) {
			Map<DecaStableVertex, String> namePerVertex = new HashMap<DecaStableVertex, String>();
			
			for (DecaStableVertex v : vtxs) {
				namePerVertex.put(v, "V" + namePerVertex.size());
			}
			
			out.println("// " + getClass().getCanonicalName());
			out.println("// " + filename);
			out.println("digraph G {");
			out.println("\tnode [label=\"\", shape=circle, width=0.1, color=gray, fillcolor=gray, style=filled];");
			out.println("\tedge [label=\"\", color=gray, arrowhead=vee];");
			out.println("\tI0 [style=none];");
			
			for (Map.Entry<DecaStableVertex, DecaStableTest> e : testPerVtx.entrySet()) {
				out.println("\t" + namePerVertex.get(e.getKey()) + " [color = " + colorPerTest.get(e.getValue()) + ", fillcolor=" + colorPerTest.get(e.getValue()) + "];");
			}
			
			for (DecaStableVertex v : vtxs) {
				Set<DecaStableVertex> untestedTargets = new HashSet<DecaStableVertex>();
				
				for (DecaStableTransition t : v.getOutgoing()) {
					Set<DecaStableTest> tests = testsPerStep.get(t);
					
					if (tests != null) {
						out.println("\t" + namePerVertex.get(v) + " -> " + namePerVertex.get(t.getTgt()) + " [color=" + colorPerTest.get(testPerStep.get(t)) + "];");
					} else {
						if (vtxs.contains(t.getTgt())) {
							untestedTargets.add(t.getTgt());
						}
					}
				}
				
				for (DecaStableVertex untestedTgt : untestedTargets) {
					out.println("\t" + namePerVertex.get(v) + " -> " + namePerVertex.get(untestedTgt) + ";");
				}
			}
			
			out.println("\tI0 -> " + namePerVertex.get(steps.get(0).getSrc()) + " [color=black];");
			out.println("}");
		}
	}
	
	public static void print(Collection<DecaStableTest> tests, String dirName, int vtxThreshold) {
		Set<Partition> partitions = new HashSet<Partition>();
		
		for (DecaStableTest test : tests) {
			boolean add = true;
			
			for (Partition p : partitions) {
				if (p.vtxs.size() < vtxThreshold) {
					if (p.canAdd(test)) {
						p.addTest(test, Dot.getRandomColor());
						add = false;
						break;
					}
				}
			}
			
			if (add) {
				partitions.add(new Partition(test));
			}
		}
		
		System.out.println("#partitions = " + partitions.size());
		int index = 0;
		
		for (Partition p : partitions) {
			p.print(dirName, String.format("deca-sink-subgraph-%03d", index));
			index++;
		}
	}
}

