package models.generic.types;

import jlx.asal.j.*;

public class EST_EfeS_State extends JUserType<EST_EfeS_State> {
	@JTypeName(s = "NONE")
	public final static class NONE extends EST_EfeS_State {}
	@JTypeName(s = "NO_OPERATING_VOLTAGE")
	public final static class NO_OPERATING_VOLTAGE extends EST_EfeS_State {}
	@JTypeName(s = "BOOTING")
	public final static class BOOTING extends EST_EfeS_State {}
	@JTypeName(s = "INITIALISING")
	public final static class INITIALISING extends EST_EfeS_State {}
	@JTypeName(s = "FALLBACK_MODE")
	public final static class FALLBACK_MODE extends EST_EfeS_State {}
	@JTypeName(s = "OPERATIONAL")
	public final static class OPERATIONAL extends EST_EfeS_State {}
}
