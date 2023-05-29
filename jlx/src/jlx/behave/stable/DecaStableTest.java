package jlx.behave.stable;

import java.io.*;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.asal.vars.ASALPort;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Texts;

public class DecaStableTest {
	private String name;
	private DecaStableStateMachine sm;
	private Map<ReprPort, ASALSymbolicValue> initialInputs;
	private List<DecaStableTransition> seq;
	
	public DecaStableTest(String name, DecaStableStateMachine sm, Map<ReprPort, ASALSymbolicValue> initialInputs, List<DecaStableTransition> seq) {
		this.name = name;
		this.sm = sm;
		this.initialInputs = new HashMap<ReprPort, ASALSymbolicValue>(initialInputs);
		this.seq = Collections.unmodifiableList(seq);
	}
	
	public String getName() {
		return name;
	}
	
	public Map<ReprPort, ASALSymbolicValue> getInitialInputs() {
		return initialInputs;
	}
	
	public List<DecaStableTransition> getSeq() {
		return seq;
	}
	
	public void print(String dirName) {
		try {
			File dir = new File(dirName);
			
			if (dir.mkdir()) {
				System.out.println("Created directory \"" + dir.getAbsolutePath() + "\" for storing tests!");
			}
			
			PrintStream out = new PrintStream(dirName + "/" + name + ".vb");
			print(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	public void print(PrintStream out) {
		out.println("Imports Testing");
		out.println("Public Class " + name);
		out.println("\tImplements Test");
		out.println("\tPublic Sub Run(api As TestAPI) Implements Test.Run");
		out.println("\t\tapi.StartNewTest(\"" + name + "\")");
		out.println("\t\t");
		
		//The first transition is the initialization transition.
		//We do NOT have to set its inputs, but we have DO have to check its outputs!
		out.println("\t\tapi.Log(\"Initialization:\")");
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : sm.initialInputs.entrySet()) {
			if (sm.durationPortPerTimeoutPort.values().contains(e.getKey())) {
				printInput(out, e.getKey(), "\"999999999\"");
			} else {
				printInput(out, e.getKey(), e.getValue());
			}
		}
		
		printOutputChecks(out, seq.get(0).getSeq());
		out.println("\t\tapi.Stabilize()");
		
		for (int index = 1; index < seq.size(); index++) {
			out.println("\t\t");
			out.println("\t\tapi.Log(\"Step " + index + " of " + (seq.size() - 1) + ":\")");
			
			printInputs(out, sm, seq.get(index));
			printStateChange(out, seq.get(index).getSeq());
			printOutputChecks(out, seq.get(index).getSeq());
			out.println("\t\tapi.Stabilize()");
		}
		
		out.println("\tEnd Sub");
		out.println("End Class");
	}
	
	private static void printOutputChecks(PrintStream out, List<DecaFourStateConfig> seq) {
		Map<ReprPort, List<PulsePack>> valueOverTimePerOutput = new HashMap<ReprPort, List<PulsePack>>();
		
		for (Map.Entry<ReprPort, PulsePack> e : seq.get(0).getOutputVal().getPackPerPort().entrySet()) {
			List<PulsePack> valueOverTime = new ArrayList<PulsePack>();
			valueOverTime.add(e.getValue());
			valueOverTimePerOutput.put(e.getKey(), valueOverTime);
		}
		
		for (int index = 1; index < seq.size(); index++) {
			PulsePackMap v1 = seq.get(index - 1).getOutputVal();
			PulsePackMap v2 = seq.get(index).getOutputVal();
			
			for (Map.Entry<ReprPort, PulsePack> change : v2.extractEventMap(v1).getPackPerPort().entrySet()) {
				List<PulsePack> valueOverTime = valueOverTimePerOutput.get(change.getKey());
				
				if (!valueOverTime.get(valueOverTime.size() - 1).equals(change.getValue())) {
					valueOverTime.add(change.getValue());
				}
			}
		}
		
		for (Map.Entry<ReprPort, List<PulsePack>> e : valueOverTimePerOutput.entrySet()) {
			out.println("\t\t" + changesToStr(e.getValue()));
		}
	}
	
	private static String changesToStr(List<PulsePack> changes) {
		List<String> pairsList = new ArrayList<String>();
		boolean missesAdapterLabel = false;
		
		for (PulsePack change : changes) {
			List<String> pairs = new ArrayList<String>();
			
			for (Map.Entry<ReprPort, ASALSymbolicValue> e : change.getValuePerPort().entrySet()) {
				String pair = "";
				
				if (e.getKey().getAdapterLabel() != null) {
					pair += "\"" + e.getKey().getAdapterLabel().label + "\"";
				} else {
					missesAdapterLabel = true;
					pair += "\"" + e.getKey().getReprOwner().getName() + "::" + e.getKey().getName() + "\"";
					//System.err.println("Missing adapter label for output port " + e.getKey().getReprOwner().getName() + "::" + e.getKey().getName());
				}
				
				pair += ", " + extractValue(e.getValue());
				pairs.add(pair);
			}
			
			pairsList.add("New PortValuePairs(" + Texts.concat(pairs, ", ") + ")");
		}
		
		if (missesAdapterLabel) {
			return "'OUT " + Texts.concat(pairsList, ", ");
		}
		
		return "api.ExpectOutputs(" + Texts.concat(pairsList, ", ") + ")";
	}
	
	private static String extractValue(ASALSymbolicValue v) {
		return extractValue(v.toString());
	}
	
	private static String extractValue(String s) {
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
			int firstIndex = s.indexOf(".") + 1;
			int lastIndex = s.lastIndexOf("\"");
			return "\"" + s.substring(firstIndex, lastIndex) + "\"";
		}
		
		return "\"" + s + "\"";
	}
	
	private static void printStateChange(PrintStream out, List<DecaFourStateConfig> seq) {
		Map<JScope, List<DecaFourVertex>> pathPerScope = DecaFourStateConfig.extractPathPerScope(seq);
		
		for (Map.Entry<JScope, List<DecaFourVertex>> e : pathPerScope.entrySet()) {
			if (e.getValue().size() == 1) {
				out.println("\t\tapi.Log(\"  " + e.getKey().getName() + " should stay in " + e.getValue().get(0).getName() + "\")");
			} else {
				List<String> vs = new ArrayList<String>();
				
				for (DecaFourVertex v : e.getValue()) {
					vs.add(v.getName());
				}
				
				out.println("\t\tapi.Log(\"  " + e.getKey().getName() + " should move from " + Texts.concat(vs, " to ") + "\")");
			}
		}
	}
	
	private static void printInputs(PrintStream out, DecaStableStateMachine sm, DecaStableTransition t) {
		if (t.isHiddenTimerTrigger()) {
			out.println("\t\tapi.TriggerHiddenTimeouts()");
			return;
		}
		
		PulsePackMap changes = t.getSicInputs().extractEventMap(t.getSrc().getExternalIncomingInputs());
		
		if (changes.getPackPerPort().isEmpty() && !t.getSrc().equals(t.getTgt())) {
			throw new Error("At least 1 input value should change!");
		}
		
		for (Map.Entry<ReprPort, PulsePack> e : changes.getPackPerPort().entrySet()) {
			ASALPort durationPort = sm.durationPortPerTimeoutPort.get(e.getKey());
			
			if (durationPort != null) {
				if (durationPort instanceof ReprPort) {
					ReprPort rp = (ReprPort)durationPort;
					
					if (rp.getAdapterLabel() != null) {
						out.println("\t\tapi.TriggerTimeout(\"" + rp.getAdapterLabel().label + "\")");
					} else {
						out.println("\t\t'ELAPSED " + rp.getReprOwner().getName() + "::" + rp.getName());
					}
				} else {
					throw new Error("Should not happen!");
				}
			} else {
				printInput(out, e.getKey(), t.getSicInputs().getPortValue(e.getKey(), false));
				
				for (ReprPort drp : e.getKey().getDataPorts()) {
					printInput(out, drp, t.getSicInputs().getPortValue(drp, false));
				}
			}
		}
	}
	
	private static void printInput(PrintStream out, ReprPort rp, ASALSymbolicValue value) {
		printInput(out, rp, extractValue(value));
	}
	
	private static void printInput(PrintStream out, ReprPort rp, String value) {
		if (rp.getAdapterLabel() != null) {
			out.println("\t\tapi.SetInput(\"" + rp.getAdapterLabel().label + "\", " + value + ")");
		} else {
			out.println("\t\t'IN " + rp.getReprOwner().getName() + "::" + rp.getName() + " := " + value);
			//System.err.println("Missing adapter label for input port " + rp.getReprOwner().getName() + "::" + rp.getName());
		}
	}
}
