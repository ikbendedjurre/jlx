package jlx.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

public class ConcurrentBFS<T> {
	private boolean debug;
	private BiConsumer<T, T> mergeFct;
	private ConcurrentHashMap<T, T> beenHere;
	private List<T> fringe;
	private ConcurrentHashMap<T, T> loops;
	private ConcurrentHashMap<T, T> newFringe;
	
	public ConcurrentBFS() {
		beenHere = new ConcurrentHashMap<T, T>();
		fringe = new ArrayList<T>();
		loops = new ConcurrentHashMap<T, T>();
		newFringe = new ConcurrentHashMap<T, T>();
	}
	
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setMergeFct(BiConsumer<T, T> mergeFct) {
		this.mergeFct = mergeFct;
	}
	
	public T getFirstOccurrence(T elem) {
		return beenHere.get(elem);
	}
	
	public void transferNewFringeToFringe() {
		fringe.clear();
		fringe.addAll(newFringe.keySet());
	}
	
	public boolean addToNewFringe(T elem) {
		T reprElem = beenHere.get(elem);
		
		if (reprElem == null) {
			beenHere.put(elem, elem);
			newFringe.put(elem, elem);
			return true;
		}
		
		if (mergeFct != null) {
			synchronized (reprElem) {
				mergeFct.accept(reprElem, elem);
			}
		}
		
		loops.put(reprElem, reprElem);
		return false;
	}
	
	public int addAllToNewFringe(Collection<T> elems) {
		int result = 0;
		
		for (T elem : elems) {
			if (addToNewFringe(elem)) {
				result++;
			}
		}
		
		return result;
	}
	
	public Collection<T> beenHereElems() {
		return beenHere.keySet();
	}
	
	public Collection<T> fringeElems() {
		return fringe;
	}
	
	public Collection<T> newFringeElems() {
		return newFringe.keySet();
	}
	
	public Collection<T> loopElems() {
		return loops.keySet();
	}
	
	public int size() {
		return fringe.size();
	}
	
	public void doNextBreadth(int workerCount, BiConsumer<T, Set<T>> fct) {
		loops.clear();
		newFringe.clear();
		
		List<Worker> workers = new ArrayList<Worker>(workerCount);
		int workPerWorker = fringe.size() / workerCount;
		int firstIndex = 0;
		
		if (workPerWorker > 0) {
			for (int index = 1; index < workerCount; index++, firstIndex += workPerWorker) {
				workers.add(new Worker(index, firstIndex, firstIndex + workPerWorker, fct));
			}
		}
		
		//Last worker does all of the last work:
		workers.add(new Worker(workerCount, firstIndex, fringe.size(), fct));
		
		for (Worker worker : workers) {
			worker.start();
		}
		
		for (Worker worker : workers) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				throw new Error("Interruption in " + getClass().getCanonicalName(), e);
			}
		}
	}
	
	private class Worker extends Thread implements Runnable {
		private int workerId;
		private int firstIndex;
		private int lastIndexExcl;
		private BiConsumer<T, Set<T>> fct;
		
		public Worker(int workerId, int firstIndex, int lastIndexExcl, BiConsumer<T, Set<T>> fct) {
			this.workerId = workerId;
			this.firstIndex = firstIndex;
			this.lastIndexExcl = lastIndexExcl;
			this.fct = fct;
		}
		
		@Override
		public void run() {
			System.out.println("Worker " + workerId + " has started");
			Set<T> dest = new HashSet<T>();
			int contribution = 0;
			int debugCounter = 0;
			
			for (int index = firstIndex; index < lastIndexExcl; index++) {
				fct.accept(fringe.get(index), dest);
				
				if (dest.size() > 100) {
					contribution += ConcurrentBFS.this.addAllToNewFringe(dest);
					dest.clear();
				}
				
				if (debug && debugCounter >= 500) {
					int x = (index - firstIndex + 1);
					int y = (lastIndexExcl - firstIndex);
					System.out.println("Worker " + workerId + " is at " + x + " / " + y + " (" + (100 * x / y) + "%); #contributions = " + contribution);
				}
				
				debugCounter++;
			}
			
			if (dest.size() > 0) {
				contribution += ConcurrentBFS.this.addAllToNewFringe(dest);
			}
			
			System.out.println("Worker " + workerId + " is done (contributed " + contribution + " new elements)");
		}
	}
}
