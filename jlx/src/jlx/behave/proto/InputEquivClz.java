package jlx.behave.proto;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.blocks.ibd1.VerificationModel;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Dir;

public class InputEquivClz {
	private final Set<PulsePackMap> inputVals;
	
//	private Set<Map<ReprPort, ASALSymbolicValue>> externalInputVals;
	private final Set<PulsePackMap> zeroExternalPulseInputVals;
	private final Set<PulsePackMap> oneExternalPulseInputVals;
	private final Map<VerificationModel, Set<PulsePackMap>> mapPerVm;
	
//	private Map<ReprPort, ASALSymbolicValue> minInputVal;
//	private int minInputValScore;
	private final Set<ASALSymbolicValue> enabledGuards;
	private final Set<ASALSymbolicValue> disabledGuards;
//	private ASALSymbolicValue combinedGuard;
//	private int minExternalPulseCount;
//	private ExternalInputEquivClz external;
	
	public InputEquivClz(Set<PulsePackMap> inputVals, Set<ASALSymbolicValue> enabledGuards, Set<ASALSymbolicValue> disabledGuards) {
		this.inputVals = inputVals;
		this.enabledGuards = enabledGuards;
		this.disabledGuards = disabledGuards;
		
//		combinedGuard = extractCombinedGuard();
//		externalInputVals = extractExternalInputVals();
		zeroExternalPulseInputVals = extractExternalPulsePackMaps(0);
		oneExternalPulseInputVals = extractExternalPulsePackMaps(1);
//		minInputVal = extractMinInputVal();
//		minInputValScore = getPulseCount(minInputVal);
//		minExternalPulseCount = extractMinExternalPulseCount();
//		external = extractExternal();
		
		mapPerVm = new HashMap<VerificationModel, Set<PulsePackMap>>();
	}
	
	public Set<PulsePackMap> getInputVals() {
		return inputVals;
	}
	
	/**
	 * All input valuations, but reduced to those that are EXTERNAL (i.e. ports from the environment) and which can CHANGE.
	 * In other words: all external D-ports are included, all external T-ports that are TRUE are included, and none of the DT-ports are included.
	 */
//	public Set<Map<ReprPort, ASALSymbolicValue>> getExternalInputVals() {
//		return externalInputVals;
//	}
	
	public Set<PulsePackMap> getZeroExternalPulseInputVals() {
		return zeroExternalPulseInputVals;
	}
	
	public Set<PulsePackMap> getOneExternalPulseInputVals() {
		return oneExternalPulseInputVals;
	}
	
//	public int getMinInputValScore() {
//		return minInputValScore;
//	}
//	
//	public Map<ReprPort, ASALSymbolicValue> getMinInputVal() {
//		return minInputVal;
//	}
//	
//	private Set<Map<ReprPort, ASALSymbolicValue>> extractExternalInputVals() {
//		Set<Map<ReprPort, ASALSymbolicValue>> result = new HashSet<Map<ReprPort, ASALSymbolicValue>>();
//		
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : inputVals) {
//			Map<ReprPort, ASALSymbolicValue> externalInputVal = new HashMap<ReprPort, ASALSymbolicValue>();
//			
//			for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//				if (e.getKey().isPortToEnvironment()) {
//					if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
//						if (e.getValue().toBoolean()) {
//							externalInputVal.put(e.getKey(), ASALSymbolicValue.TRUE);
//							
//							for (ReprPort drp : e.getKey().getDataPorts()) {
//								externalInputVal.put(drp, inputVal.get(drp));
//							}
//						}
//					} else {
//						if (e.getKey().getPulsePort() == null) {
//							externalInputVal.put(e.getKey(), e.getValue());
//						}
//					}
//				}
//			}
//			
//			result.add(externalInputVal);
//		}
//		
//		return result;
//	}
	
	private Set<PulsePackMap> extractExternalPulsePackMaps(int pulseCount) {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (PulsePackMap inputVal : inputVals) {
			PulsePackMap m = inputVal.extractExternalMap();
			
			if (m.getPulseCount() == pulseCount) {
				result.add(m);
			}
		}
		
		return result;
	}
	
	private Set<PulsePackMap> extractVmMap(VerificationModel vm) {
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (PulsePackMap inputVal : inputVals) {
			result.add(inputVal.extractVmMap(vm));
		}
		
		return result;
	}
	
	public Set<PulsePackMap> getVmMap(VerificationModel vm) {
		Set<PulsePackMap> result = mapPerVm.get(vm);
		
		if (result == null) {
			result = extractVmMap(vm);
			mapPerVm.put(vm, result);
		}
		
		return result;
	}
	
//	private Set<Map<ReprPort, ASALSymbolicValue>> extractOneExternalPulseInputVals() {
//		Set<Map<ReprPort, ASALSymbolicValue>> result = new HashSet<Map<ReprPort, ASALSymbolicValue>>();
//		
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : inputVals) {
//			Map<ReprPort, ASALSymbolicValue> externalInputVal = new HashMap<ReprPort, ASALSymbolicValue>();
//			int externalPulseCount = 0;
//			
//			for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//				if (e.getKey().isPortToEnvironment()) {
//					if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
//						if (e.getValue().toBoolean()) {
//							externalPulseCount++;
//							
//							if (externalPulseCount > 1) {
//								break;
//							}
//							
//							externalInputVal.put(e.getKey(), ASALSymbolicValue.TRUE);
//							
//							for (ReprPort drp : e.getKey().getDataPorts()) {
//								externalInputVal.put(drp, inputVal.get(drp));
//							}
//						}
//					} else {
//						if (e.getKey().getPulsePort() == null) {
//							externalInputVal.put(e.getKey(), e.getValue());
//						}
//					}
//				}
//			}
//			
//			if (externalPulseCount == 1) {
//				result.add(externalInputVal);
//			}
//		}
//		
//		return result;
//	}
//	
//	private static boolean impliesAll(Map<JScope, Map<ReprPort, ASALSymbolicValue>> premise, Map<JScope, Set<InputEquivClz>> conclusion) {
//		return false; //TODO
//	}
//	
//	private static Map<InputEquivClz, Set<InputEquivClz>> cache1 = new HashMap<InputEquivClz, Set<InputEquivClz>>();
//	private static Map<InputEquivClz, Set<InputEquivClz>> cache2 = new HashMap<InputEquivClz, Set<InputEquivClz>>();
//	private static Map<InputEquivClz, Set<InputEquivClz>> cache3 = new HashMap<InputEquivClz, Set<InputEquivClz>>();
//	private static Map<InputEquivClz, Set<InputEquivClz>> cache4 = new HashMap<InputEquivClz, Set<InputEquivClz>>();
//	
//	public boolean isExternalMatchOf(InputEquivClz other, boolean allowAEPP) {
//		if (allowAEPP) {
//			synchronized (cache1) {
//				if (HashMaps.containsValue(cache1, this, other)) {
//					return true;
//				}
//			}
//			
//			synchronized (cache3) {
//				if (HashMaps.containsValue(cache3, this, other)) {
//					return true;
//				}
//			}
//		} else {
//			synchronized (cache2) {
//				if (HashMaps.containsValue(cache2, this, other)) {
//					return true;
//				}
//			}
//			
//			synchronized (cache4) {
//				if (HashMaps.containsValue(cache4, this, other)) {
//					return true;
//				}
//			}
//		}
//		
//		for (Map<ReprPort, ASALSymbolicValue> i1 : inputVals) {
//			for (Map<ReprPort, ASALSymbolicValue> i2 : other.inputVals) {
//				if (isExternalMatch(i1, i2, allowAEPP)) {
//					if (allowAEPP) {
//						synchronized (cache1) {
//							HashMaps.containsValue(cache1, this, other);
//						}
//					} else {
//						synchronized (cache2) {
//							HashMaps.containsValue(cache2, this, other);
//						}
//					}
//					return true;
//				}
//			}
//		}
//		
//		if (allowAEPP) {
//			synchronized (cache3) {
//				HashMaps.inject(cache3, this, other);
//			}
//		} else {
//			synchronized (cache4) {
//				HashMaps.inject(cache4, this, other);
//			}
//		}
//		return false;
//	}
//	
//	private boolean isExternalMatch(Map<ReprPort, ASALSymbolicValue> inputVal1, Map<ReprPort, ASALSymbolicValue> inputVal2, boolean allowAEPP) {
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal1.entrySet()) {
//			if (e.getKey().isPortToEnvironment()) {
//				if (e.getKey().getType().equals(JPulse.class)) {
//					if (!allowAEPP && e.getValue().equals(ASALSymbolicValue.TRUE)) {
//						return false;
//					}
//				} else {
//					if (e.getKey().getPulsePort() == null) {
//						if (!e.getValue().equals(inputVal2.get(e.getKey()))) {
//							return false;
//						}
//					}
//				}
//			}
//		}
//		
//		return true;
//	}
//	
//	public Set<Map<ReprPort, ASALSymbolicValue>> getSicInputs(Map<ReprPort, ASALSymbolicValue> baseInputs) {
//		Set<Map<ReprPort, ASALSymbolicValue>> result = new HashSet<Map<ReprPort, ASALSymbolicValue>>();
//		
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : inputVals) {
//			if (isSicInput(baseInputs, inputVal)) {
//				result.add(inputVal);
//			}
//		}
//		
//		return result;
//	}
//	
//	public static void printVal(String name, Map<ReprPort, ASALSymbolicValue> val) {
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : val.entrySet()) {
//			System.out.println(name + "[" + e.getKey().getOwner().getName() + "." + e.getKey().getName() + "] = " + e.getValue());
//		}
//	}
//	
//	private static boolean isSicInput(Map<ReprPort, ASALSymbolicValue> baseInputs, Map<ReprPort, ASALSymbolicValue> inputVal) {
//		boolean foundDiff = false;
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//			if (e.getKey().getPulsePort() == null) { //Only check T-ports and D-ports.
//				if (!baseInputs.containsKey(e.getKey())) {
//					System.out.println(e.getKey().getDir().text);
//					System.out.println("inputVal[" + e.getKey().getName() + "] = " + e.getValue());
//					System.out.println("baseInputs[" + e.getKey().getName() + "] = NULL");
//					printVal("BASE", baseInputs);
//					printVal("IVAL", inputVal);
//					throw new Error("Should not happen!");
//				}
//			}
//			
//			if (foundDiff) {
//				if (!isSameInput(baseInputs, inputVal, e.getKey())) {
//					return false;
//				}
//			} else {
//				if (!isSameInput(baseInputs, inputVal, e.getKey())) {
//					foundDiff = true;
//				}
//			}
//		}
//		
//		return foundDiff;
//	}
	
	public boolean impliesInputVal(PulsePackMap implied) {
//		System.out.println("REF:");
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//			System.out.println("\t" + e.getKey().getName() + " = " + e.getValue());
//		}
		
		for (PulsePackMap iv : inputVals) {
//			System.out.println("IV:");
//			
//			for (Map.Entry<ReprPort, ASALSymbolicValue> e : iv.entrySet()) {
//				System.out.println("\t" + e.getKey().getName() + " = " + e.getValue());
//			}
			
			if (iv.implies(implied)) {
				return true;
			}
		}
		
		return false;
	}
	
//	private static boolean impliesInputVal(PulsePackMap inputVal, PulsePackMap implied) {
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : implied.entrySet()) {
//			if (e.getKey().getPulsePort() == null) {
//				if (inputVal.get(e.getKey()) == null) {
//					System.out.println("x = " + e.getKey().getName());
//					throw new Error("Suspicious!!");
//				}
//				
//				if (!e.getValue().equals(inputVal.get(e.getKey()))) {
//					return false;
//				}
//				
//				for (ReprPort drp : e.getKey().getDataPorts()) {
//					if (!implied.get(drp).equals(inputVal.get(drp))) {
//						return false;
//					}
//				}
//			}
//		}
//		
//		return true;
//	}
	
//	public boolean containsInputs(Map<ReprPort, ASALSymbolicValue> baseInputs) {
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : inputVals) {
//			if (containsInputVal(baseInputs, inputVal)) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
//	
//	private static boolean containsInputVal(Map<ReprPort, ASALSymbolicValue> baseInputs, Map<ReprPort, ASALSymbolicValue> inputVal) {
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//			if (e.getKey().getPulsePort() == null) { //Only check T-ports and D-ports.
//				if (!baseInputs.containsKey(e.getKey())) {
//					System.out.println(e.getKey().getDir().text);
//					System.out.println("inputVal[" + e.getKey().getName() + "] = " + e.getValue());
//					System.out.println("baseInputs[" + e.getKey().getName() + "] = NULL");
//					printVal("BASE", baseInputs);
//					printVal("IVAL", inputVal);
//					throw new Error("Should not happen!");
//				}
//			}
//			
//			if (!isSameInput(baseInputs, inputVal, e.getKey())) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
//	
//	private static boolean isSameInput(Map<ReprPort, ASALSymbolicValue> inputs1, Map<ReprPort, ASALSymbolicValue> inputs2, ReprPort port) {
//		if (port.isPortToEnvironment()) {
//			return true;
//		}
//		
//		if (JPulse.class.isAssignableFrom(port.getType())) {
//			ASALSymbolicValue v1 = inputs1.get(port);
//			
//			if (!v1.equals(inputs2.get(port))) {
//				return false;
//			}
//			
//			if (v1.toBoolean()) {
//				for (ReprPort drp : port.getDataPorts()) {
//					if (!inputs1.get(drp).equals(inputs2.get(drp))) {
//						return false;
//					}
//				}
//			}
//		} else {
//			if (port.getPulsePort() == null) {
//				if (!inputs1.get(port).equals(inputs2.get(port))) {
//					return false;
//				}
//			}
//		}
//		
//		return true;
//	}
	
	public Set<ASALSymbolicValue> getEnabledGuards() {
		return enabledGuards;
	}
	
	public Set<ASALSymbolicValue> getDisabledGuards() {
		return disabledGuards;
	}
	
//	public ASALSymbolicValue getCombinedGuard() {
//		return combinedGuard;
//	}
//	
//	public boolean containsMatch(Map<ReprPort, ASALSymbolicValue> inputs) {
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : inputVals) {
//			boolean success = true;
//			
//			for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//				if (!inputs.get(e.getKey()).equals(e.getValue())) {
//					success = false;
//					break;
//				}
//			}
//			
//			if (success) {
//				return true;
//			}
//		}
//		
//		return false;
//	}
//	
//	private ASALSymbolicValue extractCombinedGuard() {
//		ASALSymbolicValue result = ASALSymbolicValue.TRUE;
//		
//		for (ASALSymbolicValue enabledGuard : enabledGuards) {
//			result = ASALSymbolicValue.and(result, enabledGuard);
//		}
//		
//		for (ASALSymbolicValue disabledGuard : disabledGuards) {
//			result = ASALSymbolicValue.and(result, disabledGuard.negate());
//		}
//		
//		return result;
//	}
//	
//	private Map<ReprPort, ASALSymbolicValue> extractMinInputVal() {
//		Iterator<Map<ReprPort, ASALSymbolicValue>> q = inputVals.iterator();
//		Map<ReprPort, ASALSymbolicValue> result = q.next();
//		int resultPulseCount = getPulseCount(result);
//		
//		while (q.hasNext()) {
//			Map<ReprPort, ASALSymbolicValue> inputVal = q.next();
//			int pulseCount = getPulseCount(inputVal);
//			
//			if (pulseCount < resultPulseCount) {
//				resultPulseCount = pulseCount;
//				result = inputVal;
//			}
//		}
//		
//		return result;
//	}
//	
//	private int getPulseCount(Map<ReprPort, ASALSymbolicValue> inputVal) {
//		int result = 0;
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//			if (e.getKey().getType().equals(JPulse.class)) {
//				if (e.getValue().equals(ASALSymbolicValue.TRUE)) {
//					result++;
//				}
//			}
//		}
//		
//		return result;
//	}
//	
//	public int getMinExternalPulseCount() {
//		return minExternalPulseCount;
//	}
//	
//	private int extractMinExternalPulseCount() {
//		int result = Integer.MAX_VALUE;
//		
//		for (Map<ReprPort, ASALSymbolicValue> inputVal : inputVals) {
//			result = Math.min(result, getExternalPulseCount(inputVal));
//		}
//		
//		return result;
//	}
//	
//	private int getExternalPulseCount(Map<ReprPort, ASALSymbolicValue> inputVal) {
//		int result = 0;
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//			if (e.getKey().isPortToEnvironment()) {
//				if (e.getKey().getType().equals(JPulse.class)) {
//					if (e.getValue().equals(ASALSymbolicValue.TRUE)) {
//						result++;
//					}
//				}
//			}
//		}
//		
//		return result;
//	}
//	
//	public int getDiffCount(ExternalInputEquivClz entryInputs, int diffCountCap) {
//		switch (minExternalPulseCount) {
//			case 0:
//				//There can (/must) be 1 D-port with a different value.
//				return ExternalInputEquivClz.computeDiffCount(entryInputs, external, diffCountCap);
//			case 1:
//				//External D-ports must all have the same value:
//				return entryInputs.equals(external) ? 1 : diffCountCap;
//			default:
//				return diffCountCap;
//		}
//	}
//	
//	public ExternalInputEquivClz getExternal() {
//		return external;
//	}
	
	public boolean impliedBy(PulsePackMap inputVal) {
		for (PulsePackMap ppm : inputVals) {
			if (inputVal.implies(ppm)) {
				return true;
			}
		}
		
		return false;
	}
	
	public InputEquivClz extractDeactivatedExternal() {
		Set<PulsePackMap> externalInputVals = new HashSet<PulsePackMap>();
		
		for (PulsePackMap inputVal : inputVals) {
			addExternalInputVal(inputVal, externalInputVals);
		}
		
		if (externalInputVals.isEmpty()) {
			return null;
		}
		
		return new InputEquivClz(externalInputVals, Collections.emptySet(), Collections.emptySet());
	}
	
	private void addExternalInputVal(PulsePackMap inputVal, Set<PulsePackMap> dest) {
		Map<ReprPort, PulsePack> externalInputVal = new HashMap<ReprPort, PulsePack>();
		
		for (Map.Entry<ReprPort, PulsePack> e : inputVal.getPackPerPort().entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				if (e.getValue().isPulse()) {
					if (e.getValue().isTruePulse()) {
						return;
					}
				} else {
					externalInputVal.put(e.getKey(), e.getValue());
				}
			}
		}
		
		dest.add(new PulsePackMap(externalInputVal, Dir.IN));
	}
	
//	public String createCombinedGuardStr() {
//		String result = "TRUE";
//		
//		for (ASALSymbolicValue enabledGuard : enabledGuards) {
//			result += "\\nAND: " + enabledGuard;
//		}
//		
//		for (ASALSymbolicValue disabledGuard : disabledGuards) {
//			result += "\\nAND NOT: " + disabledGuard;
//		}
//		
//		return result;
//	}
//	
//	public static Set<Map<ReprPort, ASALSymbolicValue>> combineInputVals(Set<Map<ReprPort, ASALSymbolicValue>> xs, Set<Map<ReprPort, ASALSymbolicValue>> ys) {
//		Set<Map<ReprPort, ASALSymbolicValue>> result = new HashSet<Map<ReprPort, ASALSymbolicValue>>();
//		
//		
//		
//		return result;
//	}
//	
//	public boolean isSicInput(ExternalInputEquivClz e) {
//		return false;
//	}
}


