package jlx.behave.proto;

import java.io.*;
import java.util.*;

import jlx.asal.j.JScope;
import jlx.utils.*;

public class DecaFourCoverage3 {
	public final Set<DecaFourStateConfig> coveredCfgs1;
	public final Set<DecaFourStateConfig> coveredCfgs2;
	public final Set<DecaFourVertex> vtxs1;
	public final Set<DecaFourVertex> vtxs2;
	public final Set<Pair<DecaFourVertex>> vtxPairs1;
	public final Set<Pair<DecaFourVertex>> vtxPairs2;
	
	public DecaFourCoverage3(List<DecaFourStateConfig> list, DecaFourStateMachines sms) {
		coveredCfgs1 = new HashSet<DecaFourStateConfig>(list);
		coveredCfgs2 = new HashSet<DecaFourStateConfig>(sms.configs);
		vtxs1 = extractVtxs(list);
		vtxs2 = sms.vertices;
		vtxPairs1 = extractVtxPairs(list);
		vtxPairs2 = sms.vtxPairs;
	}
	
	private Set<DecaFourVertex> extractVtxs(Collection<DecaFourStateConfig> cfgs) {
		Set<DecaFourVertex> result = new HashSet<DecaFourVertex>();
		
		for (DecaFourStateConfig cfg : cfgs) {
			result.addAll(cfg.getVtxs().values());
		}
		
		return result;
	}
	
	private Set<Pair<DecaFourVertex>> extractVtxPairs(List<DecaFourStateConfig> cfgs) {
		Set<Pair<DecaFourVertex>> result = new HashSet<Pair<DecaFourVertex>>();
		
		for (int i = 1; i < cfgs.size(); i++) {
			DecaFourStateConfig cfg1 = cfgs.get(i - 1);
			DecaFourStateConfig cfg2 = cfgs.get(i);
			
			for (Map.Entry<JScope, DecaFourVertex> e : cfg1.getVtxs().entrySet()) {
				result.add(new Pair<DecaFourVertex>(e.getValue(), cfg2.getVtxs().get(e.getKey())));
			}
		}
		
		return result;
	}
	
	private static Set<Set<Class<?>>> getClzSets(Set<DecaFourVertex> vtxs) {
		Set<Set<Class<?>>> result = new HashSet<Set<Class<?>>>();
		
		for (DecaFourVertex vtx : vtxs) {
			result.add(vtx.getSysmlClzs());
		}
		
		return result;
	}
	
	private static String clzSetToStr(Set<Class<?>> clzSet) {
		return Texts.concat(clzSet, "+", (c) -> { return c.getSimpleName(); });
	}
	
	private static Set<String> getClzSetPairs(Set<Pair<DecaFourVertex>> vtxPairs) {
		Set<String> result = new TreeSet<String>();
		
		for (Pair<DecaFourVertex> vtxPair : vtxPairs) {
			result.add(clzSetToStr(vtxPair.getElem1().getSysmlClzs()) + " -> " + clzSetToStr(vtxPair.getElem2().getSysmlClzs()));
		}
		
		return result;
	}
	
	public void saveToFile(String filename) {
		try {
			PrintStream out = new PrintStream(filename);
			
			Set<DecaFourVertex> missingVtxs = new HashSet<DecaFourVertex>(vtxs2);
			missingVtxs.removeAll(vtxs1);
			
			Set<Set<Class<?>>> clzSets1 = getClzSets(vtxs1);
			Set<Set<Class<?>>> clzSets2 = getClzSets(vtxs2);
			Set<Set<Class<?>>> missingClzSets = new HashSet<Set<Class<?>>>(clzSets2);
			missingClzSets.removeAll(clzSets1);
			
			Set<Pair<DecaFourVertex>> missingVtxPairs = new HashSet<Pair<DecaFourVertex>>(vtxPairs2);
			missingVtxPairs.removeAll(vtxPairs1);
			
			Set<String> clzSetPairs1 = getClzSetPairs(vtxPairs1);
			Set<String> clzSetPairs2 = getClzSetPairs(vtxPairs2);
			Set<String> missingClzSetPairs = new TreeSet<String>(clzSetPairs2);
			missingClzSetPairs.removeAll(clzSetPairs1);
			
			out.println("-------------- " + filename + " --------------");
			out.println("#cfgs = " + coveredCfgs1.size() + " / " + coveredCfgs2.size());
			out.println("#vtxs = " + vtxs1.size() + " / " + vtxs2.size());
			out.println("#clz-sets = " + clzSets1.size() + " / " + clzSets2.size());
			out.println("#vtxPairs = " + vtxPairs1.size() + " / " + vtxPairs2.size());
			out.println("#clz-set-pairs = " + clzSetPairs1.size() + " / " + clzSetPairs2.size());
			
			{
				out.println("-------------- Missing class sets --------------");
				
				Set<String> srcs = new TreeSet<String>();
				
				for (Set<Class<?>> clzSet : missingClzSets) {
					srcs.add(clzSetToStr(clzSet));
				}
				
				for (String src : srcs) {
					out.println(src);
				}
			}
			
			{
				out.println("-------------- Missing vertices --------------");
				
				Set<String> srcs = new TreeSet<String>();
				
				for (DecaFourVertex missingVtx : missingVtxs) {
					srcs.add(missingVtx.getName());
				}
				
				for (String src : srcs) {
					out.println(src);
				}
			}
			
			{
				out.println("-------------- Missing class set pairs --------------");
				
				for (String missingClzSetPair : missingClzSetPairs) {
					out.println(missingClzSetPair);
				}
			}
			
			{
				out.println("-------------- Missing vertex pairs --------------");
				
				Map<String, Set<String>> tgtsPerSrc = new TreeMap<String, Set<String>>();
				
				for (Pair<DecaFourVertex> missingVtxPair : missingVtxPairs) {
					if (missingVtxs.contains(missingVtxPair.getElem1()) || missingVtxs.contains(missingVtxPair.getElem2())) {
						//Empty.
					} else {
						HashMaps.inject(tgtsPerSrc, missingVtxPair.getElem1().getName(), missingVtxPair.getElem2().getName());
					}
				}
				
				for (Map.Entry<String, Set<String>> e : tgtsPerSrc.entrySet()) {
					for (String tgt : new TreeSet<String>(e.getValue())) {
						out.println(e.getKey() + " -> " + tgt);
					}
				}
			}
			
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			throw new Error(e);
		}
	}
}
