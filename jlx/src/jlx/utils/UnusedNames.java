package jlx.utils;

import java.util.*;

public class UnusedNames {
	private Set<String> usedNames;
	
	public UnusedNames() {
		usedNames = new HashSet<String>();
	}
	
	public final void clear() {
		usedNames.clear();
	}
	
	public final void add(String name) {
		usedNames.add(name);
	}
	
	public final void addAll(Collection<String> names) {
		usedNames.addAll(names);
	}
	
	public final void addAll(UnusedNames other) {
		usedNames.addAll(other.usedNames);
	}
	
	public final Set<String> getUsedNames() {
		return new HashSet<String>(usedNames);
	}
	
	public final String generateUnusedName(String base) {
		String attempt = base;
		int nr = 1;
		
		while (usedNames.contains(attempt)) {
			attempt = getNameAttempt(base, nr);
			nr++;
		}
		
		usedNames.add(attempt);
		return attempt;
	}
	
	protected String getNameAttempt(String base, int nr) {
		return base + "" + nr;
	}
}
