package jlx.printing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jlx.asal.j.*;
import jlx.models.UnifyingBlock;
import jlx.models.UnifyingBlock.*;
import jlx.utils.*;

public class MCRL2Printer extends AbstractMCRL2ModelPrinter {
	public final static boolean ALL_CHANNELS = false;
	
	public MCRL2Printer(UnifyingBlock target, PrintingOptions options) {
		super(target, options);
	}
	
	@Override
	protected void initElemPrinters(String baseName) {
		selectElemPrinter(target).toFile(baseName + ".mcrl2");
		selectElemPrinter("rename").toFile(baseName + ".re");
		selectElemPrinter("SySim-VB").toFile(baseName + ".sysim.vb");
	}
	
	/**
	 * Helper class to store a component name and a port name
	 */
	private static class ComponentPortPair {
		final String component;
		final ReprPort port;
		final String mCRL2PortName;
		
		ComponentPortPair(String component, ReprPort port, String mCRL2PortName) {
			this.component = component;
			this.port = port;
			this.mCRL2PortName = mCRL2PortName;
		}
		
		String toMCRL2() {
			// Format is used in adapters, so the precise textual format is important!!
			return "CompPortPair(" + component + ", " + mCRL2PortName + ")";
		}
	}
	
	// Helper class to store communication labels
	static class PortChannel {
		public final ComponentPortPair sender;
		// We account for the possibility of multiple receivers by using a list
		public final List<ComponentPortPair> receivers;
		
		public PortChannel(ComponentPortPair sender, List<ComponentPortPair> receivers) {
			this.sender = sender;
			this.receivers = receivers;
		}
	}
	
	// Helper function to compute the communication labels
	protected List<PortChannel> getPortChannels() {
		List<PortChannel> portChannels = new ArrayList<PortChannel>();
		
		for (ReprCommEvt evt : target.reprCommEvts) {
			String srcBeqName = getName(evt.source.getReprOwner());
			ComponentPortPair sender = new ComponentPortPair(srcBeqName, evt.source, getName(evt.source.getLegacy()));
			List<ComponentPortPair> receivers = new ArrayList<ComponentPortPair>();
			
			for (ReprPort p2 : evt.targets) {
				String tgtBeqName = getName(p2.getReprOwner());
				ComponentPortPair recAction = new ComponentPortPair(tgtBeqName, p2, getName(p2.getLegacy()));
				receivers.add(recAction);
			}
			
			portChannels.add(new PortChannel(sender, receivers));
		}
		
		// This is new code [kednglurhgulruig], which synchronizes certain ports:
//		for (List<ReprPort> inputPorts : target.adapterMapping.portsPerLabel.values()) { //TODO fix this?
//			ReprPort firstInputPort = inputPorts.get(0);
//
		/*
		 * if (firstInputPort.getDir() == Dir.IN) { //Note that, if higher-index ports
		 * have other owners, this information is lost: String srcBeqName =
		 * envNamePerElemName.get(getName(firstInputPort.owner)); ComponentPortPair
		 * sender = new ComponentPortPair(srcBeqName, firstInputPort,
		 * getName(firstInputPort)); List<ComponentPortPair> receivers = new
		 * ArrayList<ComponentPortPair>();
		 * 
		 * for (ReprPort rp : inputPorts) { String tgtBeqName = getName(rp.owner);
		 * receivers.add(new ComponentPortPair(tgtBeqName, rp, getName(rp))); }
		 * 
		 * portChannels.add(new PortChannel(sender, receivers)); }
		 */
//		}
		
		for (ReprPort rp : target.reprPortsToEnv) {
			switch (rp.getDir()) {
				case IN: // case communication from environment
				{
					// This is old code [kednglurhgulruig]:
					String srcBeqName = envNamePerElemName.get(getName(rp.getReprOwner()));
					String tgtBeqName = getName(rp.getReprOwner());
					ComponentPortPair sender = new ComponentPortPair(srcBeqName, rp, getName(rp.getLegacy()));
					ComponentPortPair receiver = new ComponentPortPair(tgtBeqName, rp, getName(rp.getLegacy()));
					List<ComponentPortPair> receivers = new ArrayList<ComponentPortPair>();
					receivers.add(receiver);
					portChannels.add(new PortChannel(sender, receivers));
				}
					break;
				case OUT: // case communication to environment
				{
					String srcBeqName = getName(rp.getReprOwner());
					String tgtBeqName = envNamePerElemName.get(getName(rp.getReprOwner()));
					ComponentPortPair sender = new ComponentPortPair(srcBeqName, rp, getName(rp.getLegacy()));
					ComponentPortPair receiver = new ComponentPortPair(tgtBeqName, rp, getName(rp.getLegacy()));
					List<ComponentPortPair> receivers = new ArrayList<ComponentPortPair>();
					receivers.add(receiver);
					portChannels.add(new PortChannel(sender, receivers));
				}
					break;
			}
		}
		
		return portChannels;
	}
	
	protected String getValueRestrictionSum(ComponentPortPair cpp, Boolean pulsePack) {
//		if (cpp.port.getPossibleValues() == null) {
//			return "sum v: " + JTYPE + " . ";
//		}
		
		if (cpp.port.getDir() == Dir.OUT) {
			throw new Error("Cannot send to an output port (" + cpp.port.getName() + ")!");
		}
		
		if (cpp.port.getPossibleValues() == null) {
			throw new Error("Environment input ports must be restricted (" + cpp.port.getName() + ")!");
		}
		
		if (cpp.port.getPossibleValues().size() < 2) {
			return "sum v: " + JTYPE + " . (false) -> ";
		}
		
		if (dataParameterPorts.contains(cpp.port)) {
			return "sum v: " + JTYPE + " . (!USE_PULSE_PACKS) -> ";
		}
		
		if (!cpp.port.isRestricted()) {
			throw new Error("Environment ports should be restricted!!");
		}
		
		List<String> valueOptions = new ArrayList<String>();
		
		if (cpp.port.getType().equals(JPulse.class)) {
			List<ReprPort> params = dataParameterPortsPerPulsePort.get(cpp.port);
			if (params.isEmpty() || !pulsePack) {
				if (!pulsePack) {
					return "!(USE_PULSE_PACKS) -> sum v: " + JTYPE + " . (v == Value_Bool(true)) -> ";
				} else {
					return "(USE_PULSE_PACKS) -> sum v: " + JTYPE + " . (v == Value_Pulse_Pack([])) -> ";
				}
			} else {
				String sum = "(USE_PULSE_PACKS) -> sum v";
				for (int i = 0; i < params.size(); i++) {
					sum += ",v" + i;
				}
				sum += ":" + JTYPE + " . (v == Value_Pulse_Pack([";
				for (int i = 0; i < params.size(); i++) {
					if (i != 0) {
						sum += ",";
					}
					sum += "VarValuePair(" + getName(params.get(i).getLegacy()) + ",v" + i + ")";
				}
				sum += "])";
				for (int i = 0; i < params.size(); i++) {
					List<String> valueOptionsParam = new ArrayList<String>();
					for (JType pv : params.get(i).getPossibleValues()) {
						valueOptionsParam.add(literalToStr(pv));
					}
					sum += " && v" + i + " in " + transformTomCRL2List(valueOptionsParam);
				}
				sum += ") ->";
				return sum;
			}
		} else {
			for (JType pv : cpp.port.getPossibleValues()) {
				valueOptions.add(literalToStr(pv));
			}
		}
		
		return "sum v: " + JTYPE + " . (v in " + transformTomCRL2List(valueOptions) + ") -> ";
	}
	
	@Override
	protected void print() {
		printModel();
		
		Map<String, String> DTPortMap = new HashMap<String, String>();
		Map<ComponentPortPair, ComponentPortPair> DTReprPortMap = new HashMap<ComponentPortPair, ComponentPortPair>();
		List<PortChannel> portChannels = this.getPortChannels();
		for (PortChannel pc : portChannels) {
			if (this.dataParameterPorts.contains(pc.sender.port)) {
				String key = pc.sender.component + ")(" + pc.sender.mCRL2PortName;
				String target = pc.receivers.get(0).mCRL2PortName;
				DTPortMap.put(key, target);
				ComponentPortPair cpp = new ComponentPortPair(pc.sender.component, pc.sender.port, getName(pc.sender.port.getLegacy()));
				DTReprPortMap.put(cpp, pc.receivers.get(0));
				
			}
		}
		printStdMapping("getConnectedDataParameterPort", "CompName -> VarName", "VarName", DTPortMap);
		
		Map<String, String> ValueRestrictionMap = new HashMap<String, String>();
		for (PortChannel pc : portChannels) {
			String key = pc.sender.component + "," + pc.sender.mCRL2PortName;
			String values = "";
			if (pc.sender.port.getType().equals(JPulse.class)) {
				List<ReprPort> params = dataParameterPortsPerPulsePort.get(pc.sender.port);
				if (params.isEmpty()) {
					values = "if(USE_PULSE_PACKS, [Value_Pulse_Pack([])], [Value_Bool(true)])";
				} else {
					List<String> valueOptions = new ArrayList<String>();
					List<Set<String>> valuesPerDataPort = new ArrayList<Set<String>>();
					
					for (ReprPort dp : params) {
						Set<String> dpValues = new HashSet<String>();
						
						for (JType pv : dp.getPossibleValues()) {
							String key2 = pc.sender.component + ")(" + getName(dp.getLegacy());
							String vvp = "VarValuePair(" + DTPortMap.get(key2) + "," + literalToStr(pv) + ")";
							dpValues.add(vvp);
						}
						
						valuesPerDataPort.add(dpValues);
					}
					
					for (List<String> perm : ArrayLists.allCombinations(valuesPerDataPort)) {
						valueOptions.add("Value_Pulse_Pack(" + transformTomCRL2List(perm) + ")");
					}
					values = "if(USE_PULSE_PACKS, " + transformTomCRL2List(valueOptions) + ",  [Value_Bool(true)])";
//					System.out.println("some params");
//					List<ArrayList<String>> valueOptionsParams = new ArrayList<ArrayList<String>>();
//					List<ArrayList<String>> valueOptionsParamsNew = new ArrayList<ArrayList<String>>();
//					for (JType pv : params.get(0).getPossibleValues()) {
//						ArrayList<String> newList = new ArrayList<String>();
//						String key2 = pc.sender.component + ")(" + getName(params.get(0).getRef());
//						String vvp = "VarValuePair(" + DTPortMap.get(key2) + "," + literalToStr(pv) + ")";
//						newList.add(vvp);
//						valueOptionsParams.add(newList);
//					}
//					for (int i = 1; i < params.size(); i++) {
//						for (JType pv : params.get(i).getPossibleValues()) {
//							for (ArrayList<String> l : valueOptionsParams) {
//								ArrayList<String> temp = l;
//								String key2 = pc.sender.component + ")(" + getName(params.get(i).getRef());
//								String vvp = "VarValuePair(" + DTPortMap.get(key2) + "," + literalToStr(pv) + ")";
//								temp.add(vvp);
//								valueOptionsParamsNew.add(temp);
//							}
//							valueOptionsParams = valueOptionsParamsNew;
//							valueOptionsParamsNew.clear();
//							
//						}
//					}
//					for (ArrayList<String> v : valueOptionsParams) {
//						String pulse_pack = "Value_Pulse_Pack(";
//						pulse_pack += transformTomCRL2List(v);
//						pulse_pack += ")";
//						valueOptions.add(pulse_pack);
//					}
				}
			} else if (!dataParameterPorts.contains(pc.sender.port)) {
				List<String> valueOptions = new ArrayList<String>();
				for (JType pv : pc.sender.port.getPossibleValues()) {
					valueOptions.add(literalToStr(pv));
				}
				values = transformTomCRL2List(valueOptions);
			} else {
				List<String> valueOptions = new ArrayList<String>();
				for (JType pv : pc.sender.port.getPossibleValues()) {
					valueOptions.add(literalToStr(pv));
				}
				values = "if(USE_PULSE_PACKS, [], " + transformTomCRL2List(valueOptions) + ")";
			}
			ValueRestrictionMap.put(key, values);
			// I added this, because otherwise there are guards that do not evaluate to
			// true: :-s
			for (ComponentPortPair rec : pc.receivers) {
				String k = rec.component + "," + rec.mCRL2PortName;
				ValueRestrictionMap.put(k, values);
			}
		}
		printStdMapping("possibleValues", "CompName#VarName", "List(Value)", ValueRestrictionMap);
		
		String staticContent = "";
		try {
			System.out.println("Loading " + getClass().getResource("static.mcrl2").getPath() + "...");
			InputStream in = getClass().getResourceAsStream("static.mcrl2");
			byte[] b = in.readAllBytes();
			staticContent = new String(b, StandardCharsets.US_ASCII);
		} catch (IOException e) {
			e.printStackTrace();
		}
		println(staticContent);
		
		println("eqn");
		println__("COMBINE_SMS_ES = " + options.COMBINE_SMS_ES + ";");
		println__("USE_SYNCHRONOUS_PORTS = " + options.USE_SYNCHRONOUS_PORTS + ";");
		println__("RESET_VARIABLES_INITIAL = " + options.RESET_VARIABLES_INITIAL + ";");
		println__("ADD_inState_SELFLOOPS = " + options.ADD_inState_SELFLOOPS + ";");
		println__("ADD_ALL_inState_SELFLOOPS = " + options.ADD_ALL_inState_SELFLOOPS + ";");
		println__("ALLOWED_STATES_inState_SELFLOOPS = " + transformTomCRL2List(options.ALLOWED_STATES_inState_SELFLOOPS) + ";");
		println__("ADD_inEventPool_SELFLOOPS = " + options.ADD_inEventPool_SELFLOOPS + ";");
		println__("ADD_ALL_inEventPool_SELFLOOPS = " + options.ADD_ALL_inEventPool_SELFLOOPS + ";");
		println__("ADD_ABSTRACT_inEventPool_SELFLOOPS = " + options.ADD_ABSTRACT_inEventPool_SELFLOOPS + ";");
		println__("ALLOWED_EVENTS_inEventPool_SELFLOOPS = " + transformTomCRL2List(options.ALLOWED_EVENTS_inEventPool_SELFLOOPS) + ";");
		println__("ADD_varVal_SELFLOOPS = " + options.ADD_varVal_SELFLOOPS + ";");
		println__("ADD_ALL_varVal_SELFLOOPS = " + options.ADD_ALL_varVal_SELFLOOPS + ";");
		println__("ALLOWED_VARS_varVal_SELFLOOPS = " + transformTomCRL2List(options.ALLOWED_VARS_varVal_SELFLOOPS) + ";");
		println__("EVENT_QUEUE_BOUND = " + options.EVENT_QUEUE_BOUND + ";");
		println__("USE_PULSE_PACKS = " + options.USE_PULSE_PACKS + ";");
		println__("USE_ENV_RESTRICTION = " + options.USE_ENV_RESTRICTION + ";");
		
//		printStdList("local_vars", "var_name", new ArrayList<String>());
//		printStdList("instant_ports", "var_name", new ArrayList<String>());
//		printStdList("pseudo_states", "state", new ArrayList<String>());
//		printStdList("pulse_ports", "var_name", new ArrayList<String>());
		
		printMessagingIntermediary();
		
		ArrayList<ComponentPortPair> sendersToKeep = new ArrayList<ComponentPortPair>();
		ArrayList<ComponentPortPair> receiversToKeep = new ArrayList<ComponentPortPair>();
		ArrayList<String> sendersToHide = new ArrayList<String>();
		ArrayList<String> receiversToHide = new ArrayList<String>();
		
		for (PortChannel pc : portChannels) {
			boolean hasEnvironmentSrc = false;
			boolean hasEnvironmentTgt = false;
			
			if (elemNamePerEnvName.containsKey(pc.sender.component)) {
				hasEnvironmentSrc = true;
			}
			
			for (ComponentPortPair rec : pc.receivers) {
				if (elemNamePerEnvName.containsKey(rec.component)) {
					hasEnvironmentTgt = true;
				}
			}
			
			if (hasEnvironmentSrc && hasEnvironmentTgt) {
				throw new Error("Not supported!");
			}
			
			if (hasEnvironmentSrc || hasEnvironmentTgt) {
				ComponentPortPair sender = pc.sender;
				
				if (hasEnvironmentSrc) {
					if (pc.receivers.size() < 1) {
						throw new Error("Should not happen!");
					}
					
					// We only keep the first receiver.
					// Note that the choice of receiver could be unreliable:
					ComponentPortPair receiver = pc.receivers.get(0);
					receiversToKeep.add(receiver);
					
					for (int index = 1; index < pc.receivers.size(); index++) {
						receiversToHide.add(pc.receivers.get(index).toMCRL2());
					}
					
					sendersToHide.add(sender.toMCRL2());
				} else {
					if (pc.receivers.size() != 1) {
						throw new Error("Not supported!");
					}
					
					ComponentPortPair receiver = pc.receivers.get(0);
					
					sendersToKeep.add(sender);
					receiversToHide.add(receiver.toMCRL2());
				}
			} else {
				// Hide all:
				sendersToHide.add(pc.sender.toMCRL2());
				
				for (ComponentPortPair rec : pc.receivers) {
					receiversToHide.add(rec.toMCRL2());
				}
			}
		}
		
		printEnvironmentProcess();
		
		if (ALL_CHANNELS) {
			println("proc AllChannelsProcess = delta");
			
			for (PortChannel pc : portChannels) {
				boolean hasEnvironmentSrc = false;
				boolean hasEnvironmentTgt = false;
				
				if (elemNamePerEnvName.containsKey(pc.sender.component)) {
					hasEnvironmentSrc = true;
				}
				
				for (ComponentPortPair rec : pc.receivers) {
					if (elemNamePerEnvName.containsKey(rec.component)) {
						hasEnvironmentTgt = true;
					}
				}
				
				if (hasEnvironmentSrc && hasEnvironmentTgt) {
					throw new Error("Not supported!");
				}
				
				if (hasEnvironmentSrc || hasEnvironmentTgt) {
					if (pc.receivers.size() != 1) {
						throw new Error("Not supported!");
					}
					
					ComponentPortPair sender = pc.sender;
					ComponentPortPair receiver = pc.receivers.get(0);
					String v;
					
					if (JBool.class.isAssignableFrom(sender.port.getType())) {
						v = "Value_Bool(false)";
					} else if (JPulse.class.isAssignableFrom(sender.port.getType())) {
						v = "Value_Bool(true)";
					} else if (JInt.class.isAssignableFrom(sender.port.getType())) {
						v = "Value_Int(0)";
					} else {
						v = "Value_String(STR_)";
					}
					
					if (hasEnvironmentSrc) {
						println__("+ receive(" + receiver.toMCRL2() + ", " + v + ") . AllChannelsProcess");
					} else {
						println__("+ send(" + sender.toMCRL2() + ", " + v + ") . AllChannelsProcess");
					}
				}
			}
			
			println(";");
			println("init AllChannelsProcess;");
		} else {
			String allowedOneProcessActions = "discardEvent,selectMultiStep,executeBehaviour,executeStep,inState,inEventPool,resetVariables,varVal,eventPoolFull";
			
			
			
			if (options.SEQ_EXECUTION) {
				List<String> procIndepActions = new ArrayList<String>();
				procIndepActions.add("receiveComp");
				procIndepActions.add("sendComp");
				procIndepActions.add("resetVariables");
				procIndepActions.add("selectMultiStep");
				procIndepActions.add("executeStep");
				procIndepActions.add("executeSBehavior");
				procIndepActions.add("discardEvent");
				
				println("proc");
				println__("HelperProc(id: CompName)");
				println____("= sum p: VarName, v: Value . (isSyncedReceiver(CompPortPair(id, p))) -> receiveI(CompPortPair(id, p), v) . HelperProc()");
				println____("+ sum p: VarName, v: Value . (!isSyncedReceiver(CompPortPair(id, p))) -> receive(CompPortPair(id, p), v) . HelperProc()");
				println__(";");
				println("act");
				println__("sr, rr: CompPortPair # Value;");
				println("init");
				
				List<String> parallelCompInits = new ArrayList<String>();
				
				for (ReprBlock rb : target.getBlocks()) {
					if (rb.getTritoStateMachine() != null) {
						String compId = getName(rb);
						String s = "StateMachineInit(" + compId + ",compDefs(" + compId + ")) || SeqHelperProc(" + compId + ",0)";
						
						List<String> comms = new ArrayList<String>();
						List<String> blocks = new ArrayList<String>();
						List<String> renames = new ArrayList<String>();
						
						for (String a : procIndepActions) {
							comms.add(a + "|" + a + " -> keep_" + a);
							comms.add("not_" + a + "|not_" + a + " -> seq_ex_sync" + a);
							blocks.add(a);
							renames.add("keep_" + a + " -> " + a);
						}
						
						s = "comm({ " + concat(comms, ", ") + " }, " + s + ")";
						s = "block({ " + concat(blocks, ", ") + " }, " + s + ")";
						s = "rename({ " + concat(renames, ",") + " }, " + s + ")";
						parallelCompInits.add(s);
					}
				}
				
//				if (target.reprBlocks.isEmpty()) {
//					println__("delta;");
//				} else {
//					String s = parallelCompInits.get(0);
//					
//					for (int index = 1; index < parallelCompInits.size(); index++) {
//						s = s + " || " + parallelCompInits.get(index);
//						s = "comm({ send|receive -> sr }, " + s + ")";
//						s = "comm({ receive|receive -> rr }, " + s + ")";
//						s = "allow({ sr, rr, " + allowedOneProcessActions + " }, " + s + ")";
//						s = "rename({ sr -> send, rr -> receive }, " + s + ")";
//					}
//					
//					println__(s + ";");
//				}
				
				//TODO more
				
			} else {
				String envProcess = "Environment";
				
				if (options.SEQ_EXECUTION_TOGGLES) {
					allowedOneProcessActions += ",seqExToggleSend,seqExToggleRtc,seqExToggleReceive,seqExToggleDiscard,seqExToggleEnvSend,seqExClearEnvData";
					envProcess += "(true, [], [])";
				}
				
				printlines("% Initialization)", "init", "	allow({" + allowedOneProcessActions + ",", 
						"		send|receive,send|receive|receive,send|receive|receive|receive,send|receive|receive|receive|receive", "	},", 
						"		hide({checkQueueLength},",
						"		comm({", 
						"			sendComp|sendI -> send,", 
						"			receiveI|receiveComp -> receive,", 
						"			signalQueueLength|recQueueLength -> checkQueueLength",
						"		},", 
						"			MessagingIntermediary||" + envProcess);
				for (ReprBlock rb : target.getBlocks()) {
					if (rb.getTritoStateMachine() != null) {
						String component = getName(rb);
						println______("||StateMachineInit(" + component + ",compDefs(" + component + "))");
					}
				}
				println("	)));");
			}
		}
		
		selectElemPrinter("rename");
		
		println("var");
		println("	evt: Event;");
		println("	compName: CompName;");
		println("	stateName: StateName;");
		println("	cfg: StateConfig;");
		println("	v: Value;");
		println("	cpp2: CompPortPair;");
		println("rename");
		println("	discardEvent(evt) => tau;");
		// println(" inEventPool(compName, evt) => tau;");
		println("	selectMultiStep(compName, evt, cfg) => tau;");
		println("	executeStep(cfg) => tau;");
		println("	executeBehaviour => tau;");
		// println(" inState(compName, stateName) => tau;");
		
		println__("(cpp2 in " + transformTomCRL2List(sendersToHide) + ") -> send(cpp2,v) => tau;");
		println__("(cpp2 in " + transformTomCRL2List(receiversToHide) + ") -> receive(cpp2,v) => tau;");
		
//		if (options.PRINT_SYSIM_TEST_ADAPTER) {
//			selectElemPrinter("SySim-VB");
//			printSySimAdapterModule(sendersToKeep, receiversToKeep);
//			selectElemPrinter(target);
//		}
	}
	
	protected void printEnvironmentProcess() {
		println("proc");
		if (options.SEQ_EXECUTION_TOGGLES) {
			println__("Environment(canSend: Bool, dataKeys: List(CompPortPair), dataValues: List(Value)) = delta");
			println____("+ seqExClearEnvData . Environment(dataKeys = [], dataValues = [])");
			println____("+ sum x: Bool . seqExToggleEnvSend(x) . Environment(canSend = x)");
		} else {
			println__("Environment = delta");
		}
		List<PortChannel> portChannels = this.getPortChannels();
		for (PortChannel pc : portChannels) {
			// Comes from the environment, so must be restricted:
			if (elemNamePerEnvName.containsKey(pc.sender.component)) {
				if (pc.receivers.isEmpty()) {
					throw new Error("Should not happen!");
				}
				String queueRestriction = "";
				for (ReprBlock rb : target.getBlocks()) {
					if (rb.getTritoStateMachine() != null) {
						String component = getName(rb);
						queueRestriction += "recQueueLength(" + component + ",0)|";
					}
				}
				// We assume that value restrictions are set for receivers only.
				// We assume that restrictions apply to all receivers in a port channel.
				if (pc.receivers.get(0).port.getType().equals(JPulse.class)) {
					String sum = getValueRestrictionSum(pc.receivers.get(0), true);
					// We restrict receiver input by restricting sender output:
					println____("+ (" + sum + "\n           ((USE_ENV_RESTRICTION) -> " 
							+ queueRestriction + "sendComp(" + pc.sender.toMCRL2() + ", v) <> " 
							+ "sendComp(" + pc.sender.toMCRL2() + ", v)) . Environment())");
				}
				String sum = getValueRestrictionSum(pc.receivers.get(0), false);
				// We restrict receiver input by restricting sender output:
				println____("+ (" + sum + "\n           ((USE_ENV_RESTRICTION) -> " 
						+ queueRestriction + "sendComp(" + pc.sender.toMCRL2() + ", v) <> " 
						+ "sendComp(" + pc.sender.toMCRL2() + ", v)) . Environment())");
			}
			
			// Goes to the environment, could be anything:
			for (ComponentPortPair rec : pc.receivers) {
				if (elemNamePerEnvName.containsKey(rec.component)) {
					// if (rec.component.contains("Environment")) {
					if (options.SEQ_EXECUTION_TOGGLES) {
						println____("+ (sum v: Value . receiveComp(" + rec.toMCRL2() + ", v) . Environment(dataKeys = " + rec.toMCRL2() + " |> dataKeys, dataValues = v |> dataValues))");
					} else {
						println____("+ (sum v: Value . receiveComp(" + rec.toMCRL2() + ", v) . Environment())");
					}
				}
			}
		}
		println__(";");
	}
	
	protected void printMessagingIntermediary() {
		println("proc");
		println__("MessagingIntermediary = delta");
		List<PortChannel> portChannels = this.getPortChannels();
		for (PortChannel pc : portChannels) {
			String line = "+ (sum v: Value . ";
			line = line + "sendI(" + pc.sender.toMCRL2() + ", v)";
			for (ComponentPortPair receiver : pc.receivers) {
				line = line + "|receiveI(" + receiver.toMCRL2() + ", v)";
			}
			line = line + " . MessagingIntermediary)";
			println____(line);
		}
		println__(";");
	}
	
//	private static String printCppToSySim(ComponentPortPair cpp) {
//		return "New String() { \"" + cpp.toMCRL2() + "\", \"" + cpp.port.getType().getSimpleName() + "\", \"" + cpp.port.getAdapterLabel().label + "\" }";
//	}
//	
//	private static void sortCpps(List<ComponentPortPair> cpps) {
//		Map<String, ComponentPortPair> temp = new HashMap<String, ComponentPortPair>();
//		List<String> unsortedNames = new ArrayList<String>();
//		
//		for (ComponentPortPair cpp : cpps) {
//			String name = cpp.toMCRL2();
//			unsortedNames.add(name);
//			temp.put(name, cpp);
//		}
//		
//		Collections.sort(unsortedNames);
//		
//		cpps.clear();
//		
//		for (String name : unsortedNames) {
//			cpps.add(temp.get(name));
//		}
//	}
//	
//	private void printSySimAdapterModule(List<ComponentPortPair> sendersToKeep, List<ComponentPortPair> receiversToKeep) {
//		sortCpps(sendersToKeep);
//		sortCpps(receiversToKeep);
//		
//		println("Module JLXAdapterDefs", +1);
//		
//		{
//			List<String> coverageSymbols = new ArrayList<String>();
//			
//			for (UnifyingBlock.ReprBlock rb : target.getBlocks()) {
//				TritoStateMachine sm = rb.getTritoStateMachine();
//				
//				if (sm != null) {
//					for (TritoVertex v : sm.vertices) {
//						coverageSymbols.add("\"(" + getName(rb) + ", " + getName(v) + ")\"");
//					}
//				}
//			}
//			
//			println("Public ReadOnly coverageSymbols As String() = {");
//			
//			for (int index = 0; index < coverageSymbols.size(); index++) {
//				String comma = index == coverageSymbols.size() - 1 ? "" : ",";
//				println__(coverageSymbols.get(index) + comma + " '" + index);
//			}
//			
//			println("}");
//		}
//		
//		{
//			println("Public ReadOnly inputDefs As String()() = {");
//			
//			for (int index = 0; index < receiversToKeep.size(); index++) {
//				String comma = index == receiversToKeep.size() - 1 ? "" : ",";
//				println__(printCppToSySim(receiversToKeep.get(index)) + comma + " '" + index);
//			}
//			
//			println("}");
//		}
//		
//		{
//			println("Public ReadOnly outputDefs As String()() = {");
//			
//			for (int index = 0; index < sendersToKeep.size(); index++) {
//				String comma = index == sendersToKeep.size() - 1 ? "" : ",";
//				println__(printCppToSySim(sendersToKeep.get(index)) + comma + " '" + index);
//			}
//			
//			println("}");
//		}
//		
//		{
//			println("Public ReadOnly typeDefs As String()() = {");
//			
//			List<JTypeLibrary.Constructor> cstrs = new ArrayList<JTypeLibrary.Constructor>();
//			
//			for (JTypeLibrary.Type t : target.lib.getTypePerDecl().values()) {
//				// Standard data types are handled in a particular way:
//				if (t.legacy != JVoid.class && t.legacy != JPulse.class && t.legacy != JBool.class && t.legacy != JInt.class) {
//					cstrs.addAll(t.constructorsPerName.values());
//				}
//			}
//			
//			for (int index = 0; index < cstrs.size(); index++) {
//				String comma = index == cstrs.size() - 1 ? "" : ",";
//				JTypeLibrary.Constructor c = cstrs.get(index);
//				println__("New String() { \"" + c.type.legacy.getSimpleName() + "\", \"" + namePerConstrDecl.get(c.legacy) + "\", \"" + c.annotationName + "\" }" + comma + " '" + index);
//			}
//			
//			println("}");
//		}
//		
//		println(-1, "End Module");
//	}
}
