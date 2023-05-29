package jlx.models;

import java.util.*;

import jlx.blocks.ibd1.Type1IBD;
import jlx.blocks.ibd2.Type2IBD;
import jlx.common.GlobalSettings;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.*;
import jlx.models.IBDInstances.*;

public abstract class ComposableModel {
	private IBDInstances ibdInstances;
	
	public ComposableModel() {
		ibdInstances = new IBDInstances();
	}
	
	public Collection<IBD1Instance> getIBD1Instances() {
		return ibdInstances.getIBD1Instances();
	}
	
	public Collection<IBD2Instance> getIBD2Instances() {
		return ibdInstances.getIBD2Instances();
	}
	
	public Map<Type1IBD, IBD1Instance> getIBD1InstancePerOrigInstance() {
		return ibdInstances.getIBD1InstancePerOrigInstance();
	}
	
	public Map<Type2IBD, IBD2Instance> getIBD2InstancePerOrigInstance() {
		return ibdInstances.getIBD2InstancePerOrigInstance();
	}
	
	public Set<IBD1Port> getIBD1Ports() {
		Set<IBD1Port> result = new HashSet<IBD1Port>();
		
		for (IBD1Instance ibd1 : getIBD1Instances()) {
			result.addAll(ibd1.getPortPerName().values());
		}
		
		return Collections.unmodifiableSet(result);
	}
	
	public Set<IBD2Port> getIBD2Ports() {
		Set<IBD2Port> result = new HashSet<IBD2Port>();
		
		for (IBD2Instance ibd2 : getIBD2Instances()) {
			result.addAll(ibd2.getPortPerId().values());
		}
		
		return Collections.unmodifiableSet(result);
	}
	
	public Set<IBD1Flow> getIBD1Flows() {
		return ibdInstances.getIBD1Flows();
	}
	
	public Set<IBD2Flow> getIBD2Flows() {
		return ibdInstances.getIBD2Flows();
	}
	
	public Set<IBD1ToIBD2Flow> getIBD1ToIBD2Flows() {
		return ibdInstances.getIBD1ToIBD2Flows();
	}
	
	public <T extends Type2IBD> T add(String name, Class<T> blockClz) throws ReflectionException {
		return add(name, ReflectionUtils.createStdInstance(blockClz));
	}
	
	public <T extends Type1IBD> T add(String name, T instance) throws ReflectionException {
		ibdInstances.addIBDInstance(name, instance);
		return instance;
	}
	
	public <T extends Type2IBD> T add(String name, T instance) throws ReflectionException {
		ibdInstances.addIBDInstance(name, instance);
		return instance;
	}
	
	public void checkConformance() {
		try {
			validateDataFlows();
		} catch (InvalidDataFlowException e) {
			e.printStackTrace();
		}
	}
	
	public IBD1Instance get(String name) {
		for (IBD1Instance ibd1 : getIBD1Instances()) {
			if (ibd1.getName().equals(name)) {
				return ibd1;
			}
		}
		
		String msg = "Could not find IBD1 named \"" + name + "\" among";
		
		for (IBD1Instance ibd1 : getIBD1Instances()) {
			msg += "\n\t\tIBD1 " + ibd1.getName();
		}
		
		throw new Error(msg);
	}
	
	private IBD1Port convertToTarget(IBD1Port term) {
		if (term.computeDataPorts().size() > 0) {
			return term;
		} else {
			return term.computePulsePort();
		}
	}
	
	private Set<IBD1Port> getTargets(IBD1Port start) {
		Set<IBD1Port> result = new HashSet<IBD1Port>();
		
		for (IBD1Flow f : ibdInstances.getIBD1Flows()) {
			if (f.v1 == start) {
				result.add(convertToTarget(f.v2));
			}
			
			if (f.v2 == start) {
				result.add(convertToTarget(f.v1));
			}
		}
		
		return result;
	}
	
	private void addTargets(Map<IBD1Port, Set<IBD1Port>> portsPerTarget, IBD1Port port) {
		for (IBD1Port target : getTargets(port)) {
			Set<IBD1Port> ports = portsPerTarget.get(target);
			
			if (ports == null) {
				ports = new HashSet<IBD1Port>();
				portsPerTarget.put(target, ports);
			}
			
			ports.add(port);
		}
	}
	
	private void validateDataFlows() throws InvalidDataFlowException {
		if (!GlobalSettings.areDataFlowsGrouped()) {
			return;
		}
		
		//We expect that grouped ports are connected to other group ports with similar names:
		for (IBD1Flow f : ibdInstances.getIBD1Flows()) {
			IBD1Port p1 = f.v1;
			IBD1Port p2 = f.v2;
			
			if (p1.computePulsePort() != null && p2.computePulsePort() == null) {
				throw new InvalidDataFlowException(f.v1, "Data port should be connected to another data port!");
			}
			
			if (p1.computePulsePort() == null && p2.computePulsePort() != null) {
				throw new InvalidDataFlowException(f.v2, "Data port should be connected to another data port!");
			}
			
			if (p1.computePulsePort() != null && p2.computePulsePort() != null) {
				if (!p1.nameParts.hasSameSuffix(p2.nameParts)) {
					throw new InvalidDataFlowException(f.v1, "Connected data ports should have the same suffix!");
				}
			}
			
			if (p1.computeDataPorts().size() > 0 && p2.computeDataPorts().isEmpty()) {
				throw new InvalidDataFlowException(f.v1, "Pulse port should be connected to another pulse port!");
			}
			
			if (p1.computeDataPorts().isEmpty() && p2.computeDataPorts().size() > 0) {
				throw new InvalidDataFlowException(f.v2, "Pulse port should be connected to another pulse port!");
			}
			
			if (p1.computeDataPorts().size() > 0 && p2.computeDataPorts().size() > 0) {
				if (!p1.nameParts.hasSameSuffix(p2.nameParts)) {
					throw new InvalidDataFlowException(f.v1, "Connected pulse ports should have the same suffix!");
				}
			}
		}
		
		//If SOME ports of a group are connected to something,
		//   ALL ports of that group should be connected to the same thing:
		for (IBD1Port p : ibdInstances.getIBD1Ports()) {
			if (p.computeDataPorts().size() > 0) {
				Map<IBD1Port, Set<IBD1Port>> portsPerTarget = new HashMap<IBD1Port, Set<IBD1Port>>();
				addTargets(portsPerTarget, p);
				
				for (IBD1Port dataPort : p.computeDataPorts()) {
					addTargets(portsPerTarget, dataPort);
				}
				
				Set<IBD1Port> groupedPorts = new HashSet<IBD1Port>();
				groupedPorts.add(p);
				groupedPorts.addAll(p.computeDataPorts());
				
				for (Map.Entry<IBD1Port, Set<IBD1Port>> entry : portsPerTarget.entrySet()) {
					for (IBD1Port groupedPort : groupedPorts) {
						if (!entry.getValue().contains(groupedPort)) {
							String msg = "Port should be connected to " + entry.getKey() + " (or one of its data ports)";
							msg += " because the following ports are also connected to it:";
							
							for (IBD1Port gp : entry.getValue()) {
								msg += "\n\t\t\t" + gp;
							}
							
							throw new InvalidDataFlowException(groupedPort, msg);
						}
					}
				}
			}
		}
	}
}
