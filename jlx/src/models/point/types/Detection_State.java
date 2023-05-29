package models.point.types;

import jlx.asal.j.JTypeName;
import jlx.asal.j.JUserType;

public class Detection_State extends JUserType<Detection_State> {
	@JTypeName(s = "NO_END_POSITION")
	public final static class NO_END_POSITION extends Detection_State {}
	@JTypeName(s = "END_POSITION")
	public final static class END_POSITION extends Detection_State {}
	@JTypeName(s = "False")
	public final static class FALSE extends Detection_State {}
	@JTypeName(s = "")
	public final static class NONE extends Detection_State {}
	@JTypeName(s = "TRAILED")
	public final static class TRAILED extends Detection_State {}
}
