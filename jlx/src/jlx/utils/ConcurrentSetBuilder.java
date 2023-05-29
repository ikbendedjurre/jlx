package jlx.utils;

import java.util.*;

public class ConcurrentSetBuilder<E> extends ConcurrentWorker {
	private final String elemName;
	private final NormMap<E> elems;
	
	public ConcurrentSetBuilder() {
		this("elems");
	}
	
	public ConcurrentSetBuilder(String elemName) {
		elems = new NormMap<E>();
		
		this.elemName = elemName;
	}
	
	public final ConcurrentSetBuilder<E> assignCopyOf(Collection<E> initElems) {
		elems.clear();
		elems.addAll(initElems);
		return this;
	}
	
	public final NormMap<E> getElems() {
		return elems;
	}
	
	@Override
	public String getSuffix() {
		return "; #" + elemName + " = " + elems.size();
	}
}

