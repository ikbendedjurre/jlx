package jlx.behave.stable.testgen;

import java.time.LocalTime;
import java.util.*;

import jlx.behave.stable.files.DecaStableFile.*;

public class DecaStableTraceSet {
	private List<Trace> traces;
	private int stepCount;
	
	private int prevSample;
	private long lastSampleTime;
	private float millisPerTrace;
	
	public DecaStableTraceSet() {
		clear();
	}
	
	public void clear() {
		traces = new ArrayList<Trace>();
		stepCount = 0;
		
		prevSample = 0;
		lastSampleTime = System.currentTimeMillis();
		millisPerTrace = 0f;
	}
	
	public List<Trace> getTraces() {
		return traces;
	}
	
	public int getStepCount() {
		return stepCount;
	}
	
	public void add(Trace t, int targetCount, int maxTargetCount) {
		if (targetCount <= prevSample) {
			throw new Error("Should not happen, at least one more target should have been reached!");
		}
		
		traces.add(t);
		stepCount += t.getTransitions().size();
		
		final long delta = System.currentTimeMillis() - lastSampleTime;
		
		if (delta > 10000) {
			final int newTgtCount = targetCount - prevSample;
			
			millisPerTrace = 1f * delta / newTgtCount;
			lastSampleTime = System.currentTimeMillis();
			prevSample = targetCount;
			
			float eta = 0.001f * (maxTargetCount - targetCount) * millisPerTrace / 60f;
			System.out.println("[" + LocalTime.now() + "] #targets = " + targetCount + " / " + maxTargetCount + " (+" + newTgtCount + "); #tests = " + traces.size() + "; #steps = " + stepCount + "; ETA = " + String.format("%.2f", eta) + "min");
		}
	}
	
	public void done(int maxTargetCount) {
		System.out.println("[" + LocalTime.now() + "] #targets = " + maxTargetCount + " / " + maxTargetCount + "; #tests = " + traces.size() + "; #steps = " + stepCount);
	}
}
