package jlx.behave.stable;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaStableVertex {
	private final PulsePackMap entryInputs;
	private final PulsePackMap outputVal;
	private final DecaFourStateConfig cfg;
	private final int hashCode;
	
	private Set<DecaStableTransition> outgoing;
	private Set<DecaStableTransition> nonLoopOutgoing;
	private Set<DecaStableVertex> succs;
	private Set<DecaStableVertex> preds;
	private int level;
	
	public Object scc;
	
	public DecaStableVertex(DecaFourStateConfig cfg, PulsePackMap entryInputs, int level) {
		this.cfg = cfg;
		this.entryInputs = entryInputs;
		this.level = level;
		
		outputVal = extractOutputVal();
		outgoing = new HashSet<DecaStableTransition>();
		nonLoopOutgoing = new HashSet<DecaStableTransition>();
		succs = new HashSet<DecaStableVertex>();
		preds = new HashSet<DecaStableVertex>();
		
		hashCode = Objects.hash(cfg, entryInputs);
	}
	
	public PulsePackMap getExternalIncomingInputs() {
		return entryInputs;
	}
	
	public DecaFourStateConfig getCfg() {
		return cfg;
	}
	
	public PulsePackMap getOutputVal() {
		return outputVal;
	}
	
	public Set<DecaStableTransition> getOutgoing() {
		return outgoing;
	}
	
	public Set<DecaStableTransition> getNonLoopOutgoing() {
		return nonLoopOutgoing;
	}
	
	public Set<Class<?>> getClzs() {
		return cfg.getClzs();
	}
	
	private PulsePackMap extractOutputVal() {
		Map<ReprPort, PulsePack> result = new HashMap<ReprPort, PulsePack>();
		
		for (DecaFourVertex v : cfg.getVtxs().values()) {
			result.putAll(v.getOutputVal().getPackPerPort());
		}
		
		return new PulsePackMap(result, Dir.OUT);
	}
	
	public Set<DecaStableVertex> getSuccs() {
		return succs;
	}
	
	public Set<DecaStableVertex> getPreds() {
		return preds;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void normalizeSuccs(NormMap<DecaStableVertex> vertices) {
		Set<DecaStableVertex> newSuccs = new HashSet<DecaStableVertex>();
		
		for (DecaStableVertex succ : succs) {
			newSuccs.add(vertices.get(succ));
		}
		
		succs.clear();
		succs.addAll(newSuccs);
		
		for (DecaStableTransition t : outgoing) {
			t.normalizeTgt(vertices);
		}
	}
	
	public int populateOutgoing(DecaFourStateMachines legacy, NormMap<DecaStableVertex> vertices, Set<ProtoTransition> protoTrsDest, boolean nonDet) {
		int nonDetCount = 0;
		
		outgoing.clear();
		succs.clear();
		nonLoopOutgoing.clear();
		
		Map<PulsePackMap, Set<DecaFourStateConfig>> succsPerSicInput = new HashMap<PulsePackMap, Set<DecaFourStateConfig>>();
		
		for (DecaFourStateConfig succ : cfg.computeSuccs()) {
			for (PulsePackMap sicInput : cfg.getSicInputsToSucc(succ, entryInputs)) {
				HashMaps.inject(succsPerSicInput, sicInput, succ);
			}
		}
		
		for (Map.Entry<PulsePackMap, Set<DecaFourStateConfig>> e : succsPerSicInput.entrySet()) {
			PulsePackMap debugSic = null;
			
//			if (contains(cfg.getVtxs().values(), "STOPPED")) {
//				if (contains(cfg.getVtxs().values(), "ALL_LEFT")) {
//					if (contains(cfg.getVtxs().values(), "PDI_CONNECTION_ESTABLISHED")) {
//						PulsePackMap t = e.getKey();
//						
//						if (t.toString().contains("fp::T1_Cd_Move_Point = TRUE")) {
//							if (t.toString().contains("fp::DT1_Move_Point_Target = \"RIGHT\"")) {
//								if (t.toString().contains("p3::D20_F_EST_EfeS_Gen_SR_State = \"OPERATIONAL\"")) {
//									System.out.println(getCfg().getDescription());
//									System.out.println("sic: " + t.toString().replace(" | ", "\n"));
////									System.out.println("-a: " + deactivatedInputs.toString().replace(" | ", "\n"));
////									System.out.println(tgt.getCfg().getDescription());
////									System.exit(0);
//									
//									debugSic = t;
//								}
//							}
//						}
//					}
//				}
//			}
			
			PulsePackMap deactivatedInputs = e.getKey().deactivate();
			
			if (nonDet) {
				Set<DecaFourStateConfigPath> seqs = DecaFourStateConfig.followInputs2(e.getValue(), deactivatedInputs, debugSic);
				
				if (seqs.size() > 1) {
					nonDetCount++;
				}
				
				for (DecaFourStateConfigPath seq : seqs) {
					List<DecaFourStateConfig> cfgs = new ArrayList<DecaFourStateConfig>(seq.getCfgs());
					cfgs.add(0, cfg); //Add the configuration of this vertex to the start of the sequence!!
					DecaStableVertex tgt = new DecaStableVertex(cfgs.get(cfgs.size() - 1), deactivatedInputs, level + 1);
					
					protoTrsDest.addAll(cfg.getProtoTrsToSuccsViaInputVal(e.getValue(), e.getKey()));
					protoTrsDest.addAll(seq.getProtoTrs());
					
					tgt = vertices.get(tgt);
					outgoing.add(new DecaStableTransition(this, tgt, e.getKey(), cfgs));
					succs.add(tgt);
				}
			} else {
				DecaFourStateConfigPath seq = DecaFourStateConfig.followInputs(e.getValue(), deactivatedInputs, debugSic);
				
				if (seq != null) {
					List<DecaFourStateConfig> cfgs = new ArrayList<DecaFourStateConfig>(seq.getCfgs());
					cfgs.add(0, cfg); //Add the configuration of this vertex to the start of the sequence!!
					DecaStableVertex tgt = new DecaStableVertex(cfgs.get(cfgs.size() - 1), deactivatedInputs, level + 1);
					
					protoTrsDest.addAll(cfg.getProtoTrsToSuccsViaInputVal(e.getValue(), e.getKey()));
					protoTrsDest.addAll(seq.getProtoTrs());
					
//					if (!vertices.contains(tgt)) {
//						throw new Error("Should not happen!");
//					}
					
					tgt = vertices.get(tgt);
					outgoing.add(new DecaStableTransition(this, tgt, e.getKey(), cfgs));
					succs.add(tgt);
					
//					if (e.getKey().getExternalPulseCount() == 0) {
//						throw new Error("Should happen!");
//					}
				}
			}
		}
		
//		if (contains(cfg.getVtxs().values(), "STOPPED")) {
//			if (contains(cfg.getVtxs().values(), "ALL_LEFT")) {
//				if (contains(cfg.getVtxs().values(), "PDI_CONNECTION_ESTABLISHED")) {
//					for (DecaStableTransition t : outgoing) {
//						if (t.getSicInputs().toString().contains("fp::T1_Cd_Move_Point = TRUE")) {
//							System.out.println("" + t.getSicInputs().toString().replace(" | ", "\n"));
//							System.out.println(t.getTgt().getCfg().getDescription());
//							System.exit(0);
//						}
//					}
//				}
//			}
//		}
		
		outgoing.add(new DecaStableTransition(this, null, true));
		succs.add(this);
		
		for (DecaStableTransition t : outgoing) {
			if (t.getTgt() != t.getSrc()) {
				nonLoopOutgoing.add(t);
			}
		}
		
		return nonDetCount;
	}
	
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
	
	public static Map<ReprPort, ASALSymbolicValue> removeActivePulses(Map<ReprPort, ASALSymbolicValue> inputs) {
		Map<ReprPort, ASALSymbolicValue> result = new HashMap<ReprPort, ASALSymbolicValue>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : inputs.entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				if (JPulse.class.isAssignableFrom(e.getKey().getType())) {
					//Do nothing.
				} else {
					if (e.getKey().getPulsePort() == null) {
						result.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		
		return result;
	}
	
	public DecaStableTransition getOutgoing(DecaStableStateMachine stableSm, DecaStableInputChanges sicInputs) {
		DecaStableTransition result = null;
		
		for (DecaStableTransition t : outgoing) {
			DecaStableInputChanges si = t.getInputChanges(stableSm);
			
			if (si.equals(sicInputs)) {
				if (result != null) {
					System.out.println("sicInputs1 = " + sicInputs.toString());
					System.out.println("sicInputs2 = " + si.toString());
					
					throw new Error("Should not happen!");
				}
				
				result = t;
			}
		}
		
		if (result == null) {
			System.out.println("sicInputs1 = " + sicInputs.toString());
			
			for (DecaStableTransition t : outgoing) {
				DecaStableInputChanges si = t.getInputChanges(stableSm);
				System.out.println("sicInputs2 = " + si.toString());
			}
			
			throw new Error("Should not happen!");
		}
		
		return result;
	}
	
	public Set<DecaStableInputChanges> getOutgoingInputChanges(DecaStableStateMachine stableSm) {
		Set<DecaStableInputChanges> result = new HashSet<DecaStableInputChanges>();
		
		for (DecaStableTransition t : outgoing) {
			result.add(t.getInputChanges(stableSm));
		}
		
		return result;
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
		DecaStableVertex other = (DecaStableVertex) obj;
		return Objects.equals(cfg, other.cfg) && Objects.equals(entryInputs, other.entryInputs);
	}
}

