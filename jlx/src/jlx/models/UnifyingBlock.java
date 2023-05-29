package jlx.models;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.ops.ASALOp;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.behave.proto.*;
import jlx.behave.stable.DecaStableStateMachine;
import jlx.blocks.ibd1.*;
import jlx.common.reflection.*;
import jlx.models.IBDInstances.*;
import jlx.utils.*;

public class UnifyingBlock {
	public static class ReprProperty extends ASALProperty {
		private final IBD1Property legacy;
		private final ReprBlock reprOwner;
		
		public ReprProperty(ReprBlock reprOwner, String name, IBD1Property legacy, JType p, JType initialValue) {
			super(reprOwner.narrowInstance, name, p, initialValue);
			
			this.legacy = legacy;
			this.reprOwner = reprOwner;
		}
		
		public ReprBlock getReprOwner() {
			return reprOwner;
		}
		
		@Override
		public ASALVarOrigin getOrigin() {
			return ASALVarOrigin.STM_VAR;
		}
		
		@Override
		public boolean isWritable() {
			return true;
		}
	}
	
	public static class ReprPort extends ASALPort implements Comparable<ReprPort> {
		private final IBD1Port legacy;
		
		private boolean isSynchronous;
		private SortedSet<ReprFlow> incomingFlows;
		private SortedSet<ReprFlow> outgoingFlows;
//		private boolean isPortToEnvironment;
		
		private final Set<JType> possibleValues;
		private final AdapterLabels.Label adapterLabel;
		private final Set<VerificationAction> verificationActions;
		private final int executionTime;
		private final Map<VerificationModel, VerificationAction> actionPerVm;
		private final int priority;
		private List<ReprPort> reprDataPorts;
		private Set<ReprPort> reprSyncedPorts;
		private ReprPort adapterLabelPort;
		private ReprPort reprPulsePort;
		private final Dir dir;
		
		private ReprBlock reprOwner;
		
		public ReprPort(ReprBlock reprOwner, String name, ASALPortFields fields) {
			super(reprOwner.narrowInstance, name, fields.dir, fields.initialValue);
			
			this.legacy = null;
			this.reprOwner = reprOwner;
			
			dir = fields.dir;
			priority = fields.priority != null ? fields.priority : 0;
			verificationActions = fields.verificationActions;
			executionTime = fields.executionTime;
			
			reprDataPorts = new ArrayList<ReprPort>();
			reprSyncedPorts = Collections.singleton(this);
			incomingFlows = new TreeSet<ReprFlow>();
			outgoingFlows = new TreeSet<ReprFlow>();
			
			possibleValues = Collections.unmodifiableSet(JType.createAllValues(fields.initialValue.getType()));
			adapterLabel = null;
			adapterLabelPort = this;
			
			actionPerVm = extractActionPerVm();
		}
		
		public ReprPort(ReprBlock reprOwner, IBD1Port port) throws ReflectionException {
			super(reprOwner.narrowInstance, port.getLegacyPt().getField());
			
			this.reprOwner = reprOwner;
			
			legacy = port;
			dir = port.computeDir();
			reprDataPorts = new ArrayList<ReprPort>();
			reprSyncedPorts = new HashSet<ReprPort>();
			incomingFlows = new TreeSet<ReprFlow>();
			outgoingFlows = new TreeSet<ReprFlow>();
			
			Set<JType> values = port.computePossibleValues();
			
			if (values != null) {
				possibleValues = Collections.unmodifiableSet(values);
			} else {
				possibleValues = Collections.unmodifiableSet(JType.createAllValues(getType()));
			}
			
			if (possibleValues.isEmpty()) {
				throw new Error("Zero possible values for " + port.name + "!");
			}
			
			adapterLabel = port.computeAdapterLabel();
			verificationActions = port.computeVerificationActions();
			executionTime = port.computeExecutionTime();
			priority = port.computePriority();
			adapterLabelPort = this;
			
			actionPerVm = extractActionPerVm();
		}
		
		public Map<VerificationModel, VerificationAction> getActionPerVm() {
			return actionPerVm;
		}
		
		private Map<VerificationModel, VerificationAction> extractActionPerVm() {
			Map<VerificationModel, VerificationAction> result = new HashMap<VerificationModel, VerificationAction>();
			
			for (VerificationAction a : verificationActions) {
				if (result.containsKey(a.getModel())) {
					throw new Error("Ports cannot define multiple actions per verification model (" + a.getId() + ")!");
				}
				
				result.put(a.getModel(), a);
			}
			
			return result;
		}
		
		public ReprBlock getReprOwner() {
			return reprOwner;
		}
		
		/**
		 * Indicates that the domain of this port is finite.
		 * Only applies to input ports (since state machines can change output ports however they want).
		 * All input ports that connect to the environment should be restricted (this must be checked elsewhere).
		 * If this port is restricted, the value of getPossibleValues() is assigned. 
		 */
		public boolean isRestricted() {
			return possibleValues != null;
		}
		
		/**
		 * Retrieves all values that the environment can assign to this port.
		 * Cannot be null (even for output ports).
		 * The user can reduce the set of values by putting restrictions on the legacy port(s).
		 */
		public Set<JType> getPossibleValues() {
			return possibleValues;
		}
		
		/**
		 * Indicates if this port can be set to TRUE by the environment.
		 * Only applies to input ports.
		 * Only pulse ports can be disabled.
		 */
		public boolean isEnabled() {
			return possibleValues != null && JPulse.TRUE.isElemIn(possibleValues);
		}
		
		/**
		 * Returns the adapter label that has been extracted from the port's user data.
		 * Is null if no adapter label was assigned!
		 */
		public AdapterLabels.Label getAdapterLabel() {
			return adapterLabel;
		}
		
		@Override
		public Set<VerificationAction> getVerificationActions() {
			return verificationActions;
		}
		
		@Override
		public int getExecutionTime() {
			return executionTime;
		}
		
		@Override
		public int getPriority() {
			return priority;
		}
		
		/**
		 * Indicates if this port has no flow to another port in the model.
		 * (We assume that such ports must therefore be connected to the environment.)
		 */
		public boolean isPortToEnvironment() {
			return getIncomingFlows().isEmpty() && getOutgoingFlows().isEmpty();
//			return isPortToEnvironment;
		}
		
		/**
		 * Indicates that new values are written to this port directly (and not via a queue/...).
		 * Only internal ports can be synchronous (TODO confirm).
		 */
		public boolean isSynchronous() {
			return isSynchronous;
		}
		
		/**
		 * Retrieves the T port of which this DT port is essentially a parameter.
		 * Yields null if this port is not a DT port.
		 */
		@Override
		public ReprPort getPulsePort() {
			return reprPulsePort;
		}
		
		/**
		 * Retrieves the DT ports that are essentially the parameters of this T port.
		 * Is never null.
		 */
		@Override
		public List<ReprPort> getDataPorts() {
			return Collections.unmodifiableList(reprDataPorts);
		}
		
		public ReprPort getAdapterLabelPort() {
			return adapterLabelPort;
		}
		
		public Set<ReprPort> getSyncedPorts() {
			return Collections.unmodifiableSet(reprSyncedPorts);
		}
		
		public SortedSet<ReprFlow> getIncomingFlows() {
			return Collections.unmodifiableSortedSet(incomingFlows);
		}
		
		public SortedSet<ReprFlow> getOutgoingFlows() {
			return Collections.unmodifiableSortedSet(outgoingFlows);
		}
		
		@Override
		public ASALVarOrigin getOrigin() {
			return ASALVarOrigin.STM_PORT;
		}
		
		/**
		 * "Writable" from the perspective of the local block.
		 * In practice, only output ports are writable.
		 */
//		@Override
//		public boolean isWritable() {
//			return dir == Dir.OUT;
//		}
		
		public boolean isDPort() {
			return reprPulsePort == null && !getType().equals(JPulse.class);
		}
		
		@Override
		public Dir getDir() {
			return dir;
		}
		
		@Override
		public int compareTo(ReprPort o) {
			int result = reprOwner.compareTo(o.reprOwner); //(so that we can also compare flows!!)
			return result != 0 ? result : getName().compareTo(o.getName());
		}
		
		@Override
		public String toString() {
			return reprOwner.getName() + "::" + TextOptions.current().id(getName());
		}
	}
	
	public static class ReprBlock implements Comparable<ReprBlock>, JScope {
		private String name;
		private StateMachine stateMachine;
		private TritoStateMachine tritoStateMachine;
		private DecaTwoStateMachine decaTwoStateMachine;
		private DecaThreeStateMachine decaThreeStateMachine;
		private DecaTwoBStateMachine decaTwoBStateMachine;
		private Type1IBD narrowInstance;
		private JTypeLibrary typeLib;
		
		private Map<String, ASALVariable> helperVarPerName;
		private Map<JType, ASALVariable> varPerJType;
		
		private Map<ASALVariable, ASALVariable> reprVarPerNarrowVar;
		private Map<PrimitivePort<?>, ReprPort> reprReprPortPerPrimPort;
		private Map<ASALVariable, Set<JType>> possibleValuesPerReprVar;
		private Map<ASALVariable, JType> defaultValuePerReprVar;
		
		public ReprBlock(JTypeLibrary typeLib, IBD1Instance instance) throws ReflectionException {
			this.typeLib = typeLib;
			
			stateMachine = instance.getStateMachine();
			narrowInstance = instance.getNarrowInstance();
			name = instance.getName();
			
			helperVarPerName = new HashMap<String, ASALVariable>();
			varPerJType = new HashMap<JType, ASALVariable>();
			
			reprVarPerNarrowVar = new HashMap<ASALVariable, ASALVariable>();
			reprReprPortPerPrimPort = new HashMap<PrimitivePort<?>, ReprPort>();
			
			for (IBD1Port p : instance.getPortPerName().values()) {
				ReprPort newPort = new ReprPort(this, p);
				
				for (ASALPort leg : p.getLegacyPts().values()) { 
					reprReprPortPerPrimPort.put(leg.getLegacy(), newPort);
				}
				
				reprVarPerNarrowVar.put(p.getLegacyPt(), newPort);
				varPerJType.put(p.getLegacyPt().getLegacy(), newPort);
			}
			
			for (IBD1Property p : instance.getPropertyPerName().values()) {
				ReprProperty newProperty = new ReprProperty(this, p.name, p, p.getLegacyPt().getLegacy(), p.computeUserDefinedInitialValue());
				reprVarPerNarrowVar.put(p.getLegacyPt(), newProperty);
				varPerJType.put(p.getLegacyPt().getLegacy(), newProperty);
			}
			
			possibleValuesPerReprVar = new HashMap<ASALVariable, Set<JType>>();
			defaultValuePerReprVar = new HashMap<ASALVariable, JType>();
		}
		
		public Type1IBD getNarrowInstance() {
			return narrowInstance;
		}
		
		public StateMachine getStateMachine() {
			return stateMachine;
		}
		
		public TritoStateMachine getTritoStateMachine() {
			return tritoStateMachine;
		}
		
		public DecaTwoStateMachine getDecaTwoStateMachine() {
			return decaTwoStateMachine;
		}
		
		public DecaThreeStateMachine getDecaThreeStateMachine() {
			return decaThreeStateMachine;
		}
		
		public Collection<ASALVariable> getVars() {
			return reprVarPerNarrowVar.values();
		}
		
		public Collection<ReprPort> getPorts() {
			Set<ReprPort> result = new HashSet<ReprPort>();
			
			for (ASALVariable v : reprVarPerNarrowVar.values()) {
				if (v instanceof ReprPort) {
					result.add((ReprPort)v);
				}
			}
			
			return result;
		}
		
		public Collection<ReprProperty> getProperties() {
			Set<ReprProperty> result = new HashSet<ReprProperty>();
			
			for (ASALVariable v : reprVarPerNarrowVar.values()) {
				if (v instanceof ReprProperty) {
					result.add((ReprProperty)v);
				}
			}
			
			return result;
		}
		
		@Override
		public int compareTo(ReprBlock o) {
			return name.compareTo(o.name);
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public ASALVariable getVarInScope(JType scopeObject) {
			return varPerJType.get(scopeObject);
		}
		
		public Map<JType, ASALVariable> getVarPerJType() {
			return varPerJType;
		}
		
		public Map<ASALVariable, ASALVariable> getReprVarPerNarrowVar() {
			return reprVarPerNarrowVar;
		}
		
		@Override
		public JScopeType getType() {
			return JScopeType.BLOCK;
		}
		
		@Override
		public Class<? extends JType> getReturnType() {
			return null;
		}
		
		@Override
		public ASALVariable getWritableVariable(String name) {
			ASALVariable result = helperVarPerName.get(name);
			
			if (result != null) {
				return result;
			}
			
			return reprVarPerNarrowVar.get(narrowInstance.getWritableVariable(name));
		}
		
		@Override
		public ASALVariable getVariable(String name) {
			ASALVariable result = helperVarPerName.get(name);
			
			if (result != null) {
				return result;
			}
			
			return reprVarPerNarrowVar.get(narrowInstance.getVariable(name));
		}
		
		@Override
		public ASALOp getOperation(String name) {
			return narrowInstance.getOperation(name);
		}
		
		@Override
		public List<String> getScopeSuggestions(boolean readableVars, boolean writableVars, boolean operations, boolean literals) {
			List<String> result = new ArrayList<String>();
			
			if (readableVars || writableVars) {
				for (Map.Entry<String, ASALVariable> entry : helperVarPerName.entrySet()) {
					result.add("HELPER " + entry.getKey() + ": " + entry.getValue().getType().getSimpleName());
				}
			}
			
			result.addAll(narrowInstance.getScopeSuggestions(readableVars, writableVars, operations, literals));
			return result;
		}
		
		@Override
		public Set<JType> getPossibleValues(ASALVariable v) {
			Set<JType> result = possibleValuesPerReprVar.get(v);
			
			if (result == null) {
				if (v instanceof ReprPort) {
					ReprPort rp = (ReprPort)v;
					
					if (rp.isPortToEnvironment()) {
						result = rp.possibleValues;
					}
				}
				
				if (result == null) {
					result = Collections.unmodifiableSet(JType.createAllValues(v.getType()));
					
					if (result.isEmpty()) {
						throw new Error("Zero possible values for " + v.getName() + "!");
					}
					
					if (v.getInitialValue() != null) {
						if (!v.getInitialValue().isElemIn(result)) {
							throw new Error("Initial value of " + v.getName() + " is not in its possible values!");
						}
					}
				}
				
				possibleValuesPerReprVar.put(v, result);
			}
			
			return result;
		}
		
		@Override
		public JType getDefaultValue(ASALVariable v) {
			JType result = defaultValuePerReprVar.get(v);
			
			if (result == null) {
				Set<JType> pvs = getPossibleValues(v);
				
				if (pvs.isEmpty()) {
					throw new Error("Should not happen!");
				}
				
				result = pvs.iterator().next();
				defaultValuePerReprVar.put(v, result);
			}
			
			return result;
		}
		
		@Override
		public JTypeLibrary getTypeLib() throws ClassReflectionException {
			return typeLib;
		}
		
		@Override
		public Map<String, ASALVariable> getVariablePerName() {
			Map<String, ASALVariable> result = new TreeMap<String, ASALVariable>();
			
			for (ASALVariable v : reprVarPerNarrowVar.values()) {
				result.put(v.getName(), v);
			}
			
			return result;
		}
		
		@Override
		public Map<String, ASALOp> getOperationPerName() {
			return narrowInstance.getOperationPerName();
		}
		
		private String generateUniqueName(String baseName) {
			Set<String> usedNames = new HashSet<String>();
			usedNames.addAll(getOperationPerName().keySet());
			usedNames.addAll(getVariablePerName().keySet());
			
			String attempt = baseName;
			int suffix = 1;
			
			while (usedNames.contains(attempt)) {
				attempt = attempt + suffix;
				suffix++;
			}
			
			return attempt;
		}
		
		@Override
		public ReprPort generateHelperPort(String baseName, ASALPortFields fields) {
			String uniqueName = generateUniqueName(baseName);
			ReprPort newPort = new ReprPort(this, uniqueName, fields);
//			varPerJType.put(newPort.getLegacy(), newPort);
//			reprPortPerNarrowPort.put(p, newPort);
			reprVarPerNarrowVar.put(newPort, newPort);
			helperVarPerName.put(uniqueName, newPort);
			return newPort;
		}
		
		@Override
		public ReprProperty generateHelperProperty(String baseName, JType initialValue) {
			String uniqueName = generateUniqueName(baseName);
			ASALProperty p = new ASALProperty(narrowInstance, uniqueName, initialValue, initialValue);
			ReprProperty newProperty = new ReprProperty(this, uniqueName, null, initialValue, initialValue);
			varPerJType.put(newProperty.getLegacy(), newProperty);
//			reprPropPerNarrowProp.put(p, newProperty);
			reprVarPerNarrowVar.put(p, newProperty);
			helperVarPerName.put(uniqueName, newProperty);
			return newProperty;
		}
	}
	
	/**
	 * A representative flow between two ports (it may represent multiple flows
	 * that have been defined by different original blocks).
	 */
	public static class ReprFlow implements Comparable<ReprFlow> {
		private ReprFlow pulseFlow;
		private SortedSet<ReprFlow> dataFlows;
		
		public final ReprPort source;
		public final ReprPort target;
		
		private ReprFlow(ReprPort source, ReprPort target) {
			this.source = source;
			this.target = target;
			
			dataFlows = new TreeSet<ReprFlow>();
		}
		
		public ReprFlow getPulseFlow() {
			return pulseFlow;
		}
		
		public SortedSet<ReprFlow> getDataFlows() {
			return Collections.unmodifiableSortedSet(dataFlows);
		}
		
		@Override
		public int compareTo(ReprFlow o) {
			int result = source.compareTo(o.source);
			return result != 0 ? result : target.compareTo(o.target);
		}
	}
	
	/**
	 * A representative communication event from one port to a set of ports.
	 */
	public static class ReprCommEvt {
		public final ReprPort source;
		public final Set<ReprPort> targets;
		public final Set<ReprFlow> usedFlows;
		
		public ReprCommEvt(ReprPort source) {
			this.source = source;
			
			targets = new HashSet<ReprPort>();
			usedFlows = new HashSet<ReprFlow>();
		}
	}
	
	public final Model model;
	public final JTypeLibrary lib;
	
	public final Map<Type1IBD, ReprBlock> reprBlockPerNarrowBlock;
	public final Map<Type1IBD, ReprBlock> reprBlockPerType1IBD;
	
	public final Map<ASALPort, ReprPort> reprPortPerNarrowPort;
	public final Map<PrimitivePort<?>, ReprPort> reprPortPerPrimPort;
	public final SortedSet<ReprFlow> reprFlows;
	
	public final Map<AdapterLabels.Label, Set<ReprPort>> reprPortsPerAdapterLabel;
	public final Map<AdapterLabels.Label, ReprPort> adapterLabelPortPerAdapterLabel;
	
	public final List<TritoStateMachine> reprStateMachines;
	public final List<DecaThreeStateMachine> reprDecaThreeStateMachines;
	public final List<DecaTwoBStateMachine> reprDecaTwoBStateMachines;
	public final DecaFourStateMachines sms4;
	public final DecaStableStateMachine stableSm;
//	public final DecaStableScenarioPlayer scenarioPlayer;
	
	public final List<ReprCommEvt> reprCommEvts;
	public final List<ReprPort> reprPortsToEnv;
	public final Set<VerificationModel> verificationModels;
	
	public UnifyingBlock(String name, Model model, boolean explore, boolean exploreStable) throws ReflectionException {
		this.model = model;
		
		lib = model.createTypeLibrary();
		
		// Order matters:
		reprBlockPerNarrowBlock = extractReprBlockPerNarrowBlock();
		reprBlockPerType1IBD = extractReprBlockPerType1IBD();
		
		reprPortPerNarrowPort = extractReprPortPerNarrowPort();
		reprPortPerPrimPort = extractReprPortPerPrimPort();
		reprFlows = extractReprFlows();
		
		reprPortsPerAdapterLabel = extractReprPortsPerAdapterLabel();
		adapterLabelPortPerAdapterLabel = extractAdapterLabelPortPerAdapterLabel();
		
		populatePulsePortsAndDataPortsAndSyncedPorts();
		sortDataPorts();
		
		reprPortsToEnv = extractReprPortsToEnv();
		reprCommEvts = extractReprCommEvts();
		verificationModels = extractVerificationModels();
		
//		reprBlockPerName = extractReprBlockPerName();
		
//		reprPortPerType = extractReprPortPerType();
//		reprPorts = extractReprPorts();
		
//		reprPropertyPerType = extractReprPropertyPerType();
//		reprProperties = extractReprProperties();
//		reprVars = extractReprVars();
//		reprStateMachines = extractStateMachines();
//		
//		reprFlows = extractReprFlows();
//		reprCommEvts = extractReprCommEvts();
//		reprPortsToEnv = extractReprPortsToEnv();
//		
		// Populate flows with flows that provide extra data:
		for (ReprFlow f : reprFlows) {
			if (f.source.getPulsePort() != null) {
				for (ReprFlow otherFlow : f.source.getPulsePort().outgoingFlows) {
					f.dataFlows.add(otherFlow);
				}
			}
			
			for (ReprPort p : f.source.getDataPorts()) {
				for (ReprFlow otherFlow : p.outgoingFlows) {
					f.dataFlows.add(otherFlow);
				}
			}
		}
		
		populateStateMachines(name);
		
		reprStateMachines = extractStateMachines();
		reprDecaThreeStateMachines = extractDecaThreeStateMachines();
		reprDecaTwoBStateMachines = extractDecaTwoBStateMachines();
		
		//sms4 = new DecaFourStateMachines(name, reprDecaThreeStateMachines, explore);
		sms4 = new DecaFourStateMachines(name, reprDecaTwoBStateMachines, explore);
		sms4.printGraphvizFiles();
		stableSm = new DecaStableStateMachine(sms4, exploreStable);
		stableSm.printGraphvizFile();
		
//		if (name != null) {
//			DecaStableStateMachine stableSm2 = new DecaStableStateMachine(sms4, 2);
//			
//			Set<DecaStableVertex> vtxs = new HashSet<DecaStableVertex>();
//			vtxs.addAll(stableSm.vertices);
//			vtxs.removeAll(stableSm2.vertices);
//			
//			System.out.println("#stableSm.vertices = " + stableSm.vertices.size());
//			System.out.println("#stableSm2.vertices = " + stableSm2.vertices.size());
//			
//			for (DecaStableVertex vz : vtxs) {
//				System.out.println("vz not found: " + vz.getCfg().getDescription());
//				CLI.waitForEnter();
//			}
//			
//			if (vtxs.isEmpty()) {
//				System.out.println("No vz mismatch!");
//				CLI.waitForEnter();
//			}
//		}
		
//		stableSm.printGraphvizFile();
//		scenarioPlayer = new DecaStableScenarioPlayer(this, stableSm);
	}
	
	public Collection<ReprBlock> getBlocks() {
		return reprBlockPerNarrowBlock.values();
	}
	
	public ReprBlock findBlock(String name) {
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			if (rb.name.equals(name)) {
				return rb;
			}
		}
		
		return null;
	}
	
	private Map<Type1IBD, ReprBlock> extractReprBlockPerNarrowBlock() throws ReflectionException {
		Map<Type1IBD, ReprBlock> result = new HashMap<Type1IBD, ReprBlock>();
		
		for (IBD1Instance e : model.getIBD1Instances()) {
			if (e.getStateMachine() != null) {
				result.put(e.getNarrowInstance(), new ReprBlock(lib, e));
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<Type1IBD, ReprBlock> extractReprBlockPerType1IBD() {
		Map<Type1IBD, ReprBlock> result = new HashMap<Type1IBD, ReprBlock>();
		
		for (IBD1Instance e : model.getIBD1Instances()) {
			ReprBlock rb = reprBlockPerNarrowBlock.get(e.getNarrowInstance());
			
			if (rb != null) {
				for (Type1IBD i : e.getLegacyPts()) {
					result.put(i, rb);
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
//	private SortedMap<String, ReprBlock> extractReprBlockPerName() {
//		SortedMap<String, ReprBlock> result = new TreeMap<String, ReprBlock>();
//		
//		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
//			result.put(rb.getName(), rb);
//		}
//		
//		return Collections.unmodifiableSortedMap(result);
//	}
	
	private Map<ASALPort, ReprPort> extractReprPortPerNarrowPort() {
		Map<ASALPort, ReprPort> result = new HashMap<ASALPort, ReprPort>();
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			for (Map.Entry<ASALVariable, ASALVariable> entry : rb.reprVarPerNarrowVar.entrySet()) {
				if (entry.getKey() instanceof ASALPort && entry.getValue() instanceof ReprPort) {
					result.put((ASALPort)entry.getKey(), (ReprPort)entry.getValue());
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<PrimitivePort<?>, ReprPort> extractReprPortPerPrimPort() {
		Map<PrimitivePort<?>, ReprPort> result = new HashMap<PrimitivePort<?>, ReprPort>();
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			result.putAll(rb.reprReprPortPerPrimPort);
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private SortedSet<ReprFlow> extractReprFlows() {
		SortedSet<ReprFlow> result = new TreeSet<ReprFlow>();
		
		for (IBD1Port outPort : model.getIBD1Ports()) {
			// Flows only flow from output port to one or more input ports:
			if (outPort.getLegacyPt().getDir() == Dir.OUT) {
				ReprPort rp1 = reprPortPerNarrowPort.get(outPort.getLegacyPt());
				
				if (rp1 != null) { //Can be null when it belongs to a block without a state machine.
					for (IBD1Port inPort : model.getConjugateIBD1Ports(outPort)) {
						ReprPort rp2 = reprPortPerNarrowPort.get(inPort.getLegacyPt());
						
						if (rp2 != null) {
							ReprFlow flow = new ReprFlow(rp1, rp2);
							rp1.outgoingFlows.add(flow);
							rp2.incomingFlows.add(flow);
							result.add(flow);
						}
					}
				}
			}
		}
		
		return Collections.unmodifiableSortedSet(result);
	}
	
	private Map<AdapterLabels.Label, Set<ReprPort>> extractReprPortsPerAdapterLabel() {
		Map<AdapterLabels.Label, Set<ReprPort>> result = new HashMap<AdapterLabels.Label, Set<ReprPort>>();
		
		for (IBD1Port p : model.getIBD1Ports()) {
			ReprPort rp = reprPortPerNarrowPort.get(p.getLegacyPt());
			
			if (rp != null) {
				if (rp.getAdapterLabel() != null) {
					HashMaps.inject(result, rp.getAdapterLabel(), rp);
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<AdapterLabels.Label, ReprPort> extractAdapterLabelPortPerAdapterLabel() {
		Map<AdapterLabels.Label, ReprPort> result = new HashMap<AdapterLabels.Label, ReprPort>();
		
		for (Map.Entry<AdapterLabels.Label, Set<ReprPort>> e : reprPortsPerAdapterLabel.entrySet()) {
			ReprPort arp = e.getValue().iterator().next();
			result.put(e.getKey(), arp);
			
			for (ReprPort rp : e.getValue()) {
				rp.adapterLabelPort = arp;
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
//	private SortedSet<ReprPort> extractReprPorts() {
//		return Collections.unmodifiableSortedSet(new TreeSet<ReprPort>(reprPortPerType.values()));
//	}
	
	private void populatePulsePortsAndDataPortsAndSyncedPorts() {
		for (IBD1Port p : model.getIBD1Ports()) {
			ReprPort rp = reprPortPerNarrowPort.get(p.getLegacyPt());
			
			if (rp != null) {
				for (IBD1Port dp : p.computeDataPorts()) {
					ReprPort rdp = reprPortPerNarrowPort.get(dp.getLegacyPt());
					
					if (rdp != null) {
						rp.reprDataPorts.add(rdp);
					} else {
						throw new Error("Should have found data port!");
					}
				}
				
				IBD1Port pp = p.computePulsePort();
				
				if (pp != null) {
					ReprPort rpp = reprPortPerNarrowPort.get(pp.getLegacyPt());
					
					if (rpp != null) {
						rp.reprPulsePort = rpp;
					} else {
						throw new Error("Should have found pulse port!");
					}
				}
				
				if (rp.getAdapterLabel() != null) {
					rp.reprSyncedPorts.addAll(reprPortsPerAdapterLabel.get(rp.getAdapterLabel()));
					
//					if (rp.reprSyncedPorts.size() > 1) {
//						throw new Error("Should happen!");
//					}
				} else {
					rp.reprSyncedPorts.add(rp);
				}
			}
		}
	}
	
	private void sortDataPorts() {
		for (IBD1Port p : model.getIBD1Ports()) {
			ReprPort rp = reprPortPerNarrowPort.get(p.getLegacyPt());
			
			if (rp != null) {
				if (rp.getDir() == Dir.OUT) {
					Set<ReprPort> receiving1 = new HashSet<ReprPort>();
					
					for (ReprFlow f : rp.outgoingFlows) {
						receiving1.add(f.target);
						f.target.reprDataPorts.clear();
					}
					
					for (ReprPort drp : rp.reprDataPorts) {
						for (ReprFlow f : drp.outgoingFlows) {
							if (f.target.getPulsePort() == null) {
								throw new Error("Should connect to a DT-port!!");
							}
							
							if (!receiving1.contains(f.target.getPulsePort())) {
								throw new Error("Should connect to a DT-port that belongs to a particular T-port!!");
							}
							
							//Sort data ports in the order of the data ports of the output pulse port:
							for (ReprFlow f2 : rp.outgoingFlows) {
								f2.target.reprDataPorts.add(f.target);
							}
						}
					}
				}
			}
		}
	}
	
//	private Map<JType, ReprProperty> extractReprPropertyPerType() {
//		Map<JType, ReprProperty> result = new HashMap<JType, ReprProperty>();
//		
//		for (ReprBlock rb : reprBlocks) {
//			result.putAll(rb.propertyPerType);
//		}
//		
//		return Collections.unmodifiableMap(result);
//	}
//	
//	private SortedSet<ReprProperty> extractReprProperties() {
//		return Collections.unmodifiableSortedSet(new TreeSet<ReprProperty>(reprPropertyPerType.values()));
//	}
//	
//	private SortedSet<ReprVar<?>> extractReprVars() {
//		SortedSet<ReprVar<?>> result = new TreeSet<ReprVar<?>>();
//		result.addAll(reprPorts);
//		result.addAll(reprProperties);
//		return Collections.unmodifiableSortedSet(result);
//	}
	
	/**
	 * Sets ports and properties that were NOT initialized in the model "main" or
	 * in the initial transition of a state machine.
	 */
	private void finalizeInitialValues() throws InstanceReflectionException {
		for (ReprBlock rb : getBlocks()) {
			for (ReprProperty rp : rb.getProperties()) {
				if (rp.legacy != null) {
					if (rp.getInitialValue() == null) {
						rp.setInitialValue(rp.legacy.computeUserDefinedInitialValue());
						
						if (rp.getInitialValue() == null) {
							rp.setInitialValue(rp.legacy.computeDefaultValue());
						}
					}
				} else {
					//By design, the initial value has been set (b/c the property is generated)!
				}
			}
		}
		
		for (ReprPort rp : reprPortPerNarrowPort.values()) {
			if (rp.legacy != null) {
				if (rp.getInitialValue() == null) {
					rp.setInitialValue(rp.legacy.computeUserDefinedInitialValue());
					
					if (rp.getInitialValue() == null) {
						rp.setInitialValue(rp.legacy.computeDefaultValue());
					}
				}
			} else {
				//By design, the initial value has been set (b/c the port is generated)!
			}
		}
	}
	
	private List<TritoStateMachine> extractStateMachines() {
		List<TritoStateMachine> result = new ArrayList<TritoStateMachine>();
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			if (rb.stateMachine != null) {
				result.add(rb.tritoStateMachine);
			}
		}
		
		return Collections.unmodifiableList(result);
	}
	
	private List<DecaThreeStateMachine> extractDecaThreeStateMachines() {
		List<DecaThreeStateMachine> result = new ArrayList<DecaThreeStateMachine>();
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			if (rb.decaThreeStateMachine != null) {
				result.add(rb.decaThreeStateMachine);
			}
		}
		
		return Collections.unmodifiableList(result);
	}
	
	private List<DecaTwoBStateMachine> extractDecaTwoBStateMachines() {
		List<DecaTwoBStateMachine> result = new ArrayList<DecaTwoBStateMachine>();
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			if (rb.decaTwoBStateMachine != null) {
				result.add(rb.decaTwoBStateMachine);
			}
		}
		
		return Collections.unmodifiableList(result);
	}
	
//	private List<DecaTwoStateMachine> extractDecaTwoStateMachines() {
//		List<DecaTwoStateMachine> result = new ArrayList<DecaTwoStateMachine>();
//		
//		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
//			if (rb.decaTwoStateMachine != null) {
//				result.add(rb.decaTwoStateMachine);
//			}
//		}
//		
//		return Collections.unmodifiableList(result);
//	}
	
	private List<ReprCommEvt> extractReprCommEvts() {
		List<ReprCommEvt> result = new ArrayList<ReprCommEvt>();
		
		for (ReprPort rp : reprPortPerNarrowPort.values()) {
			if (rp.outgoingFlows.size() > 0) {
				ReprCommEvt evt = new ReprCommEvt(rp);
				
				for (ReprFlow flow : rp.outgoingFlows) {
					evt.targets.add(flow.target);
					evt.usedFlows.add(flow);
				}
				
				result.add(evt);
			}
		}
		
		return Collections.unmodifiableList(result);
	}
	
	private List<ReprPort> extractReprPortsToEnv() {
		List<ReprPort> result = new ArrayList<ReprPort>();
		
		for (ReprPort rp : reprPortPerNarrowPort.values()) {
			if (rp.incomingFlows.isEmpty() && rp.outgoingFlows.isEmpty()) {
//				rp.isPortToEnvironment = true;
				result.add(rp);
			}
		}
		
		return Collections.unmodifiableList(result);
	}
	
	private Set<VerificationModel> extractVerificationModels() {
		Set<VerificationModel> result = new HashSet<VerificationModel>();
		
		for (ReprPort rp : reprPortPerNarrowPort.values()) {
			result.addAll(rp.getActionPerVm().keySet());
		}
		
		return result;
	}
	
	private void populateStateMachines(String name) throws ReflectionException {
		Map<ReprBlock, TritoStateMachine> tritoPerRb = new HashMap<ReprBlock, TritoStateMachine>();
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			if (rb.stateMachine != null) {
				System.out.println("proto");
				ProtoStateMachine proto = new ProtoStateMachine(rb.stateMachine);
				System.out.println("deutero");
				DeuteroStateMachine deutero = new DeuteroStateMachine(proto, rb);
				System.out.println("trito");
				TritoStateMachine trito = new TritoStateMachine(deutero);
				tritoPerRb.put(rb, trito);
			}
		}
		
		finalizeInitialValues(); //Now we (should) know all initial values.
		
		for (ReprBlock rb : reprBlockPerNarrowBlock.values()) {
			if (rb.stateMachine != null) {
				System.out.println("tetra");
				TetraStateMachine tetra = new TetraStateMachine(tritoPerRb.get(rb));
				tetra.printGraphvizFile();
				System.out.println("penta");
				PentaStateMachine penta = new PentaStateMachine(tetra);
				penta.printGraphvizFile();
				System.out.println("hexa");
				HexaStateMachine hexa = new HexaStateMachine(penta);
				hexa.printGraphvizFile();
				System.out.println("septa");
				SeptaStateMachine septa = new SeptaStateMachine(hexa);
				septa.printGraphvizFile();
				System.out.println("octo");
				OctoStateMachine octo = new OctoStateMachine(septa);
				octo.printGraphvizFile();
				System.out.println("nona");
				NonaStateMachine nona = new NonaStateMachine(octo);
				nona.printGraphvizFile();
				
//				System.out.println("xdeca");
//				XDecaStateMachine xdeca = new XDecaStateMachine(nona);
//				xdeca.printGraphvizFile();
				
				System.out.println("deca");
				DecaStateMachine deca = new DecaStateMachine(nona);
				deca.printGraphvizFile();
				System.out.println("decb");
				DecbStateMachine decb = new DecbStateMachine(deca);
				decb.printGraphvizFile();
				System.out.println("deca-one");
				DecaOneStateMachine decaOne = new DecaOneStateMachine(decb);
				System.out.println("deca-two-" + rb.name);
				DecaTwoStateMachine decaTwo = new DecaTwoStateMachine(decaOne);
				decaOne.printGraphvizFile(); //Do this later, decaTwo writes names!!
				decaTwo.printGraphvizFile();
				System.out.println("deca-two-b-" + rb.name);
				DecaTwoBStateMachine decaTwoB = new DecaTwoBStateMachine(decaTwo);
				decaTwoB.printGraphvizFile();
				System.out.println("deca-three-" + rb.name);
				DecaThreeStateMachine decaThree = new DecaThreeStateMachine(name, decaTwo);
//				decaThree.printGraphvizFile(); //Bit too big . . .
				System.out.println("deca-three-b-" + rb.name);
//				DecaThreeBStateMachine decaThreeB = new DecaThreeBStateMachine(decaThree, true);
				//new DecaThreeBExporter(decaThreeB).saveToFile("deca-three-b-" + rb.name + ".gv");
//				DecaThreeBReducer.bla(decaThreeB);
				
				System.out.println("deca-done-" + rb.name);
//				DecaFiveStateMachine2 decaFive2 = new DecaFiveStateMachine2(decaThree);
//				DecaFiveStateMachine decaFive = new DecaFiveStateMachine(decaThree);
				
				//decaThree.printGraphvizFile();
				
				//System.out.println("[" + LocalTime.now().toString() + "] deca-four-" + rb.getName());
				//DecaFourStateMachine decaFour = new DecaFourStateMachine(decaThree);
				//decaFour.printGraphvizFile();
				System.out.println("done");
				
				rb.tritoStateMachine = tritoPerRb.get(rb);
				rb.decaTwoStateMachine = decaTwo;
				rb.decaThreeStateMachine = decaThree;
				rb.decaTwoBStateMachine = decaTwoB;
			}
		}
	}
	
//	public boolean isPortToEnvironment(ReprPort port) {
//		for (ReprFlow f : reprFlows) {
//			if (f.source == port || f.target == port) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
	
//	public Map<ReprPort, List<ASALLiteral>> getEnvRestrictions() {
//		return this.EnvRestrictions;
//	}
//	
//	public Map<ReprPort, ASALLiteral> getInitValuesEnvPorts() {
//		return this.EnvInitValues;
//	}
	
//	public void setEnvRestrictions(Map<IBDInstances.IBD1Port, List<ASALLiteral>> restr) {
//		this.EnvRestrictions = new HashMap<ReprPort, List<ASALLiteral>>();
//		
//		for (Map.Entry<IBDInstances.IBD1Port, List<ASALLiteral>> entry : restr.entrySet()) {
//			ReprPort reprPort = this.reprPortPerPort.get(entry.getKey());
//			List<ASALLiteral> possibleValues = entry.getValue();
//			
//			if (!reprPort.isPortToEnvironment) {
//				throw new Error("Port " + reprPort.name + " is not a port connected to the environment, can't be restricted");
//			}
//			
//			this.EnvRestrictions.put(reprPort, possibleValues);
//		}
//	}
//	
//	public void setInitialValuesEnvPorts(Map<IBDInstances.IBD1Port, ASALLiteral> init) {
//		this.EnvInitValues = new HashMap<ReprPort, ASALLiteral>();
//		
//		for(Map.Entry<IBDInstances.IBD1Port, ASALLiteral> entry : init.entrySet()) {
//			ReprPort reprPort = this.reprPortPerPort.get(entry.getKey());
//			
//			this.EnvInitValues.put(reprPort, entry.getValue());
//		}
//	}
	
	
	
//	public void setSynchronisityPort(IBDInstances.IBD1Port p, Boolean b) {
//		ReprPort reprPort = this.reprPortPerType.get(p.getRef());
//		reprPort.isSynchronous = b;
//		
//		//Make connected ports synchronous as well
//		for(ReprCommEvt e : this.reprCommEvts) {
//			if(e.source == reprPort) {
//				for(ReprPort p_rec : e.targets) {
//					p_rec.isSynchronous = b;
//				}
//			}
//		}
//	}
	
	public void makeEnvInputPulsePortsSynchronous() {
		for (ReprPort reprPort: reprPortsToEnv) {
			if (reprPort.getDir() == Dir.IN && reprPort.getType() == JPulse.class) {
				System.out.println("Port " + reprPort.getName() + " is made synchronous!");
				reprPort.isSynchronous = true;
			}
		}
	}
	
//	public void setEnvironmentPortsSynchronous(IBDInstances.IBD1Port... exc) {
//		HashSet<ReprPort> exceptions = new HashSet<ReprPort>();
//		for(int i = 0; i < exc.length; i++) {
//			exceptions.add(this.reprPortPerType.get(exc[i].getRef()));
//		}
//		for (ReprPort reprPort: reprPortsToEnv) {
//			if (reprPort.getDir() == Dir.IN && !exceptions.contains(reprPort)) {
//				reprPort.isSynchronous = true;
//			}
//		}
//	}
}
