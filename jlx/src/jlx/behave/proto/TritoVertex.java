package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.ops.ASALOp;
import jlx.asal.vars.ASALProperty;
import jlx.behave.State;
import jlx.common.FileLocation;
import jlx.utils.*;

public class TritoVertex {
	private TritoVertex parentVertex;
	private TritoVertex regionVertex;
	private List<TritoVertex> childVertices;
	private List<TritoVertex> initialVertices;
	
	private TritoTransition onEntry;
	private List<TritoTransition> onDo;
	private TritoTransition onExit;
	
	private DeuteroVertex legacy;
	
//	public TritoVertex(Class<?> clz, TritoVertex parentVertex) {
//		this.clz = clz;
//		this.parentVertex = parentVertex;
//		
//		onDo = new ArrayList<TritoTransition>();
//		childVertices = new ArrayList<TritoVertex>();
//		initialVertices = new ArrayList<TritoVertex>();
//	}
	
	public TritoVertex(DeuteroVertex vertex, JScope scope, Map<ASALOp, ASALProperty> helperPulsePortPerCallOp) {
		//this(vertex.getClz(), null);
		
		onDo = new ArrayList<TritoTransition>();
		childVertices = new ArrayList<TritoVertex>();
		initialVertices = new ArrayList<TritoVertex>();
		
		if (vertex.getOnEntry() != null) {
			onEntry = new TritoTransition(vertex.getOnEntry(), null, this, scope, helperPulsePortPerCallOp);
		}
		
		for (DeuteroTransition transition : vertex.getOnDo()) {
			onDo.add(new TritoTransition(transition, this, this, scope, helperPulsePortPerCallOp));
		}
		
		if (vertex.getOnExit() != null) {
			onExit = new TritoTransition(vertex.getOnExit(), this, null, scope, helperPulsePortPerCallOp);
		}
		
		legacy = vertex;
	}
	
	public int computeDepth() {
		TritoVertex v = parentVertex;
		int result = 0;
		
		while (v != null) {
			v = v.parentVertex;
			result++;
		}
		
		return result;
	}
	
	public Class<?> getSysmlClz() {
		return legacy.getSysmlClz();
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public DeuteroVertex getLegacy() {
		return legacy;
	}
	
	public TritoVertex getParentVertex() {
		return parentVertex;
	}
	
	public void setParentVertex(TritoVertex parentVertex) {
		this.parentVertex = parentVertex;
	}
	
	public TritoVertex getRegionVertex() {
		return regionVertex;
	}
	
	public void setRegionVertex(TritoVertex regionVertex) {
		this.regionVertex = regionVertex;
	}
	
	/**
	 * Returns the child vertices of this vertex.
	 * Does NOT return child vertices of child vertices!
	 */
	public List<TritoVertex> getChildVertices() {
		return childVertices;
	}
	
	public List<TritoVertex> getInitialVertices() {
		return initialVertices;
	}
	
	public TritoTransition getOnEntry() {
		return onEntry;
	}
	
	public List<TritoTransition> getOnDo() {
		return onDo;
	}
	
	public TritoTransition getOnExit() {
		return onExit;
	}
	
	/**
	 * Constructs a list with all ancestors of this vertex, including itself.
	 * The left-most element in the list will be the root container (which must be a state machine), and
	 * the right-most element in the list will be this vertex.
	 */
	public List<TritoVertex> getLineage() {
		List<TritoVertex> result;
		
		if (parentVertex != null) {
			result = parentVertex.getLineage();
		} else {
			result = new ArrayList<TritoVertex>();
		}
		
		result.add(this);
		return result;
	}
	
	public static TritoVertex getSharedParent(List<TritoVertex> lineage1, List<TritoVertex> lineage2) {
		for (int index = lineage1.size() - 1; index >= 0; index--) {
			if (lineage2.contains(lineage1.get(index))) {
				return lineage1.get(index);
			}
		}
		
		return null;
	}
	
	public static TritoVertex getSharedParent(TritoVertex v1, TritoVertex v2) {
		return getSharedParent(v1.getLineage(), v2.getLineage());
	}
	
	/**
	 * Follows all initial vertices towards the first state vertex/vertices.
	 */
	public Set<TritoVertex> getInitialStates(TritoStateMachine stateMachine, boolean permitChoices) {
		Set<TritoVertex> result = new HashSet<TritoVertex>();
		
		Set<TritoVertex> fringe = new HashSet<TritoVertex>();
		Set<TritoVertex> newFringe = new HashSet<TritoVertex>();
		fringe.addAll(initialVertices);
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (TritoVertex v : fringe) {
				Set<TritoVertex> verticesAfterOneTransition = new HashSet<TritoVertex>();
				
				for (TritoTransition t : stateMachine.transitions) {
					if (v == t.getSourceVertex()) {
						verticesAfterOneTransition.add(t.getTargetVertex());
					}
				}
				
				if (!permitChoices && verticesAfterOneTransition.size() > 1) {
					throw new Error("Choices are not permitted!");
				}
				
				for (TritoVertex nextVertex : verticesAfterOneTransition) {
					if (nextVertex.getInitialVertices().size() > 0) {
						result.add(nextVertex);
					} else {
						if (State.class.isAssignableFrom(nextVertex.getSysmlClz())) {
							result.add(nextVertex);
						} else {
							newFringe.add(nextVertex);
						}
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		return result;
	}
	
	/**
	 * Includes this vertex!
	 */
	public Set<TritoVertex> getVertices() {
		Set<TritoVertex> result = new HashSet<TritoVertex>();
		result.add(this);
		
		for (TritoVertex childVertex : getChildVertices()) {
			result.addAll(childVertex.getVertices());
		}
		
		return result;
	}
	
	/**
	 * Includes this vertex!
	 */
	public Set<TritoVertex> getChildlessVertices() {
		Set<TritoVertex> result = new HashSet<TritoVertex>();
		
		if (getChildVertices().isEmpty()) {
			result.add(this);
		} else {
			for (TritoVertex childVertex : getChildVertices()) {
				result.addAll(childVertex.getChildlessVertices());
			}
		}
		
		return result;
	}
	
	/**
	 * Children of this vertex have depth 0; their children have depth 1; and so on.
	 * Does not include this vertex!
	 */
	public Map<Integer, Set<TritoVertex>> getVerticesPerDepth() {
		Map<Integer, Set<TritoVertex>> result = new HashMap<Integer, Set<TritoVertex>>();
		result.put(0, new HashSet<TritoVertex>(getChildVertices()));
		
		for (TritoVertex childVertex : getChildVertices()) {
			
			for (Map.Entry<Integer, Set<TritoVertex>> entry : childVertex.getVerticesPerDepth().entrySet()) {
				Set<TritoVertex> vertices = result.get(entry.getKey() + 1);
				
				if (vertices == null) {
					vertices = new HashSet<TritoVertex>();
					result.put(entry.getKey() + 1, vertices);
				}
				
				vertices.addAll(entry.getValue());
			}
		}
		
		return result;
	}
	
	public Map<TritoVertex, Set<TritoVertex>> getVerticesPerRegionVertex() {
		Map<TritoVertex, Set<TritoVertex>> result = new HashMap<TritoVertex, Set<TritoVertex>>();
		
		for (TritoVertex childVertex : getChildVertices()) {
			HashMaps.inject(result, childVertex.getRegionVertex(), childVertex);
		}
		
		return result;
	}
	
	/**
	 * Gives all possible sequences in which child vertices can be exited.
	 * <b>1. Includes this vertex!</b>
	 * <b>2. All interleavings of vertices are computed, so behavior could be dependent!!</b>
	 * <b>3. Vertices are grouped per region, to reduce #interleavings.</b>
	 */
	public Set<List<TritoVertex>> getVerticesInAllExitOrders() {
		if (getChildVertices().isEmpty()) {
			if (getOnExit() != null) {
				return Collections.singleton(Collections.singletonList(this));
			} else {
				return Collections.singleton(Collections.emptyList());
			}
		}
		
		Map<TritoVertex, Set<TritoVertex>> verticesPerRegion = new HashMap<TritoVertex, Set<TritoVertex>>();
		
		for (TritoVertex childVertex : getChildVertices()) {
			HashMaps.inject(verticesPerRegion, childVertex.getRegionVertex(), childVertex);
		}
		
		Map<TritoVertex, Set<List<TritoVertex>>> exitOrdersPerRegion = new HashMap<TritoVertex, Set<List<TritoVertex>>>();
		
		// [ verticesPerRegion.size() > 0 ]
		//Per region, we do not care in which order vertices are exited, only one can be active anyway.
		//But we must still make all combinations of the possible exit sequences:
		for (Map.Entry<TritoVertex, Set<TritoVertex>> entry : verticesPerRegion.entrySet()) {
			Set<List<TritoVertex>> fringe = new HashSet<List<TritoVertex>>();
			fringe.add(new ArrayList<TritoVertex>());
			
			for (TritoVertex childVertex : entry.getValue()) { //<-- Some order.
				Set<List<TritoVertex>> newFringe = new HashSet<List<TritoVertex>>();
				
				for (List<TritoVertex> exitOrder : childVertex.getVerticesInAllExitOrders()) {
					for (List<TritoVertex> f : fringe) {
						List<TritoVertex> fCopy = new ArrayList<TritoVertex>(f);
						fCopy.addAll(exitOrder);
						newFringe.add(fCopy);
					}
				}
				
				fringe = newFringe;
			}
			
			exitOrdersPerRegion.put(entry.getKey(), fringe);
		}
		
		Set<List<TritoVertex>> interleavedExitOrders = new HashSet<List<TritoVertex>>();
		
		//Pick one exit order per region:
		for (Map<TritoVertex, List<TritoVertex>> perm : HashMaps.allCombinations(exitOrdersPerRegion)) {
			//Interleave the exit orders (we do not know which region exits first):
			interleavedExitOrders.addAll(ArrayLists.allInterleavings(perm));
		}
		
		if (onExit != null) {
			for (List<TritoVertex> interleavedExitOrder : interleavedExitOrders) {
				interleavedExitOrder.add(this);
			}
		}
		
		//[ interleavedExitOrders.size() > 0 ]
		return interleavedExitOrders;
	}
}
