package jlx.utils;

public class ConcurrentUpdates<I> extends ConcurrentWork<I, ConcurrentWorker> {
	public void createDefaultWorkers(int workerCount) {
		getWorkers().clear();
		
		for (int index = 1; index <= workerCount; index++) {
			getWorkers().add(new ConcurrentWorker());
		}
	}
}

