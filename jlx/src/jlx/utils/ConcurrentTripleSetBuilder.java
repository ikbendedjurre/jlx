package jlx.utils;

import java.util.*;

public class ConcurrentTripleSetBuilder<E1, E2, E3> extends ConcurrentWorker {
	private final String elemName1;
	private final String elemName2;
	private final String elemName3;
	private final NormMap<E1> elems1;
	private final NormMap<E2> elems2;
	private final NormMap<E3> elems3;
	
	public ConcurrentTripleSetBuilder() {
		this("elems1", "elems2", "elems3");
	}
	
	public ConcurrentTripleSetBuilder(String elemName1, String elemName2, String elemName3) {
		elems1 = new NormMap<E1>();
		elems2 = new NormMap<E2>();
		elems3 = new NormMap<E3>();
		
		this.elemName1 = elemName1;
		this.elemName2 = elemName2;
		this.elemName3 = elemName3;
	}
	
	public final ConcurrentTripleSetBuilder<E1, E2, E3> assignCopyOf(Collection<E1> initElems1, Collection<E2> initElems2, Collection<E3> initElems3) {
		elems1.clear();
		elems1.addAll(initElems1);
		elems2.clear();
		elems2.addAll(initElems2);
		elems3.clear();
		elems3.addAll(initElems3);
		return this;
	}
	
	public final NormMap<E1> getElems1() {
		return elems1;
	}
	
	public final NormMap<E2> getElems2() {
		return elems2;
	}
	
	public final NormMap<E3> getElems3() {
		return elems3;
	}
	
	@Override
	public String getSuffix() {
		return "; #" + elemName1 + " = " + elems1.size() + "; #" + elemName2 + " = " + elems2.size() + "; #" + elemName3 + " = " + elems3.size();
	}
}

