package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JPulse;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.TextOptions;
import jlx.utils.Texts;

public class PulsePack {
	private final ReprPort mainPort;
	private final Map<ReprPort, ASALSymbolicValue> valuePerPort;
	private final int hashCode;
	
	public PulsePack(ReprPort mainPort, Map<ReprPort, ASALSymbolicValue> valuePerPort) {
		this.mainPort = mainPort;
		this.valuePerPort = valuePerPort;
		
		hashCode = Objects.hash(mainPort, valuePerPort);
	}
	
	public PulsePack(ReprPort mainPort, ASALSymbolicValue value) {
		this(mainPort, Collections.singletonMap(mainPort, value));
	}
	
	public ReprPort getMainPort() {
		return mainPort;
	}
	
	public Map<ReprPort, ASALSymbolicValue> getValuePerPort() {
		return valuePerPort;
	}
	
	public List<ASALSymbolicValue> getOrderedValues() {
		List<ASALSymbolicValue> result = new ArrayList<ASALSymbolicValue>();
		result.add(valuePerPort.get(mainPort));
		
		for (ReprPort drp : mainPort.getDataPorts()) {
			result.add(valuePerPort.get(drp));
		}
		
		return result;
	}
	
	public boolean isPulse() {
		return JPulse.class.isAssignableFrom(mainPort.getType());
	}
	
	public boolean isTruePulse() {
		if (JPulse.class.isAssignableFrom(mainPort.getType())) {
			return valuePerPort.get(mainPort).toBoolean();
		}
		
		return false;
	}
	
	public boolean hasMultiplePvs() {
		if (mainPort.getPossibleValues().size() > 1) {
			return true;
		}
		
		for (ReprPort drp : mainPort.getDataPorts()) {
			if (drp.getPossibleValues().size() > 1) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PulsePack other = (PulsePack) obj;
		return Objects.equals(mainPort, other.mainPort) && Objects.equals(valuePerPort, other.valuePerPort);
	}
	
	@Override
	public String toString() {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : valuePerPort.entrySet()) {
			elems.add(e.getKey() + " = " + TextOptions.current().escapeChars(e.getValue().toString()));
		}
		
		return "{ " + Texts.concat(elems, "; ") + " }";
	}
}
