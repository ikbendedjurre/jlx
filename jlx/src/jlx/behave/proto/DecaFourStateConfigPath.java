package jlx.behave.proto;

import java.util.*;

public class DecaFourStateConfigPath {
	private final List<DecaFourStateConfig> cfgs;
	private final Set<ProtoTransition> protoTrs;
	private final DecaFourStateConfig lastCfg;
	private final int hashCode;
	
	public DecaFourStateConfigPath(DecaFourStateConfigPath source, Set<ProtoTransition> sourceProtoTrs) {
		cfgs = new ArrayList<DecaFourStateConfig>(source.cfgs);
		protoTrs = new HashSet<ProtoTransition>(sourceProtoTrs);
		lastCfg = source.lastCfg;
		
		hashCode = Objects.hash(cfgs, protoTrs);
	}
	
	public DecaFourStateConfigPath(DecaFourStateConfig startCfg) {
		cfgs = new ArrayList<DecaFourStateConfig>();
		cfgs.add(startCfg);
		protoTrs = new HashSet<ProtoTransition>();
		lastCfg = startCfg;
		
		hashCode = Objects.hash(cfgs, protoTrs);
	}
	
	public DecaFourStateConfigPath(DecaFourStateConfigPath source, DecaFourStateConfig nextCfg, Set<ProtoTransition> nextProtoTrs) {
		cfgs = new ArrayList<DecaFourStateConfig>(source.cfgs);
		cfgs.add(nextCfg);
		protoTrs = new HashSet<ProtoTransition>(source.protoTrs);
		protoTrs.addAll(nextProtoTrs);
		lastCfg = nextCfg;
		
		hashCode = Objects.hash(cfgs, protoTrs);
	}
	
	public List<DecaFourStateConfig> getCfgs() {
		return cfgs;
	}
	
	public Set<ProtoTransition> getProtoTrs() {
		return protoTrs;
	}
	
	public DecaFourStateConfig getLastCfg() {
		return lastCfg;
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
		DecaFourStateConfigPath other = (DecaFourStateConfigPath) obj;
		return Objects.equals(cfgs, other.cfgs) && Objects.equals(protoTrs, other.protoTrs);
	}
}

