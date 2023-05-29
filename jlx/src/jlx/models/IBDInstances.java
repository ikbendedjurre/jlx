package jlx.models;

import java.lang.reflect.Field;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.vars.*;
import jlx.behave.StateMachine;
import jlx.blocks.ibd1.*;
import jlx.blocks.ibd2.*;
import jlx.common.*;
import jlx.common.reflection.*;
import jlx.utils.*;

public class IBDInstances {
	public static interface IPort {
		// Empty.
	}
	
	public static class IBD1Instance {
		private List<Type1IBD> legacy;
		private Type1IBD narrowInstance;
		private StateMachine stateMachine;
		private Map<String, IBD1Property> propertyPerName;
		private Map<String, IBD1Port> portPerName;
		private String name;
		
		private IBD1Instance(String name, Type1IBD ibd) {
			this.name = name;
			
			propertyPerName = new HashMap<String, IBD1Property>();
			portPerName = new HashMap<String, IBD1Port>();
			narrowInstance = ibd;
			
			legacy = new ArrayList<Type1IBD>();
			addToLegacy(ibd);
		}
		
		private void addToLegacy(Type1IBD ibd) {
			if (ibd instanceof StateMachine) {
				if (stateMachine != null) {
					throw new Error("Multiple state machines per block not supported at this time!");
				}
				
				stateMachine = (StateMachine)ibd;
			}
			
			legacy.add(ibd);
		}
		
		public String getName() {
			return name;
		}
		
		public List<Type1IBD> getLegacyPts() {
			return Collections.unmodifiableList(legacy);
		}
		
		public Map<String, IBD1Port> getPortPerName() {
			return Collections.unmodifiableMap(portPerName);
		}
		
		public Map<String, IBD1Property> getPropertyPerName() {
			return Collections.unmodifiableMap(propertyPerName);
		}
		
		public StateMachine getStateMachine() {
			return stateMachine;
		}
		
		private IBD1Property getOrCreateProperty(ASALProperty property, int indent) {
			IBD1Property result = propertyPerName.get(property.getName());
			
			if (result == null) {
				println(indent, "Adding property " + property.getName() + ": " + property.getType().getCanonicalName());
				result = new IBD1Property(this, property);
				propertyPerName.put(property.getName(), result);
			} else {
				println(indent, "Adding property " + property.getName() + ": " + property.getType().getCanonicalName() + " (again)");
			}
			
			result.addLegacyPt(property);
			return result;
		}
		
		private IBD1Port getOrCreatePort(ASALPort port, int indent) {
			IBD1Port result = portPerName.get(port.getName());
			
			if (result == null) {
				println(indent, "Adding port " + port.getName() + ": " + port.getType().getCanonicalName());
				result = new IBD1Port(this, port);
				portPerName.put(port.getName(), result);
			} else {
				println(indent, "Adding port " + port.getName() + ": " + port.getType().getCanonicalName() + " (again)");
			}
			
			result.addLegacyPt(port);
			return result;
		}
		
		public Class<? extends Type1IBD> getType() {
			return narrowInstance.getClass();
		}
		
		public Type1IBD getNarrowInstance() {
			return narrowInstance;
		}
	}
	
	public abstract static class IBD1Var<T extends ASALVariable> {
		private Map<Type1IBD, T> legacyPts;
		
		public final IBD1Instance owner;
		public final String name;
		public final ASALNameParts nameParts;
		
		public IBD1Var(IBD1Instance owner, T pt) {
			this.owner = owner;
			
			name = pt.getName();
			nameParts = ASALNameParts.get(pt.getName());
			
			legacyPts = new HashMap<Type1IBD, T>();
			addLegacyPt(pt);
		}
		
		protected void addLegacyPt(T p) {
			legacyPts.put(p.getOwner(), p);
		}
		
		public Map<Type1IBD, T> getLegacyPts() {
			return Collections.unmodifiableMap(legacyPts);
		}
		
		public T getLegacyPt() {
			return legacyPts.get(owner.getNarrowInstance());
		}
		
		public Class<? extends JType> getType() {
			return legacyPts.values().iterator().next().getType();
		}
		
		/**
		 * Can return NULL.
		 */
		public abstract JType computeUserDefinedInitialValue();
		public abstract JType computeDefaultValue();
		
		public boolean isSameSuffix(IBD1Port other) {
			return nameParts.hasSameSuffix(other.nameParts);
		}
	}
	
	public static class IBD1Property extends IBD1Var<ASALProperty> {
		public IBD1Property(IBD1Instance owner, ASALProperty prop) {
			super(owner, prop);
		}
		
		@Override
		public JType computeUserDefinedInitialValue() {
			JType result = null;
			
			for (ASALProperty p : getLegacyPts().values()) {
				JType other = p.getInitialValue();
				
				if (other != null) {
					if (result != null) {
						if (!JType.isEqual(other, result)) {
							throw new Error("Inconsistent initial values!");
						}
					} else {
						result = other;
					}
				}
			}
			
			if (result != null && !getType().isAssignableFrom(result.getClass())) {
				throw new Error(result.getClass().getCanonicalName() + " is not a subclass of " + getType().getCanonicalName() + "!");
			}
			
			return result;
		}
		
		@Override
		public JType computeDefaultValue() {
			return JType.createDefaultValue(getType());
		}
	}
	
	public static class IBD1Port extends IBD1Var<ASALPort> implements IPort {
		private Set<IBD2Port> attachedPorts;
		
		private IBD1Port(IBD1Instance owner, ASALPort port) {
			super(owner, port);
			
			attachedPorts = new HashSet<IBD2Port>();
		}
		
		public Set<IBD2Port> getAttachedPorts() {
			return Collections.unmodifiableSet(attachedPorts);
		}
		
		public AdapterLabels.Label computeAdapterLabel() {
			AdapterLabels.Label result = null;
			
			for (ASALPort p : getLegacyPts().values()) {
				result = AdapterLabels.combine(result, p.getLegacy().getAdapterLabel());
			}
			
			return result;
		}
		
		public Set<VerificationAction> computeVerificationActions() {
			Set<VerificationAction> result = new HashSet<VerificationAction>();
			
			for (ASALPort p : getLegacyPts().values()) {
				result.addAll(p.getLegacy().getVerificationActions());
			}
			
			return result;
		}
		
		public int computeExecutionTime() {
			Integer v = null;
			
			for (ASALPort p : getLegacyPts().values()) {
				if (p.getLegacy().getExecutionTime() != null) {
					if (v != null) {
						if (!p.getLegacy().getExecutionTime().equals(v)) {
							throw new Error("Inconsistent port execution times!" + p.getLegacy().getFileLocation());
						}
					} else {
						v = p.getLegacy().getExecutionTime();
					}
				}
			}
			
			return v != null ? v.intValue() : 0;
		}
		
		public int computePriority() {
			Integer result = null;
			
			for (ASALPort p : getLegacyPts().values()) {
				Integer pr = p.getLegacy().getPriority();
				
				if (pr != null) {
					if (result == null) {
						result = pr;
					} else {
						if (!pr.equals(result)) {
							throw new Error("Inconsistent port priority!" + p.getLegacy().getFileLocation());
						}
					}
				}
			}
			
			return result != null ? result.intValue() : 0;
		}
		
		public Set<JType> computePossibleValues() {
			Set<JType> result = null;
			
			for (ASALPort p : getLegacyPts().values()) {
				if (p.getLegacy() instanceof InPort) {
					InPort<?> inPort = (InPort<?>) p.getLegacy();
					Set<JType> possibleValues = inPort.getPossibleValues();
					
					if (possibleValues != null) {
						if (result != null) {
							Set<JType> newResult = new HashSet<JType>();
							
							for (JType r : result) {
								if (r.isElemIn(possibleValues)) {
									newResult.add(r);
								} else {
									System.err.println("Possible value " + r.getClass().getSimpleName() + " discarded because it is not permitted by all instances of port " + name + "!");
								}
							}
							
							result = newResult;
						} else {
							result = possibleValues;
						}
					}
				}
			}
			
			return result;
		}
		
		public IBD1Port computePulsePort() {
			if (GlobalSettings.areDataFlowsGrouped()) {
				if ("DT".equals(nameParts.prefix)) {
					for (IBD1Port p : owner.portPerName.values()) {
						if ("T".equals(p.nameParts.prefix)) {
							if (p.nameParts.index == nameParts.index) {
								if (p.computeDir() == computeDir()) {
									return p;
								}
							}
						}
					}
				}
			}
			
			return null;
		}
		
		public List<IBD1Port> computeDataPorts() {
			List<IBD1Port> result = new ArrayList<IBD1Port>();
			
			if (GlobalSettings.areDataFlowsGrouped()) {
				if ("T".equals(nameParts.prefix)) {
					for (IBD1Port p : owner.portPerName.values()) {
						if ("DT".equals(p.nameParts.prefix)) {
							if (p.nameParts.index == nameParts.index) {
								if (p.computeDir() == computeDir()) {
									result.add(p);
								}
							}
						}
					}
				}
			}
			
			return Collections.unmodifiableList(result);
		}
		
		public Dir computeDir() {
			return getLegacyPts().values().iterator().next().getDir();
		}
		
		@Override
		public JType computeUserDefinedInitialValue() {
			JType result = null;
			
			for (ASALPort p : getLegacyPts().values()) {
				JType other = p.getInitialValue();
				
				if (other != null) {
					if (result != null) {
						if (!other.equals(result)) {
							throw new Error("Inconsistent initial values!");
						}
					} else {
						result = other;
					}
				}
			}
			
			if (result != null) {
				Set<JType> possibleValues = computePossibleValues();
				
				if (possibleValues != null) {
					if (!result.isElemIn(possibleValues)) {
						throw new Error("Initial value violates value restrictions!" + getLegacyPts().values().iterator().next().getLegacy().getFileLocation());
					}
				}
			}
			
			if (result != null && !getType().isAssignableFrom(result.getClass())) {
				throw new Error(result.getClass().getCanonicalName() + " is not a subclass of " + getType().getCanonicalName() + "!");
			}
			
			return result;
		}
		
		/**
		 * Computes a default value for this port, under the restrictions 
		 */
		@Override
		public JType computeDefaultValue() {
			JType result = null;
			Set<JType> possibleValues = computePossibleValues();
			
			if (possibleValues != null && possibleValues.size() > 0) {
				result = JType.createDefaultValue(getType());
				
				if (!result.isElemIn(possibleValues)) {
					result = possibleValues.iterator().next();
				}
			}
			
			if (result == null) {
				result = JType.createDefaultValue(getType());
			}
			
			return result;
		}
	}
	
	public static class IBD1Flow {
		public final IBD1Port v1;
		public final IBD1Port v2;
		
		private IBD1Flow(IBD1Port v1, IBD1Port v2) {
			this.v1 = v1;
			this.v2 = v2;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(v1, v2);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof IBD1Flow)) {
				return false;
			}
			IBD1Flow other = (IBD1Flow) obj;
			return Objects.equals(v1, other.v1) && Objects.equals(v2, other.v2);
		}
	}
	
	public static class IBD1ToIBD2Flow {
		public final IBD1Port v1;
		public final IBD2Port v2;
		
		private IBD1ToIBD2Flow(IBD1Port v1, IBD2Port v2) {
			this.v1 = v1;
			this.v2 = v2;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(v1, v2);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof IBD1ToIBD2Flow)) {
				return false;
			}
			IBD1ToIBD2Flow other = (IBD1ToIBD2Flow) obj;
			return Objects.equals(v1, other.v1) && Objects.equals(v2, other.v2);
		}
	}
	
	public static class IBD2Instance {
		private Map<String, IBD2Port> portPerId;
		private Set<IBD1Instance> childIBD1s;
		private Set<IBD2Instance> childIBD2s;
		private Type2IBD narrowInstance;
		private List<Type2IBD> legacy;
		
		public final String name;
		
		private IBD2Instance(String name, Type2IBD ibd) {
			this.name = name;
			
			narrowInstance = ibd;
			portPerId = new HashMap<String, IBD2Port>();
			childIBD1s = new HashSet<IBD1Instance>();
			childIBD2s = new HashSet<IBD2Instance>();
			
			legacy = new ArrayList<Type2IBD>();
			addToLegacy(ibd);
		}
		
		private void addToLegacy(Type2IBD ibd) {
			legacy.add(ibd);
		}
		
		public List<Type2IBD> getLegacy() {
			return Collections.unmodifiableList(legacy);
		}
		
		public Map<String, IBD2Port> getPortPerId() {
			return Collections.unmodifiableMap(portPerId);
		}
		
		public Set<IBD1Instance> getChildIBD1s() {
			return Collections.unmodifiableSet(childIBD1s);
		}
		
		public Set<IBD2Instance> getChildIBD2s() {
			return Collections.unmodifiableSet(childIBD2s);
		}
		
		private IBD2Port getOrCreatePort(Field field, InterfacePort port, int indent) {
			IBD2Port result = portPerId.get(field.getName());
			
			if (result == null) {
				println(indent, "Adding ITF port " + field.getName() + " [" + port.hashCode() + "]");
				result = new IBD2Port(this, field, port);
				portPerId.put(field.getName(), result);
			} else {
				println(indent, "Adding ITF port " + field.getName() + " [" + port.hashCode() + "]" + " (again)");
			}
			
			result.legacyPts.add(port);
			return result;
		}
		
		public Class<? extends Type2IBD> getType() {
			return narrowInstance.getClass();
		}
		
		public Type2IBD getNarrowInstance() {
			return narrowInstance;
		}
	}
	
	public static class IBD2Port implements IPort {
		private Set<InterfacePort> legacyPts;
		
		public final IBD2Instance owner;
		public final InterfacePort someLegacyPt;
		public final String id;
		
		private IBD2Port(IBD2Instance owner, Field field, InterfacePort port) {
			this.owner = owner;
			
			someLegacyPt = port;
			id = field.getName();
			
			legacyPts = new HashSet<InterfacePort>();
			legacyPts.add(port);
		}
		
		public Set<InterfacePort> getLegacyPts() {
			return Collections.unmodifiableSet(legacyPts);
		}
		
		@Override
		public String toString() {
			return id;
		}
	}
	
	public static class IBD2Flow {
		public final IBD2Port v1;
		public final Dir dir1; // We only support dirs with value 'OUT'!
		public final IBD2Port v2;
		public final Dir dir2; // We only support dirs with value 'OUT'!
		
		public IBD2Flow(IBD2Port v1, Dir dir1, IBD2Port v2, Dir dir2) {
			this.v1 = v1;
			this.dir1 = dir1;
			this.v2 = v2;
			this.dir2 = dir2;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(dir1, dir2, v1, v2);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof IBD2Flow)) {
				return false;
			}
			IBD2Flow other = (IBD2Flow) obj;
			return dir1 == other.dir1 && dir2 == other.dir2 && Objects.equals(v1, other.v1) && Objects.equals(v2, other.v2);
		}
	}
	
	private Map<Port, IPort> portPerPort;
	private Map<Type1IBD, IBD1Instance> ibd1InstancePerOrigInstance;
	private Map<Type2IBD, IBD2Instance> ibd2InstancePerOrigInstance;
	private Map<String, IBD1Instance> ibd1InstancePerName;
	private Map<String, IBD2Instance> ibd2InstancePerName;
	private Set<IBD1Property> ibd1Properties;
	private Set<IBD1Port> ibd1Ports;
	private Set<IBD2Port> ibd2Ports;
	private Set<IBD1Flow> ibd1Flows;
	private Set<IBD2Flow> ibd2Flows;
	private Set<IBD1ToIBD2Flow> ibd1ToIBD2Flows;
	
	public IBDInstances() {
		portPerPort = new HashMap<Port, IPort>();
		ibd1InstancePerOrigInstance = new HashMap<Type1IBD, IBD1Instance>();
		ibd2InstancePerOrigInstance = new HashMap<Type2IBD, IBD2Instance>();
		ibd1InstancePerName = new HashMap<String, IBD1Instance>();
		ibd2InstancePerName = new HashMap<String, IBD2Instance>();
		ibd1Properties = new HashSet<IBD1Property>();
		ibd1Ports = new HashSet<IBD1Port>();
		ibd2Ports = new HashSet<IBD2Port>();
		ibd1Flows = new HashSet<IBD1Flow>();
		ibd2Flows = new HashSet<IBD2Flow>();
		ibd1ToIBD2Flows = new HashSet<IBD1ToIBD2Flow>();
	}
	
	public void clear() {
		portPerPort.clear();
		ibd1InstancePerOrigInstance.clear();
		ibd2InstancePerOrigInstance.clear();
		ibd1InstancePerName.clear();
		ibd2InstancePerName.clear();
		ibd1Properties.clear();
		ibd1Ports.clear();
		ibd2Ports.clear();
		ibd1Flows.clear();
		ibd2Flows.clear();
		ibd1ToIBD2Flows.clear();
	}
	
	public Collection<IBD1Instance> getIBD1Instances() {
		return Collections.unmodifiableCollection(ibd1InstancePerName.values());
	}
	
	public Collection<IBD2Instance> getIBD2Instances() {
		return Collections.unmodifiableCollection(ibd2InstancePerName.values());
	}
	
	public Map<Type1IBD, IBD1Instance> getIBD1InstancePerOrigInstance() {
		return Collections.unmodifiableMap(ibd1InstancePerOrigInstance);
	}
	
	public Map<Type2IBD, IBD2Instance> getIBD2InstancePerOrigInstance() {
		return Collections.unmodifiableMap(ibd2InstancePerOrigInstance);
	}
	
	public Set<IBD1Property> getIBD1Properties() {
		return Collections.unmodifiableSet(ibd1Properties);
	}
	
	public Set<IBD1Port> getIBD1Ports() {
		return Collections.unmodifiableSet(ibd1Ports);
	}
	
	public Set<IBD2Port> getIBD2Ports() {
		return Collections.unmodifiableSet(ibd2Ports);
	}
	
	public Set<IBD1Flow> getIBD1Flows() {
		return Collections.unmodifiableSet(ibd1Flows);
	}
	
	public Set<IBD2Flow> getIBD2Flows() {
		return Collections.unmodifiableSet(ibd2Flows);
	}
	
	public Set<IBD1ToIBD2Flow> getIBD1ToIBD2Flows() {
		return Collections.unmodifiableSet(ibd1ToIBD2Flows);
	}
	
	private final static boolean DEBUG = true;
	
	private static void println(int indent, String s) {
		if (DEBUG) {
			System.out.println("\t".repeat(indent) + s);
		}
	}
	
	public IBD1Instance addIBDInstance(String name, Type1IBD ibd) throws ClassReflectionException {
		return addIBDInstance(name, ibd, 0);
	}
	
	private IBD1Instance addIBDInstance(String name, Type1IBD ibd, int indent) throws ClassReflectionException {
		if (ibd instanceof StateMachine) {
			JSTMValidation.check(((StateMachine) ibd).getClass());
		} else {
			JType1IBDValidation.check(ibd.getClass());
		}
		
		IBD1Instance ibd1 = getOrCreateIBD1(name, ibd, indent);
		
		for (ASALProperty p1 : ibd.getPropertyPerName().values()) {
			IBD1Property p2 = ibd1.getOrCreateProperty(p1, indent + 1);
			ibd1Properties.add(p2);
		}
		
		for (ASALPort p1 : ibd.getPrimitivePortPerName().values()) {
			IBD1Port p2 = ibd1.getOrCreatePort(p1, indent + 1);
			portPerPort.put(p1.getLegacy(), p2);
			ibd1Ports.add(p2);
		}
		
		return ibd1;
	}
	
	public IBD2Instance addIBDInstance(String name, Type2IBD ibd) throws ClassReflectionException {
		return addIBDInstance(name, ibd, 0);
	}
	
	private IBD2Instance addIBDInstance(String name, Type2IBD ibd, int indent) throws ClassReflectionException {
		JType2IBDValidation.check(ibd.getClass());
		
		IBD2Instance ibd2 = getOrCreateIBD2(name, ibd, indent);
		
		for (Map.Entry<Field, InterfacePort> entry : ibd.getInterfacePortPerName().entrySet()) {
			IBD2Port p = ibd2.getOrCreatePort(entry.getKey(), entry.getValue(), indent + 1);
			portPerPort.put(entry.getValue(), p);
			ibd2Ports.add(p);
		}
		
		for (Map.Entry<Field, Type1IBD> entry : ibd.getType1IBDPerName().entrySet()) {
			ibd2.childIBD1s.add(addIBDInstance(entry.getKey().getName(), entry.getValue(), indent + 1));
		}
		
		for (Map.Entry<Field, Type2IBD> entry : ibd.getType2IBDPerName().entrySet()) {
			ibd2.childIBD2s.add(addIBDInstance(entry.getKey().getName(), entry.getValue(), indent + 1));
		}
		
		addType2IBDFlows(ibd);
		return ibd2;
	}
	
	private void addType2IBDFlows(Type2IBD ibd) {
		ibd.connectFlows(); // (do this AFTER nested IBD2s have been created!)
		
		for (InterfacePort interfacePort : ibd.getInterfacePortPerName().values()) {
			for (Port connectedPort : interfacePort.getConnectedPorts()) {
				addFlow(portPerPort.get(interfacePort), portPerPort.get(connectedPort), interfacePort, connectedPort);
			}
		}
		
		for (Type1IBD ibd1 : ibd.getType1IBDPerName().values()) {
			for (ASALPort primitivePort : ibd1.getPrimitivePortPerName().values()) {
				for (Port connectedPort : primitivePort.getLegacy().getConnectedPorts()) {
					addFlow(portPerPort.get(primitivePort.getLegacy()), portPerPort.get(connectedPort), primitivePort.getLegacy(), connectedPort);
				}
			}
		}
		
		for (Type2IBD ibd2 : ibd.getType2IBDPerName().values()) {
			addType2IBDFlows(ibd2);
		}
	}
	
	private void addFlow(IPort p1, IPort p2, Port p1p, Port p2p) {
		if (p1 instanceof IBD1Port && p2 instanceof IBD1Port) {
			addIBD1Flow((IBD1Port) p1, (IBD1Port) p2);
			return;
		}
		
		if (p1 instanceof IBD2Port && p2 instanceof IBD2Port) {
			addIBD2Flow((IBD2Port) p1, (IBD2Port) p2);
			return;
		}
		
		if (p1 instanceof IBD1Port && p2 instanceof IBD2Port) {
			addIBD1ToIBD2Flow((IBD1Port) p1, (IBD2Port) p2);
			return;
		}
		
		if (p1 instanceof IBD2Port && p2 instanceof IBD1Port) {
			addIBD1ToIBD2Flow((IBD1Port) p2, (IBD2Port) p1);
			return;
		}
		
		if (p1 == null) {
			throw new Error("Should not happen!");
		}
		
		if (p2 == null) {
			for (Map.Entry<Port, IPort> e : portPerPort.entrySet()) {
				if (e.getKey().getFileLocation().equals(p2p.getFileLocation())) {
					throw new Error("Should not happen, could not find repr of " + p2p.getFileLocation() + " / " + p2p.hashCode() + " (but it is there?!)\nFlow starts at " + p1p.getFileLocation());
				}
			}
			
			throw new Error("Should not happen, could not find repr of " + p2p.getFileLocation());
		}
		
		throw new Error("Should not happen, connecting " + p1.getClass().getCanonicalName() + " to " + p2.getClass().getCanonicalName() + "!");
	}
	
	private void addIBD1Flow(IBD1Port v1, IBD1Port v2) {
		if (v1.computeDir() == Dir.OUT && v2.computeDir() == Dir.IN) {
			if (getIBD1Flow(v1, v2) == null) {
				ibd1Flows.add(new IBD1Flow(v1, v2));
			}
			
			return;
		}
		
		if (v1.computeDir() == Dir.IN && v2.computeDir() == Dir.OUT) {
			addIBD1Flow(v2, v1);
			return;
		}
		
		String msg = "Should not connect ports:";
		msg += "\n\t" + v1.toString();
		msg += "\n\t" + v2.toString();
		throw new Error(msg);
	}
	
	private void addIBD2Flow(IBD2Port v1, IBD2Port v2) {
		if (getIBD2Flow(v1, v2) == null) {
			ibd2Flows.add(new IBD2Flow(v1, Dir.OUT, v2, Dir.OUT));
			
			for (IBD1ToIBD2Flow f12 : ibd1ToIBD2Flows) {
				for (IBD1ToIBD2Flow f21 : ibd1ToIBD2Flows) {
					if ((f21.v2 == v2 && f12.v2 == v1) || (f12.v2 == v2 && f21.v2 == v1)) {
						if (f21.v1.isSameSuffix(f12.v1)) {
							addIBD1Flow(f21.v1, f12.v1);
						}
					}
				}
			}
		}
	}
	
	private void addIBD1ToIBD2Flow(IBD1Port v1, IBD2Port v2) {
		if (getIBD1ToIBD2Flow(v1, v2) == null) {
			IBD1ToIBD2Flow f12 = new IBD1ToIBD2Flow(v1, v2);
			ibd1ToIBD2Flows.add(f12);
			
			v1.attachedPorts.add(v2);
			
			for (IBD2Flow f22 : ibd2Flows) {
				for (IBD1ToIBD2Flow f21 : ibd1ToIBD2Flows) {
					if ((f12.v2 == f22.v1 && f21.v2 == f22.v2) || (f12.v2 == f22.v2 && f21.v2 == f22.v1)) {
						if (f12.v1.isSameSuffix(f21.v1)) {
							addIBD1Flow(f12.v1, f21.v1);
						}
					}
				}
			}
		}
	}
	
	private IBD1Flow getIBD1Flow(IBD1Port v1, IBD1Port v2) {
		for (IBD1Flow f : ibd1Flows) {
			if (f.v1 == v1 && f.v2 == v2) {
				return f;
			}
		}
		
		return null;
	}
	
	private IBD2Flow getIBD2Flow(IBD2Port v1, IBD2Port v2) {
		for (IBD2Flow f : ibd2Flows) {
			if (f.v1 == v1 && f.v2 == v2) {
				return f;
			}
			
			if (f.v2 == v1 && f.v1 == v2) {
				return f;
			}
		}
		
		return null;
	}
	
	private IBD1ToIBD2Flow getIBD1ToIBD2Flow(IBD1Port v1, IBD2Port v2) {
		for (IBD1ToIBD2Flow f : ibd1ToIBD2Flows) {
			if (f.v1 == v1 && f.v2 == v2) {
				return f;
			}
		}
		
		return null;
	}
	
	private IBD1Instance getOrCreateIBD1(String name, Type1IBD instance, int indent) {
		IBD1Instance result = ibd1InstancePerName.get(name);
		
		if (result != null) {
			println(indent, "Adding IBD1 instance " + name + ": " + instance.getName() + " (again)");
			result.narrowInstance = getNarrowInstance(name, result.narrowInstance, instance);
			result.addToLegacy(instance);
		} else {
			println(indent, "Adding IBD1 instance " + name + ": " + instance.getName());
			result = new IBD1Instance(name, instance);
			ibd1InstancePerName.put(name, result);
		}
		
		ibd1InstancePerOrigInstance.put(instance, result);
		return result;
	}
	
	private IBD2Instance getOrCreateIBD2(String name, Type2IBD instance, int indent) {
		IBD2Instance result = ibd2InstancePerName.get(name);
		
		if (result != null) {
			println(indent, "Adding IBD2 instance " + name + ": " + instance.getName() + " (again)");
			result.narrowInstance = getNarrowInstance(name, result.narrowInstance, instance);
			result.addToLegacy(instance);
		} else {
			println(indent, "Adding IBD2 instance " + name + ": " + instance.getName());
			result = new IBD2Instance(name, instance);
			ibd2InstancePerName.put(name, result);
		}
		
		ibd2InstancePerOrigInstance.put(instance, result);
		return result;
	}
	
	private static <T> T getNarrowInstance(String name, T type1, T type2) {
		if (type1.getClass().isAssignableFrom(type2.getClass())) {
			return type2;
		}
		
		if (type2.getClass().isAssignableFrom(type1.getClass())) {
			return type1;
		}
		
		String msg = "Incompatible types for block " + name + ":";
		msg += "\n\t\t\t" + type1.getClass().getCanonicalName();
		msg += "\n\t\t\t" + type2.getClass().getCanonicalName();
		throw new Error(msg);
	}
	
	public void print() {
		for (IBD2Instance ibd2 : ibd2InstancePerName.values()) {
			System.out.println("IBD2 " + ibd2.name + ": " + ibd2.narrowInstance.getName());
			
			for (Map.Entry<String, IBD2Port> e : ibd2.portPerId.entrySet()) {
				System.out.println("\towns " + e.getValue());
			}
			
			for (IBD2Flow f2 : ibd2Flows) {
				if (f2.v1.owner == ibd2) {
					System.out.println("\t" + f2.v1 + "\n\t\t<--> " + f2.v2);
				}
				
				if (f2.v2.owner == ibd2) {
					System.out.println("\t" + f2.v2 + "\n\t\t<--> " + f2.v1);
				}
			}
		}
		
		for (IBD1ToIBD2Flow f : ibd1ToIBD2Flows) {
			System.out.println("IBD1 to IBD2:");
			System.out.println("\t" + f.v1 + "\n\t\t<--> " + f.v2);
		}
	}
}
