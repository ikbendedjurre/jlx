package models.generic.types;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class PDI_Checksum_Data extends JUserType<PDI_Checksum_Data> {
	@JTypeName(s = "not applicable")
	public final static class NotApplicable extends PDI_Checksum_Data {}
	@JTypeName(s = "12491")
	public final static class D1 extends PDI_Checksum_Data {}
	@JTypeName(s = "12492")
	public final static class D2 extends PDI_Checksum_Data {}
	@JTypeName(s = "12493")
	public final static class D3 extends PDI_Checksum_Data {}
	
	public final static NotApplicable NotApplicable = new NotApplicable();
	public final static D1 D1 = new D1();
	public final static D2 D2 = new D2();
	public final static D3 D3 = new D3();
}
