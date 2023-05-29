package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class NecronExporter {
	private final NecronGraph graph;
	private final NameMap<NecronNode> idPerNode;
	
	public NecronExporter(NecronGraph graph) {
		this.graph = graph;
		
		UnusedNames unusedNames = new UnusedNames();
		
		idPerNode = new NameMap<NecronNode>(unusedNames);
		
		for (NecronNode node : graph.getNodes()) {
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
		
		for (NecronNode node : graph.getNodes()) {
			out.println("\t" + idPerNode.get(node) + " [label = \"" + toString(node.getSysmlClzs()) + "\", shape = ellipse];");
		}
		
		out.println("\tINIT [label = \"(initial)\", shape = ellipse];");
		out.println("\tINIT -> " + idPerNode.get(graph.getInitialNode()) + ";");
		
		for (NecronNode node : graph.getNodes()) {
		//for (NecronNode node : Collections.singleton(graph.getInitialNode())) {
			String src = idPerNode.get(node);
			int suffix = 0;
			
//			for (Map.Entry<Set<PulsePackMapIO>, Set<NecronNode>> e : node.getOutgoing2().entrySet()) {
//				String c = Dot.getRandomColor();
//				String mid = src + "_" + suffix;
//				suffix++;
//				
//				out.println("\t" + mid + " [label = \"" + toString(e.getKey()) + "\", shape = rectangle, color = \"" + c + "\", fontcolor = \"" + c + "\"];");
//				out.println("\t" + src + " -> " + mid + " [color = \"" + c + "\"];");
//				
//				for (NecronNode tgt : e.getValue()) {
//					out.println("\t" + mid + " -> " + idPerNode.get(tgt) + " [color = \"" + c + "\"];");
//				}
//			}
			
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : node.getOutgoing().entrySet()) {
				String s = toString(e.getKey());
				
//				if (s.contains("(T21,TRUE)")) {
//					continue;
//				}
				
				String c = Dot.getRandomColor();
				String mid = src + "_" + suffix;
				suffix++;
				
				out.println("\t" + mid + " [label = \"" + toString(e.getKey()) + "\", shape = rectangle, color = \"" + c + "\", fontcolor = \"" + c + "\"];");
				out.println("\t" + src + " -> " + mid + " [color = \"" + c + "\"];");
				
				for (NecronNode tgt : e.getValue()) {
					out.println("\t" + mid + " -> " + idPerNode.get(tgt) + " [color = \"" + c + "\"];");
				}
			}
		}
		
		out.println("}");
	}
	
	private String toString(Set<PulsePackMapIO> ms) {
		List<String> elems = new ArrayList<String>();
		
		for (PulsePackMapIO m : ms) {
			elems.add(toString(m));
		}
		
		return Texts.concat(elems, "\\n");
	}
	
	private String toString(PulsePackMapIO m) {
		return toString(m.getI()) + " / " + toString(m.getO().get(0));
	}
	
	private String toString(PulsePackMap p) {
		TextOptions.select(TextOptions.ALDEBARAN);
		
		List<String> items = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : p.getPackPerPort().entrySet()) {
			for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValue().getValuePerPort().entrySet()) {
				String k = e2.getKey().getActionPerVm().get(graph.getVm()).getId();
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
		return graph;
	}
	
	
}
