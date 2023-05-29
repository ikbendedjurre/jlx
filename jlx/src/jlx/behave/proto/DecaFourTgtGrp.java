package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;

public class DecaFourTgtGrp implements Comparable<DecaFourTgtGrp> {
	private JScope scope;
	private Set<DecaFourVertex> vtxs;
	private int id;
	
	public DecaFourTgtGrp(JScope scope, Set<DecaFourVertex> vtxs, int id) {
		this.scope = scope;
		this.vtxs = vtxs;
		this.id = id;
	}
	
	public JScope getScope() {
		return scope;
	}
	
	public Set<DecaFourVertex> getVtxs() {
		return vtxs;
	}
	
	public int getId() {
		return id;
	}
	
	@Override
	public int compareTo(DecaFourTgtGrp other) {
		return id - other.id;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(scope, id, vtxs);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DecaFourTgtGrp)) {
			return false;
		}
		DecaFourTgtGrp other = (DecaFourTgtGrp) obj;
		return scope == other.scope && id == other.id && Objects.equals(vtxs, other.vtxs);
	}
}

