package jlx.common.reflection;

import jlx.models.IBDInstances.IBD1Port;

@SuppressWarnings("serial")
public class InvalidDataFlowException extends Exception {
	public InvalidDataFlowException(IBD1Port port, String msg) {
		super(getMsg(port), new Exception(msg));
	}
	
	public InvalidDataFlowException(IBD1Port port, Throwable cause) {
		super(getMsg(port), cause);
	}
	
	private static String getMsg(IBD1Port port) {
		return "Port " + port + " is used incorrectly!";
	}
}
