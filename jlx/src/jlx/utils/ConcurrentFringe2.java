package jlx.utils;

import java.util.*;
import java.util.function.*;

public class ConcurrentFringe2<T> {
	private List<Worker<T>> workers;
	private Map<Integer, Set<T>> fringe;
	private Map<T, T> allFound;
	private Set<T> recentlyNew;
	private Set<T> recentlyFound;
	private int nextWorkerIndex;
	private int workerCount;
	
	public ConcurrentFringe2(int workerCount) {
		this.workerCount = workerCount;
		
		workers = new ArrayList<Worker<T>>(workerCount);
		fringe = new HashMap<Integer, Set<T>>();
		
		for (int index = 0; index < workerCount; index++) {
			fringe.put(index, new HashSet<T>());
		}
		
		allFound = new HashMap<T, T>();
		recentlyNew = new HashSet<T>();
		recentlyFound = new HashSet<T>();
		nextWorkerIndex = 0;
	}
	
	public boolean add(T elem) {
		return add(elem, null);
	}
	
	public boolean add(T elem, BiConsumer<T, T> mergeFct) {
		T reprElem = allFound.get(elem);
		
		if (reprElem != null) {
			if (mergeFct != null) {
				mergeFct.accept(reprElem, elem);
			}
			
			recentlyFound.add(reprElem);
			return false;
		}
		
		allFound.put(elem, elem);
		recentlyNew.add(elem);
		recentlyFound.add(elem);
		fringe.get(nextWorkerIndex).add(elem);
		nextWorkerIndex = (nextWorkerIndex + 1) % workerCount;
		return true;
	}
	
	public boolean addAll(Set<T> elems) {
		return addAll(elems, null);
	}
	
	public boolean addAll(Set<T> elems, BiConsumer<T, T> mergeFct) {
		boolean result = false;
		
		for (T elem : elems) {
			if (add(elem, mergeFct)) {
				result = true;
			}
		}
		
		return result;
	}
	
	public Collection<T> allFound() {
		return allFound.values();
	}
	
	public Set<T> recentlyFound() {
		return recentlyFound;
	}
	
	public Set<T> recentlyNew() {
		return recentlyNew;
	}
	
	public int size() {
		return recentlyNew.size();
	}
	
	public void doWork(BiConsumer<T, Set<T>> fct) {
		workers.clear();
		
		for (int index = 0; index < workerCount; index++) {
			workers.add(new Worker<T>(index + 1, fct, fringe.get(index)));
		}
		
		for (int index = 0; index < workerCount; index++) {
			workers.get(index).start();
		}
		
		for (int index = 0; index < workerCount; index++) {
			try {
				workers.get(index).join();
			} catch (InterruptedException e) {
				throw new Error("Interruption in " + getClass().getCanonicalName(), e);
			}
		}
	}
	
	public void mergeWork(BiConsumer<T, T> mergeFct) {
		recentlyFound.clear();
		recentlyNew.clear();
		
		for (int index = 0; index < workerCount; index++) {
			fringe.get(index).clear();
		}
		
		nextWorkerIndex = 0;
		
		for (int index = 0; index < workerCount; index++) {
			addAll(workers.get(index).dest, mergeFct);
		}
	}
	
	private static class Worker<T> extends Thread implements Runnable {
		private int workerId;
		private BiConsumer<T, Set<T>> fct;
		private Set<T> elems;
		private Set<T> dest;
		
		public Worker(int workerId, BiConsumer<T, Set<T>> fct, Set<T> elems) {
			this.workerId = workerId;
			this.fct = fct;
			this.elems = elems;
			
			dest = new HashSet<T>();
		}
		
		@Override
		public void run() {
			System.out.println("Worker " + workerId + " has started (load = " + elems.size() + ")");
			dest = new HashSet<T>();
			int x = 0;
			
			for (T elem : elems) {
				fct.accept(elem, dest);
				x++;
				
				if (x % 1000 == 0) {
					System.out.println("Worker " + workerId + " makes progress (done = " + x + " / " + elems.size() + "; work = " + dest.size() + ")");
				}
			}
			
			System.out.println("Worker " + workerId + " is done (load = " + dest.size() + ")");
		}
	}
}
