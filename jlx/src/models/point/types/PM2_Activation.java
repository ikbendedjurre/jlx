package models.point.types;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class PM2_Activation extends JUserType<PM2_Activation> {
	@JTypeName(s = "ACTIVE")
	public final static class ACTIVE extends PM2_Activation {}
	@JTypeName(s = "INACTIVE")
	public final static class INACTIVE extends PM2_Activation {}
	
	public final static ACTIVE ACTIVE = new ACTIVE();
	public final static INACTIVE INACTIVE = new INACTIVE();
}

