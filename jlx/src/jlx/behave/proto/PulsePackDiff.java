package jlx.behave.proto;

import java.util.Map;

import jlx.models.UnifyingBlock.ReprPort;

public class PulsePackDiff {
	public final static PulsePackDiff NO_DIFF = new PulsePackDiff(null);
	public final static PulsePackDiff TOO_MANY_DIFFS = new PulsePackDiff(null);
	
	private final ReprPort adapterLabelPort;
	
	private PulsePackDiff(ReprPort adapterLabelPort) {
		this.adapterLabelPort = adapterLabelPort;
	}
	
	public ReprPort getAdapterLabelPort() {
		return adapterLabelPort;
	}
	
	/**
	 * Baseline parameter must contain AT LEAST the keys of the primary map!
	 */
	public static PulsePackDiff compute(PulsePackMap map, PulsePackMap baselineMap) {
		PulsePackDiff result = NO_DIFF;
		
		for (Map.Entry<ReprPort, PulsePack> e : map.getPackPerPort().entrySet()) {
			if (e.getKey().isPortToEnvironment()) {
				if (e.getValue().isPulse()) {
					if (e.getValue().isTruePulse()) {
						if (result.getAdapterLabelPort() != null) {
							if (result.getAdapterLabelPort() != e.getKey().getAdapterLabelPort()) {
								return TOO_MANY_DIFFS;
							}
						} else {
							result = new PulsePackDiff(e.getKey().getAdapterLabelPort());
						}
					}
				} else {
					if (!e.getValue().equals(baselineMap.getPackPerPort().get(e.getKey()))) {
						if (result.getAdapterLabelPort() != null) {
							if (result.getAdapterLabelPort() != e.getKey().getAdapterLabelPort()) {
								return TOO_MANY_DIFFS;
							}
						} else {
							result = new PulsePackDiff(e.getKey().getAdapterLabelPort());
						}
					}
				}
			}
		}
		
		return result;
	}
}
