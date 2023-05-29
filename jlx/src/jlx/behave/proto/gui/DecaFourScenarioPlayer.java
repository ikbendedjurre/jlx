package jlx.behave.proto.gui;

import java.io.*;
import java.util.*;

import jlx.asal.j.*;
import jlx.asal.rewrite.ASALSymbolicValue;
import jlx.behave.proto.*;
import jlx.behave.stable.DecaStableOutputEvolution;
import jlx.blocks.ibd1.OutPort;
import jlx.models.UnifyingBlock;
import jlx.models.UnifyingBlock.ReprPort;
import jlx.scenario.*;
import jlx.utils.CLI;
import jlx.utils.Dir;
import jlx.utils.Texts;

public class DecaFourScenarioPlayer {
	private final UnifyingBlock unifyingBlock;
	
	public DecaFourScenarioPlayer(UnifyingBlock unifyingBlock) {
		this.unifyingBlock = unifyingBlock;
	}
	
	public UnifyingBlock getUnifyingBlock() {
		return unifyingBlock;
	}
	
	public DecaFourScenarioLog play(Scenario scenario, String failVerdictLogFilename) {
		DecaFourScenarioLog result = play(scenario);
		
		if (result.getMismatches().size() > 0) {
			if (failVerdictLogFilename != null) {
				try {
					PrintStream out = new PrintStream(failVerdictLogFilename);
					
					for (String line : result.getLines()) {
						out.println(line);
					}
					
					out.close();
				} catch (FileNotFoundException e) {
					throw new Error(e);
				}
			} else {
				for (String line : result.getLines()) {
					System.out.println(line);
				}
				
				CLI.waitForEnter();
			}
		}
		
		return result;
	}
	
//	private Map<ReprPort, ASALSymbolicValue> extractInitialInputVal() {
//		Map<ReprPort, ASALSymbolicValue> valuePerPort = new HashMap<ReprPort, ASALSymbolicValue>();
//		
//		for (Map.Entry<ReprPort, PulsePack> e : unifyingBlock.sms4.initialInputs.getPackPerPort().entrySet()) {
//			if (e.getKey().isPortToEnvironment()) {
//				valuePerPort.put(e.getKey(), e.getValue());
//			}
//		}
//		
//		return valuePerPort;
//	}
	
	private final static String INDENT = "  ";
	
	public DecaFourScenarioLog play(Scenario scenario) {
		DecaFourScenarioLog result = new DecaFourScenarioLog(scenario, unifyingBlock.sms4.initCfg);
		result.getLines().add("========== " + scenario.getClass().getCanonicalName() + " ==========");
		result.getLines().add("Starting in state");
		
		for (Map.Entry<JScope, DecaFourVertex> e : unifyingBlock.sms4.initCfg.getVtxs().entrySet()) {
			result.getLines().add(INDENT + e.getValue().getName());
		}
		
		result.getLines().add("Initial input values:");
		
		for (Map.Entry<ReprPort, PulsePack> e : unifyingBlock.sms4.initialInputs.getPackPerPort().entrySet()) {
			result.getLines().add(INDENT + e.getValue());
		}
		
		result.getLines().add("Initial output values:");
		
		for (Map.Entry<ReprPort, PulsePack> e : unifyingBlock.sms4.initCfg.getOutputVal().getPackPerPort().entrySet()) {
			result.getLines().add(INDENT + e.getValue());
		}
		
		Map<ReprPort, ASALSymbolicValue> currInputs = unifyingBlock.sms4.initialInputs.extractExternalMap().extractValuation();
		Set<DecaStableOutputEvolution> expectedOutputs = new HashSet<DecaStableOutputEvolution>();
		Set<String> expectedStateChanges = new TreeSet<String>();
		Step[] steps = scenario.getSteps();
		
		for (int index = 0; index < steps.length; index++) {
			Step step = steps[index];
			
			if (result.getLastPotCfgs().isEmpty()) {
				throw new Error("Should not happen!");
			}
			
			if (step instanceof Step.InputStep) {
				Step.InputStep i = (Step.InputStep)step;
				ReprPort rp = unifyingBlock.reprPortPerPrimPort.get(i.getPort());
				
				if (rp == null) {
					throw new Error("Could not find input port" + i.getPort().getFileLocation());
				}
				
				if (!rp.isPortToEnvironment()) {
					throw new Error("Input port should be an environmental input port:" + i.getPort().getFileLocation());
				}
				
				ASALSymbolicValue v = ASALSymbolicValue.from(i.getNewValue());
				result.getLines().add("Setting " + rp.getReprOwner().getName() + "::" + rp.getName() + " to " + v.toString());
				currInputs.put(rp, v);
				continue;
			}
			
			if (step instanceof Step.TimeoutStep) {
				Step.TimeoutStep t = (Step.TimeoutStep)step;
				ReprPort rp = unifyingBlock.reprPortPerPrimPort.get(t.getDurationPort());
				
				if (rp == null) {
					throw new Error("Could not find duration port" + t.getDurationPort().getFileLocation());
				}
				
				ReprPort rp2 = getTimeoutPort(rp);
				
				if (rp2 == null) {
					throw new Error("Could not find timeout port that corresponds with duration port" + rp.getFileLocation());
				}
				
				result.getLines().add("Triggering timeout " + rp2.getReprOwner().getName() + "::" + rp2.getName());
				currInputs.put(rp2, ASALSymbolicValue.TRUE);
				continue;
			}
			
			if (step instanceof Step.OutputStep) {
				Step.OutputStep o = (Step.OutputStep)step;
				List<PulsePack> outputSeq = new ArrayList<PulsePack>();
				
				for (Map<OutPort<?>, JType> e : o.getOutputSeq()) {
					Map<ReprPort, ASALSymbolicValue> m = new HashMap<ReprPort, ASALSymbolicValue>();
					ReprPort mainPort = null;
					
					for (Map.Entry<OutPort<?>, JType> e2 : e.entrySet()) {
						ReprPort rp = unifyingBlock.reprPortPerPrimPort.get(e2.getKey());
						
						if (rp == null) {
							throw new Error("Could not find output port" + e2.getKey().getFileLocation());
						}
						
						m.put(rp, ASALSymbolicValue.from(e2.getValue()));
						
						if (rp.getPulsePort() == null) {
							if (mainPort != null) {
								throw new Error("Pulse pack has too many main ports" + e2.getKey().getFileLocation());
							}
							
							mainPort = rp;
						}
					}
					
					if (outputSeq.isEmpty() || !outputSeq.get(outputSeq.size() - 1).equals(m)) {
						if (mainPort == null) {
							throw new Error("Pulse pack has no main ports");
						}
						
						outputSeq.add(new PulsePack(mainPort, m));
					}
				}
				
				DecaStableOutputEvolution evo = new DecaStableOutputEvolution(outputSeq);
				result.getLines().add(INDENT + "Expecting " + evo);
				expectedOutputs.add(evo);
				continue;
			}
			
			if (step instanceof Step.SingleStep) {
				result.getLines().add("Observing 1 step");
				result.getLines().addAll(expectedStateChanges);
				
				PulsePackMap inputVal = PulsePackMap.from(currInputs, Dir.IN);
				Set<DecaFourStateConfig> newPotCfgs = new HashSet<DecaFourStateConfig>();
				Set<DecaStableOutputEvolution> evos = new HashSet<DecaStableOutputEvolution>();
				Set<DecaFourScenarioMismatch> leastWrongMismatches = null;
				
				for (DecaFourStateConfig potCfg : result.getLastPotCfgs()) {
					Set<DecaFourStateConfig> succs = potCfg.computeSuccsViaInputVal(inputVal);
					
					for (DecaFourStateConfig succ : succs) {
						Set<DecaStableOutputEvolution> evos__ = DecaStableOutputEvolution.getOutputEvolutions(potCfg, succ);
						Set<DecaFourScenarioMismatch> mismatches = getMismatches(evos__, expectedOutputs);
						
						if (mismatches.isEmpty()) {
							newPotCfgs.add(succ);
							evos.addAll(evos__);
						} else {
							if (leastWrongMismatches == null || mismatches.size() < leastWrongMismatches.size()) {
								leastWrongMismatches = mismatches;
							}
						}
					}
				}
				
				if (newPotCfgs.isEmpty()) {
					addMismatchesToLog(leastWrongMismatches, result);
					result.getMismatches().addAll(leastWrongMismatches);
					result.setStepFileLocation(step.getFileLocation());
					result.getLines().add("Fail verdict");
					return result;
				}
				
				for (DecaStableOutputEvolution evo : evos) {
					result.getLines().add(INDENT + "Observed " + evo);
				}
				
				addNewPotCfgsToLog(newPotCfgs, result);
				result.getPotCfgsSeq().add(newPotCfgs);
				result.getInputValSeq().add(inputVal);
				currInputs = inputVal.deactivate().extractValuation();
				expectedOutputs.clear();
				expectedStateChanges.clear();
				continue;
			}
			
			if (step instanceof Step.StabilizeStep) {
				result.getLines().add("Observing until stable");
				result.getLines().addAll(expectedStateChanges);
				
				PulsePackMap inputVal = PulsePackMap.from(currInputs, Dir.IN);
				Set<DecaFourStateConfig> newPotCfgs = new HashSet<DecaFourStateConfig>();
				Set<DecaStableOutputEvolution> evos = new HashSet<DecaStableOutputEvolution>();
				Set<DecaFourScenarioMismatch> leastWrongMismatches = null;
				
				for (DecaFourStateConfig potCfg : result.getLastPotCfgs()) {
					Set<DecaFourStateConfig> succs = potCfg.computeSuccsViaInputVal(inputVal);
					
					if (succs.size() > 0) {
						List<DecaFourStateConfig> seq = DecaFourStateConfig.followInputs(succs, inputVal.deactivate(), inputVal).getCfgs();
						
						if (seq == null) {
							seq = new ArrayList<DecaFourStateConfig>();
							seq.add(potCfg);
						}
						
						seq.add(0, potCfg);
						DecaFourStateConfig succ = seq.get(seq.size() - 1);
						Set<DecaStableOutputEvolution> evos__ = DecaStableOutputEvolution.getOutputEvolutions(seq);
						Set<DecaFourScenarioMismatch> mismatches = getMismatches(evos__, expectedOutputs);
						
						if (mismatches.isEmpty()) {
							newPotCfgs.add(succ);
							evos.addAll(evos__);
						} else {
							if (leastWrongMismatches == null || mismatches.size() < leastWrongMismatches.size()) {
								leastWrongMismatches = mismatches;
							}
						}
					}
				}
				
				if (newPotCfgs.isEmpty()) {
					addMismatchesToLog(leastWrongMismatches, result);
					result.getMismatches().addAll(leastWrongMismatches);
					result.setStepFileLocation(step.getFileLocation());
					result.getLines().add("Fail verdict");
					return result;
				}
				
				for (DecaStableOutputEvolution evo : evos) {
					result.getLines().add(INDENT + "Observed " + evo);
				}
				
				addNewPotCfgsToLog(newPotCfgs, result);
				result.getPotCfgsSeq().add(newPotCfgs);
				result.getInputValSeq().add(inputVal);
				currInputs = inputVal.deactivate().extractValuation();
				expectedOutputs.clear();
				expectedStateChanges.clear();
				continue;
			}
			
			if (step instanceof Step.StateChange) {
				Step.StateChange s = (Step.StateChange)step;
				
				if (s.getStateSeq().size() == 1) {
					expectedStateChanges.add(INDENT + s.getBlockInstance().getName() + " should stay in " + s.getStateSeq().get(0));
				} else {
					expectedStateChanges.add(INDENT + s.getBlockInstance().getName() + " should move from " + Texts.concat(s.getStateSeq(), " to "));
				}
				
				continue;
			}
			
			throw new Error("Should not happen!");
		}
		
		result.getLines().add("Pass verdict");
//		CLI.waitForEnter();
		return result;
	}
	
	private static Set<DecaFourScenarioMismatch> getMismatches(Set<DecaStableOutputEvolution> found, Set<DecaStableOutputEvolution> expected) {
		Set<DecaFourScenarioMismatch> result = new HashSet<DecaFourScenarioMismatch>();
		
		for (DecaStableOutputEvolution evo : found) {
			DecaFourScenarioMismatch mismatch = getMismatch(evo, expected);
			
			if (mismatch != null) {
				result.add(mismatch);
			}
		}
		
		return result;
	}
	
	private static DecaFourScenarioMismatch getMismatch(DecaStableOutputEvolution found, Set<DecaStableOutputEvolution> expected) {
		if (found.getEvolution().size() == 1) {
			for (DecaStableOutputEvolution evo : expected) {
				if (evo.getEvolution().get(0).equals(found.getEvolution().get(0))) {
					return new DecaFourScenarioMismatch("(!) Incomplete output evolution", found, evo);
				}
			}
			
			return null;
		}
		
		for (DecaStableOutputEvolution evo : expected) {
			int sharedPrefixLength = DecaStableOutputEvolution.getSharedPrefixLength(evo, found);
			
			if (sharedPrefixLength > 0) {
				if (sharedPrefixLength == evo.getEvolution().size() && sharedPrefixLength == found.getEvolution().size()) {
					return null;
				}
				
				if (sharedPrefixLength == found.getEvolution().size()) {
					return new DecaFourScenarioMismatch("(!) Incomplete output evolution", found, evo);
				}
				
				if (sharedPrefixLength == evo.getEvolution().size()) {
					return new DecaFourScenarioMismatch("(!) Unexpected output evolution", found, evo);
				}
				
				return new DecaFourScenarioMismatch("(!) Different output evolution", found, evo);
			}
		}
		
		DecaStableOutputEvolution evo = new DecaStableOutputEvolution(Collections.singletonList(found.getEvolution().get(0)));
		return new DecaFourScenarioMismatch("(!) Unexpected output evolution", found, evo);
	}
	
	private ReprPort getTimeoutPort(ReprPort durationPort) {
		DecaFourStateMachine sm = unifyingBlock.sms4.getSmPerScope().get(durationPort.getReprOwner());
		return (ReprPort)sm.timeoutPortPerDurationPort.get(durationPort);
	}
	
	private static void addMismatchesToLog(Set<DecaFourScenarioMismatch> mismatches, DecaFourScenarioLog log) {
		for (DecaFourScenarioMismatch mismatch : mismatches) {
			log.getLines().add(mismatch.getHeader() + ":");
			log.getLines().add(INDENT + "Expected: " + mismatch.getExpected());
			log.getLines().add(INDENT + "Observed: " + mismatch.getFound());
		}
	}
	
	private static void addNewPotCfgsToLog(Set<DecaFourStateConfig> newPotCfgs, DecaFourScenarioLog log) {
		Iterator<DecaFourStateConfig> q = newPotCfgs.iterator();
		log.getLines().add("Moving to state");
		addNewPotCfgToLog(q.next(), log);
		
		while (q.hasNext()) {
			log.getLines().add("or to state");
			addNewPotCfgToLog(q.next(), log);
		}
	}
	
	private static void addNewPotCfgToLog(DecaFourStateConfig newPotCfg, DecaFourScenarioLog log) {
		for (Map.Entry<JScope, DecaFourVertex> e : newPotCfg.getVtxs().entrySet()) {
			log.getLines().add(INDENT + e.getValue().getName());
		}
	}
}

