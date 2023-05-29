package models.point.types;

import jlx.asal.j.*;

public class PointPos extends JUserType<PointPos> {
	@JTypeName(s = "")
	public final static class NONE extends PointPos {}
	@JTypeName(s = "UNKNOWN")
	public final static class UNKNOWN extends PointPos {}
	@JTypeName(s = "ERROR")
	public final static class ERROR extends PointPos {}
	@JTypeName(s = "LEFT")
	public final static class LEFT extends PointPos {}
	@JTypeName(s = "RIGHT")
	public final static class RIGHT extends PointPos {}
	@JTypeName(s = "TRAILED")
	public final static class TRAILED extends PointPos {}
	@JTypeName(s = "NO_END_POSITION")
	public final static class NO_END_POSITION extends PointPos {}
	
	public final static NONE NONE = new NONE();
	public final static UNKNOWN UNKNOWN = new UNKNOWN();
	public final static ERROR ERROR = new ERROR();
	public final static LEFT LEFT = new LEFT();
	public final static RIGHT RIGHT = new RIGHT();
	public final static TRAILED TRAILED = new TRAILED();
	public final static NO_END_POSITION NO_END_POSITION = new NO_END_POSITION();
}
