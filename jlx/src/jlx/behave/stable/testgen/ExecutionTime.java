package jlx.behave.stable.testgen;

import java.util.*;

import jlx.behave.stable.files.*;
import jlx.behave.stable.files.DecaStableFile.*;
import jlx.utils.*;

public class ExecutionTime {
	private final long noTimeoutExecutionTime;
	private final long hiddenTimeoutExecutionTime;
	
	private int noTimeoutCount;
	private int hiddenTimeoutCount;
	private Map<Port, Integer> countPerTimeoutPort;
	
	public ExecutionTime(long noTimeoutExecutionTime, long hiddenTimeoutExecutionTime) {
		this.noTimeoutExecutionTime = noTimeoutExecutionTime;
		this.hiddenTimeoutExecutionTime = hiddenTimeoutExecutionTime;
		
		noTimeoutCount = 0;
		hiddenTimeoutCount = 0;
		countPerTimeoutPort = new HashMap<Port, Integer>();
	}
	
	public void add(DecaStableFileReader file) {
		for (Trace trace : file.getTests()) {
			add(trace);
		}
	}
	
	public void add(Trace trace) {
		for (Transition t : trace.getTransitions()) {
			add(t);
		}
	}
	
	public void add(Transition t) {
		if (t.getInputChanges().isHiddenTimerTrigger()) {
			hiddenTimeoutCount++;
			return;
		}
		
		if (t.getInputChanges().getDurationPort() != null) {
			HashMaps.increment(countPerTimeoutPort, t.getInputChanges().getDurationPort(), 1, 0);
			return;
		}
		
		noTimeoutCount++;
	}
	
	public long computeMillis() {
		long millis = 0L;
		millis += noTimeoutExecutionTime * noTimeoutCount;
		millis += hiddenTimeoutExecutionTime * hiddenTimeoutCount;
		
		for (Map.Entry<Port, Integer> e : countPerTimeoutPort.entrySet()) {
			millis += e.getValue() * e.getKey().getExecutionTime();
		}
		
		return millis;
	}
	
	public void print() {
		System.out.println("#steps-default = " + noTimeoutCount);
		
		for (Map.Entry<Port, Integer> e : countPerTimeoutPort.entrySet()) {
			System.out.println("#steps(" + e.getKey().getName() + ", " + e.getKey().getExecutionTime() + "m) = " + e.getValue());
		}
		
		long millis = computeMillis();
		
		System.out.println("Execution time");
		System.out.println("\tin Millis:  " + millis);
		System.out.println("\tin Seconds: " + (millis / 1000));
		System.out.println("\tin Minutes: " + String.format("%.2f", 0.001f * millis / 60f));
		System.out.println("\tin Hours:   " + String.format("%.3f", 0.001f * millis / 60f / 60f));
	}
}

