package models.pendeltrain;

import jlx.asal.j.*;

public class SignalColor extends JUserType<SignalColor> {
	@JTypeDefaultValue
	@JTypeName(s = "RED")
	public final static class RED extends SignalColor {}
	@JTypeName(s = "GREEN")
	public final static class GREEN extends SignalColor {}
	
	public final static SignalColor RED = new RED();
	public final static SignalColor GREEN = new GREEN();
}
