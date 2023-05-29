package jlx.behave.stable;

import java.io.*;
import java.util.*;

public class DecaStableStateMachineExporter {
	private final DecaStableStateMachine sm;
	private final Map<DecaStableVertex, String> namePerVertex;
	private final Map<DecaStableTransition, String> namePerTransition;
	
	public DecaStableStateMachineExporter(DecaStableStateMachine sm) {
		this.sm = sm;
		
		namePerVertex = new HashMap<DecaStableVertex, String>();
		namePerTransition = new HashMap<DecaStableTransition, String>();
		
		for (DecaStableVertex v : sm.vertices) {
			namePerVertex.put(v, "V" + namePerVertex.size());
		}
		
		for (DecaStableTransition t : sm.transitions) {
			if (t.getTgt() != t.getSrc()) {
				namePerTransition.put(t, "T" + namePerTransition.size());
			}
		}
		
		
	}
	
	public void printAldebaranFile() {
		try {
			PrintStream ps = new PrintStream("deca-stable-overview.aut");
			printAldebaranFile(ps);
			ps.flush();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public void printAldebaranFile(PrintStream out) {
		out.println("des (0," + namePerTransition.size() + "," + namePerVertex.size() + ")");
	}
}
