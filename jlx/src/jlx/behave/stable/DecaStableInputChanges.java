package jlx.behave.stable;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.PulsePack;
import jlx.behave.proto.PulsePackMap;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Dir;
import jlx.utils.Texts;

public class DecaStableInputChanges {
	private final PulsePackMap newValuePerPort;
	private final ReprPort timeoutPort;
	private final ReprPort durationPort;
	private final boolean isHiddenTimerTrigger;
	
	public DecaStableInputChanges(PulsePackMap newValuePerPort, ReprPort timeoutPort, ReprPort durationPort, boolean isHiddenTimerTrigger) {
		this.newValuePerPort = newValuePerPort;
		this.timeoutPort = timeoutPort;
		this.durationPort = durationPort;
		this.isHiddenTimerTrigger = isHiddenTimerTrigger;
	}
	
	public PulsePackMap getNewValuePerPort() {
		return newValuePerPort;
	}
	
	public ReprPort getTimeoutPort() {
		return timeoutPort;
	}
	
	public ReprPort getDurationPort() {
		return durationPort;
	}
	
	public boolean isHiddenTimerTrigger() {
		return isHiddenTimerTrigger;
	}
	
	public PulsePackMap applyToValuation(PulsePackMap valuePerPort) {
		Map<ReprPort, PulsePack> result = new HashMap<ReprPort, PulsePack>();
		result.putAll(valuePerPort.getPackPerPort());
		
		if (isHiddenTimerTrigger) {
			return new PulsePackMap(result, Dir.IN);
		}
		
		if (timeoutPort != null && durationPort != null) {
			result.put(timeoutPort, new PulsePack(timeoutPort, ASALSymbolicValue.TRUE));
			return new PulsePackMap(result, Dir.IN);
		}
		
		for (Map.Entry<ReprPort, PulsePack> e : newValuePerPort.getPackPerPort().entrySet()) {
			result.put(e.getKey(), e.getValue());
		}
		
		return new PulsePackMap(result, Dir.IN);
	}
	
	@Override
	public String toString() {
		if (isHiddenTimerTrigger) {
			return "<<hidden-timer-trigger>>";
		}
		
		if (timeoutPort != null && durationPort != null) {
			return "<<timer " + durationPort.getReprOwner().getName() + "::" + durationPort.getName() + ">>";
		}
		
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, PulsePack> e : newValuePerPort.getPackPerPort().entrySet()) {
			elems.add(e.getKey().getReprOwner().getName() + "::" + e.getKey().getName() + "=" + e.getValue());
		}
		
		return Texts.concat(elems, "++");
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(newValuePerPort, timeoutPort, durationPort, isHiddenTimerTrigger);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecaStableInputChanges other = (DecaStableInputChanges) obj;
		return Objects.equals(newValuePerPort, other.newValuePerPort) && Objects.equals(timeoutPort, other.timeoutPort) && Objects.equals(durationPort, other.durationPort) && isHiddenTimerTrigger == other.isHiddenTimerTrigger;
	}
}

