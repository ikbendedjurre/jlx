package jlx.utils;

import java.util.*;

public class ConcurrentDoubleSetBuild<I, O1, O2> extends ConcurrentWork<I, ConcurrentDoubleSetBuilder<O1, O2>> {
	public void createDefaultWorkers(String outputName1, String outputName2, int setBuilderCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= setBuilderCount; index++) {
			getWorkers().add(new ConcurrentDoubleSetBuilder<O1, O2>(outputName1, outputName2));
		}
	}
	
	public void createDefaultWorkers(String outputName1, Collection<O1> initOutputs1, String outputName2, Collection<O2> initOutputs2, int setBuilderCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= setBuilderCount; index++) {
			getWorkers().add(new ConcurrentDoubleSetBuilder<O1, O2>(outputName1, outputName2).assignCopyOf(initOutputs1, initOutputs2));
		}
	}
	
	private NormMap<O1> combinedOutputs1;
	private NormMap<O2> combinedOutputs2;
	
	public ConcurrentDoubleSetBuild() {
		combinedOutputs1 = new NormMap<O1>();
		combinedOutputs2 = new NormMap<O2>();
	}
	
	public NormMap<O1> getCombinedOutputs1() {
		return combinedOutputs1;
	}
	
	public NormMap<O2> getCombinedOutputs2() {
		return combinedOutputs2;
	}
	
	public void combineOutputs() {
		combinedOutputs1.clear();
		combinedOutputs2.clear();
		combineOutputsInto(combinedOutputs1, combinedOutputs2);
	}
	
	public void combineOutputsInto(NormMap<O1> dest1, NormMap<O2> dest2) {
		for (ConcurrentDoubleSetBuilder<O1, O2> updater : getWorkers()) {
			dest1.addAll(updater.getElems1());
			dest2.addAll(updater.getElems2());
		}
	}
}

