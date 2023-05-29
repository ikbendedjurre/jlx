package jlx.utils;

import java.util.*;

public class ConcurrentDoubleSetBuilder<E1, E2> extends ConcurrentWorker {
	private final String elemName1;
	private final String elemName2;
	private final NormMap<E1> elems1;
	private final NormMap<E2> elems2;
	
	public ConcurrentDoubleSetBuilder() {
		this("elems1", "elems2");
	}
	
	public ConcurrentDoubleSetBuilder(String elemName1, String elemName2) {
		elems1 = new NormMap<E1>();
		elems2 = new NormMap<E2>();
		
		this.elemName1 = elemName1;
		this.elemName2 = elemName2;
	}
	
	public final ConcurrentDoubleSetBuilder<E1, E2> assignCopyOf(Collection<E1> initElems1, Collection<E2> initElems2) {
		elems1.clear();
		elems1.addAll(initElems1);
		elems2.clear();
		elems2.addAll(initElems2);
		return this;
	}
	
	public final NormMap<E1> getElems1() {
		return elems1;
	}
	
	public final NormMap<E2> getElems2() {
		return elems2;
	}
	
	@Override
	public String getSuffix() {
		return "; #" + elemName1 + " = " + elems1.size() + "; #" + elemName2 + " = " + elems2.size();
	}
}

