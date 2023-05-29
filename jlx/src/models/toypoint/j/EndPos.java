package models.toypoint.j;

import jlx.asal.j.*;

public class EndPos extends JUserType<EndPos> {
	public final static class LEFT extends EndPos {}
	public final static class RIGHT extends EndPos {}
	public final static class NEITHER extends EndPos {}
	
	public final static LEFT LEFT = new LEFT();
	public final static RIGHT RIGHT = new RIGHT();
	public final static NEITHER NEITHER = new NEITHER();
}
