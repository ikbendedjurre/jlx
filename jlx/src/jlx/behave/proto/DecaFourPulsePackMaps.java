package jlx.behave.proto;

import java.util.*;

public class DecaFourPulsePackMaps {
	private final Set<PulsePackMap> maps;
	private final Set<PulsePackMap> zeroExternalPulseMaps;
	private final Set<PulsePackMap> oneExternalPulseMaps;
//	private final Set<PulsePackMap> deactivatedPremises;
	
	public DecaFourPulsePackMaps(Set<PulsePackMap> externalMaps, boolean external) {
		this.maps = external ? extractMaps(externalMaps) : externalMaps;
		
		zeroExternalPulseMaps = extractMaps(0);
		oneExternalPulseMaps = extractMaps(1);
//		deactivatedPremises = extractDeactivatedPremises();
	}
	
	private static Set<PulsePackMap> extractMaps(Set<PulsePackMap> maps) {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (PulsePackMap map : maps) {
			result.add(map.extractExternalMap());
		}
		
		return Collections.unmodifiableSet(result);
	}
	
	private Set<PulsePackMap> extractMaps(int externalPulseCount) {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (PulsePackMap map : maps) {
			if (map.getExternalPulseCount() == externalPulseCount) {
				result.add(map.extractExternalMap());
			}
		}
		
		return Collections.unmodifiableSet(result);
	}
	
//	private Set<PulsePackMap> extractDeactivatedPremises() {
//		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
//		
//		for (PulsePackMap map : maps) {
//			result.add(map.deactivate());
//		}
//		
//		return Collections.unmodifiableSet(result);
//	}
	
	public Set<PulsePackMap> getMaps() {
		return maps;
	}
	
	public Set<PulsePackMap> getZeroExternalPulseMaps() {
		return zeroExternalPulseMaps;
	}
	
	public Set<PulsePackMap> getOneExternalPulseMaps() {
		return oneExternalPulseMaps;
	}
	
	public boolean implies(PulsePackMap implied) {
		for (PulsePackMap map : maps) {
			if (map.implies(implied)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean couldBeImpliedBy(PulsePackMap premise) {
		for (PulsePackMap map : maps) {
			if (premise.couldImply(map)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isImpliedBy(PulsePackMap premise) {
		for (PulsePackMap map : maps) {
			if (premise.implies(map)) {
//				System.out.println("Does");
//				System.out.println("\t" + premise.toString());
//				System.out.println("imply");
//				System.out.println("\t" + map.toString());
//				System.out.println("?");
				return true;
			}
		}
		
		return false;
	}
	
//	public boolean isImpliedByDeactivated(PulsePackMap deactivatedPremise) {
//		return deactivatedPremises.contains(deactivatedPremise);
//	}
}
