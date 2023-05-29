package models.lc.v1x0;

import jlx.asal.j.*;

public class Functional_Status extends JUserType<Functional_Status>{
	@JTypeName(s = "undefined")
	public final static class undefined extends Functional_Status {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Activated and protected")
	public final static class Activated_and_protected extends Functional_Status {}
	public final static Activated_and_protected Activated_and_protected = new Activated_and_protected();
	@JTypeName(s = "Deactivated and unprotected")
	public final static class Deactivated_and_unprotected extends Functional_Status {}
	public final static Deactivated_and_unprotected Deactivated_and_unprotected = new Deactivated_and_unprotected();
	@JTypeName(s = "Pre-Activated")
	public final static class Pre_Activated extends Functional_Status {}
	public final static Pre_Activated Pre_Activated = new Pre_Activated();
	@JTypeName(s = "Isolated LC")
	public final static class Isolated_LC extends Functional_Status {}
	public final static Isolated_LC Isolated_LC = new Isolated_LC();
	@JTypeName(s = "Activated and unprotected")
	public final static class Activated_and_unprotected extends Functional_Status {}
	public final static Activated_and_unprotected Activated_and_unprotected = new Activated_and_unprotected();
}
