package jlx.behave;

import jlx.common.FileLocation;

public abstract class Vertex {
	private FileLocation fileLocation;
	
	public Vertex() {
		fileLocation = new FileLocation();
	}
	
	public final FileLocation getFileLocation() {
		return fileLocation;
	}
	
	public Outgoing[] getOutgoing() {
		return null;
	}
	
	public Incoming[] getIncoming() {
		return null;
	}
}
