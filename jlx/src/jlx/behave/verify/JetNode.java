package jlx.behave.verify;

import java.util.*;

import jlx.behave.proto.*;
import jlx.utils.Dir;

public class JetNode {
	private final JetGraph owner;
	private final TeleNode legacy;
	private final Map<List<PulsePackMap>, Set<JetTransition>> outgoing;
	private final Dir dir;
	
	public JetNode(JetGraph owner, TeleNode legacy, Dir dir) {
		this.owner = owner;
		this.legacy = legacy;
		this.dir = dir;
		
		outgoing = new HashMap<List<PulsePackMap>, Set<JetTransition>>();
	}
	
	public JetGraph getOwner() {
		return owner;
	}
	
	public TeleNode getLegacy() {
		return legacy;
	}
	
	public Dir getDir() {
		return dir;
	}
	
	public int getId() {
		return owner.getIdPerNode().get(this);
	}
	
	public Map<List<PulsePackMap>, Set<JetTransition>> getOutgoing() {
		return outgoing;
	}
	
	public Map<String, Set<Class<?>>> getSysmlClzs() {
		return legacy.getSysmlClzs();
	}
}

