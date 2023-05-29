package models.toypoint.j;

import jlx.asal.j.*;
import jlx.blocks.ibd1.*;

public class Point_SR {
	public static class Block extends Type1IBD {
		public final InPort<JPulse> T1_move = new InPort<>();
		public final InPort<EndPos> DT1_move_pos = new InPort<>();
		public final OutPort<JBool> D2_move_left = new OutPort<>();
		public final OutPort<JBool> D3_move_right = new OutPort<>();
		public final InPort<EndPos> D4_curr_pos = new InPort<>();
		public final OutPort<JPulse> T5_stopped = new OutPort<>();
		public final OutPort<JPulse> T6_timeout = new OutPort<>();
		public final InPort<JInt> D60_timeout_duration = new InPort<>();
	}
}
