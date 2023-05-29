package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class NecronDiffExporter {
	private final NecronGraph graph1;
	private final NecronGraph graph2;
	private final NameMap<NecronNode> idPerNode;
	
	public NecronDiffExporter(NecronGraph graph1, NecronGraph graph2) {
		this.graph1 = graph1;
		this.graph2 = graph2;
		
		int nonLoopDiffCount = 0;
		int loopDiffCount = 0;
		
		for (NecronNode src1 : graph1.getNodes()) {
			NecronNode src2 = graph2.getNodes().getNoAdd(src1);
			
			if (src2 == null) {
				throw new Error("Should not happen!");
			}
			
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : src1.getOutgoing().entrySet()) {
				Set<NecronNode> succs2 = src2.getOutgoing().get(e.getKey());
				
				if (succs2 == null) {
					throw new Error("Should not happen!");
				}
				
				for (NecronNode succ2 : succs2) {
					NecronNode succ1 = graph1.getNodes().getNoAdd(succ2);
					
					if (succ1 == null) {
						throw new Error("Should not happen!");
					}
					
					if (e.getValue().contains(succ1)) {
						//Do nothing.
					} else {
						HashMaps.inject(src2.getNewOutgoing(), e.getKey(), succ2);
						
						if (succ2.equals(src2)) {
							loopDiffCount++;
						} else {
							nonLoopDiffCount++;
						}
					}
				}
			}
			
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : src2.getOutgoing().entrySet()) {
				if (src1.getOutgoing().containsKey(e.getKey())) {
					//Do nothing.
				} else {
					HashMaps.injectAll(src2.getNewOutgoing(), e.getKey(), e.getValue());
					
					for (NecronNode z : e.getValue()) {
						if (z.equals(src2)) {
							loopDiffCount++;
						} else {
							nonLoopDiffCount++;
						}
					}
				}
			}
		}
		
		System.out.println("#necron-non-loop-diffs = " + nonLoopDiffCount);
		System.out.println("#necron-loop-diffs = " + loopDiffCount);
		
		UnusedNames unusedNames = new UnusedNames();
		
		idPerNode = new NameMap<NecronNode>(unusedNames);
		
//		for (NecronNode node : graph1.getNodes()) {
//			idPerNode.generate(node, "N");
//		}
		
		for (NecronNode node : graph2.getNodes()) {
			idPerNode.generate(node, "N");
		}
	}
	
	public void saveToFile(String filename) throws IOException {
		File targetFile = new File(filename);
		targetFile.getCanonicalFile().getParentFile().mkdirs();
		PrintStream out = new PrintStream(new File(filename));
		saveToFile(out);
		out.close();
	}
	
	public void saveToFile(PrintStream out) {
		out.println("// " + getClass().getCanonicalName());
		out.println("digraph G {");
		
		for (NecronNode node : graph2.getNodes()) {
			out.println("\t" + idPerNode.get(node) + " [label = \"" + toString(node.getSysmlClzs()) + "\", shape = ellipse];");
		}
		
		out.println("\tINIT [label = \"(initial)\", shape = ellipse];");
		out.println("\tINIT -> " + idPerNode.get(graph2.getInitialNode()) + ";");
		
		for (NecronNode node : graph2.getNodes()) {
			String src = idPerNode.get(node);
			int suffix = 0;
			
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : node.getOutgoing().entrySet()) {
//				if (e.getValue() == node) {
//					continue;
//				}
				
				Set<NecronNode> zs = node.getNewOutgoing().getOrDefault(e.getKey(), Collections.emptySet());
				
				if (zs.size() > 0) {
					if (Collections.disjoint(zs, e.getValue())) {
						throw new Error("NOT Gopod");
					}
					
//					throw new Error("Gopod");
				}
				
				String c = Dot.getRandomColor();
				String mid = src + "_" + suffix;
				suffix++;
				
				out.println("\t" + mid + " [label = \"" + toString(e.getKey()) + "\", shape = rectangle, color = \"" + c + "\", fontcolor = \"" + c + "\"];");
				out.println("\t" + src + " -> " + mid + " [color = \"" + c + "\"];");
				
				for (NecronNode tgt : e.getValue()) {
					if (zs.contains(tgt)) {
						out.println("\t" + mid + " -> " + idPerNode.get(tgt) + " [color = \"" + c + "\", style=dashed];");
//						throw new Error("djcn");
						
					} else {
						out.println("\t" + mid + " -> " + idPerNode.get(tgt) + " [color = \"" + c + "\"];");
					}
				}
			}
		}
		
		out.println("}");
	}
	
	private String toString(PulsePackMapIO m) {
		return toString(m.getI()) + " / " + toString(m.getO().get(0));
	}
	
	private String toString(PulsePackMap p) {
		TextOptions.select(TextOptions.ALDEBARAN);
		
		List<String> items = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : p.getPackPerPort().entrySet()) {
			for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValue().getValuePerPort().entrySet()) {
				String k = e2.getKey().getActionPerVm().get(graph1.getVm()).getId();
				items.add("(" + k + "," + e2.getValue().toString() + ")");
			}
		}
		
		return (p.getDir() == Dir.IN ? "in" : "out") + "([" + Texts.concat(items, ",") + "])";
	}
	
	private static String toString(Map<String, Set<Class<?>>> clzs) {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<String, Set<Class<?>>> e : clzs.entrySet()) {
			List<String> items = new ArrayList<String>();
			
			for (Class<?> c : e.getValue()) {
				items.add(c.getSimpleName());
			}
			
			elems.add(e.getKey() + " = " + Texts.concat(items, " / "));
		}
		
		return Texts.concat(elems, "\\n");
	}
	
	public NecronGraph getGraph() {
		return graph2;
	}
	
	
}
