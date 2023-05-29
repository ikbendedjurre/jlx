package jlx.blocks.ibd1;

import java.util.*;

import jlx.asal.j.JType;
import jlx.common.Port;
import jlx.utils.Dir;

public abstract class PrimitivePort<T extends JType> extends Port {
	private AdapterLabels.Label adapterLabel;
	private Set<VerificationAction> verificationActions;
	private Integer priority;
	private Integer executionTime;
	
	public PrimitivePort() {
		adapterLabel = null;
		verificationActions = new HashSet<VerificationAction>();
		priority = null;
		executionTime = null;
	}
	
	public abstract Dir getDir();
	
	public final AdapterLabels.Label getAdapterLabel() {
		return adapterLabel;
	}
	
	public final void set(AdapterLabels.Label adapterLabel) {
		if (this.adapterLabel != null) {
			throw new Error("Cannot set the adapter label \"" + this.adapterLabel.label + "\" multiple times!");
		}
		
		this.adapterLabel = adapterLabel;
		
		if (adapterLabel != null) {
			adapterLabel.add(this);
		}
	}
	
	public final Set<VerificationAction> getVerificationActions() {
		return verificationActions;
	}
	
	public final Integer getPriority() {
		return priority;
	}
	
	public final void setPriority(int priority) {
		this.priority = priority;
	}
	
	public final Integer getExecutionTime() {
		return executionTime;
	}
	
	public final void setExecutionTime(int executionTime) {
		this.executionTime = executionTime;
	}
}
