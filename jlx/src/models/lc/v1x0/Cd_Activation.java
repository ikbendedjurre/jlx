package models.lc.v1x0;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Cd_Activation extends JUserType<Cd_Activation> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Cd_Activation {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Activation")
	public final static class Activation extends Cd_Activation {}
	public final static Activation Activation = new Activation();
	@JTypeName(s = "Pre-Activation")
	public final static class Pre_Activation extends Cd_Activation {}
	public final static Pre_Activation Pre_Activation = new Pre_Activation();
}
