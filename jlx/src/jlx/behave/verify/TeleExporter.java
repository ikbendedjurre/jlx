package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class TeleExporter {
	private final TeleGraph graph;
	private final NameMap<TeleNode> idPerNode;
	
	public TeleExporter(TeleGraph graph) {
		this.graph = graph;
		
		UnusedNames unusedNames = new UnusedNames();
		
		idPerNode = new NameMap<TeleNode>(unusedNames);
		
		for (TeleNode node : graph.getNodes()) {
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
		
		for (TeleNode node : graph.getNodes()) {
			out.println("\t" + idPerNode.get(node) + " [label = \"" + toString(node.getSysmlClzs()) + "\", shape = ellipse];");
		}
		
		out.println("\tINIT [label = \"(initial)\", shape = ellipse];");
		out.println("\tINIT -> " + idPerNode.get(graph.getInitialNode()) + ";");
		
		for (TeleNode node : graph.getNodes()) {
			String src = idPerNode.get(node);
			int suffix = 0;
			
			for (Map.Entry<Set<PulsePackMapIO>, Set<TeleNode>> e : node.getOutgoing2().entrySet()) {
				String c = Dot.getRandomColor();
				String mid = src + "_" + suffix;
				suffix++;
				
				out.println("\t" + mid + " [label = \"" + toString(e.getKey()) + "\", shape = rectangle, color = \"" + c + "\", fontcolor = \"" + c + "\"];");
				out.println("\t" + src + " -> " + mid + " [color = \"" + c + "\"];");
				
				for (TeleNode tgt : e.getValue()) {
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
		List<String> outputs = new ArrayList<String>();
		
		for (PulsePackMap o : m.getO()) {
			outputs.add(toString(o));
		}
		
		return toString(m.getI()) + " / " + Texts.concat(outputs, ", ");
	}
	
	private String toString(PulsePackMap p) {
		List<String> items = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : p.getPackPerPort().entrySet()) {
			for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValue().getValuePerPort().entrySet()) {
				String k = e2.getKey().getActionPerVm().get(graph.getVm()).getId();
				items.add(k + " = " + e2.getValue().toString());
			}
		}
		
		return Texts.concat(items, " | ");
	}
	
	private static String toString(Map<String, Set<Class<?>>> clzs) {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<String, Set<Class<?>>> e : clzs.entrySet()) {
			List<String> items = new ArrayList<String>();
			
			for (Class<?> c : e.getValue()) {
				items.add(c.getSimpleName());
			}
			
			switch (items.size()) {
				case 0:
					items.add("(none)");
					break;
				case 1:
				case 2:
					break;
				default:
					while (items.size() > 2) {
						items.remove(1);
					}
					
					items.add(1, "...");
					break;
			}
			
			elems.add(e.getKey() + " = " + Texts.concat(items, " / "));
		}
		
		return Texts.concat(elems, "\\n");
	}
	
	public TeleGraph getGraph() {
		return graph;
	}
	
	
}
