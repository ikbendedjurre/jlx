package jlx.behave.stable;

import java.io.*;
import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.*;

public class DecaStableCoverage {
	public final DecaStableStateMachine legacy;
	public final Set<DecaFourVertex> vtxs1;
	public final Set<DecaFourVertex> vtxs2;
	public final Set<Pair<DecaFourVertex>> vtxPairs1;
	public final Set<Pair<DecaFourVertex>> vtxPairs2;
	
	public DecaStableCoverage(DecaStableStateMachine sm) {
		legacy = sm;
		vtxs1 = sm.vtxs;
		vtxs2 = sm.legacy.vtxs;
		vtxPairs1 = sm.vtxPairs;
		vtxPairs2 = sm.legacy.vtxPairs;
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
	
	private static Set<Pair<Class<?>>> getSysmlClassPairs(Set<Pair<DecaFourVertex>> vtxPairs) {
		Set<Pair<Class<?>>> result = new HashSet<Pair<Class<?>>>();
		
		for (Pair<DecaFourVertex> vtxPair : vtxPairs) {
			for (Class<?> clz1 : vtxPair.getElem1().getSysmlClzs()) {
				for (Class<?> clz2 : vtxPair.getElem2().getSysmlClzs()) {
					result.add(new Pair<Class<?>>(clz1, clz2));
				}
			}
		}
		
		return result;
	}
	
	private static Set<Class<?>> getSysmlClzs(Set<DecaFourVertex> vtxs) {
		Set<Class<?>> result = new HashSet<Class<?>>();
		
		for (DecaFourVertex vtx : vtxs) {
			result.addAll(vtx.getSysmlClzs());
		}
		
		return result;
	}
	
	public void saveToFile(String filename) {
		try {
			PrintStream out = new PrintStream(filename);
			
			Set<DecaFourVertex> missingVtxs = new HashSet<DecaFourVertex>(vtxs2);
			missingVtxs.removeAll(vtxs1);
			
			Set<ProtoTransition> missingProtoTrs = new HashSet<ProtoTransition>(legacy.legacy.protoTrs);
			missingProtoTrs.removeAll(legacy.protoTrs);
			
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
			
			Set<Pair<Class<?>>> sysmlClzPairs1 = getSysmlClassPairs(vtxPairs1);
			Set<Pair<Class<?>>> sysmlClzPairs2 = getSysmlClassPairs(vtxPairs2);
			Set<Pair<Class<?>>> missingSysmlClzPairs = new HashSet<Pair<Class<?>>>(sysmlClzPairs2);
			missingSysmlClzPairs.removeAll(sysmlClzPairs1);
			
			Set<Class<?>> sysmlClzs1 = getSysmlClzs(vtxs1);
			Set<Class<?>> sysmlClzs2 = getSysmlClzs(vtxs2);
			Set<Class<?>> missingSysmlClzs = new HashSet<Class<?>>(sysmlClzs2);
			missingSysmlClzs.removeAll(sysmlClzs1);
			
			out.println("-------------- " + filename + " --------------");
			out.println("#stable-states = " + legacy.vertices.size() + " //states in the SIC-DFSM");
			out.println("#stable-transitions = " + legacy.transitions.size() + " //transitions in the SIC-DFSM");
			
			out.println("#proto-trs = " + legacy.protoTrs.size() + " / " + legacy.legacy.protoTrs.size() + " //proto transitions");
			
			out.println("#cfgs = " + legacy.coveredCfgs.size() + " / " + legacy.legacy.configs.size() + " //states in the FSM");
			out.println("#cfgPairs = " + legacy.cfgPairs.size() + " / " + legacy.legacy.getTotalTrsCount() + " //connected state pairs in the FSM");
			
			out.println("#vtxs = " + vtxs1.size() + " / " + vtxs2.size() + " //states in component FSMs");
			out.println("#vtxPairs = " + vtxPairs1.size() + " / " + vtxPairs2.size() + " //connected state pairs in component FSMs");
			
			out.println("#clz-sets = " + clzSets1.size() + " / " + clzSets2.size() + " //flattened SysML states");
			out.println("#clz-set-pairs = " + clzSetPairs1.size() + " / " + clzSetPairs2.size() + " //connected pairs of flattened SysML states");
			
			out.println("#SysML-states = " + sysmlClzs1.size() + " / " + sysmlClzs2.size() + " //SysML states");
			out.println("#SysML-state-pairs = " + sysmlClzPairs1.size() + " / " + sysmlClzPairs2.size() + " //connected pairs of SysML states");
			
			{
				out.println("-------------- Missing proto transitions --------------");
				
				for (ProtoTransition t : missingProtoTrs) {
					out.println(t.toString());
				}
			}
			
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
				out.println("-------------- Missing SysML classes --------------");
				
				for (Class<?> missingSysmlClz : missingSysmlClzs) {
					out.println(missingSysmlClz.getCanonicalName());
				}
			}
			
			{
				out.println("-------------- Missing SysML class pairs --------------");
				
				for (Pair<Class<?>> missingSysmlClzPair : missingSysmlClzPairs) {
					out.println(missingSysmlClzPair.getElem1().getCanonicalName() + " -> " + missingSysmlClzPair.getElem2().getCanonicalName());
				}
			}
			
			{
				out.println("-------------- Missing class sets --------------");
				
				for (Set<Class<?>> missingClzSet : missingClzSets) {
					out.println(Texts.concat(missingClzSet, "+", (e) -> { return e.getCanonicalName(); }));
				}
			}
			
			{
				out.println("-------------- Missing class set pairs --------------");
				
				for (String missingClzSetPair : missingClzSetPairs) {
					out.println(missingClzSetPair);
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
