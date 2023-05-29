package models.pendeltrain.j;

import jlx.asal.j.*;

public class TrainPos extends JUserType<TrainPos> {
	public final static class S1 extends TrainPos {}
	public final static class S2 extends TrainPos {}
	
	public final static S1 S1 = new S1();
	public final static S2 S2 = new S2();
}
