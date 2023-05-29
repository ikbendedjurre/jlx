package jlx.behave.verify;

import java.util.*;

public class IntRange {
	private final int min;
	private final int max;
	
	public IntRange(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	public boolean canAdd(int value) {
		return value >= min - 1 && value <= max + 1;
	}
	
	public boolean contains(int value) {
		return value >= min && value <= max;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(max, min);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntRange other = (IntRange) obj;
		return max == other.max && min == other.min;
	}
}

