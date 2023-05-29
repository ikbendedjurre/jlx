package jlx.behave.proto;

import java.util.*;

import jlx.models.UnifyingBlock.ReprPort;
import jlx.utils.*;

public class PulsePackMapIO {
	private final PulsePackMap i;
	private final List<PulsePackMap> o;
	private final int hashCode;
	
	public PulsePackMapIO(PulsePackMap i, PulsePackMap o) {
		this(i, Collections.singletonList(o));
	}
	
	public PulsePackMapIO(PulsePackMap i, List<PulsePackMap> o) {
		this.i = i;
		this.o = o;
		
		hashCode = Objects.hash(i, o);
	}
	
	public PulsePackMap getI() {
		return i;
	}
	
	public List<PulsePackMap> getO() {
		return o;
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
		PulsePackMapIO other = (PulsePackMapIO) obj;
		return Objects.equals(i, other.i) && Objects.equals(o, other.o);
	}
	
	public static Set<PulsePackMapIO> trim(Set<PulsePackMapIO> maps, Map<ReprPort, Set<PulsePack>> pvsPerPort) {
		Map<List<PulsePackMap>, Set<PulsePackMap>> perO = new HashMap<List<PulsePackMap>, Set<PulsePackMap>>();
		
		for (PulsePackMapIO map : maps) {
			HashMaps.inject(perO, map.getO(), map.getI());
		}
		
		Set<PulsePackMapIO> result = new HashSet<PulsePackMapIO>();
		
		for (Map.Entry<List<PulsePackMap>, Set<PulsePackMap>> e : perO.entrySet()) {
			for (PulsePackMap v : PulsePackMap.trim(e.getValue(), pvsPerPort, Dir.IN)) {
				result.add(new PulsePackMapIO(v, e.getKey()));
			}
		}
		
		return result;
	}
}

