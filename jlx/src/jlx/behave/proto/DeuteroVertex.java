package jlx.behave.proto;

import java.util.*;
import java.util.function.Predicate;

import jlx.asal.j.JScope;
import jlx.asal.parsing.api.ASALStatement;
import jlx.behave.*;
import jlx.common.FileLocation;
import jlx.common.reflection.*;

public class DeuteroVertex {
	private DeuteroVertex parentVertex;
	private DeuteroVertex regionVertex;
	private List<DeuteroVertex> childVertices;
	private List<DeuteroVertex> initialVertices;
	
	private DeuteroTransition onEntry;
	private List<DeuteroTransition> onDo;
	private DeuteroTransition onExit;
	
	private ProtoVertex legacy;
	
	public DeuteroVertex(ProtoVertex vertex, JScope scope) throws ModelException {
		if (vertex.onEntry != null) {
			onEntry = vertex.onEntry.parse(scope);
			onEntry.setTargetVertex(this);
		}
		
		onDo = new ArrayList<DeuteroTransition>();
		
		for (ProtoTransition transition : vertex.onDo) {
			DeuteroTransition newTransition = transition.parse(scope);
			newTransition.setSourceVertex(this);
			newTransition.setTargetVertex(this);
			onDo.add(newTransition);
		}
		
		if (vertex.onExit != null) {
			onExit = vertex.onExit.parse(scope);
			onExit.setSourceVertex(this);
		}
		
		childVertices = new ArrayList<DeuteroVertex>();
		initialVertices = new ArrayList<DeuteroVertex>();
		legacy = vertex;
	}
	
	public Class<?> getSysmlClz() {
		return legacy.sysmlClz;
	}
	
	public FileLocation getFileLocation() {
		return legacy.getFileLocation();
	}
	
	public ProtoVertex getLegacy() {
		return legacy;
	}
	
	public DeuteroVertex getParentVertex() {
		return parentVertex;
	}
	
	public DeuteroVertex getRegionVertex() {
		return regionVertex;
	}
	
	public void setParentVertex(DeuteroVertex parentVertex) {
		this.parentVertex = parentVertex;
	}
	
	public List<DeuteroVertex> getChildVertices() {
		return childVertices;
	}
	
	public List<DeuteroVertex> getInitialVertices() {
		return initialVertices;
	}
	
	public DeuteroTransition getOnEntry() {
		return onEntry;
	}
	
	public List<DeuteroTransition> getOnDo() {
		return onDo;
	}
	
	public DeuteroTransition getOnExit() {
		return onExit;
	}
	
	public void setRegionVertex(DeuteroVertex regionVertex) throws ClassReflectionException {
		if (this.regionVertex != null) {
			throw new ClassReflectionException(getSysmlClz(), "Should not be reachable from more than 1 " + InitialState.class.getCanonicalName() + "!");
		}
		
		this.regionVertex = regionVertex;
	}
	
	/**
	 * Constructs a list with all ancestors of this vertex, including itself.
	 * The left-most element in the list will be the root container (which must be a state machine), and
	 * the right-most element in the list will be this vertex.
	 */
	public List<DeuteroVertex> getLineage() {
		List<DeuteroVertex> result;
		
		if (parentVertex != null) {
			result = parentVertex.getLineage();
		} else {
			result = new ArrayList<DeuteroVertex>();
		}
		
		result.add(this);
		return result;
	}
	
	/**
	 * Does NOT include this vertex!
	 */
	public Set<DeuteroVertex> getVertices() {
		Set<DeuteroVertex> result = new HashSet<DeuteroVertex>();
		
		for (DeuteroVertex childVertex : getChildVertices()) {
			result.addAll(childVertex.getVertices());
			result.add(childVertex);
		}
		
		return result;
	}
	
	public Map<DeuteroVertex, List<ASALStatement>> getExitBehaviourPerChildlessDescendantVertex(Predicate<DeuteroVertex> filter) {
		Map<DeuteroVertex, List<ASALStatement>> result = new HashMap<DeuteroVertex, List<ASALStatement>>();
		addExitBehaviourPerChildlessDescendantVertex(result, new ArrayList<ASALStatement>(), filter);
		return result;
	}
	
	private void addExitBehaviourPerChildlessDescendantVertex(Map<DeuteroVertex, List<ASALStatement>> destination, List<ASALStatement> exitBehaviourSoFar, Predicate<DeuteroVertex> filter) {
		List<ASALStatement> exitBehaviour = new ArrayList<ASALStatement>(exitBehaviourSoFar);
		
		if (onExit != null) {
			exitBehaviour.add(0, onExit.getStatement());
		}
		
		if (childVertices.size() > 0) {
			for (DeuteroVertex childVertex : childVertices) {
				childVertex.addExitBehaviourPerChildlessDescendantVertex(destination, exitBehaviour, filter);
			}
		} else {
			if (filter.test(this)) {
				destination.put(this, exitBehaviour);
			}
		}
	}
	
	public Map<DeuteroVertex, List<ASALStatement>> getExitBehaviourPerFinalVertex() {
		Map<DeuteroVertex, List<ASALStatement>> result = new HashMap<DeuteroVertex, List<ASALStatement>>();
		List<ASALStatement> exitBehaviour = new ArrayList<ASALStatement>();
		
		if (onExit != null) {
			exitBehaviour.add(0, onExit.getStatement());
		}
		
		if (childVertices.size() > 0) {
			for (DeuteroVertex childVertex : childVertices) {
				if (FinalState.class.isAssignableFrom(childVertex.getSysmlClz())) {
					result.put(childVertex, exitBehaviour); //There should be only one!
				}
			}
		} else {
			result.put(this, exitBehaviour);
		}
		
		return result;
	}
	
	public Set<DeuteroVertex> getChildlessDescendantVertices(Predicate<DeuteroVertex> filter) {
		Set<DeuteroVertex> result = new HashSet<DeuteroVertex>();
		addChildlessDescendantVertices(filter, result);
		return result;
	}
	
	private void addChildlessDescendantVertices(Predicate<DeuteroVertex> filter, Set<DeuteroVertex> destination) {
		if (childVertices.size() > 0) {
			for (DeuteroVertex childVertex : childVertices) {
				childVertex.addChildlessDescendantVertices(filter, destination);
			}
		} else {
			if (filter.test(this)) {
				destination.add(this);
			}
		}
	}
}
