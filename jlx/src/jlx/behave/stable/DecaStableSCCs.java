package jlx.behave.stable;

import java.time.LocalTime;
import java.util.*;

public class DecaStableSCCs {
	private DecaStableStateMachine stableSm;
	private Set<DecaStableVertex> beenHere;
	private List<DecaStableVertex> orderedVtxs;
	private Map<DecaStableVertex, SCC> sccPerRootVtx;
	private Map<DecaStableVertex, SCC> sccPerVtx;
	
	public static class SCC {
		private DecaStableVertex rootVtx;
		private Set<DecaStableVertex> vtxs;
		
		private SCC(DecaStableVertex rootCfg) {
			this.rootVtx = rootCfg;
			
			vtxs = new HashSet<DecaStableVertex>();
			vtxs.add(rootCfg);
		}
		
		public DecaStableVertex getRootVtx() {
			return rootVtx;
		}
		
		public Set<DecaStableVertex> getVtxs() {
			return vtxs;
		}
	}
	
	public DecaStableSCCs(DecaStableStateMachine model) {
		this.stableSm = model;
		
		beenHere = new HashSet<DecaStableVertex>();
		orderedVtxs = new ArrayList<DecaStableVertex>();
		sccPerRootVtx = new HashMap<DecaStableVertex, SCC>();
		sccPerVtx = new HashMap<DecaStableVertex, SCC>();
		
		doVisiting();
		doAssigning();
	}
	
	private void doVisiting() {
		int cix = 0;
		
		for (DecaStableVertex c : stableSm.vertices) {
			if (cix % 10000 == 0) {
				System.out.println("[SCCs][" + LocalTime.now() + "] Visited " + cix + " of " + stableSm.vertices.size() + " stabilized vertices");
			}
			
			visit(c);
			cix++;
		}
		
		System.out.println("[SCCs][" + LocalTime.now() + "] Visited all of " + stableSm.vertices.size() + " stabilized vertices");
	}
	
	private void doAssigning() {
		for (int index = 0; index < orderedVtxs.size(); index++) {
			if (index % 10000 == 0) {
				System.out.println("[SCCs][" + LocalTime.now() + "] Assigned roots to " + index + " of " + stableSm.vertices.size() + " stabilized vertices (#SCCs = " + sccPerRootVtx.size() + ")");
			}
			
			DecaStableVertex c = orderedVtxs.get(index);
			assign(c, c);
		}
		
		System.out.println("[SCCs][" + LocalTime.now() + "] Assigned roots to all of " + stableSm.vertices.size() + " stabilized vertices (#roots = " + sccPerRootVtx.size() + ")");
	}
	
	private void visit(DecaStableVertex c) {
		if (beenHere.add(c)) {
			Stack<DecaStableVertex> stack = new Stack<DecaStableVertex>();
			stack.push(c);
			
			while (stack.size() > 0) {
				boolean pop = true;
				
				for (DecaStableVertex succ : stack.peek().getSuccs()) {
					if (beenHere.add(succ)) {
						stack.push(succ);
						pop = false;
						break;
					}
				}
				
				if (pop) {
					orderedVtxs.add(0, stack.pop());
				}
			}
		}
	}
	
	private void assign(DecaStableVertex c, DecaStableVertex rootVtx) {
		if (!sccPerVtx.containsKey(c)) {
			SCC scc = sccPerRootVtx.get(rootVtx);
			
			if (scc == null) {
				scc = new SCC(rootVtx);
				sccPerRootVtx.put(rootVtx, scc);
			}
			
			scc.vtxs.add(c);
			sccPerVtx.put(c, scc);
			
			Set<DecaStableVertex> fringe = new HashSet<DecaStableVertex>();
			Set<DecaStableVertex> newFringe = new HashSet<DecaStableVertex>();
			fringe.add(c);
			
			while (fringe.size() > 0) {
				newFringe.clear();
				
				for (DecaStableVertex cfg : fringe) {
					for (DecaStableVertex pred : cfg.getPreds()) {
						if (!sccPerVtx.containsKey(pred)) {
							sccPerVtx.put(pred, scc);
							newFringe.add(pred);
							scc.vtxs.add(pred);
						}
					}
				}
				
				fringe.clear();
				fringe.addAll(newFringe);
			}
		}
	}
	
	public DecaStableStateMachine getModel() {
		return stableSm;
	}
	
	public Set<DecaStableVertex> getBeenHere() {
		return beenHere;
	}
	
	public List<DecaStableVertex> getOrderedCfgs() {
		return orderedVtxs;
	}
	
	public Map<DecaStableVertex, SCC> getSccPerRootCfg() {
		return sccPerRootVtx;
	}
	
	public Map<DecaStableVertex, SCC> getSccPerCfg() {
		return sccPerVtx;
	}
}

