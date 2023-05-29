package jlx.utils;

import java.util.*;

public class WallClock {
	private static Map<String, Long> totalPerClock = new HashMap<String, Long>();
	private static Map<String, Long> timePerClock = new HashMap<String, Long>();
	
	public static void reset() {
		totalPerClock.clear();
		timePerClock.clear();
	}
	
//	private static long getTime() {
//		return System.currentTimeMillis();
//	}
	
	public static void tick(String clock) {
//		timePerClock.put(clock, getTime());
	}
	
	public static void tock(String clock) {
//		final long t = getTime();
//		final long dt = t - timePerClock.getOrDefault(clock, t);
//		totalPerClock.put(clock, totalPerClock.getOrDefault(clock, 0L) + dt);
	}
	
	private static String clockToStr(long t) {
		return (0.001f * t) + "s";
	}
	
	public static void printAll() {
		for (Map.Entry<String, Long> e : totalPerClock.entrySet()) {
			System.out.println("clock(" + e.getKey() + ") = " + clockToStr(e.getValue()));
		}
	}
	
	public static String str() {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<String, Long> e : totalPerClock.entrySet()) {
			elems.add(e.getKey() + " = " + clockToStr(e.getValue()));
		}
		
		return Texts.concat(elems, "; ");
	}
}
