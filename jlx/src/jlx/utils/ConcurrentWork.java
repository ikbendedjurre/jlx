package jlx.utils;

import java.util.*;
import java.util.function.BiConsumer;

public class ConcurrentWork<I, W extends ConcurrentWorker> {
	private List<W> workers;
	private long logInterval;
	
	public ConcurrentWork() {
		workers = new ArrayList<W>();
		logInterval = 10000L;
	}
	
	public void setLogInterval(long ms) {
		logInterval = ms;
	}
	
	public List<W> getWorkers() {
		return workers;
	}
	
	public void apply(Collection<I> work, BiConsumer<I, W> fct) {
		List<I> workList = new ArrayList<I>(work);
		List<UpdaterThread> workerThreads = new ArrayList<UpdaterThread>(workers.size());
		int workPerWorker = work.size() / workers.size();
		int firstIndex = 0;
		
		if (workPerWorker > 0) {
			for (int index = 1; index < workers.size(); index++, firstIndex += workPerWorker) {
				workerThreads.add(new UpdaterThread(index, workers.get(index - 1), workList, firstIndex, firstIndex + workPerWorker, fct));
			}
		}
		
		//Last worker does all of the remaining work:
		workerThreads.add(new UpdaterThread(workers.size(), workers.get(workers.size() - 1), workList, firstIndex, work.size(), fct));
		
		for (UpdaterThread workerThread : workerThreads) {
			workerThread.start();
		}
		
		for (UpdaterThread workerThread : workerThreads) {
			try {
				workerThread.join();
			} catch (InterruptedException e) {
				throw new Error("Interruption in " + getClass().getCanonicalName(), e);
			}
		}
	}
	
	private class UpdaterThread extends Thread implements Runnable {
		private final int workerId;
		private final List<I> inputs;
		private final W worker;
		private final int firstIndex;
		private final int lastIndexExcl;
		private final BiConsumer<I, W> fct;
		
		public UpdaterThread(int workerId, W worker, List<I> inputs, int firstIndex, int lastIndexExcl, BiConsumer<I, W> fct) {
			this.workerId = workerId;
			this.worker = worker;
			this.inputs = inputs;
			this.firstIndex = firstIndex;
			this.lastIndexExcl = lastIndexExcl;
			this.fct = fct;
		}
		
		@Override
		public void run() {
			boolean firstLog = true;
			final long startTime = System.currentTimeMillis();
			long prevTime = startTime;
			int prevIndex = firstIndex;
			
			for (int index = firstIndex; index < lastIndexExcl; index++) {
				try {
					fct.accept(inputs.get(index), worker);
				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace(System.err);
					System.exit(0);
				}
				
				long currTime = System.currentTimeMillis();
				
				if (currTime - prevTime > logInterval) {
					float elapsedMins = (currTime - startTime) / 1000f / 60f;
					float elemsPerMin = (index + 1 - firstIndex) / elapsedMins;
					float etaMinutes = (lastIndexExcl - index - 1) / elemsPerMin;
					String etaMinutesText = String.format("; ETA = %.1fmin", etaMinutes);
					
					if (firstLog) {
						System.out.println("Worker " + workerId + " processed its first " + (index + 1 - prevIndex) + " elements (" + (index + 1 - firstIndex) + " / " + (lastIndexExcl - firstIndex) + ")" + etaMinutesText + worker.getSuffix());
						firstLog = false;
					} else {
						System.out.println("Worker " + workerId + " processed another " + (index + 1 - prevIndex) + " elements (" + (index + 1 - firstIndex) + " / " + (lastIndexExcl - firstIndex) + ")" + etaMinutesText + worker.getSuffix());
					}
					
					prevTime = currTime;
					prevIndex = index;
				}
			}
			
			if (firstLog) {
				System.out.println("Worker " + workerId + " processed " + (lastIndexExcl - firstIndex) + " elements" + worker.getSuffix());
			} else {
				System.out.println("Worker " + workerId + " is done" + worker.getSuffix());
			}
		}
	}
}

