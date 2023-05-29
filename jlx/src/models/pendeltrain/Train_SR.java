package models.pendeltrain;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Train_SR {
	public static class Block extends Type1IBD {
		public final InPort<JPulse> T1_arrival = new InPort<>();
		public final OutPort<JBool> D2_moving = new OutPort<>();
		public final OutPort<TrainPos> D3_pos = new OutPort<>();
		public final InPort<SignalColor> D4_signal1 = new InPort<>();
		public final InPort<SignalColor> D5_signal2 = new InPort<>();
//		public final InPort<JInt> D99_timeBetweenStations = new InPort<>();
	}
}
