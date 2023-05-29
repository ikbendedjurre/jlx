package jlx.utils;

import java.util.*;

public class ConcurrentSetBuild<I, O> extends ConcurrentWork<I, ConcurrentSetBuilder<O>> {
	public void createDefaultWorkers(String outputName, int setBuilderCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= setBuilderCount; index++) {
			getWorkers().add(new ConcurrentSetBuilder<O>(outputName));
		}
	}
	
	public void createDefaultWorkers(String outputName, Collection<O> initOutputs, int setBuilderCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= setBuilderCount; index++) {
			getWorkers().add(new ConcurrentSetBuilder<O>(outputName).assignCopyOf(initOutputs));
		}
	}
	
	public NormMap<O> combineOutputs() {
		NormMap<O> result = new NormMap<O>();
		combineOutputsInto(result);
		return result;
	}
	
	public void combineOutputsInto(NormMap<O> dest) {
		for (ConcurrentSetBuilder<O> updater : getWorkers()) {
			dest.addAll(updater.getElems());
		}
	}
}

