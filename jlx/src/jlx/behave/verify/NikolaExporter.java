package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.behave.proto.PulsePackMap;
import jlx.utils.*;

public class NikolaExporter {
	private final NikolaGraph graph;
	private final NameMap<NikolaNode> idPerNode;
	
	public NikolaExporter(NikolaGraph graph) {
		this.graph = graph;
		
		UnusedNames unusedNames = new UnusedNames();
		
		idPerNode = new NameMap<NikolaNode>(unusedNames);
		
		for (NikolaNode node : graph.getNodes()) {
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
		
		for (NikolaNode node : graph.getNodes()) {
			out.println("\t" + idPerNode.get(node) + " [label = \"" + toString(node.getOutput()) + "\\n" + toString(node.getSysmlClzs()) + "\", shape = ellipse];");
		}
		
		out.println("\tINIT [label = \"(initial)\", shape = ellipse];");
		out.println("\tINIT -> " + idPerNode.get(graph.getInitialNode()) + ";");
		
		for (NikolaNode node : graph.getNodes()) {
			String src = idPerNode.get(node);
			int suffix = 0;
			
			for (Map.Entry<PulsePackMap, Set<NikolaNode>> e : node.getOutgoing().entrySet()) {
				String c = Dot.getRandomColor();
				String mid = src + "_" + suffix;
				suffix++;
				
				out.println("\t" + mid + " [label = \"" + toString(e.getKey()) + "\", shape = rectangle, color = \"" + c + "\", fontcolor = \"" + c + "\"];");
				out.println("\t" + src + " -> " + mid + " [color = \"" + c + "\"];");
				
				for (NikolaNode tgt : e.getValue()) {
					out.println("\t" + mid + " -> " + idPerNode.get(tgt) + " [color = \"" + c + "\"];");
				}
			}
		}
		
		out.println("}");
	}
	
	private static String toString(PulsePackMap m) {
		return m.toString();
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
	
	public NikolaGraph getGraph() {
		return graph;
	}
	
	
}
