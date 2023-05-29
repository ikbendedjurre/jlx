package jlx.behave.proto.gui;

import java.util.*;

import jlx.asal.j.*;
import jlx.asal.j.JTypeLibrary.Constructor;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.behave.stable.*;
import jlx.common.reflection.ClassReflectionException;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class DecaFourScenarioExport {
	private final List<JScope> orderedScopes;
	private final List<UserSimStep> userSteps;
	private final List<PulsePackMap> inputValSeq;
	private final List<DecaFourStateConfig> cfgSeq;
	
	public DecaFourScenarioExport(List<JScope> orderedScopes, List<UserSimStep> userSteps, List<PulsePackMap> inputValSeq, List<DecaFourStateConfig> cfgSeq) {
		this.orderedScopes = orderedScopes;
		this.userSteps = userSteps;
		this.inputValSeq = inputValSeq;
		this.cfgSeq = cfgSeq;
	}
	
	public List<String> extractScenario() {
		List<String> steps = new ArrayList<String>();
		int index = 0;
		
		while (index + 1 < cfgSeq.size()) {
			PulsePackMap inputVal1 = inputValSeq.get(index);
			PulsePackMap inputVal2 = inputValSeq.get(index + 1);
			List<DecaFourStateConfig> outputCfgs = new ArrayList<DecaFourStateConfig>();
			outputCfgs.add(cfgSeq.get(index));
			
			index++;
			outputCfgs.add(cfgSeq.get(index));
			
			while (userSteps.get(index) == UserSimStep.UNSTABLE) {
				index++;
				outputCfgs.add(cfgSeq.get(index));
			}
			
			for (PulsePack e : inputVal2.extractEventMap(inputVal1).getPackPerPort().values()) {
				for (Map.Entry<ReprPort, ASALSymbolicValue> e2 : e.getValuePerPort().entrySet()) {
					if (!JPulse.class.isAssignableFrom(e2.getKey().getType()) || e2.getValue().equals(ASALSymbolicValue.TRUE)) {
						steps.add("setInput(" + toStr(e2.getKey()) + ", " + toStr(e2.getKey(), e2.getValue()) + ")");
					}
				}
			}
			
			for (JScope scope : orderedScopes) {
				List<String> elems = new ArrayList<String>();
				elems.add("\"" + scope.getName() + "\"");
				elems.add("\"" + outputCfgs.get(0).getVtxs().get(scope).getName() + "\"");
				
				for (int i = 1; i < outputCfgs.size(); i++) {
					String v = "\"" + outputCfgs.get(i).getVtxs().get(scope).getName() + "\"";
					
					if (!v.equals(elems.get(elems.size() - 1))) {
						elems.add(v);
					}
				}
				
				steps.add("expectStateChange(" + Texts.concat(elems, ", ") + ")");
			}
			
			for (DecaStableOutputEvolution evo : DecaStableOutputEvolution.getOutputEvolutions(outputCfgs)) {
				if (evo.getEvolution().size() > 1) {
					steps.add(toStr(evo));
				}
			}
			
			switch (userSteps.get(index)) {
				case INITIALIZATION:
					throw new Error("Should not happen!");
				case STEP:
					steps.add("step()");
					break;
				case UNSTABLE:
					throw new Error("Should not happen!");
				case STABILIZED:
					steps.add("stabilize()");
					break;
			}
		}
		
		List<String> result = new ArrayList<String>();
		result.add("@Override");
		result.add("public Step[] getSteps() {");
		
		if (steps.isEmpty()) {
			result.add("\treturn new Step[0];");
		} else {
			result.add("\treturn new Step[] {");
			
			for (int i = 1; i < steps.size(); i++) {
				result.add("\t\t" + steps.get(i - 1) + ",");
			}
			
			result.add("\t\t" + steps.get(steps.size() - 1));
			result.add("\t};");
		}
		
		result.add("}");
		return result;
	}
	
	private static String toStr(DecaStableOutputEvolution evo) {
		return "expectOutput(" + Texts.concat(evo.getEvolution(), ", ", e -> { return toStr(e); }) + ")";
	}
	
	private static String toStr(PulsePack pulsePack) {
		java.util.List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<ReprPort, ASALSymbolicValue> e : pulsePack.getValuePerPort().entrySet()) {
			elems.add(toStr(e.getKey()));
			elems.add(toStr(e.getKey(), e.getValue()));
		}
		
		return "Output.from(" + Texts.concat(elems, ", ") + ")";
	}
	
	private static String toStr(ReprPort rp) {
		return "get_" + rp.getReprOwner().getName() + "()." + rp.getName();
	}
	
	private static String toStr(ReprPort rp, ASALSymbolicValue value) {
		if (value.isBooleanType()) {
			if (rp.getType().equals(JBool.class)) {
				return JBool.class.getSimpleName() + "." + value.toString();
			}
			
			if (rp.getType().equals(JPulse.class)) {
				return JPulse.class.getSimpleName() + "." + value.toString();
			}
			
			throw new Error("Should not happen!");
		}
		
		try {
			Constructor c = rp.getReprOwner().getTypeLib().getConstructor(value.toString(), rp.getType());
			return "new " + c.getCanonicalName() + "()";
		} catch (ClassReflectionException e) {
			throw new Error("Should not happen!", e);
		}
	}
}

