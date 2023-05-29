package models.lc.v1x0;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Cd_Isolate_LC extends JUserType<Cd_Isolate_LC> {
	@JTypeName(s = "undefined")
	public final static class undefined extends Cd_Isolate_LC {}
	public final static undefined undefined = new undefined();
	@JTypeName(s = "Isolate LC enable")
	public final static class Isolate_LC_enable extends Cd_Isolate_LC {}
	public final static Isolate_LC_enable Isolate_LC_enable = new Isolate_LC_enable();
	@JTypeName(s = "Isolate LC disable")
	public final static class Isolate_LC_disable extends Cd_Isolate_LC {}
	public final static Isolate_LC_disable Isolate_LC_disable = new Isolate_LC_disable();
}
