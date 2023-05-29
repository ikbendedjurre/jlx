package models.pendeltrain;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Infrastructure_SR {
	public static class Block extends Type1IBD {
		public final TrainPos oldTrainPos = new TrainPos();
		
		public final InPort<TrainPos> D1_trainPos = new InPort<>();
		public final OutPort<SignalColor> D2_signal1 = new OutPort<>();
		public final OutPort<SignalColor> D3_signal2 = new OutPort<>();
		public final InPort<JPulse> T4_breakdown = new InPort<>();
		public final InPort<JPulse> T5_repair = new InPort<>();
	}
}
