package jlx.blocks.ibd1;

import java.util.*;

import jlx.asal.j.JPulse;
import jlx.models.UnifyingBlock.ReprPort;

public class VerificationModel {
	private final String name;
	private final Map<String, VerificationAction> actionPerId;
	private final Map<VerificationAction, ReprPort> reprPortPerAction;
	
	public VerificationModel(String name) {
		this.name = name;
		
		actionPerId = new HashMap<String, VerificationAction>();
		reprPortPerAction = new HashMap<VerificationAction, ReprPort>();
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, VerificationAction> getActionPerId() {
		return actionPerId;
	}
	
	public ReprPort getReprPort(ReprPort port) {
		VerificationAction action = port.getActionPerVm().get(this);
		
		if (action == null) {
			return null; //Not part of the VM, therefore no representative port.
		}
		
		ReprPort reprPort = reprPortPerAction.get(action);
		
		if (reprPort == null) {
			reprPort = port;
			reprPortPerAction.put(action, reprPort);
		}
		
		if (reprPort != port) {
			if (!JPulse.class.isAssignableFrom(port.getType()) || port.getDataPorts().size() > 0) {
				throw new Error("Action abstraction is only permitted for pulse ports without a data port (" + port.getName() + ")!");
			}
			
			if (!JPulse.class.isAssignableFrom(reprPort.getType()) || reprPort.getDataPorts().size() > 0) {
				throw new Error("Action abstraction is only permitted for pulse ports without a data port (" + reprPort.getName() + ")!");
			}
			
			if (!port.getPossibleValues().equals(reprPort.getPossibleValues())) {
				throw new Error("Ports do not have the same possible values (" + port.getName() + " vs " + reprPort.getName() + ")!");
			}
		}
		
		return reprPort;
	}
	
//	public Map<ReprPort, ReprPort> getReprPortPerPort(Collection<ReprPort> ports) {
//		Map<ReprPort, ReprPort> result = new HashMap<ReprPort, ReprPort>();
//		
//		for (ReprPort port : ports) {
//			ReprPort reprPort = getReprPort(port);
//			
//			if (reprPort != null) {
//				result.put(port, reprPort);
//			}
//		}
//		
//		return result;
//	}
	
	public Set<ReprPort> getReprPorts(Collection<ReprPort> ports) {
		Set<ReprPort> result = new HashSet<ReprPort>();
		
		for (ReprPort port : ports) {
			ReprPort reprPort = getReprPort(port);
			
			if (reprPort != null) {
				result.add(reprPort);
			}
		}
		
		return result;
	}
	
	public VerificationAction action(String actionId) {
		VerificationAction result = actionPerId.get(actionId);
		
		if (result == null) {
			result = new VerificationAction(this, actionId);
			actionPerId.put(actionId, result);
		}
		
		return result;
	}
}

