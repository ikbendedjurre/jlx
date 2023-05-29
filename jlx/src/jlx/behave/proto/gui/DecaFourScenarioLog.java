package jlx.behave.proto.gui;

import java.util.*;

import jlx.behave.proto.*;
import jlx.common.FileLocation;
import jlx.scenario.Scenario;

public class DecaFourScenarioLog {
	private final Scenario scenario;
	private final List<String> lines;
	private final List<Set<DecaFourStateConfig>> potCfgsSeq;
	private final List<PulsePackMap> inputValSeq;
	private final Set<DecaFourScenarioMismatch> mismatches;
	
	private FileLocation stepFileLocation;
	
	public DecaFourScenarioLog(Scenario scenario, DecaFourStateConfig firstCfg) {
		this.scenario = scenario;
		
		lines = new ArrayList<String>();
		potCfgsSeq = new ArrayList<Set<DecaFourStateConfig>>();
		potCfgsSeq.add(Collections.singleton(firstCfg));
		inputValSeq = new ArrayList<PulsePackMap>();
		mismatches = new HashSet<DecaFourScenarioMismatch>();
	}
	
	public Scenario getScenario() {
		return scenario;
	}
	
	public List<Set<DecaFourStateConfig>> getPotCfgsSeq() {
		return potCfgsSeq;
	}
	
	public Set<DecaFourStateConfig> getLastPotCfgs() {
		return potCfgsSeq.get(potCfgsSeq.size() - 1);
	}
	
	public List<PulsePackMap> getInputValSeq() {
		return inputValSeq;
	}
	
	public List<String> getLines() {
		return lines;
	}
	
	public Set<DecaFourScenarioMismatch> getMismatches() {
		return mismatches;
	}
	
	public FileLocation getStepFileLocation() {
		return stepFileLocation;
	}
	
	public void setStepFileLocation(FileLocation stepFileLocation) {
		this.stepFileLocation = stepFileLocation;
	}
}

