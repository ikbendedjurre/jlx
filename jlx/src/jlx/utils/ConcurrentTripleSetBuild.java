package jlx.utils;

import java.util.*;

public class ConcurrentTripleSetBuild<I, O1, O2, O3> extends ConcurrentWork<I, ConcurrentTripleSetBuilder<O1, O2, O3>> {
	public void createDefaultWorkers(String outputName1, String outputName2, String outputName3, int setBuilderCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= setBuilderCount; index++) {
			getWorkers().add(new ConcurrentTripleSetBuilder<O1, O2, O3>(outputName1, outputName2, outputName3));
		}
	}
	
	public void createDefaultWorkers(String outputName1, Collection<O1> initOutputs1, String outputName2, Collection<O2> initOutputs2, String outputName3, Collection<O3> initOutputs3, int setBuilderCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= setBuilderCount; index++) {
			getWorkers().add(new ConcurrentTripleSetBuilder<O1, O2, O3>(outputName1, outputName2, outputName3).assignCopyOf(initOutputs1, initOutputs2, initOutputs3));
		}
	}
	
	private NormMap<O1> combinedOutputs1;
	private NormMap<O2> combinedOutputs2;
	private NormMap<O3> combinedOutputs3;
	
	public ConcurrentTripleSetBuild() {
		combinedOutputs1 = new NormMap<O1>();
		combinedOutputs2 = new NormMap<O2>();
		combinedOutputs3 = new NormMap<O3>();
	}
	
	public NormMap<O1> getCombinedOutputs1() {
		return combinedOutputs1;
	}
	
	public NormMap<O2> getCombinedOutputs2() {
		return combinedOutputs2;
	}
	
	public NormMap<O3> getCombinedOutputs3() {
		return combinedOutputs3;
	}
	
	public void combineOutputs() {
		combinedOutputs1.clear();
		combinedOutputs2.clear();
		combinedOutputs3.clear();
		combineOutputsInto(combinedOutputs1, combinedOutputs2, combinedOutputs3);
	}
	
	public void combineOutputsInto(NormMap<O1> dest1, NormMap<O2> dest2, NormMap<O3> dest3) {
		for (ConcurrentTripleSetBuilder<O1, O2, O3> updater : getWorkers()) {
			dest1.addAll(updater.getElems1());
			dest2.addAll(updater.getElems2());
			dest3.addAll(updater.getElems3());
		}
	}
}

