package jlx.behave.verify;

import java.io.*;
import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class TeleMcrl2Exporter {
	private final TeleGraph graph;
	private final IdMap<TeleNode> idPerNode;
	private final IdMap<TeleX> idPerX;
	
	private final String initProcName;
	private final String receiveProcName;
	private final String sendProcName;
	private final String queueProcName;
	private final String sendActionName; //Between queue and main.
	private final String receiveActionName; //Between queue and main.
	private final String sendReceiveActionName; //Between queue and main.
	private final Map<PulsePack, String> namePerPack; 
	private final Map<ReprPort, String> paramNamePerInput;
	private final Map<ReprPort, String> receiveNamePerInput;
	private final Map<ReprPort, String> sendNamePerOutput;
	private final List<ReprPort> orderedInputs;
	private final Map<ReprPort, PulsePack> resetPackPerInput;
	
	public TeleMcrl2Exporter(TeleGraph graph) {
		this.graph = graph;
		
		UnusedNames unusedNames = new UnusedNames();
		
		String baseProcName = Texts.toValidIdentifier(graph.getVm().getName(), '_');
		
		initProcName = baseProcName;
		namePerPack = extractNamePerPack(unusedNames);
		paramNamePerInput = extractParamNamePerInput(unusedNames);
		receiveNamePerInput = extractReceiveNamePerInput(unusedNames);
		sendNamePerOutput = extractSendNamePerOutput(unusedNames);
		
		receiveProcName = unusedNames.generateUnusedName(baseProcName + "_recv");
		sendProcName = unusedNames.generateUnusedName(baseProcName + "_send");
		queueProcName = unusedNames.generateUnusedName(baseProcName + "_queue");
		sendActionName = unusedNames.generateUnusedName("sync_" + baseProcName);
		receiveActionName = sendActionName;
		sendReceiveActionName = unusedNames.generateUnusedName("synced_" + baseProcName);
		
		orderedInputs = new ArrayList<ReprPort>(graph.getPvsPerInputPort().keySet());
		resetPackPerInput = extractResetPackPerInput(); 
		
		idPerNode = new IdMap<TeleNode>();
		
		for (TeleNode node : graph.getNodes()) {
			idPerNode.getOrAdd(node);
		}
		
		idPerX = new IdMap<TeleX>();
		
		for (TeleX x : graph.getSrcsPerX().keySet()) {
			idPerX.getOrAdd(x);
		}
	}
	
	private Map<ReprPort, PulsePack> extractResetPackPerInput() {
		Map<ReprPort, PulsePack> result = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, Set<PulsePack>> e : graph.getPvsPerInputPort().entrySet()) {
			PulsePack p = getResetValue(e.getValue());
			
			if (p != null) {
				result.put(e.getKey(), p);
			}
		}
		
		return result;
	}
	
	private static PulsePack getResetValue(Collection<PulsePack> values) {
		for (PulsePack p : values) {
			if (p.isPulse() && !p.isTruePulse()) {
				return p;
			}
		}
		
		return null;
	}
	
	private Map<PulsePack, String> extractNamePerPack(UnusedNames unusedNames) {
		Map<List<ASALSymbolicValue>, Set<PulsePack>> packsPerOrderedValues = new HashMap<List<ASALSymbolicValue>, Set<PulsePack>>();
		
		for (Map.Entry<ReprPort, Set<PulsePack>> e : graph.getPvsPerInputPort().entrySet()) {
			for (PulsePack pack : e.getValue()) {
				HashMaps.inject(packsPerOrderedValues, pack.getOrderedValues(), pack);
			}
			
			for (PulsePackMapIO a : graph.getAlphabet()) {
				for (PulsePackMap m : a.getO()) {
					for (PulsePack pack : m.getPackPerPort().values()) {
						HashMaps.inject(packsPerOrderedValues, pack.getOrderedValues(), pack);
					}
				}
			}
		}
		
		Map<PulsePack, String> result = new HashMap<PulsePack, String>();
		
		for (Map.Entry<List<ASALSymbolicValue>, Set<PulsePack>> e2 : packsPerOrderedValues.entrySet()) {
			List<String> elems = new ArrayList<String>();
			TextOptions.select(TextOptions.MCRL2);
			
			for (ASALSymbolicValue v : e2.getKey()) {
				elems.add(Texts.toValidIdentifier(v.toString(), '_'));
			}
			
			String name = unusedNames.generateUnusedName(Texts.concat(elems, "_"));
			
			for (PulsePack pack : e2.getValue()) {
				result.put(pack, name);
			}
		}
		
		return result;
	}
	
	private Map<ReprPort, String> extractParamNamePerInput(UnusedNames unusedNames) {
		Map<ReprPort, String> result = new HashMap<ReprPort, String>();
		
		for (ReprPort input : graph.getPvsPerInputPort().keySet()) {
			String k = input.getActionPerVm().get(graph.getVm()).getId();
			result.put(input, unusedNames.generateUnusedName("v" + k));
		}
		
		return result;
	}
	
	private Map<ReprPort, String> extractReceiveNamePerInput(UnusedNames unusedNames) {
		Map<ReprPort, String> result = new HashMap<ReprPort, String>();
		
		for (ReprPort input : graph.getPvsPerInputPort().keySet()) {
			String k = input.getActionPerVm().get(graph.getVm()).getId();
			result.put(input, unusedNames.generateUnusedName(k));
		}
		
		return result;
	}
	
	private Map<ReprPort, String> extractSendNamePerOutput(UnusedNames unusedNames) {
		Set<ReprPort> outputs = new HashSet<ReprPort>();
		
		for (PulsePackMapIO a : graph.getAlphabet()) {
			for (PulsePackMap m : a.getO()) {
				outputs.addAll(m.getPackPerPort().keySet());
			}
		}
		
		Map<ReprPort, String> result = new HashMap<ReprPort, String>();
		
		for (ReprPort output : outputs) {
			String k = output.getActionPerVm().get(graph.getVm()).getId();
			result.put(output, unusedNames.generateUnusedName(k));
		}
		
		return result;
	}
	
	public void saveToFile(String filename) throws IOException {
		File targetFile = new File(filename);
		targetFile.getCanonicalFile().getParentFile().mkdirs();
		PrintStream out = new PrintStream(new File(filename));
		saveToFile(out);
		out.close();
	}
	
	public void saveToFile(PrintStream out) {
		out.println("% " + getClass().getCanonicalName());
		
		if (namePerPack.isEmpty()) {
			out.println("sort Data;");
		} else {
			out.println("sort");
			out.println("\tData");
			
			List<String> elems = new ArrayList<String>();
			elems.addAll(new HashSet<String>(namePerPack.values()));
			Collections.sort(elems);
			
			out.println("\t\t= struct " + elems.get(0));
			
			for (int index = 1; index < elems.size(); index++) {
				out.println("\t\t| " + elems.get(index));
			}
			
			out.println("\t;");
		}
		
		out.println("act");
		printActions(out);
		
		out.println("proc");
		printInitProc(out);
		printQueueProc(out);
		printReceiveProc(out);
		printSendProc(out);
		
		out.println("\t;");
		out.println("init");
		out.println("\t\t" + initProcName);
		out.println("\t;");
	}
	
	private void printActions(PrintStream out) {
		out.println("\t" + Texts.concat(receiveNamePerInput.values(), ", ") + ": Data;");
		out.println("\t" + Texts.concat(sendNamePerOutput.values(), ", ") + ": Data;");
		
		List<String> elems = new ArrayList<String>();
		
		for (int index = 1; index <= orderedInputs.size(); index++) {
			elems.add("Data");
		}
		
		if (sendActionName.equals(receiveActionName)) {
			out.println("\t" + sendActionName + ", " + sendReceiveActionName + ": " + Texts.concat(elems, " # ") + ";");
		} else {
			out.println("\t" + sendActionName + ", " + receiveActionName + ", " + sendReceiveActionName + ": " + Texts.concat(elems, " # ") + ";");
		}
	}
	
	private void printInitProc(PrintStream out) {
		List<String> queueProcParams = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : graph.getInitialInputs().getPackPerPort().entrySet()) {
			queueProcParams.add(paramNamePerInput.get(e.getKey()) + " = " + namePerPack.get(e.getValue()));
		}
		
		List<String> allowElems = new ArrayList<String>();
		allowElems.addAll(receiveNamePerInput.values());
		allowElems.addAll(sendNamePerOutput.values());
		
		out.println("\t" + initProcName);
		out.println("\t\t= allow(");
		out.println("\t\t\t{ " + Texts.concat(allowElems, ", ") + " },");
		out.println("\t\t\thide({ " + sendReceiveActionName + " },");
		out.println("\t\t\t\tcomm(");
		out.println("\t\t\t\t\t{ " + sendActionName + "|" + receiveActionName + " -> " + sendReceiveActionName + " },");
		out.println("\t\t\t\t\t" + receiveProcName + "(s = " + idPerNode.get(graph.getInitialNode()) + ") || " + queueProcName + "(" + Texts.concat(queueProcParams, ", ") + ")");
		out.println("\t\t\t\t)");
		out.println("\t\t\t)");
		out.println("\t\t)");
		out.println("\t;");
	}
	
	private void printReceiveProc(PrintStream out) {
		List<String> boundedVars = new ArrayList<String>();
		
		for (ReprPort input : orderedInputs) {
			boundedVars.add(paramNamePerInput.get(input));
		}
		
		List<String> pvsConstraints = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, Set<PulsePack>> e : graph.getPvsPerInputPort().entrySet()) {
			List<String> pvs = new ArrayList<String>();
			
			for (PulsePack pack : e.getValue()) {
				pvs.add(namePerPack.get(pack));
			}
			
			pvsConstraints.add(paramNamePerInput.get(e.getKey()) + " in [" + Texts.concat(pvs, ", ") + "]");
		}
		
		List<String> sendProcParams = new ArrayList<String>();
		sendProcParams.add("s = s");
		
		for (ReprPort input : graph.getPvsPerInputPort().keySet()) {
			String s = paramNamePerInput.get(input);
			sendProcParams.add(s + " = " + s);
		}
		
		out.println("\t" + receiveProcName + "(s: Int)");
		out.println("\t\t= sum " + Texts.concat(boundedVars, ", ") + ": Data .");
		out.println("\t\t\t(" + Texts.concat(pvsConstraints, " && ") + ") ->");
		out.println("\t\t\t\t" + receiveActionName + "(" + Texts.concat(boundedVars, ", ") + ") .");
		out.println("\t\t\t\t" + sendProcName + "(" + Texts.concat(sendProcParams, ", ") + ")");
		out.println("\t;");
	}
	
	private void printSendProc(PrintStream out) {
		List<String> inputs = new ArrayList<String>();
		
		for (ReprPort input : orderedInputs) {
			inputs.add(paramNamePerInput.get(input));
		}
		
		out.println("\t" + sendProcName + "(s: Int, " + Texts.concat(inputs, ", ") + ": Data)");
		out.println("\t\t= delta");
		
		for (Map.Entry<TeleX, Set<TeleNode>> e : graph.getSrcsPerX().entrySet()) {
			String srcsStr = toSrcsStr(e.getValue());
			String gdStr = "(" + toGuard(e.getKey().getInputs()) + ")";
			String tgtStr = "s = " + idPerNode.get(e.getKey().getTgt());
			
			Set<List<String>> elems1 = new HashSet<List<String>>();
			elems1.add(Collections.emptyList());
			
			for (PulsePackMap map : e.getKey().getOutput()) {
				List<String> elems = new ArrayList<String>();
				
				for (Map.Entry<ReprPort, PulsePack> e2 : map.getPackPerPort().entrySet()) {
					elems.add(sendNamePerOutput.get(e2.getKey()) + "(" + namePerPack.get(e2.getValue()) + ")");
				}
				
				Set<List<String>> newElems1 = new HashSet<List<String>>();
				
				for (List<String> e1 : elems1) {
					for (List<String> seq : ArrayLists.allOrderings(elems)) {
						List<String> e2 = new ArrayList<String>(e1);
						e2.addAll(seq);
						newElems1.add(e2);
					}
				}
				
				elems1.clear();
				elems1.addAll(newElems1);
			}
			
			for (List<String> elem1 : elems1) {
				if (elem1.isEmpty()) {
					out.println("\t\t+ (" + gdStr + " && s in " + srcsStr + ") -> " + receiveProcName + "(" + tgtStr + ")");
				} else {
					out.println("\t\t+ (" + gdStr + " && s in " + srcsStr + ") -> " + Texts.concat(elem1, " . ") + " . " + receiveProcName + "(" + tgtStr + ")");
				}
			}
		}
	}
	
	private void printQueueProc(PrintStream out) {
		List<String> inputs = new ArrayList<String>();
		
		for (ReprPort input : orderedInputs) {
			inputs.add(paramNamePerInput.get(input));
		}
		
		out.println("\t" + queueProcName + "(" + Texts.concat(inputs, ", ") + ": Data)");
		out.println("\t\t= delta");
		
		for (Map.Entry<ReprPort, Set<PulsePack>> e : graph.getPvsPerInputPort().entrySet()) {
			String inputName = receiveNamePerInput.get(e.getKey());
			String varName = paramNamePerInput.get(e.getKey());
			
			for (PulsePack pack : e.getValue()) {
				String packName = namePerPack.get(pack);
				out.println("\t\t+ " + inputName + "(" + packName + ") . " + queueProcName + "(" + varName + " = " + packName + ")");
			}
		}
		
		List<String> resetAssignments = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : resetPackPerInput.entrySet()) {
			resetAssignments.add(paramNamePerInput.get(e.getKey()) + " = " + namePerPack.get(e.getValue()));
		}
		
		out.println("\t\t+ " + sendActionName + "(" + Texts.concat(inputs, ", ") + ") . " + queueProcName + "(" + Texts.concat(resetAssignments, ", ") + ")");
		out.println("\t;");
	}
	
	private String toSrcsStr(Collection<TeleNode> srcs) {
		List<String> elems = new ArrayList<String>();
		
		for (TeleNode src : srcs) {
			elems.add("" + idPerNode.get(src));
		}
		
		Collections.sort(elems);
		
		return "[" + Texts.concat(elems, ",") + "]";
	}
	
	private String toGuard(Set<PulsePackMap> ps) {
		Set<PulsePackMap> trimmedInputs = PulsePackMap.trim(ps, graph.getPvsPerInputPort(), Dir.IN);
		List<String> items = new ArrayList<String>();
		
		for (PulsePackMap p : trimmedInputs) {
			items.add("(" + toGuard(p) + ")");
		}
		
		if (items.isEmpty()) {
			return "true";
		}
		
		Collections.sort(items);
		return Texts.concat(items, " || ");
	}
	
	private String toGuard(PulsePackMap p) {
		List<String> items = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : p.getPackPerPort().entrySet()) {
			items.add(paramNamePerInput.get(e.getKey()) + " == " + namePerPack.get(e.getValue()));
		}
		
		if (items.isEmpty()) {
			return "true";
		}
		
		Collections.sort(items);
		return Texts.concat(items, " && ");
	}
	
	public TeleGraph getGraph() {
		return graph;
	}
}

