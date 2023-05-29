package jlx.behave.stable;

import java.util.*;

import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.Texts;

public class DecaStableOutputEvolution {
	private final List<PulsePack> evolution;
	private final int hashCode;
	
	public DecaStableOutputEvolution(List<PulsePack> evolution) {
		this.evolution = evolution;
		
		hashCode = Objects.hash(evolution);
	}
	
	public List<PulsePack> getEvolution() {
		return evolution;
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
		DecaStableOutputEvolution other = (DecaStableOutputEvolution) obj;
		return Objects.equals(evolution, other.evolution);
	}
	
	@Override
	public String toString() {
		return Texts.concat(getEvolution(), " -> ", DecaStableOutputEvolution::toStr);
	}
	
	private static String toStr(PulsePack m) {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : m.getValuePerPort().entrySet()) {
			elems.add(e.getKey() + " = " + e.getValue());
		}
		
		return elems.size() > 0 ? "{ " + Texts.concat(elems, ", ") + " }" : "{ }";
	}
	
	public static Set<DecaStableOutputEvolution> getOutputEvolutions(DecaFourStateConfig cfg1, DecaFourStateConfig cfg2) {
		List<DecaFourStateConfig> seq = new ArrayList<DecaFourStateConfig>();
		seq.add(cfg1);
		seq.add(cfg2);
		return getOutputEvolutions(seq);
	}
	
	public static Set<DecaStableOutputEvolution> getOutputEvolutions(List<DecaFourStateConfig> seq) {
		Map<ReprPort, List<PulsePack>> valueOverTimePerOutput = new HashMap<ReprPort, List<PulsePack>>();
		
		for (Map.Entry<ReprPort, PulsePack> e : seq.get(0).getOutputVal().getPackPerPort().entrySet()) {
			List<PulsePack> valueOverTime = new ArrayList<PulsePack>();
			valueOverTime.add(e.getValue());
			valueOverTimePerOutput.put(e.getKey(), valueOverTime);
		}
		
		for (int index = 1; index < seq.size(); index++) {
			PulsePackMap v = seq.get(index).getOutputVal();
			
			for (Map.Entry<ReprPort, PulsePack> e : v.getPackPerPort().entrySet()) {
				List<PulsePack> valueOverTime = valueOverTimePerOutput.get(e.getKey());
				
				if (!e.getValue().equals(valueOverTime.get(valueOverTime.size() - 1))) {
					valueOverTime.add(e.getValue());
				}
			}
		}
		
		Set<DecaStableOutputEvolution> result = new HashSet<DecaStableOutputEvolution>();
		
		for (Map.Entry<ReprPort, List<PulsePack>> e : valueOverTimePerOutput.entrySet()) {
			result.add(new DecaStableOutputEvolution(e.getValue()));
		}
		
		return result;
	}
	
	public static int getSharedPrefixLength(DecaStableOutputEvolution evo1, DecaStableOutputEvolution evo2) {
		int max = Math.min(evo1.getEvolution().size(), evo2.getEvolution().size());
		
		for (int i = 0; i < max; i++) {
			if (!evo1.getEvolution().get(i).equals(evo2.getEvolution().get(i))) {
				return i;
			}
		}
		
		return max;
	}
}

