package jlx.behave.stable;

import java.io.PrintStream;
import java.util.*;

import jlx.behave.proto.DecaFourStateConfig;
import jlx.models.UnifyingBlock;
import jlx.utils.AbstractExporter;
import jlx.utils.HashMaps;

public class DecaStableLevels extends AbstractExporter {
	private final UnifyingBlock ub;
	private final Map<Integer, Integer> cfgCountPerLevel;
	private final Map<Integer, Integer> stableCfgCountPerLevel;
	private final int maxLevel;
	
	public DecaStableLevels(UnifyingBlock ub) {
		this.ub = ub;
		
		cfgCountPerLevel = new HashMap<Integer, Integer>();
		
		for (DecaFourStateConfig cfg : ub.sms4.configs) {
			HashMaps.increment(cfgCountPerLevel, ub.sms4.levelPerCfg.get(cfg), 1, 0);
		}
		
		stableCfgCountPerLevel = new HashMap<Integer, Integer>();
		
		for (DecaFourStateConfig cfg : ub.stableSm.coveredCfgs) {
			HashMaps.increment(stableCfgCountPerLevel, ub.sms4.levelPerCfg.get(cfg), 1, 0);
		}
		
		maxLevel = extractMaxLevel();
	}
	
	private int extractMaxLevel() {
		int result = 0;
		
		for (int level : cfgCountPerLevel.keySet()) {
			result = Math.max(result, level);
		}
		
		for (int level : stableCfgCountPerLevel.keySet()) {
			result = Math.max(result, level);
		}
		
		return result;
	}
	
	@Override
	public void saveToFile(PrintStream out) {
		out.println("LEVEL\tFSM\tSIC-DFSM");
		
		for (int level = 0; level < maxLevel; level++) {
			final int c1 = cfgCountPerLevel.getOrDefault(level, 0);
			final int c2 = stableCfgCountPerLevel.getOrDefault(level, 0);
			out.println(level + "\t" + c1 + "\t" + c2);
		}
	}
	
}
