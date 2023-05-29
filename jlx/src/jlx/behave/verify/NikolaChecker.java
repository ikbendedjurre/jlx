package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.Texts;

public class NikolaChecker {
	private final NikolaGraph graph;
	
	public NikolaChecker(NikolaGraph graph) {
		this.graph = graph;
		
		for (NikolaNode node : graph.getNodes()) {
			for (Map.Entry<PulsePackMap, Set<NikolaNode>> e : node.getOutgoing().entrySet()) {
				List<Explore> path = checkTransition(node, e.getKey());
				
				if (path != null) {
					for (int index = 1; index < path.size(); index++) {
						Explore x = path.get(index - 1);
						System.out.println(index + ": " + toString(x.node.getSysmlClzs()));
						System.out.println(empty(index + ": ") + x.node.getOutput().toString());
						System.out.println(" => " + x.input.toString());
					}
					
					{
						int index = path.size();
						Explore x = path.get(index - 1);
						System.out.println(index + ": " + toString(x.node.getSysmlClzs()));
						System.out.println(empty(index + ": ") + x.node.getOutput().toString());
					}
					
					throw new Error("Cycle!");
				}
			}
		}
	}
	
	private static class Explore {
		private final NikolaNode node;
		private final PulsePackMap input;
		private final int hashCode;
		
		private Explore(NikolaNode node, PulsePackMap input) {
			this.node = node;
			this.input = input;
			
			hashCode = Objects.hash(input, node);
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
			Explore other = (Explore) obj;
			return Objects.equals(input, other.input) && Objects.equals(node, other.node);
		}
	}
	
	private List<Explore> checkTransition(NikolaNode node, PulsePackMap input) {
		Set<List<Explore>> fringe = new HashSet<List<Explore>>();
		Set<List<Explore>> newFringe = new HashSet<List<Explore>>();
		fringe.add(Collections.singletonList(new Explore(node, input)));
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (List<Explore> path : fringe) {
				Explore last = path.get(path.size() - 1);
				
				for (Map.Entry<PulsePackMap, Set<NikolaNode>> e : last.node.getOutgoing().entrySet()) {
					if (last.input.canCombine(e.getKey())) {
						PulsePackMap newInput = last.input.combine(e.getKey());
						
						for (NikolaNode succ : e.getValue()) {
							Explore newExplore = new Explore(succ, newInput);
							List<Explore> newPath = new ArrayList<Explore>(path);
							newPath.add(newExplore);
							
							int xIndex = path.indexOf(newExplore);
							
							if (xIndex >= 0 && xIndex < path.size() - 1) {
								return newPath;
							}
							
							newFringe.add(newPath);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return null;
	}
	
	private static String empty(String s) {
		return " ".repeat(s.length());
	}
	
	private static String toString(Map<String, Set<Class<?>>> clzs) {
		List<String> elems = new ArrayList<String>();
		
		for (Map.Entry<String, Set<Class<?>>> e : clzs.entrySet()) {
			List<String> items = new ArrayList<String>();
			
			for (Class<?> c : e.getValue()) {
				items.add(c.getSimpleName());
			}
			
			elems.add(e.getKey() + " = " + Texts.concat(items, " / "));
		}
		
		return Texts.concat(elems, " | ");
	}
	
	public NikolaGraph getGraph() {
		return graph;
	}
}
