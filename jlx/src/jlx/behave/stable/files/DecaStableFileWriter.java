package jlx.behave.stable.files;

import java.io.*;
import java.util.*;

import jlx.utils.TextOptions;

public class DecaStableFileWriter extends DecaStableFileReader {
	public void saveToFile(String dirName, String fileName) {
		try {
			File dir = new File(dirName);
			
			if (dir.mkdir()) {
				System.out.println("Created directory \"" + dir.getAbsolutePath() + "\" for storing " + getClass().getCanonicalName() + "!");
			}
			
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dirName + "/" + fileName)));
			writeToStream(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	private void writeToStream(DataOutputStream out) throws IOException {
		TextOptions.select(TextOptions.FULL);
		
		out.writeInt(getScopes().size());
		
		for (Map.Entry<Integer, Scope> e : getScopes().entrySet()) {
			writeScope(out, e.getValue());
		}
		
		out.writeInt(getInputPorts().size());
		
		for (Map.Entry<Integer, Port> e : getInputPorts().entrySet()) {
			writePort(out, e.getValue());
		}
		
		out.writeInt(getOutputPorts().size());
		
		for (Map.Entry<Integer, Port> e : getOutputPorts().entrySet()) {
			writePort(out, e.getValue());
		}
		
		out.writeInt(getVertices().size());
		
		for (Map.Entry<Integer, Vertex> e : getVertices().entrySet()) {
			writeVertex(out, e.getValue());
		}
		
		out.writeInt(getInputChanges().size());
		
		for (Map.Entry<Integer, InputChanges> e : getInputChanges().entrySet()) {
			writeInputChanges(out, e.getValue());
		}
		
		out.writeInt(getOutputEvolutions().size());
		
		for (Map.Entry<Integer, OutputEvolution> e : getOutputEvolutions().entrySet()) {
			writeOutputEvolution(out, e.getValue());
		}
		
		writeTransition(out, getInitialTransition());
		out.writeInt(getInitialTransition().getTgt().getId());
		out.writeInt(getVertices().size());
		
		for (Map.Entry<Integer, Vertex> e1 : getVertices().entrySet()) {
			SortedMap<Integer, List<Transition>> transitionsPerTgtId = e1.getValue().getTransitionsPerTgtId();
			
			out.writeInt(e1.getKey());
			out.writeInt(transitionsPerTgtId.size());
			
			for (Map.Entry<Integer, List<Transition>> e : transitionsPerTgtId.entrySet()) {
				out.writeInt(e.getKey());
				out.writeInt(e.getValue().size());
				
				for (Transition transition : e.getValue()) {
					writeTransition(out, transition);
				}
			}
		}
		
		if (getCharSetPerVertex().size() > 0) {
			if (getCharSetPerVertex().size() != getVertices().size()) {
				throw new Error("Should not happen!");
			}
			
			out.writeBoolean(true);
			
			for (Vertex vertex : getVertices().values()) {
				CharSet charSet = getCharSetPerVertex().get(vertex);
				writeCharSet(out, charSet);
			}
		} else {
			out.writeBoolean(false);
		}
		
		if (getTests().size() > 0) {
			out.writeBoolean(true);
			out.writeInt(getTests().size());
			
			for (Trace test : getTests()) {
				writeTrace(out, test);
			}
		} else {
			out.writeBoolean(false);
		}
	}
	
	private void writeScope(DataOutputStream out, Scope scope) throws IOException {
		out.writeUTF(scope.getName());
	}
	
	private void writePort(DataOutputStream out, Port port) throws IOException {
		out.writeUTF(port.getName());
		out.writeInt(port.getOwner().getId());
		out.writeUTF(port.getTypeName());
		out.writeInt(port.getPortIndex());
		out.writeUTF(port.getAdapterLabel());
		out.writeBoolean(port.isPortToEnvironment());
		out.writeBoolean(port.isTimeoutPort());
		out.writeInt(port.getExecutionTime());
	}
	
	private void writeVertex(DataOutputStream out, Vertex vertex) throws IOException {
		SortedMap<Integer, String> statePerScope = new TreeMap<Integer, String>();
		
		for (Map.Entry<Scope, String> e : vertex.getStatePerScope().entrySet()) {
			statePerScope.put(e.getKey().getId(), e.getValue());
		}
		
		for (Map.Entry<Integer, String> e : statePerScope.entrySet()) {
			out.writeUTF(e.getValue());
		}
		
		SortedMap<Integer, String> clzsPerScope = new TreeMap<Integer, String>();
		
		for (Map.Entry<Scope, String> e : vertex.getClzsPerScope().entrySet()) {
			clzsPerScope.put(e.getKey().getId(), e.getValue());
		}
		
		for (Map.Entry<Integer, String> e : clzsPerScope.entrySet()) {
			out.writeUTF(e.getValue());
		}
	}
	
	private void writeInputChanges(DataOutputStream out, InputChanges inputChanges) throws IOException {
		out.writeInt(inputChanges.getNewValuePerPort().size());
		
		for (Map.Entry<Port, String> e2 : inputChanges.getNewValuePerPort().entrySet()) {
			out.writeInt(e2.getKey().getId());
			out.writeUTF(e2.getValue());
		}
		
		if (inputChanges.getDurationPort() != null) {
			out.writeBoolean(true);
			out.writeInt(inputChanges.getDurationPort().getId());
		} else {
			out.writeBoolean(false);
			out.writeBoolean(inputChanges.isHiddenTimerTrigger());
		}
	}
	
	private void writeOutputEvolution(DataOutputStream out, OutputEvolution outputEvolution) throws IOException {
		out.writeInt(outputEvolution.getEvolution().size());
		
		for (Map<Port, String> m : outputEvolution.getEvolution()) {
			out.writeInt(m.size());
			
			for (Map.Entry<Port, String> e2 : m.entrySet()) {
				out.writeInt(e2.getKey().getId());
				out.writeUTF(e2.getValue());
			}
		}
	}
	
	private void writeTransition(DataOutputStream out, Transition transition) throws IOException {
		out.writeInt(transition.getOutputEvolutions().size() + 1);
		out.writeInt(transition.getInputChanges().getId());
		
		for (OutputEvolution evo : transition.getOutputEvolutions()) {
			out.writeInt(evo.getId());
		}
	}
	
//	private void writeResponse(DataOutputStream out, Response response) throws IOException {
//		out.writeInt(response.getEvos().size());
//		
//		for (OutputEvolution evo : response.getEvos()) {
//			out.writeInt(evo.getId());
//		}
//		
//		out.writeInt(response.getStatePerScope().size());
//		
//		for (Map.Entry<Scope, String> e : response.getStatePerScope().entrySet()) {
//			out.writeInt(e.getKey().getId());
//			out.writeUTF(e.getValue());
//		}
//		
//		out.writeInt(response.getValuation().size());
//		
//		for (Map.Entry<Port, String> e : response.getValuation().entrySet()) {
//			out.writeInt(e.getKey().getId());
//			out.writeUTF(e.getValue());
//		}
//	}
	
	private void writeCharSet(DataOutputStream out, CharSet charSet) throws IOException {
		out.writeInt(charSet.getVertex().getId());
		out.writeInt(charSet.getTraces().size());
		
		for (Trace trace : charSet.getTraces()) {
			writeTrace(out, trace);
		}
	}
	
	private void writeTrace(DataOutputStream out, Trace trace) throws IOException {
		out.writeInt(trace.getTransitions().size());
		
		for (Transition transition : trace.getTransitions()) {
			out.writeInt(transition.getId());
		}
	}
	
//	public static void main(String[] args) {
//		DecaStableFileWriter x = new DecaStableFileWriter();
//		x.loadFromFile("models", "all.reduced.2.stable", true);
//		x.saveToFile("models", "all.reduced.2.stable");
//	}
}

