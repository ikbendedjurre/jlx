package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.*;

public class NecronMiniExporter {
	private final NecronGraph graph;
	private final NameMap<NecronNode> idPerNode;
	
	public NecronMiniExporter(NecronGraph graph) {
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
			String src = idPerNode.get(node);
			
			for (Map.Entry<PulsePackMapIO, Set<NecronNode>> e : node.getOutgoing().entrySet()) {
//				if (e.getValue() == node) {
//					continue;
//				}
				
				for (NecronNode tgt : e.getValue()) {
					String c = "black"; //Dot.getRandomColor();
					out.println("\t" + src + " -> " + idPerNode.get(tgt) + " [color = \"" + c + "\"];");
				}
			}
		}
		
		out.println("}");
	}
	
//	private static String toString(PulsePackMap m) {
//		return m.toString();
//	}
	
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
