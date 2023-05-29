package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class PulsePackMap {
	private final Map<ReprPort, PulsePack> packPerPort;
	private final Dir dir;
	private final int hashCode;
	
	public final static PulsePackMap EMPTY_IN = new PulsePackMap(Collections.emptyMap(), Dir.IN);
	public final static PulsePackMap EMPTY_OUT = new PulsePackMap(Collections.emptyMap(), Dir.OUT);
	
	public PulsePackMap(Map<ReprPort, PulsePack> packPerPort, Dir dir) {
		this.packPerPort = packPerPort;
		this.dir = dir;
		
		hashCode = Objects.hash(packPerPort, dir);
	}
	
	public Map<ReprPort, PulsePack> getPackPerPort() {
		return packPerPort;
	}
	
	public Dir getDir() {
		return dir;
	}
	
	public boolean isEmpty() {
		return packPerPort.isEmpty();
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
		PulsePackMap other = (PulsePackMap) obj;
		return Objects.equals(packPerPort, other.packPerPort) && Objects.equals(dir, other.dir);
	}
	
	@Override
	public String toString() {
		return Texts.concat(packPerPort.values(), " | ");
	}
	
	public boolean containsTruePulse() {
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getValue().isTruePulse()) {
				 return true;
			}
		}
		
		return false;
	}
	
	public Map<ReprPort, ASALSymbolicValue> extractValuation() {
		Map<ReprPort, ASALSymbolicValue> result = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			result.putAll(e.getValue().getValuePerPort());
		}
		
		return result;
	}
	
	public boolean containsPortValue(ReprPort port) {
		boolean result;
		
		if (port.getPulsePort() != null) {
			result = packPerPort.get(port.getPulsePort()).getValuePerPort().containsKey(port);
		} else {
			PulsePack pack = packPerPort.get(port);
			
			if (pack == null) {
				throw new Error("Should not happen; no entry for " + port + "!");
			}
			
			result = pack.getValuePerPort().containsKey(port);
		}
		
		return result;
	}
	
	public ASALSymbolicValue getPortValue(ReprPort port, boolean expectAssigned) {
		ASALSymbolicValue result;
		
		if (port.getPulsePort() != null) {
			result = packPerPort.get(port.getPulsePort()).getValuePerPort().get(port);
			
			if (!expectAssigned) {
				if (result == null) {
					result = ASALSymbolicValue.from(port.getInitialValue());
				}
			}
		} else {
			PulsePack pack = packPerPort.get(port);
			
			if (pack != null) {
				result = pack.getValuePerPort().get(port);
			} else {
				if (expectAssigned) {
					throw new Error("Should not happen; no entry for " + port + "!");
				}
				
				result = ASALSymbolicValue.from(port.getInitialValue());
			}
		}
		
		if (result == null) {
			throw new Error("Should not happen; value of " + port + " is null!");
		}
		
		return result;
	}
	
	public PulsePackMap extractExternalMap() {
		Map<ReprPort, PulsePack> packPerExternalPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				packPerExternalPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(packPerExternalPort, dir);
	}
	
	public PulsePackMap extractInternalMap() {
		Map<ReprPort, PulsePack> packPerInternalPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (!e.getKey().isPortToEnvironment()) {
				packPerInternalPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(packPerInternalPort, dir);
	}
	
	public PulsePackMap extractMultiplePvsMap() {
		Map<ReprPort, PulsePack> newPackPerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getValue().hasMultiplePvs()) {
				newPackPerPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(newPackPerPort, dir);
	}
	
	public PulsePackMap extractOwnerMap(JScope owner) {
		Map<ReprPort, PulsePack> packPerOwnerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getKey().getReprOwner() == owner) {
				packPerOwnerPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(packPerOwnerPort, dir);
	}
	
	public PulsePackMap extractVmMap(VerificationModel vm) {
		Map<ReprPort, PulsePack> vmPackPerPort = new HashMap<ReprPort, PulsePack>();
		Map<ReprPort, PulsePack> vmPackPerTruePulsePort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			ReprPort reprPort = vm.getReprPort(e.getKey());
			
			if (reprPort != null) {
				if (e.getValue().isTruePulse()) {
					vmPackPerTruePulsePort.put(reprPort, new PulsePack(reprPort, ASALSymbolicValue.TRUE));
				} else {
					vmPackPerPort.put(reprPort, new PulsePack(reprPort, e.getValue().getValuePerPort()));
				}
			}
		}
		
		vmPackPerPort.putAll(vmPackPerTruePulsePort);
		return new PulsePackMap(vmPackPerPort, dir);
	}
	
	public PulsePackMap deactivate() {
		Map<ReprPort, PulsePack> deactivatedPackPerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getValue().isPulse()) {
				deactivatedPackPerPort.put(e.getKey(), new PulsePack(e.getKey(), ASALSymbolicValue.FALSE));
			} else {
				deactivatedPackPerPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(deactivatedPackPerPort, dir);
	}
	
	public PulsePackMap discardFalsePulses() {
		Map<ReprPort, PulsePack> newPackPerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (!e.getValue().isPulse() || e.getValue().isTruePulse()) {
				newPackPerPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(newPackPerPort, dir);
	}
	
	public PulsePackMap prioritize() {
		Map<ReprPort, PulsePack> prioritizedPackPerPort = new HashMap<ReprPort, PulsePack>();
		Map<ReprPort, PulsePack> maxPriorityPackPerPort = new HashMap<ReprPort, PulsePack>();
		Integer maxPriority = null;
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getValue().isTruePulse()) {
				if (maxPriority == null || e.getKey().getPriority() > maxPriority) {
					for (Map.Entry<ReprPort, PulsePack> e2 : maxPriorityPackPerPort.entrySet()) {
						prioritizedPackPerPort.put(e2.getKey(), new PulsePack(e2.getKey(), ASALSymbolicValue.FALSE));
					}
					
					maxPriorityPackPerPort.clear();
					maxPriority = e.getKey().getPriority();
				}
				
				if (e.getKey().getPriority() == maxPriority) {
					maxPriorityPackPerPort.put(e.getKey(), e.getValue());
				} else {
					prioritizedPackPerPort.put(e.getKey(), new PulsePack(e.getKey(), ASALSymbolicValue.FALSE));
				}
			} else {
				prioritizedPackPerPort.put(e.getKey(), e.getValue());
			}
		}
		
		prioritizedPackPerPort.putAll(maxPriorityPackPerPort);
		PulsePackMap result = new PulsePackMap(prioritizedPackPerPort, dir);
		
		if (!result.equals(this)) {
			System.out.println("this = " + this);
			System.out.println("result = " + result);
			throw new Error("...");
		}
		
		return result;
	}
	
	public int getPulseCount() {
		int result = 0;
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
				result++;
			}
		}
		
		return result;
	}
	
	public static PulsePackMap from(Map<ReprPort, ASALSymbolicValue> valuePerPort, Dir dir) {
		Map<ReprPort, PulsePack> packPerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : valuePerPort.entrySet()) {
			if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
				Map<ReprPort, ASALSymbolicValue> val = new HashMap<ReprPort, ASALSymbolicValue>();
				val.put(e.getKey(), e.getValue());
				
				if (e.getValue().toBoolean()) {
					for (ReprPort drp : e.getKey().getDataPorts()) {
						val.put(drp, valuePerPort.get(drp));
					}
				}
				
				packPerPort.put(e.getKey(), new PulsePack(e.getKey(), val));
			} else {
				if (e.getKey().getPulsePort() == null) {
					packPerPort.put(e.getKey(), new PulsePack(e.getKey(), e.getValue()));
				}
			}
		}
		
		return new PulsePackMap(packPerPort, dir);
	}
	
	public boolean implies(PulsePackMap other) {
		for (Map.Entry<ReprPort, PulsePack> e : other.packPerPort.entrySet()) {
			PulsePack v = packPerPort.get(e.getKey());
			
			if (v == null) {
				System.out.println("Base map does not have a value for " + e.getKey());
				System.out.println("Candidate map for implication has value " + e.getValue());
				throw new Error("Should not happen!");
			}
			
			if (!e.getValue().equals(v)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean couldImply(PulsePackMap other) {
		for (Map.Entry<ReprPort, PulsePack> e : other.packPerPort.entrySet()) {
			PulsePack v = packPerPort.get(e.getKey());
			
			if (v != null && !e.getValue().equals(v)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns TRUE iff the other map does not conflict with this map.
	 * In other words, the pulse packs associated with each port in this map will remain the same
	 * if all mappings of the other map were to be added to this map.
	 */
	public boolean canAdd(PulsePackMap other) {
		for (Map.Entry<ReprPort, PulsePack> e : other.packPerPort.entrySet()) {
			PulsePack value = packPerPort.get(e.getKey());
			
			if (value != null && !value.equals(e.getValue())) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean canCombine(PulsePackMap other) {
		return canAdd(other) && other.canAdd(this);
	}
	
	/**
	 * Adds another map to a copy of this map.
	 * (Values of the other map override values in the copy.)
	 */
	public PulsePackMap combine(PulsePackMap other) {
		Map<ReprPort, PulsePack> result = new HashMap<ReprPort, PulsePack>(packPerPort);
		result.putAll(other.packPerPort);
		return new PulsePackMap(result, dir);
	}
	
//	public PulsePackMap keepLastChange(PulsePackMap other) {
//		Map<ReprPort, PulsePack> result = new HashMap<ReprPort, PulsePack>(packPerPort);
//		
//		for (Map.Entry<ReprPort, PulsePack> e : other.packPerPort.entrySet()) {
//			if (!e.getValue().isPulse() || !result.containsKey(e.getKey()) || e.getValue().isTruePulse()) {
//				result.put(e.getKey(), e.getValue());
//			}
//		}
//		
//		return new PulsePackMap(result, dir);
//	}
	
	public int getExternalPulseCount() {
		int result = 0;
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				if (e.getValue().isTruePulse()) {
					result++;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Baseline parameter must contain AT LEAST the keys of this map!
	 */
	public int getExternalDiffCount(PulsePackMap baseline) {
		int result = 0;
		
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				if (!e.getValue().equals(baseline.packPerPort.get(e.getKey()))) {
					result++;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Computes changes of this map compared to a given predecessor map.
	 * Pulse packs that are TRUE are always included, regardless of a change in value.
	 * Only considers ports that are present in THIS map!!
	 */
	public PulsePackMap extractEventMap(PulsePackMap pred) {
		Map<ReprPort, PulsePack> changedPackPerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : getPackPerPort().entrySet()) {
			if (e.getValue().isPulse()) {
				if (e.getValue().isTruePulse()) {
					changedPackPerPort.put(e.getKey(), e.getValue());
				}
			} else {
				if (!e.getValue().equals(pred.getPackPerPort().get(e.getKey()))) {
					changedPackPerPort.put(e.getKey(), e.getValue());
				}
			}
		}
		
		return new PulsePackMap(changedPackPerPort, dir);
	}
	
	public PulsePackMap getChanges(PulsePackMap pred) {
		Map<ReprPort, PulsePack> changedPackPerPort = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : getPackPerPort().entrySet()) {
			if (!e.getValue().equals(pred.getPackPerPort().get(e.getKey()))) {
				changedPackPerPort.put(e.getKey(), e.getValue());
			}
		}
		
		return new PulsePackMap(changedPackPerPort, dir);
	}
	
	public static boolean isConsistent(Map<ReprPort, PulsePack> packPerPort) {
		for (Map.Entry<ReprPort, PulsePack> e : packPerPort.entrySet()) {
			List<ASALSymbolicValue> vs = e.getValue().getOrderedValues();
			
			for (ReprPort sp : e.getKey().getSyncedPorts()) {
				if (!packPerPort.get(sp).getOrderedValues().equals(vs)) {
					if (e.getKey().getSyncedPorts().size() == 1) {
						throw new Error("Should not happen!");
					}
					
//					if (e.getKey().getSyncedPorts().size() > 1) {
//						System.out.println(e.getValue() + " vs " + packPerPort.get(sp));
//					}
					
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static Set<PulsePackMap> trim(Set<PulsePackMap> maps, Map<ReprPort, Set<PulsePack>> pvsPerPort, Dir dir) {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (PulsePackMap map1 : maps) {
			Map<ReprPort, PulsePack> relevantPackPerPort = new HashMap<ReprPort, PulsePack>();
			
			for (Map.Entry<ReprPort, PulsePack> e : map1.getPackPerPort().entrySet()) {
				Set<PulsePack> pvs = new HashSet<PulsePack>();
				
				for (PulsePackMap map2 : maps) {
					Map<ReprPort, PulsePack> m = new HashMap<ReprPort, PulsePack>(map2.getPackPerPort());
					m.put(e.getKey(), e.getValue());
					
					if (m.equals(map1.getPackPerPort())) { //Now equal to the primary map?
						pvs.add(map2.getPackPerPort().get(e.getKey())); //Then an alternative value!
					}
				}
				
				if (pvs.size() < pvsPerPort.get(e.getKey()).size()) {
					relevantPackPerPort.put(e.getKey(), e.getValue());
				}
			}
			
			result.add(new PulsePackMap(relevantPackPerPort, dir));
		}
		
		return result;
	}
}

