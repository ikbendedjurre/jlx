package jlx.behave.proto;

import java.lang.reflect.*;
import java.util.*;

import jlx.behave.*;
import jlx.common.FileLocation;
import jlx.common.reflection.ClassReflectionException;

public class ProtoVertex {
	private FileLocation fileLocation;
	
	public final Class<?> sysmlClz;
	public final ProtoVertex parentVertex;
	public final Set<ProtoVertex> childVertices;
	public final Set<ProtoVertex> initialVertices;
	public final Set<ProtoTransition> onDo;
	
	public ProtoTransition onEntry;
	public ProtoTransition onExit;
	
	public ProtoVertex(ProtoVertex parentVertex, Class<?> sysmlClz) {
		this.sysmlClz = sysmlClz;
		this.parentVertex = parentVertex;
		
		childVertices = new HashSet<ProtoVertex>();
		initialVertices = new HashSet<ProtoVertex>();
		onDo = new HashSet<ProtoTransition>();
	}
	
	public FileLocation getFileLocation() {
		return fileLocation;
	}
	
	public void setFileLocation(Object v) {
		fileLocation = FileLocation.find(v);
	}
	
	public void populateTransitions(ProtoStateMachine sm, Object v) throws ClassReflectionException {
		try {
			Class<?> vClz = v.getClass();
			
			//onEntry
			if (State.class.isAssignableFrom(vClz)) {
				Method m = State.class.getMethod("onEntry");
				LocalTransition elem = (LocalTransition)m.invoke(v);
				
				if (elem != null) {
					onEntry = new ProtoTransition(sm, null, this, elem, true);
				}
			}
			
			//onDo
			if (State.class.isAssignableFrom(vClz)) {
				Method m = State.class.getMethod("onDo");
				LocalTransition[] elems = (LocalTransition[])m.invoke(v);
				
				if (elems != null) {
					for (LocalTransition elem : elems) {
						onDo.add(new ProtoTransition(sm, this, this, elem, true));
					}
				}
			}
			
			//onExit
			if (State.class.isAssignableFrom(vClz)) {
				Method m = State.class.getMethod("onExit");
				LocalTransition elem = (LocalTransition)m.invoke(v);
				
				if (elem != null) {
					onExit = new ProtoTransition(sm, this, null, elem, true);
				}
			}
			
			//Incoming transitions:
			if (Vertex.class.isAssignableFrom(vClz)) {
				Method m = Vertex.class.getMethod("getIncoming");
				Incoming[] elems = (Incoming[])m.invoke(v);
				
				if (elems != null) {
					for (Incoming elem : elems) {
						ProtoVertex otherState = sm.vertices.get(elem.getSourceState());
						
						if (otherState == null) {
							throw new Error("Unknown state (" + elem.getSourceState().getCanonicalName() + ")!");
						}
						
						sm.transitions.add(new ProtoTransition(sm, otherState, this, elem, false));
					}
				}
			}
			
			//Outgoing transitions:
			if (Vertex.class.isAssignableFrom(vClz)) {
				Method m = Vertex.class.getMethod("getOutgoing");
				Outgoing[] elems = (Outgoing[])m.invoke(v);
				
				if (elems != null) {
					for (Outgoing elem : elems) {
						ProtoVertex otherState = sm.vertices.get(elem.getTargetState());
						
						if (otherState == null) {
							throw new Error("Unknown state (" + elem.getTargetState().getCanonicalName() + ")!");
						}
						
						sm.transitions.add(new ProtoTransition(sm, this, otherState, elem, false));
					}
				}
			}
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ClassReflectionException(sysmlClz, e);
		}
	}
	
	public void printDebugText(String indent) {
		System.out.println(indent + sysmlClz.getCanonicalName() + " extends " + sysmlClz.getSuperclass().getSimpleName() + " {");
		
		if (initialVertices.size() > 0) {
			System.out.println(indent + "\tinitial {");
			
			for (ProtoVertex cv : initialVertices) {
				cv.printDebugText(indent + "\t\t");
			}
			
			System.out.println(indent + "\t}");
		}
		
		if (childVertices.size() > 0) {
			System.out.println(indent + "\tchildren {");
			
			for (ProtoVertex cv : childVertices) {
				cv.printDebugText(indent + "\t\t");
			}
			
			System.out.println(indent + "\t}");
		}
		
		System.out.println(indent + "}");
	}
}
