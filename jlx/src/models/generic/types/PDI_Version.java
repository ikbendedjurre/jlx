package models.generic.types;

import jlx.asal.j.*;

public class PDI_Version extends JUserType<PDI_Version> {
	@JTypeName(s = "1")
	public final static class V1 extends PDI_Version {}
	@JTypeName(s = "2")
	public final static class V2 extends PDI_Version {}
	@JTypeName(s = "3")
	public final static class V3 extends PDI_Version {}
	
	public final static V1 V1 = new V1();
	public final static V2 V2 = new V2();
	public final static V3 V3 = new V3();
}
