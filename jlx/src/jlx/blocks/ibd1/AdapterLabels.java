package jlx.blocks.ibd1;

import java.util.*;

public class AdapterLabels {
	private Map<String, Label> labelPerName;
	
	public class Label {
		private Set<PrimitivePort<?>> synchronizedPorts;
		
		public final String label;
		public final int maxPortCount;
		
		private Label(String label, int maxPortCount) {
			this.label = label;
			this.maxPortCount = maxPortCount;
			
			synchronizedPorts = new HashSet<PrimitivePort<?>>();
		}
		
		public void add(PrimitivePort<?> synchronizedPort) {
			synchronizedPorts.add(synchronizedPort);
			
			if (synchronizedPorts.size() > maxPortCount) {
				throw new Error("Can only synchronize up to " + maxPortCount + " ports!");
			}
			
			if (synchronizedPorts.size() > 1) {
				for (PrimitivePort<?> p : synchronizedPorts) {
					if (!(p instanceof InPort)) {
						throw new Error("Can only synchronize input ports!");
					}
				}
			}
		}
		
		public Set<PrimitivePort<?>> getSynchronizedPorts() {
			return synchronizedPorts;
		}
	}
	
	public AdapterLabels() {
		labelPerName = new HashMap<String, Label>();
	}
	
	public Label get(String label) {
		return get(label, 1);
	}
	
	public Label get(String label, int maxPortCount) {
		Label labelObject = labelPerName.get(label);
		
		if (labelObject == null) {
			labelObject = new Label(label, maxPortCount);
			labelPerName.put(label, labelObject);
		}
		
		if (labelObject.maxPortCount != maxPortCount) {
			throw new Error("Inconsistent values of \"maxPortCount\"!");
		}
		
		return labelObject;
	}
	
	public static Label combine(Label l1, Label l2) {
		if (l1 == null) {
			return l2;
		}
		
		if (l2 == null) {
			return l1;
		}
		
		if (l1 != l2) {
			throw new Error("Inconsistent adapter labels (\"" + l1.label + "\" and \"" + l2.label + "\")!");
		}
		
		return l1;
	}
}

