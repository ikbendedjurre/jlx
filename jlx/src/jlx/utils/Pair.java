package jlx.utils;

import java.util.Objects;

public class Pair<T> {
	private final T elem1;
	private final T elem2;
	private final int hashCode;
	
	public Pair(T elem1, T elem2) {
		this.elem1 = elem1;
		this.elem2 = elem2;
		
		hashCode = Objects.hash(elem1, elem2);
	}
	
	public T getElem1() {
		return elem1;
	}
	
	public T getElem2() {
		return elem2;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?> other = (Pair<?>) obj;
		return Objects.equals(elem1, other.elem1) && Objects.equals(elem2, other.elem2);
	}
}
