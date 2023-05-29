package jlx.utils;

import java.util.*;
import java.util.function.*;

public class MonteCarloWork {
	private final int workerCount;
	private final long workerTimeMillis;
	
	public MonteCarloWork(int workerCount, long workerTimeMillis) {
		this.workerCount = workerCount;
		this.workerTimeMillis = workerTimeMillis;
	}
	
	public <T> Set<T> apply(Collection<T> work, Function<T, Integer> fct) {
		List<MonteCarloThread<T>> threads = new ArrayList<MonteCarloThread<T>>();
		List<T> workList = new ArrayList<T>(work);
		
		for (int index = 0; index < workerCount; index++) {
			threads.add(new MonteCarloThread<T>(fct, workList));
		}
		
		for (MonteCarloThread<T> thread : threads) {
			thread.start();
		}
		
		try {
			Thread.sleep(workerTimeMillis);
		} catch (InterruptedException e) {
			throw new Error("Interruption in " + getClass().getCanonicalName(), e);
		}
		
		for (MonteCarloThread<T> thread : threads) {
			thread.interrupt();
		}
		
		for (MonteCarloThread<T> thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new Error("Interruption in " + getClass().getCanonicalName(), e);
			}
		}
		
		Set<T> bestWorkSet = new HashSet<T>();
		Integer bestWorkValue = null;
		
		for (MonteCarloThread<T> thread : threads) {
			if (thread.totalBestWorkValue != null) {
				if (bestWorkValue == null || thread.totalBestWorkValue > bestWorkValue) {
					bestWorkValue = thread.totalBestWorkValue;
					bestWorkSet.clear();
				}
				
				if (thread.totalBestWorkValue == bestWorkValue) {
					bestWorkSet.addAll(thread.totalBestWorkSet);
				}
			}
		}
		
		return bestWorkSet;
	}
	
	private static class MonteCarloThread<T> extends Thread implements Runnable {
		private Function<T, Integer> fct;
		private List<T> workList;
		private Set<T> totalBestWorkSet;
		private Integer totalBestWorkValue;
		
		public MonteCarloThread(Function<T, Integer> fct, List<T> workList) {
			this.fct = fct;
			this.workList = workList;
			
			totalBestWorkSet = new HashSet<T>();
			totalBestWorkValue = null;
		}
		
		@Override
		public void run() {
			while (Thread.interrupted()) {
				Set<T> bestWorkSet = new HashSet<T>(totalBestWorkSet);
				Integer bestWorkValue = totalBestWorkValue;
				
				for (T work : workList) {
					if (Thread.interrupted()) {
						return;
					}
					
					int workValue = fct.apply(work);
					
					if (bestWorkValue == null || workValue > bestWorkValue) {
						bestWorkValue = workValue;
						bestWorkSet.clear();
					}
					
					if (workValue == bestWorkValue) {
						bestWorkSet.add(work);
					}
				}
				
				totalBestWorkSet = new HashSet<T>(bestWorkSet);
				totalBestWorkValue = bestWorkValue;
				Thread.yield();
			}
		}
	}
}

