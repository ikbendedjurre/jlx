package jlx.behave.proto;

import java.util.*;

import jlx.asal.j.JScope;
import jlx.asal.ops.ASALOp;
import jlx.asal.parsing.api.ASALFinalized;
import jlx.behave.*;
import jlx.common.*;
import jlx.common.reflection.*;
import jlx.utils.*;

/**
 * In this machine, ASAL has been parsed and validated.
 */
public class DeuteroStateMachine {
	public final JScope scope;
	public final StateMachine instance;
	public final DeuteroVertex rootVertex;
	public final Set<DeuteroVertex> vertices;
	public final Set<DeuteroVertex> initialVertices;
	public final Set<DeuteroTransition> transitions;
	
	public final ProtoStateMachine legacy;
	
	public DeuteroStateMachine(ProtoStateMachine source, JScope scope) throws ClassReflectionException {
		instance = source.instance;
		legacy = source;
		
		this.scope = scope;
		
		//Parse the operations:
		for (ASALOp op : scope.getOperationPerName().values()) {
			try {
				op.initBody(op.createScope(scope));
			} catch (ModelException e) {
				throw new ClassReflectionException(instance.getClass(), e);
			}
		}
		
		//First copy vertices blindly:
		vertices = new HashSet<DeuteroVertex>();
		Map<ProtoVertex, DeuteroVertex> newVertexPerOldVertex = new HashMap<ProtoVertex, DeuteroVertex>();
		
		for (ProtoVertex v : source.vertices.values()) {
			try {
				DeuteroVertex w = new DeuteroVertex(v, scope);
				newVertexPerOldVertex.put(v, w);
				vertices.add(w);
			} catch (ModelException e) {
				throw new ClassReflectionException(v.sysmlClz, e);
			}
		}
		
		//Resolve references to root/initial/parent/child vertices:
		rootVertex = newVertexPerOldVertex.get(source.rootVertex);
		initialVertices = new HashSet<DeuteroVertex>();
		
		for (ProtoVertex initialVertex : source.initialVertices) {
			initialVertices.add(newVertexPerOldVertex.get(initialVertex));
		}
		
		for (Map.Entry<ProtoVertex, DeuteroVertex> entry : newVertexPerOldVertex.entrySet()) {
			for (ProtoVertex initialVertex : entry.getKey().initialVertices) {
				entry.getValue().getInitialVertices().add(newVertexPerOldVertex.get(initialVertex));
			}
			
			if (entry.getKey().parentVertex != null) {
				entry.getValue().setParentVertex(newVertexPerOldVertex.get(entry.getKey().parentVertex));
			}
			
			for (ProtoVertex childVertex : entry.getKey().childVertices) {
				entry.getValue().getChildVertices().add(newVertexPerOldVertex.get(childVertex));
			}
		}
		
		//Copy transitions:
		transitions = new HashSet<DeuteroTransition>();
		
		for (ProtoTransition transition : source.transitions) {
			try {
				//Parse the ASAL code:
				DeuteroTransition newTransition = transition.parse(scope);
				newTransition.setSourceVertex(newVertexPerOldVertex.get(transition.sourceState));
				newTransition.setTargetVertex(newVertexPerOldVertex.get(transition.targetState));
				newTransition.setIsLocal(transition.isLocal);
				transitions.add(newTransition);
			} catch (ModelException e) {
				throw new ClassReflectionException(instance.getClass(), e);
			}
		}
		
		//Set region vertices:
		for (DeuteroVertex v : vertices) { //(vertices includes rootVertex, so the entire state machine is covered)
			Set<DeuteroVertex> stayHere = v.getVertices();
			
			for (DeuteroVertex iv : v.getInitialVertices()) {
				Set<DeuteroVertex> reachedVertices = new HashSet<DeuteroVertex>();
				reachedVertices.add(iv);
				
				Set<DeuteroVertex> fringe = new HashSet<DeuteroVertex>();
				Set<DeuteroVertex> newFringe = new HashSet<DeuteroVertex>();
				fringe.add(iv);
				
				while (fringe.size() > 0) {
					newFringe.clear();
					
					for (DeuteroVertex f : fringe) {
						for (DeuteroTransition t : transitions) {
							if (t.getSourceVertex() == f) {
								if (stayHere.contains(t.getTargetVertex()) && reachedVertices.add(t.getTargetVertex())) {
									newFringe.add(t.getTargetVertex());
								}
								
								for (DeuteroVertex rv : t.getTargetVertex().getVertices()) {
									if (stayHere.contains(rv) && reachedVertices.add(rv)) {
										newFringe.add(rv);
									}
								}
							}
						}
					}
					
					fringe.clear();
					fringe.addAll(newFringe);
				}
				
				for (DeuteroVertex rv : reachedVertices) {
					if (v.getChildVertices().contains(rv)) {
						rv.setRegionVertex(iv);
					}
				}
			}
		}
		
		try {
			checkFinalization();
		} catch (ModelException e) {
			throw new ClassReflectionException(instance.getClass(), e);
		}
	}
	
	public void checkFinalization() throws ModelException {
		Map<DeuteroVertex, Set<FileLocation>> finalVerticesPerVertex = new HashMap<DeuteroVertex, Set<FileLocation>>();
		
		for (DeuteroVertex vertex : vertices) {
			if (FinalState.class.isAssignableFrom(vertex.getSysmlClz())) {
				HashMaps.inject(finalVerticesPerVertex, vertex.getParentVertex(), vertex.getFileLocation());
			}
		}
		
		for (Map.Entry<DeuteroVertex, Set<FileLocation>> entry : finalVerticesPerVertex.entrySet()) {
			checkStateWithFinalStates(entry.getKey(), entry.getValue());
		}
		
		for (DeuteroVertex vertex : vertices) {
			for (DeuteroTransition t : vertex.getOnDo()) {
				if (t.getEvent() instanceof ASALFinalized) {
					throw new ModelException("Internal transitions cannot be triggered with a finalization event!", FileLocations.from("Internal transition", t.getFileLocation()));
				}
			}
		}
	}
	
	private void checkStateWithFinalStates(DeuteroVertex compositeState, Set<FileLocation> finalVertices) throws ModelException {
		boolean foundFinalizedEvent = false;
		
		for (DeuteroTransition t : transitions) {
			if (t.getSourceVertex() == compositeState) {
				if (t.getEvent() instanceof ASALFinalized) {
					foundFinalizedEvent = true;
					break;
				}
			}
		}
		
		if (!foundFinalizedEvent) {
			throw new ModelException("Composite state with final states should have an outgoing transition with a finalization event!", FileLocations.from("Composite state", compositeState.getFileLocation()), FileLocations.from("Final vertex", finalVertices));
		}
		
		//In case of multiple regions, there cannot be boundary-crossing:
		if (compositeState.getInitialVertices().size() > 1) {
			Set<DeuteroVertex> internalVertices = compositeState.getVertices();
			
			for (DeuteroTransition t : transitions) {
				if (internalVertices.contains(t.getSourceVertex())) {
					if (!internalVertices.contains(t.getTargetVertex())) {
						throw new ModelException("Composite state with multiple regions and final states should not have border-crossing transitions!", FileLocations.from("Transition", t.getFileLocation()));
					}
				}
			}
		}
	}
}
