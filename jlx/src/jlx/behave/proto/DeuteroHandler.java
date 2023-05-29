package jlx.behave.proto;

import java.util.List;

import jlx.asal.ASALVisitor;

public abstract class DeuteroHandler<T> extends ASALVisitor<T> {
	public T handle(DeuteroStateMachine node, List<T> functions, List<T> vertices, List<T> transitions) {
		return null;
	}
	
	public T handle(DeuteroTransition node, T event, T guard, T stat) {
		return null;
	}
	
	public T handle(DeuteroVertex node, T onEntry, T onExit, List<T> onDo) {
		return null;
	}
}
