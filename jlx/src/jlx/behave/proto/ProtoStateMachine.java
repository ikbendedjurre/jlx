package jlx.behave.proto;

import java.lang.reflect.*;
import java.util.*;

import jlx.behave.*;
import jlx.common.ReflectionUtils;
import jlx.common.reflection.*;

/**
 * This machine contains all declarations of variables, flows, functions, and states.
 * The state hierarchy is validated.
 */
public class ProtoStateMachine {
	public final StateMachine instance;
	public final ProtoVertex rootVertex;
	public final Map<Class<?>, ProtoVertex> vertices;
	public final Set<ProtoVertex> initialVertices;
	
	/**
	 * Does NOT include local (= onEntry/onDo/onExit) transitions!
	 */
	public final Set<ProtoTransition> transitions;
	
	public ProtoStateMachine(StateMachine instance) throws ClassReflectionException {
		this.instance = instance;
		
		if (!StateMachine.class.isAssignableFrom(instance.getClass())) {
			throw new ClassReflectionException(instance.getClass(), "Should be a " + StateMachine.class.getCanonicalName() + "!");
		}
		
		//Vertices:
		vertices = new HashMap<Class<?>, ProtoVertex>();
		
		rootVertex = new ProtoVertex(null, instance.getClass());
		rootVertex.setFileLocation(instance);
		addVertex(rootVertex); //This recursively adds child vertices, too!
		initialVertices = new HashSet<ProtoVertex>(rootVertex.initialVertices);
		
		//Transitions:
		transitions = new HashSet<ProtoTransition>();
		
		for (ProtoVertex cv : rootVertex.childVertices) { //(the root vertex does not have transitions)
			populateTransitions(instance, cv); //(we need a parent instance because the vertex classes are not static)
		}
		
		checkReachability();
		checkLiveness();
	}
	
	private void addVertex(ProtoVertex pv) throws ClassReflectionException {
		if (vertices.containsKey(pv.sysmlClz)) {
			throw new ClassReflectionException(pv.sysmlClz, "Cannot use recursion!");
		}
		
		vertices.put(pv.sysmlClz, pv);
		
		//Recursively add the children of composite states:
		if (CompositeState.class.equals(pv.sysmlClz.getSuperclass()) || StateMachine.class.isAssignableFrom(pv.sysmlClz)) {
			for (Class<?> childClass : pv.sysmlClz.getDeclaredClasses()) {
				try {
					addChild(pv, childClass);
				} catch (ClassReflectionException e) {
					throw new ClassReflectionException(pv.sysmlClz, e);
				}
			}
			
			if (pv.initialVertices.isEmpty()) {
				throw new ClassReflectionException(pv.sysmlClz, "Should contain at least 1 " + InitialState.class.getCanonicalName() + "!");
			}
			
			return;
		}
		
		if (pv.sysmlClz.getDeclaredClasses().length > 0) {
			throw new ClassReflectionException(pv.sysmlClz, "Should not nest other states!");
		}
		
		//Inject reference states:
		if (ReferenceState.class.equals(ReflectionUtils.getRawSuperclass(pv.sysmlClz))) {
			Class<? extends StateMachine> smClz = ReflectionUtils.getSuperclassTypeParam(pv.sysmlClz).asSubclass(StateMachine.class);
			
			try {
				for (Class<?> childClass : smClz.getDeclaredClasses()) {
					addChild(pv, childClass);
				}
			} catch (ClassReflectionException e) {
				throw new ClassReflectionException(pv.sysmlClz, new ClassReflectionException(smClz, e));
			}
			
			return;
		}
	}
	
	private void addChild(ProtoVertex parent, Class<?> childClz) throws ClassReflectionException {
		int mod = childClz.getModifiers();
		
		if (!Modifier.isPublic(mod)) {
			throw new ClassReflectionException(childClz, "Should be public!");
		}
		
		if (Modifier.isInterface(mod)) {
			throw new ClassReflectionException(childClz, "Should not be an interface!");
		}
		
		if (Modifier.isStatic(mod)) {
			throw new ClassReflectionException(childClz, "Should not be static!");
		}
		
		if (childClz.getSuperclass() == Vertex.class) {
			throw new ClassReflectionException(childClz, "Should not have superclass " + Vertex.class.getCanonicalName() + "!");
		}
		
		if (!Vertex.class.isAssignableFrom(childClz) && !StateMachine.class.isAssignableFrom(childClz)) {
			throw new ClassReflectionException(childClz, "Should be a " + Vertex.class.getCanonicalName() + " or " + StateMachine.class.getCanonicalName() + "!");
		}
		
		ProtoVertex child = new ProtoVertex(parent, childClz);
		parent.childVertices.add(child);
		
		if (InitialState.class.isAssignableFrom(childClz)) {
			parent.initialVertices.add(child);
		}
		
		addVertex(child);
	}
	
	private void populateTransitions(Object enclosingInstance, ProtoVertex pv) throws ClassReflectionException {
		Object nestedInstance = createNestedInstance(enclosingInstance, pv.sysmlClz);
		pv.setFileLocation(nestedInstance);
		pv.populateTransitions(this, nestedInstance);
		
		//Change the nested instance to an instance of a reference state machine:
		if (ReferenceState.class.equals(ReflectionUtils.getRawSuperclass(pv.sysmlClz))) {
			Class<? extends StateMachine> smClz = ReflectionUtils.getSuperclassTypeParam(pv.sysmlClz).asSubclass(StateMachine.class);
			
			try {
				nestedInstance = ReflectionUtils.createStdInstance(smClz);
			} catch (ReflectionException e) {
				throw new ClassReflectionException(smClz, e);
			}
		}
		
		for (ProtoVertex cv : pv.childVertices) {
			populateTransitions(nestedInstance, cv);
		}
	}
	
	/**
	 * Could return a Vertex or a StateMachine!
	 */
	private static Object createNestedInstance(Object enclosingInstance, Class<?> clz) throws ClassReflectionException {
		try {
			for (Constructor<?> c : clz.getConstructors()) {
				if (c.getParameterCount() == 1) {
					return Vertex.class.cast(c.newInstance(enclosingInstance));
				}
			}
			
			throw new ClassReflectionException(clz, "Could not find constructor!");
		} catch (ClassCastException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ClassReflectionException(clz, "Invalid constructor!");
		} catch (ClassReflectionException e) {
			throw new ClassReflectionException(clz, e);
		}
	}
	
	public void checkReachability() {
		Set<ProtoVertex> beenHere = new HashSet<ProtoVertex>();
		beenHere.add(vertices.get(instance.getClass()));
		
		Set<ProtoVertex> fringe = new HashSet<ProtoVertex>();
		Set<ProtoVertex> newFringe = new HashSet<ProtoVertex>();
		fringe.add(vertices.get(instance.getClass()));
		
		Map<ProtoVertex, Set<ProtoVertex>> targetVerticesPerVertex = new HashMap<ProtoVertex, Set<ProtoVertex>>();
		
		for (ProtoVertex vertex : vertices.values()) {
			targetVerticesPerVertex.put(vertex, new HashSet<ProtoVertex>());
		}
		
		for (ProtoTransition transition : transitions) {
			targetVerticesPerVertex.get(transition.sourceState).add(transition.targetState);
		}
		
		while (fringe.size() > 0) {
			newFringe.clear();
			
			for (ProtoVertex f : fringe) {
				for (ProtoVertex targetVertex : targetVerticesPerVertex.get(f)) {
					ProtoVertex x = targetVertex;
					
					while (x != null && beenHere.add(x)) {
						newFringe.add(x);
						x = x.parentVertex;
					}
				}
				
				for (ProtoVertex initialVertex : f.initialVertices) {
					if (beenHere.add(initialVertex)) {
						newFringe.add(initialVertex);
					}
				}
			}
			
			fringe.clear();
			fringe.addAll(newFringe);
		}
		
		Set<ProtoVertex> unreachedVertices = new HashSet<ProtoVertex>(vertices.values());
		unreachedVertices.removeAll(beenHere);
		
		if (unreachedVertices.size() > 0) {
//			rootVertex.printDebugText("");
//			
//			for (ProtoTransition transition : transitions) {
//				System.out.println(transition.sourceState.clz.getCanonicalName() + " -> " + transition.targetState.clz.getCanonicalName());
//			}
			
			String s = "";
			
			for (ProtoVertex v : unreachedVertices) {
				s += "\n\t" + v.sysmlClz.getCanonicalName();
			}
			
			throw new Error("Could not reach the following vertices/states:" + s);
		}
	}
	
	public void checkLiveness() {
		Set<ProtoVertex> verticesWithOutgoingTransitions = new HashSet<ProtoVertex>();
		
		for (ProtoTransition transition : transitions) {
			verticesWithOutgoingTransitions.add(transition.sourceState);
		}
		
		Set<ProtoVertex> stateVertices = new HashSet<ProtoVertex>();
		
		for (ProtoVertex vertex : vertices.values()) {
			if (State.class.isAssignableFrom(vertex.sysmlClz) && !FinalState.class.isAssignableFrom(vertex.sysmlClz)) {
				if (vertex.initialVertices.size() == 0) {
					stateVertices.add(vertex);
				}
			}
		}
		
		Set<ProtoVertex> livelessStateVertices = new HashSet<ProtoVertex>(stateVertices);
		livelessStateVertices.removeAll(verticesWithOutgoingTransitions);
		
		if (livelessStateVertices.size() > 0) {
			String s = "";
			
			for (ProtoVertex v : livelessStateVertices) {
				s += "\n\t" + v.sysmlClz.getCanonicalName();
			}
			
			System.err.println("Warning! Could not leave the following states:" + s);
			//throw new Error("Could not leave the following states:" + s);
		}
	}
}
