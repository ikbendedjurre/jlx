package jlx.behave;

public abstract class State extends Vertex {
	public LocalTransition onEntry() {
		return null;
	}
	
	public LocalTransition[] onDo() {
		return null;
	}
	
	public LocalTransition onExit() {
		return null;
	}
}
