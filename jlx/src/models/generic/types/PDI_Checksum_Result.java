package models.generic.types;

import jlx.asal.j.*;

public class PDI_Checksum_Result extends JUserType<PDI_Checksum_Result> {
	@JTypeName(s = "")
	public final static class Empty extends PDI_Checksum_Result {}
	
	@JTypeName(s = "match")
	public final static class Match extends PDI_Checksum_Result {}
	
	@JTypeName(s = "not match")
	public final static class Not_Match extends PDI_Checksum_Result {}
}
