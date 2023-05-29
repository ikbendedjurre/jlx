package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class JetAldebaranExporter {
	private final JetGraph graph;
	
	public JetAldebaranExporter(JetGraph graph) {
		this.graph = graph;
	}
	
	public void saveToFile(String filename) throws IOException {
		File targetFile = new File(filename);
		targetFile.getCanonicalFile().getParentFile().mkdirs();
		PrintStream out = new PrintStream(new File(filename));
		saveToFile(out);
		out.close();
	}
	
	public void saveToFile(PrintStream out) {
		out.println("des (0," + graph.getTransitions().size() + "," + graph.getNodes().size() + ")");
		
		for (JetTransition t : graph.getTransitions()) {
			List<String> xs = new ArrayList<String>();
			
			for (PulsePackMap m : t.getMap()) {
				xs.add(toString(m));
			}
			
			out.println("(" + t.getSrc().getId() + ",\"" + Texts.concat(xs, ",") + "\"," + t.getTgt().getId() + ")");
		}
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
	
	public JetGraph getGraph() {
		return graph;
	}
}
