package jlx.behave.proto.gui;

public enum UserSimStep {
	INITIALIZATION("Initialization"),
	STEP("Step"),
	UNSTABLE(""),
	STABILIZED("Stabilized"),
	
	;
	
	public final String text;
	
	private UserSimStep(String text) {
		this.text = text;
	}
}
