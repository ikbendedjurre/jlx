package jlx.behave.proto;

import java.time.LocalTime;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.stable.DecaStableOutputEvolution;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaFourStateConfig {
	private final Map<JScope, DecaFourVertex> vtxs;
	private final int vtxsHashCode;
	
	public int level;
	public int fileId;
	
	public DecaFourStateConfig(Map<JScope, DecaFourVertex> vtxs) {
		this.vtxs = vtxs;
		
		vtxsHashCode = Objects.hash(vtxs);
	}
	
	public Map<String, String> getValuePerOutput() {
		Map<String, String> result = new TreeMap<String, String>();
		
		for (DecaFourVertex v : vtxs.values()) {
			for (Map.Entry<ReprPort, ASALSymbolicValue> e : v.getOutputVal().extractValuation().entrySet()) {
				String key = e.getKey().getReprOwner().getName() + "::" + e.getKey().getName();
				String value = e.getValue().toString();
				result.put(key, value);
			}
		}
		
		return result;
	}
	
	public Set<Class<?>> getClzs() {
		Set<Class<?>> result = new HashSet<Class<?>>();
		
		for (DecaFourVertex v : vtxs.values()) {
			result.addAll(v.getSysmlClzs());
		}
		
		return result;
	}
	
	public PulsePackMap getOutputVal() {
		return extractOutputVal();
	}
	
	private PulsePackMap extractOutputVal() {
		Map<ReprPort, PulsePack> result = new HashMap<ReprPort, PulsePack>();
		
		for (DecaFourVertex v : vtxs.values()) {
			result.putAll(v.getOutputVal().getPackPerPort());
		}
		
		return new PulsePackMap(result, Dir.OUT);
	}
	
	public Map<JScope, DecaFourVertex> getVtxs() {
		return vtxs;
	}
	
	/**
	 * Gives all successors that can be reached in one step via the given (external!) inputs.
	 * Pulses in the given inputs are ignored (in other words, given inputs are deactivated).
	 * @param deactivatedExternalInputs Inputs.
	 * @param sicInputs Exists for debug purposes only.
	 */
	private Map<DecaFourStateConfig, Set<ProtoTransition>> getSuccsViaInputs(PulsePackMap deactivatedExternalInputs, PulsePackMap sicInputs) {
		Map<DecaFourVertex, Set<ProtoTransition>> protoTrsPerTgt = new HashMap<DecaFourVertex, Set<ProtoTransition>>();
		Map<JScope, Set<DecaFourVertex>> pssPerScope = new HashMap<JScope, Set<DecaFourVertex>>(); //Possible SuccessorS
		int index = 0;
		
		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
			Set<DecaFourVertex> pss = new HashSet<DecaFourVertex>();
			
			for (Map.Entry<DecaFourTransitionReqSet, Map<PulsePackMap, Set<DecaFourTransition>>> e2 : e1.getValue().getTrsPerNonDetTgtsPerReqSet().entrySet()) {
				if (e2.getKey().containsMatch(getVtxs())) {
//					System.out.println("option " + index);
					index++;
					
					for (Map.Entry<PulsePackMap, Set<DecaFourTransition>> e3 : e2.getValue().entrySet()) {
						if (deactivatedExternalInputs.implies(e3.getKey())) {
							for (DecaFourTransition t : e3.getValue()) {
								for (DecaFourVertex tgt : t.getTgtGrp().getVtxs()) {
									HashMaps.injectAll(protoTrsPerTgt, tgt, t.getProtoTrs());
								}
								
								pss.addAll(t.getTgtGrp().getVtxs());
							}
							
//							protoTrsDest.addAll(e1.getValue().getProtoTrsPerReqSet().get(
//							System.out.println("adding " + e3.getKey().getVtxs().size());
						}
					}
				}
			}
			
			if (pss.isEmpty()) {
				throw new Error("Should not happen!");
			}
			
			pssPerScope.put(e1.getKey(), pss);
		}
		
//		System.out.println("#combis = " + HashMaps.getCombinationCount(pssPerScope, false));
		
		Map<DecaFourStateConfig, Set<ProtoTransition>> result = new HashMap<DecaFourStateConfig, Set<ProtoTransition>>();
		
		for (Map<JScope, DecaFourVertex> perm : HashMaps.allCombinations(pssPerScope)) {
			Set<ProtoTransition> protoTrs = new HashSet<ProtoTransition>();
			
			for (DecaFourVertex tgt : perm.values()) {
				protoTrs.addAll(protoTrsPerTgt.get(tgt));
			}
			
			result.put(new DecaFourStateConfig(perm), protoTrs);
		}
		
		return result;
	}
	
	/**
	 * Gives a sequence of successors that:
	 *  (1) is the only one that can be initiated with the given inputs;
	 *  (2) ends with a stable successor;
	 *  (3) does not pass through cycles.
	 * If NULL, no such sequence exists.
	 * Pulses in the given inputs are ignored (in other words, given inputs are deactivated).
	 * @param deactivatedExternalInputs Inputs.
	 * @param sicInputs Exists for debug purposes only.
	 */
	public static DecaFourStateConfigPath followInputs(Set<DecaFourStateConfig> start, PulsePackMap deactivatedExternalInputs, PulsePackMap sicInputs) {
		//DecaFourStateConfig endPoint = null;
		DecaFourStateConfigPath basePathToEndPoint = null;
		Set<DecaStableOutputEvolution> baseEvos = null;
		Map<JScope, List<DecaFourVertex>> basePathPerScopeToEndPoint = null;
		
		Set<DecaFourStateConfigPath> paths = new HashSet<DecaFourStateConfigPath>();
		Set<DecaFourStateConfigPath> newPaths = new HashSet<DecaFourStateConfigPath>();
		
		for (DecaFourStateConfig cfg : start) {
			paths.add(new DecaFourStateConfigPath(cfg));
		}
		
		if ("disable".equals("off")) {
			throw new Error("DISABLED");
		}
		
//		System.out.println("[" + LocalTime.now() + "] #paths = " + paths.size());
		
		while (paths.size() > 0) {
			newPaths.clear();
			
			for (DecaFourStateConfigPath path : paths) {
				Map<DecaFourStateConfig, Set<ProtoTransition>> succsViaInputs = path.getLastCfg().getSuccsViaInputs(deactivatedExternalInputs, sicInputs);
//				System.out.println("#succs = " + succsViaInputs.size() + "; len = " + path.size());
				
				if (succsViaInputs.size() == 1) {
					DecaFourStateConfig succ = succsViaInputs.keySet().iterator().next();
					
					if (succ.equals(path.getLastCfg())) {
						if (basePathToEndPoint != null) {
							if (path.getLastCfg().equals(basePathToEndPoint.getLastCfg())) {
								if (isCompatiblePath(basePathPerScopeToEndPoint, baseEvos, path.getCfgs())) {
									//Do nothing.
								} else {
									if (sicInputs != null) {
										System.out.println("Incompatible path!");
										System.exit(0);
									}
									
									return null;
								}
							} else {
								if (sicInputs != null) {
									System.out.println("Different end point!");
									System.out.println("Path 1:");
									
									for (int i = 0; i < path.getCfgs().size(); i++) {
										System.out.println(" [" + i + "] " + path.getCfgs().get(i).getDescription());
									}
									
									System.out.println("Path 2:");
									
									for (int i = 0; i < basePathToEndPoint.getCfgs().size(); i++) {
										System.out.println(" [" + i + "] " + basePathToEndPoint.getCfgs().get(i).getDescription());
									}
									
									System.out.println("Input:");
									System.out.println(" " + deactivatedExternalInputs.toString().replace(" | ", "\n "));
									
									System.exit(0);
								}
								
								return null;
							}
						} else {
//							endPoint = lastCfg;
							basePathToEndPoint = path;
//							basePathPerScopeToEndPoint = extractPathPerScope(path);
							baseEvos = DecaStableOutputEvolution.getOutputEvolutions(path.getCfgs());
						}
					} else {
						if (path.getCfgs().contains(succ)) {
							if (sicInputs != null) {
								System.out.println("Loop!");
								System.exit(0);
							}
							
							return null;
						} else {
							newPaths.add(new DecaFourStateConfigPath(path, succ, succsViaInputs.values().iterator().next()));
						}
					}
				} else {
					for (Map.Entry<DecaFourStateConfig, Set<ProtoTransition>> e : succsViaInputs.entrySet()) {
						if (path.getCfgs().contains(e.getKey())) {
							System.out.println(deactivatedExternalInputs.toString());
							
							for (DecaFourStateConfig x : path.getCfgs()) {
								System.out.println(x.getDescription());
							}
							
							for (DecaFourStateConfig x : succsViaInputs.keySet()) {
								System.out.println("Available: " + x.getDescription());
							}
							throw new Error("Infinite loop!");
						}
						
						newPaths.add(new DecaFourStateConfigPath(path, e.getKey(), e.getValue()));
					}
				}
			}
			
			paths.clear();
			paths.addAll(newPaths);
			
//			System.out.println("[" + LocalTime.now() + "] #paths = " + paths.size());
		}
		
		if (basePathToEndPoint == null) {
			throw new Error("Should not happen!");
		}
		
		return basePathToEndPoint;
	}
	
	public static Set<DecaFourStateConfigPath> computeStabilizingPaths(Set<DecaFourStateConfig> start, PulsePackMap deactivatedExternalInputs, PulsePackMap sicInputs) {
		Set<DecaFourStateConfigPath> result = new HashSet<DecaFourStateConfigPath>();
		Set<DecaFourStateConfigPath> paths = new HashSet<DecaFourStateConfigPath>();
		Set<DecaFourStateConfigPath> newPaths = new HashSet<DecaFourStateConfigPath>();
		
		for (DecaFourStateConfig cfg : start) {
			paths.add(new DecaFourStateConfigPath(cfg));
		}
		
		while (paths.size() > 0) {
			newPaths.clear();
			
			for (DecaFourStateConfigPath path : paths) {
				Map<DecaFourStateConfig, Set<ProtoTransition>> succsViaInputs = path.getLastCfg().getSuccsViaInputs(deactivatedExternalInputs, sicInputs);
				
				for (Map.Entry<DecaFourStateConfig, Set<ProtoTransition>> e : succsViaInputs.entrySet()) {
					if (path.getLastCfg().equals(e.getKey())) {
						result.add(path); //Stabilized!
					} else {
						if (path.getCfgs().contains(e.getKey())) {
//							System.out.println("divergence!");
							return Collections.emptySet(); //Divergence!
						} else {
							newPaths.add(new DecaFourStateConfigPath(path, e.getKey(), e.getValue()));
						}
					}
				}
			}
			
			paths.clear();
			paths.addAll(newPaths);
		}
		
		return result;
	}
	
	public static Set<DecaFourStateConfigPath> followInputs2(Set<DecaFourStateConfig> start, PulsePackMap deactivatedExternalInputs, PulsePackMap sicInputs) {
		Map<DecaFourStateConfig, Set<DecaFourStateConfigPath>> pathsPerTgt = new HashMap<DecaFourStateConfig, Set<DecaFourStateConfigPath>>();
		Set<DecaFourStateConfigPath> paths = computeStabilizingPaths(start, deactivatedExternalInputs, sicInputs);
//		System.out.println("#paths = " + paths.size());
		
		for (DecaFourStateConfigPath path : paths) {
			HashMaps.inject(pathsPerTgt, path.getLastCfg(), path);
		}
		
		Set<DecaFourStateConfigPath> result = new HashSet<DecaFourStateConfigPath>();
		
		for (Map.Entry<DecaFourStateConfig, Set<DecaFourStateConfigPath>> e : pathsPerTgt.entrySet()) {
			Map<Set<DecaStableOutputEvolution>, Set<DecaFourStateConfigPath>> pathsPerEvos = new HashMap<Set<DecaStableOutputEvolution>, Set<DecaFourStateConfigPath>>();
			
			for (DecaFourStateConfigPath p : e.getValue()) {
				Set<DecaStableOutputEvolution> evos = DecaStableOutputEvolution.getOutputEvolutions(p.getCfgs());
				HashMaps.inject(pathsPerEvos, evos, p);
			}
			
			for (Map.Entry<Set<DecaStableOutputEvolution>, Set<DecaFourStateConfigPath>> e2 : pathsPerEvos.entrySet()) {
				Set<ProtoTransition> trs = new HashSet<ProtoTransition>();
				
				for (DecaFourStateConfigPath p : e2.getValue()) {
					trs.addAll(p.getProtoTrs());
				}
				
				result.add(new DecaFourStateConfigPath(e2.getValue().iterator().next(), trs));
			}
		}
		
		return result;
	}
	
	public static Map<JScope, List<DecaFourVertex>> extractPathPerScope(List<DecaFourStateConfig> path) {
		Map<JScope, List<DecaFourVertex>> result = new HashMap<JScope, List<DecaFourVertex>>();
		
		for (Map.Entry<JScope, DecaFourVertex> e : path.get(0).getVtxs().entrySet()) {
			List<DecaFourVertex> p = new ArrayList<DecaFourVertex>();
			p.add(e.getValue());
			result.put(e.getKey(), p);
		}
		
		for (int index = 1; index < path.size(); index++) {
			for (Map.Entry<JScope, DecaFourVertex> e : path.get(index).getVtxs().entrySet()) {
				List<DecaFourVertex> vs = result.get(e.getKey());
				
				if (vs.get(vs.size() - 1) != e.getValue()) {
					vs.add(e.getValue());
				}
			}
		}
		
		return result;
	}
	
	private static boolean isCompatiblePath(Map<JScope, List<DecaFourVertex>> basePathPerScope, Set<DecaStableOutputEvolution> baseEvos, List<DecaFourStateConfig> path) {
		return DecaStableOutputEvolution.getOutputEvolutions(path).equals(baseEvos);
		//return extractPathPerScope(path).equals(basePathPerScope);
	}
	
//	public Set<Map<ReprPort, ASALSymbolicValue>> getSelfLoopZeroPulseSicInputs(Map<ReprPort, ASALSymbolicValue> entryInputs, Collection<ASALPort> timeoutPorts) {
//		Map<JScope, Set<Map<ReprPort, ASALSymbolicValue>>> zeroDiffInputsPerScope = new HashMap<JScope, Set<Map<ReprPort, ASALSymbolicValue>>>();
//		Map<Set<ReprPort>, Map<JScope, Set<PulsePackMap>>> oneDiffInputsPerScope = new HashMap<Set<ReprPort>, Map<JScope, Set<PulsePackMap>>>();
//		
//		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
//			DecaFourVertex succVtx = e1.getValue();
//			
//			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<InputEquivClz>>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet().entrySet()) {
//				if (e2.getKey().containsMatch(getVtxs())) {
//					for (Map.Entry<DecaFourTgtGrp, Set<InputEquivClz>> e3 : e2.getValue().entrySet()) {
//						if (e3.getKey().getVtxs().contains(succVtx)) {
//							for (InputEquivClz i : e3.getValue()) {
//								for (PulsePackMap inputVal : i.getZeroExternalPulseInputVals()) {
//									InputValDiff diff = getInputValDiff(inputVal, entryInputs);
//									
//									if (diff == NO_DIFF) {
//										//We may combine this with other inputs that DO change:
//										HashMaps.inject(zeroDiffInputsPerScope, e1.getKey(), inputVal);
//									} else {
//										if (diff == TOO_MANY_DIFFS) {
//											//...
//										} else {
//											HashMaps.injectInject(oneDiffInputsPerScope, diff.port.getSyncedPorts(), e1.getKey(), inputVal);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		Set<Map<ReprPort, ASALSymbolicValue>> result = new HashSet<Map<ReprPort, ASALSymbolicValue>>();
//		
//		for (Map.Entry<Set<ReprPort>, Map<JScope, Set<PulsePackMap>>> e5 : oneDiffInputsPerScope.entrySet()) {
//			Set<JScope> unchangedScopes = new HashSet<JScope>(getVtxs().keySet());
//			unchangedScopes.removeAll(e5.getValue().keySet());
//			
//			if (zeroDiffInputsPerScope.keySet().containsAll(unchangedScopes)) {
//				Map<JScope, Set<Map<ReprPort, ASALSymbolicValue>>> permMap = new HashMap<JScope, Set<Map<ReprPort, ASALSymbolicValue>>>();
//				
//				for (Map.Entry<JScope, Set<Map<ReprPort, ASALSymbolicValue>>> e6 : zeroDiffInputsPerScope.entrySet()) {
//					permMap.put(e6.getKey(), new HashSet<Map<ReprPort, ASALSymbolicValue>>(e6.getValue()));
//				}
//				
//				for (Map.Entry<JScope, Set<PulsePackMap>> e7 : e5.getValue().entrySet()) {
//					HashMaps.injectAll(permMap, e7.getKey(), e7.getValue());
//				}
//				
//				for (Map.Entry<JScope, Set<PulsePackMap>> e8 : e5.getValue().entrySet()) {
//					Map<JScope, Set<Map<ReprPort, ASALSymbolicValue>>> permMapCopy = new HashMap<JScope, Set<Map<ReprPort, ASALSymbolicValue>>>(permMap);
//					permMapCopy.keySet().remove(e8.getKey());
//					
//					for (Map<JScope, Map<ReprPort, ASALSymbolicValue>> perm : HashMaps.allCombinations(permMapCopy)) {
//						Map<ReprPort, ASALSymbolicValue> flatPerm = new HashMap<ReprPort, ASALSymbolicValue>();
//						
//						for (Map.Entry<JScope, Map<ReprPort, ASALSymbolicValue>> e9 : perm.entrySet()) {
//							flatPerm.putAll(e9.getValue());
//						}
//						
//						for (PulsePackMap m : e8.getValue()) {
//							Map<ReprPort, ASALSymbolicValue> flatPermCopy = new HashMap<ReprPort, ASALSymbolicValue>();
//							flatPermCopy.putAll(flatPerm);
//							flatPermCopy.putAll(m);
//							
//							if (isConsistentPerm(flatPermCopy)) {
//								InputValDiff diff = getInputValDiff(flatPermCopy, entryInputs);
//								
//								if (diff != NO_DIFF && diff != TOO_MANY_DIFFS) {
//									result.add(flatPermCopy);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		return result;
//	}
	
	private static boolean contains(Collection<DecaFourVertex> vs, String s) {
		for (DecaFourVertex v : vs) {
			if (contains(v, s)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean contains(DecaFourVertex v, String s) {
		return clzSetToStr(v.getSysmlClzs()).contains(s);
	}
	
	private static String clzSetToStr(Set<Class<?>> clzSet) {
		return Texts.concat(clzSet, "+", (c) -> { return c.getSimpleName(); });
	}
	
	/**
	 * This method is inefficient, may need rewriting.
	 */
	public Set<ProtoTransition> getProtoTrsToSuccsViaInputVal(Set<DecaFourStateConfig> succs, PulsePackMap inputVal) {
		Map<DecaFourStateConfig, Set<ProtoTransition>> z = getSuccsViaInputs(inputVal, inputVal);
		Set<ProtoTransition> result = new HashSet<ProtoTransition>();
		
		for (DecaFourStateConfig succ : succs) {
			Set<ProtoTransition> zs = z.get(succ);
			
			if (zs == null) {
				throw new Error("Should not happen!");
			}
			
			result.addAll(zs);
		}
		
		return result;
	}
	
	/**
	 * Return values are EXTERNAL.
	 */
	public Set<PulsePackMap> getSicInputsToSucc(DecaFourStateConfig succ, PulsePackMap entryInputs) {
		Map<JScope, Set<PulsePackMap>> zeroDiffInputsPerScope = new HashMap<JScope, Set<PulsePackMap>>();
		Map<Set<ReprPort>, Map<JScope, Set<PulsePackMap>>> oneDiffInputsPerScopePerPort = new HashMap<Set<ReprPort>, Map<JScope, Set<PulsePackMap>>>();
		
		WallClock.tick("inputs");
		
		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
			DecaFourVertex succVtx = succ.getVtxs().get(e1.getKey());
//			Map<ReprPort, ASALSymbolicValue> minDiffInputVal;
			
			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>> e2 : e1.getValue().getExternalInputsPerNonDetTgtsPerReqSet2().entrySet()) {
				if (e2.getKey().containsMatch(getVtxs())) {
					for (Map.Entry<DecaFourTgtGrp, DecaFourPulsePackMaps> e3 : e2.getValue().entrySet()) {
						if (e3.getKey().getVtxs().contains(succVtx)) {
							for (PulsePackMap inputVal : e3.getValue().getZeroExternalPulseMaps()) {
								PulsePackDiff diff = PulsePackDiff.compute(inputVal, entryInputs);
								
								if (diff == PulsePackDiff.NO_DIFF) {
									//We may combine this with other inputs that DO change:
									HashMaps.inject(zeroDiffInputsPerScope, e1.getKey(), inputVal);
								} else {
									if (diff == PulsePackDiff.TOO_MANY_DIFFS) {
										//...
									} else {
										HashMaps.injectInject(oneDiffInputsPerScopePerPort, diff.getAdapterLabelPort().getSyncedPorts(), e1.getKey(), inputVal);
									}
								}
							}
							
							for (PulsePackMap inputVal : e3.getValue().getOneExternalPulseMaps()) {
								PulsePackDiff diff = PulsePackDiff.compute(inputVal, entryInputs);
								
								if (diff == PulsePackDiff.NO_DIFF) {
									throw new Error("Should not happen!");
								} else {
									if (diff == PulsePackDiff.TOO_MANY_DIFFS) {
										//...
									} else {
										HashMaps.injectInject(oneDiffInputsPerScopePerPort, diff.getAdapterLabelPort().getSyncedPorts(), e1.getKey(), inputVal);
									}
								}
							}
						}
					}
				}
			}
		}
		
		WallClock.tock("inputs");
		WallClock.tick("succs");
		
		Set<PulsePackMap> result = new HashSet<PulsePackMap>();
		
		for (Map.Entry<Set<ReprPort>, Map<JScope, Set<PulsePackMap>>> e : oneDiffInputsPerScopePerPort.entrySet()) {
			Set<JScope> unchangedScopes = new HashSet<JScope>(getVtxs().keySet());
			unchangedScopes.removeAll(e.getValue().keySet());
			
			if (zeroDiffInputsPerScope.keySet().containsAll(unchangedScopes)) {
				Map<JScope, Set<PulsePackMap>> permMap = new HashMap<JScope, Set<PulsePackMap>>();
				
				//As a base, copy the inputs WITHOUT changes:
				for (Map.Entry<JScope, Set<PulsePackMap>> zeroDiff : zeroDiffInputsPerScope.entrySet()) {
					permMap.put(zeroDiff.getKey(), new HashSet<PulsePackMap>(zeroDiff.getValue()));
				}
				
				//Extend with inputs of the changing port.
				//The port can occur in more than one scope (b/c of adapter labels):
				for (Map.Entry<JScope, Set<PulsePackMap>> oneDiff : e.getValue().entrySet()) {
					HashMaps.injectAll(permMap, oneDiff.getKey(), oneDiff.getValue());
				}
				
				//We now have a map with a key for each scope.
				//Per scope, we have inputs, which can be a 0-diff input or 1-diff input.
				
				//Now pick a scope, and combine:
				// - its possible inputs, and
				// - the possible inputs of the other scopes.
				//(TODO Why do it this way?)
				for (Map.Entry<JScope, Set<PulsePackMap>> e8 : e.getValue().entrySet()) {
					Map<JScope, Set<PulsePackMap>> permMapCopy = new HashMap<JScope, Set<PulsePackMap>>(permMap);
					permMapCopy.keySet().remove(e8.getKey());
					
					for (Map<JScope, PulsePackMap> perm : HashMaps.allCombinations(permMapCopy)) {
						Map<ReprPort, PulsePack> flatPerm = new HashMap<ReprPort, PulsePack>();
						
						for (Map.Entry<JScope, PulsePackMap> e9 : perm.entrySet()) {
							flatPerm.putAll(e9.getValue().getPackPerPort());
						}
						
						for (PulsePackMap m : e8.getValue()) {
							Map<ReprPort, PulsePack> flatPermCopy = new HashMap<ReprPort, PulsePack>();
							flatPermCopy.putAll(flatPerm);
							flatPermCopy.putAll(m.getPackPerPort());
							
							if (PulsePackMap.isConsistent(flatPermCopy)) {
								PulsePackMap ppm = new PulsePackMap(flatPermCopy, Dir.IN);
								PulsePackDiff diff = PulsePackDiff.compute(ppm, entryInputs);
								
								if (diff.getAdapterLabelPort() != null) {
									//TODO if we change multiple data ports, add a flatPermCopy per change!!
									//     no -> if we do not make all changes, the successor is incorrect
									
									result.add(ppm);
								}
							}
						}
					}
				}
			}
		}
		
		WallClock.tock("succs");
		
//		if (contains(getVtxs().values(), "STOPPED")) {
//			if (contains(getVtxs().values(), "ALL_LEFT")) {
//				if (contains(getVtxs().values(), "PDI_CONNECTION_ESTABLISHED")) {
//					if (result.size() > 0) {
//						System.out.println("# = " + result.size());
//						
//						for (PulsePackMap ppm : result) {
//							System.out.println("\t" + ppm);
//						}
//						
//						throw new Error("#");
//					}
//				}
//			}
//		}
		
		return result;
	}
	
//	private static boolean isConsistentPerm(Map<ReprPort, ASALSymbolicValue> flatPerm) {
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : flatPerm.entrySet()) {
//			if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
//				if (e.getValue().toBoolean()) {
//					for (ReprPort sp : e.getKey().getSyncedPorts()) {
//						ASALSymbolicValue v = flatPerm.get(sp);
//						
//						if (v == null || !v.toBoolean()) {
//							return false;
//						}
//						
//						for (ReprPort rdp : e.getKey().getDataPorts()) {
//							for (ReprPort srdp : rdp.getSyncedPorts()) {
//								if (!flatPerm.get(srdp).equals(flatPerm.get(rdp))) {
//									return false;
//								}
//							}
//						}
//					}
//				} else {
//					for (ReprPort sp : e.getKey().getSyncedPorts()) {
//						if (flatPerm.get(sp).toBoolean()) {
//							return false;
//						}
//					}
//				}
//			} else {
//				if (e.getKey().getPulsePort() == null) {
//					for (ReprPort sp : e.getKey().getSyncedPorts()) {
//						if (!flatPerm.get(sp).equals(e.getValue())) {
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
//	private static class InputValDiff {
//		public final ReprPort port;
//		
//		public InputValDiff(ReprPort port) {
//			this.port = port;
//		}
//	}
//	
//	private final static InputValDiff NO_DIFF = new InputValDiff(null);
//	private final static InputValDiff TOO_MANY_DIFFS = new InputValDiff(null);
//	
//	private static InputValDiff getInputValDiff(Map<ReprPort, ASALSymbolicValue> inputVal, Map<ReprPort, ASALSymbolicValue> entryInputVal) {
//		InputValDiff result = NO_DIFF;
//		
//		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputVal.entrySet()) {
//			if (e.getKey().isPortToEnvironment()) {
//				if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
//					if (e.getValue().toBoolean()) {
//						if (result.port != null) {
//							if (result.port != e.getKey().getAdapterLabelPort()) {
//								return TOO_MANY_DIFFS;
//							}
//						} else {
//							result = new InputValDiff(e.getKey().getAdapterLabelPort());
//						}
//					}
//				} else {
//					if (e.getKey().getPulsePort() == null) {
//						if (!e.getValue().equals(entryInputVal.get(e.getKey()))) {
//							if (result.port != null) {
//								if (result.port != e.getKey().getAdapterLabelPort()) {
//									return TOO_MANY_DIFFS;
//								}
//							} else {
//								result = new InputValDiff(e.getKey().getAdapterLabelPort());
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		return result;
//	}
	
//	public Map<JScope, Set<InputEquivClz>> computeEnabledPerScope() {
//		Map<JScope, Set<InputEquivClz>> enabledPerScope = new HashMap<JScope, Set<InputEquivClz>>();
//		
//		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
//			Set<InputEquivClz> enabled = new HashSet<InputEquivClz>();
//			
//			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet().entrySet()) {
//				if (e2.getKey().containsMatch(getVtxs())) {
//					for (Map.Entry<DecaFourTgtGrp, Set<PulsePackMap>> e3 : e2.getValue().entrySet()) {
//						enabled.addAll(e3.getValue());
//					}
//				}
//			}
//			
//			enabledPerScope.put(e1.getKey(), enabled);
//		}
//		
//		return enabledPerScope;
//	}
	
//	public Map<JScope, Map<DecaFourVertex, Set<InputEquivClz>>> computeInputsPerSuccsPerScope() {
//		Map<JScope, Map<DecaFourVertex, Set<InputEquivClz>>> allTgtsPerScope = new HashMap<JScope, Map<DecaFourVertex, Set<InputEquivClz>>>();
//		
//		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
//			Map<DecaFourVertex, Set<InputEquivClz>> allTgts = new HashMap<DecaFourVertex, Set<InputEquivClz>>();
//			
//			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<InputEquivClz>>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet().entrySet()) {
//				if (e2.getKey().containsMatch(getVtxs())) {
//					for (Map.Entry<DecaFourTgtGrp, Set<InputEquivClz>> e3 : e2.getValue().entrySet()) {
//						for (DecaFourVertex vtx : e3.getKey().getVtxs()) {
//							HashMaps.injectAll(allTgts, vtx, e3.getValue());
//						}
//					}
//				}
//			}
//			
//			allTgtsPerScope.put(e1.getKey(), allTgts);
//		}
//		
//		return allTgtsPerScope;
//	}
	
	public Map<JScope, Set<DecaFourVertex>> computeSuccsPerScope() {
		Map<JScope, Set<DecaFourVertex>> allTgtsPerScope = new HashMap<JScope, Set<DecaFourVertex>>();
		
		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
			Set<DecaFourVertex> allTgts = new HashSet<DecaFourVertex>();
			
			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet().entrySet()) {
				if (e2.getKey().containsMatch(getVtxs())) {
					for (DecaFourTgtGrp vtxs : e2.getValue().keySet()) {
						allTgts.addAll(vtxs.getVtxs());
					}
				}
			}
			
			allTgtsPerScope.put(e1.getKey(), allTgts);
		}
		
		return allTgtsPerScope;
	}
	
	public Map<JScope, List<DecaFourVertex>> computeOrderedSuccsPerScope() {
		Map<JScope, List<DecaFourVertex>> result = new HashMap<JScope, List<DecaFourVertex>>();
		
		for (Map.Entry<JScope, Set<DecaFourVertex>> e : computeSuccsPerScope().entrySet()) {
			List<DecaFourVertex> orderedVtxs = new ArrayList<DecaFourVertex>(e.getValue());
			Collections.sort(orderedVtxs);
			result.put(e.getKey(), orderedVtxs);
		}
		
		return result;
	}
	
	public Set<DecaFourStateConfig> computeSuccs() {
		return computeSuccs(computeSuccsPerScope());
	}
	
	public Set<DecaFourStateConfig> computeSuccs(Map<JScope, ? extends Collection<DecaFourVertex>> succsPerScope) {
		Set<DecaFourStateConfig> result = new HashSet<DecaFourStateConfig>();
		
		HashMaps.searchCombinations(succsPerScope, (perm) -> {
			DecaFourStateConfig f2 = new DecaFourStateConfig(perm);
			
			if (result.add(f2)) {
				f2.level = level + 1;
			}
			
			return null;
		});
		
		return result;
	}
	
	/**
	 * Input valuation can be partial.
	 */
	public Set<DecaFourStateConfig> computeSuccsViaInputVal(PulsePackMap inputVal) {
		Map<JScope, Set<DecaFourVertex>> allTgtsPerScope = new HashMap<JScope, Set<DecaFourVertex>>();
		
		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
			PulsePackMap premise = inputVal.extractOwnerMap(e1.getKey());
			Set<DecaFourVertex> allTgts = new HashSet<DecaFourVertex>();
			
			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, DecaFourPulsePackMaps>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet2().entrySet()) {
				if (e2.getKey().containsMatch(getVtxs())) {
					for (Map.Entry<DecaFourTgtGrp, DecaFourPulsePackMaps> z : e2.getValue().entrySet()) {
						if (z.getValue().couldBeImpliedBy(premise)) {
							allTgts.addAll(z.getKey().getVtxs());
						}
					}
				}
			}
			
			//Because input-enabled:
			if (allTgts.isEmpty()) {
				throw new Error("Should not happen!");
			}
			
			allTgtsPerScope.put(e1.getKey(), allTgts);
		}
		
		Set<DecaFourStateConfig> result = new HashSet<DecaFourStateConfig>();
		
		for (Map<JScope, DecaFourVertex> perm : HashMaps.allCombinations(allTgtsPerScope)) {
			result.add(new DecaFourStateConfig(perm));
		}
		
		return result;
	}
	
//	/**
//	 * Fixed point of successors after a specific input valuation.
//	 * Pulses are deactivated after the first step.
//	 */
//	public Set<DecaFourStateConfig> computeReachableViaInputVal(PulsePackMap inputVal) {
//		Set<DecaFourStateConfig> firstFringe = computeSuccsViaInputVal(inputVal);
//		PulsePackMap deactivatedInput = inputVal.deactivate();
//		
//		Set<DecaFourStateConfig> result = new HashSet<DecaFourStateConfig>();
//		result.addAll(firstFringe);
//		
//		Set<DecaFourStateConfig> fringe = new HashSet<DecaFourStateConfig>();
//		Set<DecaFourStateConfig> newFringe = new HashSet<DecaFourStateConfig>();
//		fringe.addAll(firstFringe);
//		
//		while (fringe.size() > 0) {
//			newFringe.clear();
//			
//			for (DecaFourStateConfig cfg : fringe) {
//				for (DecaFourStateConfig succ : cfg.computeSuccsViaInputVal(deactivatedInput)) {
//					if (result.add(succ)) {
//						newFringe.add(succ);
//					}
//				}
//			}
//			
//			fringe.clear();
//			fringe.addAll(newFringe);
//		}
//		
//		return result;
//	}
	
	/**
	 * Adds to the given set the successors of this state configuration.
	 */
	public long addAllSuccs(NormMap<DecaFourStateConfig> dest, NormMap<Pair<DecaFourVertex>> pairs, NormMap<ProtoTransition> proto) {
		Map<JScope, Set<DecaFourVertex>> allTgtsPerScope = new HashMap<JScope, Set<DecaFourVertex>>();
		
		for (Map.Entry<JScope, DecaFourVertex> e1 : getVtxs().entrySet()) {
			Set<DecaFourVertex> allTgts = new HashSet<DecaFourVertex>();
			
			int b1 = 0;
			
			for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet().entrySet()) {
				if (e2.getKey().containsMatch(getVtxs())) {
					b1++;
					
					for (DecaFourTgtGrp vtxs : e2.getValue().keySet()) {
						allTgts.addAll(vtxs.getVtxs());
					}
					
					for (Set<DecaFourTransition> trs : e1.getValue().getTrsPerNonDetTgtsPerReqSet().get(e2.getKey()).values()) {
						for (DecaFourTransition t : trs) {
							proto.addAll(t.getProtoTrs());
						}
					}
//					Set<ProtoTransition> protoTrs = ;
//					
//					if (protoTrs != null) {
//						proto.addAll(protoTrs);
//					}
				}
			}
			
			if (allTgts.isEmpty()) {
				System.out.println("No targets for " + e1.getKey().getName() + " -> " + e1.getValue().getName());
				System.out.println("e1.getValue() = " + e1.getValue().getInputsPerNonDetTgtsPerReqSet().size());
				System.out.println("e2.getKey().containsMatch(getVtxs()) = " + b1);
				
//				for (Map.Entry<DecaFourTransitionReqSet, Map<DecaFourTgtGrp, Set<PulsePackMap>>> e2 : e1.getValue().getInputsPerNonDetTgtsPerReqSet().entrySet()) {
//					System.out.println("() " + e2.getKey().containsMatch(getVtxs()) + " =>");
//					
//					for (Map<JScope, DecaFourVertex> x : e2.getKey().getReqCfgs()) {
//						List<String> elems = new ArrayList<String>();
//						
//						for (Map.Entry<JScope, DecaFourVertex> xx : x.entrySet()) {
//							elems.add(xx.getValue().getName());
//						}
//						
//						System.out.println("\t" + Texts.concat(elems, " || "));
//					}
//				}
				
				//fp[0][WATING]$0
				//est[0][NO_OPERATING_VOLTAGE]$0
				//sec[0][NOT_READY_FOR_CONNECTION]$0
				System.out.println("b/c we are at " + getDescription());
				
				throw new Error("Should not happen!");
			}
			
			allTgtsPerScope.put(e1.getKey(), allTgts);
		}
		
		HashMaps.searchCombinations(allTgtsPerScope, (perm) -> {
			DecaFourStateConfig f2 = new DecaFourStateConfig(perm);
			pairs.addAll(f2.computeVtxPairsFrom(this));
			
			if (dest.add(f2)) {
				f2.level = level + 1;
			}
			
			return null;
		});
		
		return HashMaps.getCombinationCount(allTgtsPerScope, false);
	}
	
	public Set<Pair<DecaFourVertex>> computeVtxPairsFrom(DecaFourStateConfig src) {
		Set<Pair<DecaFourVertex>> result = new HashSet<Pair<DecaFourVertex>>();
		
		for (Map.Entry<JScope, DecaFourVertex> e : src.vtxs.entrySet()) {
			result.add(new Pair<DecaFourVertex>(e.getValue(), vtxs.get(e.getKey())));
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return vtxsHashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DecaFourStateConfig)) {
			return false;
		}
		DecaFourStateConfig other = (DecaFourStateConfig) obj;
		return Objects.equals(vtxs, other.vtxs);
	}
	
	public String getDescription() {
		return Texts.concat(getVtxs().values(), "+", (v) -> { return v.getName() + "$" + v.getId(); });
	}
	
	public String getDescription(String lineSep) {
		return Texts.concat(getVtxs().values(), lineSep, (v) -> { return v.getName() + "$" + v.getId(); });
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
}

