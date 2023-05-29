package jlx.behave.proto;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

import jlx.asal.j.JScope;
import jlx.behave.stable.DecaStableTest;
import jlx.utils.*;

public class DecaFourCoverage {
	public static class VtxPair {
		public final DecaFourVertex v1;
		public final DecaFourVertex v2;
		
		private VtxPair(DecaFourVertex v1, DecaFourVertex v2) {
			int h1 = v1.hashCode();
			int h2 = v2.hashCode();
			
			if (h1 < h2) {
				this.v1 = v1;
				this.v2 = v2;
			} else {
				this.v1 = v2;
				this.v2 = v1;
			}
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(v1, v2);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VtxPair other = (VtxPair) obj;
			return Objects.equals(v1, other.v1) && Objects.equals(v2, other.v2);
		}
	}
	
	public final DecaFourCoverage base;
	public final int cfgCount;
	public final Map<DecaFourVertex, Integer> countPerVtx;
	public final Map<VtxPair, Integer> countPerVtxPair;
	
	public DecaFourCoverage() {
		base = null;
		cfgCount = 0;
		countPerVtx = new HashMap<DecaFourVertex, Integer>();
		countPerVtxPair = new HashMap<VtxPair, Integer>();
	}
	
	public DecaFourCoverage(Set<DecaFourStateConfig> cfgs) {
		this(new DecaFourCoverage(), cfgs);
	}
	
	public DecaFourCoverage(DecaFourCoverage base, Set<DecaFourStateConfig> cfgs) {
		this.base = base;
		
		cfgCount = cfgs.size();
		countPerVtx = new HashMap<DecaFourVertex, Integer>();
		countPerVtxPair = new HashMap<VtxPair, Integer>();
		
		for (DecaFourVertex v : base.countPerVtx.keySet()) {
			countPerVtx.put(v, 0);
		}
		
		for (VtxPair pr : base.countPerVtxPair.keySet()) {
			countPerVtxPair.put(pr, 0);
		}
		
		int index = 0;
		
		for (DecaFourStateConfig cfg : cfgs) {
			Set<VtxPair> prs = new HashSet<VtxPair>();
			
			for (Map.Entry<JScope, DecaFourVertex> e : cfg.getVtxs().entrySet()) {
				HashMaps.increment(countPerVtx, e.getValue(), +1, 0);
				
				for (Map.Entry<JScope, DecaFourVertex> e2 : cfg.getVtxs().entrySet()) {
					if (e2.getValue() != e.getValue()) {
						prs.add(new VtxPair(e.getValue(), e2.getValue()));
					}
				}
			}
			
			for (VtxPair pr : prs) {
				HashMaps.increment(countPerVtxPair, pr, +1, 0);
			}
			
			if (index % 100000 == 0 ) {
				System.out.println("[metrics][" + LocalTime.now() + "] cfg = " + index + " / " + cfgs.size());
			}
			
			index++;
		}
		
		System.out.println("[metrics][" + LocalTime.now() + "] cfg = " + index + " / " + cfgs.size());
	}
	
	public void saveToFile(String filename) {
		try {
			PrintStream out = new PrintStream(filename);
			
			if (base != null) {
				int vtxCount = 0;
				int baseVtxCount = 0;
				int vtxPairCount = 0;
				int baseVtxPairCount = 0;
				int forgivingBaseVtxPairCount = 0;
				
				for (Map.Entry<DecaFourVertex, Integer> e : countPerVtx.entrySet()) {
					Integer i = base.countPerVtx.get(e.getKey());
					
					if (i != null && i > 0) {
						if (e.getValue() > 0) {
							vtxCount++;
						}
						
						baseVtxCount++;
					}
				}
				
				for (Map.Entry<VtxPair, Integer> e : countPerVtxPair.entrySet()) {
					Integer i = base.countPerVtxPair.get(e.getKey());
					
					if (i != null && i > 0) {
						if (e.getValue() > 0) {
							vtxPairCount++;
						}
						
						baseVtxPairCount++;
						
						Integer i1 = countPerVtx.get(e.getKey().v1);
						Integer i2 = countPerVtx.get(e.getKey().v2);
						
						if (i1 != null && i2 != null && i1 > 0 && i2 > 0) {
							forgivingBaseVtxPairCount++;
						}
					}
				}
				
				out.println("#found-vtxs / #base-found-vtxs = " + vtxCount + " / " + baseVtxCount);
				out.println("#found-vtx-pairs / #forgiving-base-found-vtx-pairs / #base-found-vtx-pairs = " + vtxPairCount + " / " + forgivingBaseVtxPairCount + " / " + baseVtxPairCount);
			}
			
			out.println("#input-cfgs = " + cfgCount);
			out.println("#vtxs = " + countPerVtx.size() + " (includes not found)");
			out.println("#vtx-pairs = " + countPerVtxPair.size() + " (includes not found)");
			
			for (Map.Entry<DecaFourVertex, Integer> e : countPerVtx.entrySet()) {
				String s = e.getKey().getName() + "\t" + e.getValue();
				
				if (base != null) {
					s += "\t" + base.countPerVtx.get(e.getKey());
				}
				
				out.println(s);
			}
			
			for (Map.Entry<VtxPair, Integer> e : countPerVtxPair.entrySet()) {
				String s = e.getKey().v1.getName();
				
				s += "\t" + countPerVtx.get(e.getKey().v1);
				s += "\t" + countPerVtx.get(e.getKey().v2);
				s += "\t" + e.getValue();
				
				if (base != null) {
					s += "\t" + base.countPerVtxPair.get(e.getKey());
				}
				
				out.println(s);
			}
			
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
	
	public static DecaFourCoverage createFromTests(DecaFourCoverage base, Collection<DecaStableTest> tests) {
		Set<DecaFourStateConfig> cfgs = new HashSet<DecaFourStateConfig>();
		
		for (DecaStableTest test : tests) {
			for (int index = 0; index < test.getSeq().get(0).getSeq().size(); index++) {
				cfgs.add(test.getSeq().get(0).getSeq().get(index));
			}
			
			for (int idx = 1; idx < test.getSeq().size(); idx++) {
				for (int index = 1; index < test.getSeq().get(idx).getSeq().size(); index++) {
					cfgs.add(test.getSeq().get(idx).getSeq().get(index));
				}
			}
		}
		
		return new DecaFourCoverage(base, cfgs);
	}
}

